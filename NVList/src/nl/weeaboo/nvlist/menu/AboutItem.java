package nl.weeaboo.nvlist.menu;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import nl.weeaboo.common.StringUtil;
import nl.weeaboo.game.IGameDisplay;
import nl.weeaboo.io.StreamUtil;
import nl.weeaboo.nvlist.Game;
import nl.weeaboo.vn.impl.nvlist.Novel;

public class AboutItem extends GameMenuAction {

	@Override
	public JMenuItem createItem(Game game, Novel nvl) {
		JMenuItem item = new JMenuItem("About");
		return item;
	}

	@Override
	public void actionPerformed(JMenuItem item, ActionEvent e, Game maybeGame, Novel maybeNovel) {
		//Create version string
		String pkgVersion = null;
		if (maybeNovel != null) {
			maybeNovel.getClass().getPackage().getImplementationVersion();
		}
		String version = String.format("NVList engine version: %s %s",
				Game.VERSION_STRING, (pkgVersion != null ? "("+pkgVersion+")" : ""));
				
		//Create content panel
		final JPanel panel = new JPanel(new BorderLayout(10, 10));
		panel.setBorder(new EmptyBorder(10, 0, 0, 0));
		
		JLabel messageLabel = new JLabel(version);
		panel.add(messageLabel, BorderLayout.NORTH);
		
		//Add license component
		String license = getLicense();
		if (license != null) {
			JPanel licensePanel = new JPanel(new BorderLayout());
			licensePanel.add(new JLabel(
				"<html><font color=666666>&nbsp;NVList license:</font></html>"),
				BorderLayout.NORTH);
			
			JTextArea textArea = new JTextArea(license, 10, 80);
			textArea.setEditable(false);
			textArea.setFont(textArea.getFont().deriveFont(11f));

			JScrollPane scrollPane = new JScrollPane(textArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
					JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			licensePanel.add(scrollPane, BorderLayout.CENTER);
			
			panel.add(licensePanel, BorderLayout.CENTER);			
		}
		
		//Show message box
		if (maybeGame != null) {
			IGameDisplay display = maybeGame.getDisplay();
			display.showMessageDialog(panel, "About");
		} else {
			Runnable r = new Runnable() {
				public void run() {
					JOptionPane.showMessageDialog(null, panel, "About", JOptionPane.PLAIN_MESSAGE);
				}
			};
			if (SwingUtilities.isEventDispatchThread()) {
				r.run();
			} else {
				SwingUtilities.invokeLater(r);
			}
		}
	}

	protected String getLicense() {
		String license = null;
			
		InputStream licenseIn = null;
		try {
			licenseIn = getClass().getResourceAsStream("/license.txt");
			if (licenseIn == null) {
				try {
					File file = new File("license.txt");
					if (file.exists()) {
						licenseIn = new FileInputStream(file);
					}
				} catch (SecurityException se) {
					//Ignore
				}
			}
			
			if (licenseIn != null) { 
				byte[] licenseBytes = StreamUtil.readFully(licenseIn);
				license = StringUtil.fromUTF8(licenseBytes, 0, licenseBytes.length);
			}
		} catch (IOException ioe) {
			//Ignore
		} finally {
			try {
				if (licenseIn != null) licenseIn.close();
			} catch (IOException ioe) {
				//Ignore
			}
		}
		
		return license;
	}
	
}
