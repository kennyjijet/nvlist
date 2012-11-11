package nl.weeaboo.nvlist.build;

import static nl.weeaboo.nvlist.build.BuildGUIUtil.getWindowIcons;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import nl.weeaboo.awt.AwtUtil;
import nl.weeaboo.awt.MessageBox;
import nl.weeaboo.io.FileUtil;
import nl.weeaboo.nvlist.build.android.AndroidGUI;
import nl.weeaboo.settings.INIFile;

@SuppressWarnings("serial")
public class BuildCommandPanel extends JPanel {

	private Build build;

	private final RunPanel runPanel;
	private final ConsoleOutputPanel outputPanel;
	private final Action optimizerAction, buildAppletAction, buildInstallerAction, buildInstallerCDAction, androidAction;
	private final JButton rebuildButton, editButton, backupButton;
	private final JButton moreButton;
	
	private boolean busy;

	public BuildCommandPanel(ConsoleOutputPanel output) {
		outputPanel = output;
		
		runPanel = new RunPanel(this, outputPanel);

		optimizerAction = new AbstractAction("Resource Optimizer...") {
			public void actionPerformed(ActionEvent event) {
				try {
					BuildGUIUtil.createOptimizerGUI(build, false, true);
				} catch (Exception e) {
					e.printStackTrace();
					AwtUtil.showError(e);
				}
			}
		};

		buildAppletAction = new AbstractAction("Create Applet") {
			public void actionPerformed(ActionEvent e) {
				if (preReleaseCheck()) {
					dist("clean dist-applet");
				}
			}
		};

		buildInstallerAction = new AbstractAction("Create Release") {
			public void actionPerformed(ActionEvent e) {
				if (preReleaseCheck()) {
					dist("clean dist make-installer make-installer-mac make-installer-zip clean-dist-jre");
				}
			}
		};

		buildInstallerCDAction = new AbstractAction("Create CD Release") {
			public void actionPerformed(ActionEvent e) {
				if (preReleaseCheck()) {
					dist("clean dist make-installer-cd clean-dist-jre");
				}
			}
		};
		
		androidAction = new AbstractAction("Create Android Project...") {
			public void actionPerformed(ActionEvent event) {				
				final Window myWindow = SwingUtilities.getWindowAncestor(BuildCommandPanel.this);
				myWindow.setVisible(false);

				try {
					AndroidGUI agui = new AndroidGUI(build);				
					JFrame frame = AndroidGUI.createFrame(agui);
	
					frame.addWindowListener(new WindowAdapter() {
						@Override
						public void windowClosed(WindowEvent event) {
							myWindow.setVisible(true);
						}
					});
				} catch (RuntimeException re) {
					myWindow.setVisible(true);
					throw re;
				}
			}
		};

		rebuildButton = new JButton("Rebuild");
		rebuildButton.setOpaque(false);
		rebuildButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				rebuild();
			}
		});

		editButton = new JButton("Edit");
		editButton.setOpaque(false);
		editButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File scriptF = new File(build.getProjectFolder(), "res/script");
				try {
					Desktop.getDesktop().open(scriptF);
				} catch (IOException ioe) {
					try {
						Runtime.getRuntime().exec("open " + scriptF);
					} catch (IOException ioe2) {
						JOptionPane.showMessageDialog(BuildCommandPanel.this, "Error opening script folder: "
								+ scriptF, "Unable to perform action", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		});

		backupButton = new JButton("Backup");
		backupButton.setOpaque(false);
		backupButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ant("backup");
			}
		});

		JPanel buttonPanel = new JPanel(new GridLayout(-1, 3, 10, 10));
		buttonPanel.setOpaque(false);
		buttonPanel.add(rebuildButton);
		buttonPanel.add(editButton);
		buttonPanel.add(backupButton);

		moreButton = new JButton("...");
		moreButton.setPreferredSize(new Dimension(30, 22));
		moreButton.setOpaque(false);
		moreButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JPopupMenu popup = new JPopupMenu();
				popup.add(optimizerAction);
				popup.add(buildAppletAction);
				popup.add(buildInstallerAction);
				popup.add(buildInstallerCDAction);
				popup.add(androidAction);
				popup.show(moreButton, 0, 0);
			}
		});

		JPanel mainPanel = new JPanel(new BorderLayout(5, 5));
		mainPanel.setOpaque(false);
		mainPanel.add(buttonPanel, BorderLayout.CENTER);
		mainPanel.add(moreButton, BorderLayout.EAST);
		mainPanel.add(runPanel, BorderLayout.SOUTH);

		JLabel titleLabel = new JLabel("Build Commands");
		titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD));
		titleLabel.setBorder(new EmptyBorder(5, 0, 5, 0));

		setOpaque(false);
		setLayout(new BorderLayout());
		add(titleLabel, BorderLayout.NORTH);
		add(mainPanel, BorderLayout.CENTER);
	}
    
	// Functions
	public void rebuild(final ProcessCallback... postBuildCallbacks) {
		ant(build.getRebuildTarget(), postBuildCallbacks);		
	}
	
	public void ant(String target, final ProcessCallback... postBuildCallbacks) {
		setBusy(true);
		try {
			outputPanel.process(build.ant(target), new ProcessCallback() {
				public void run(int exitCode) {
					setBusy(false);
					runPanel.update();
					
					if (postBuildCallbacks != null) {
						for (ProcessCallback r : postBuildCallbacks) {
							r.run(exitCode);
						}
					}
				}
			});
		} catch (IOException e) {			
			AwtUtil.showError("Error starting " + target + " command: " + e);
			setBusy(false);
		}
	}

	protected boolean preReleaseCheck() {
		INIFile ini = build.getProperties();

		if (ini.getBoolean("debug", false)) {
			MessageBox mb = BuildGUIUtil.newMessageBox("Confirm action", "Debug mode is still on. "
					+ "Are you sure you want to build a release with it turned on?");
			mb.setIcons(getWindowIcons(this));
			mb.addButton("Build a debug release", "");
			mb.addButton("Cancel", "");
			if (mb.showMessage(this) != 0) {
				return false;
			}
		}
		
		if (ini.getBoolean("vn.enableProofreaderTools", false)) {
			MessageBox mb = BuildGUIUtil.newMessageBox("Confirm action", "Proofreader tools are currently turned on. "
					+ "Are you sure you want to build a release with proofreader tools enabled?");
			mb.setIcons(getWindowIcons(this));
			mb.addButton("I want proofreader tools enabled", "");
			mb.addButton("Cancel", "");
			if (mb.showMessage(this) != 0) {
				return false;
			}
		}
		
		return true;
	}
	
	private void showResourceOptimizer(final Runnable callback) {
		final boolean wasBusy = busy;
		busy = true;
		updateEnabled();
		
		//Show resource optimizer
		try {
			JFrame frame = BuildGUIUtil.createOptimizerGUI(build, false, true);
			frame.addWindowListener(new WindowAdapter() {
				@Override
			    public void windowClosed(WindowEvent e) {
					busy = wasBusy;
					updateEnabled();
					callback.run();
			    }
			});
		} catch (Exception e) {
			e.printStackTrace();
			AwtUtil.showError(e);

			busy = wasBusy;
			updateEnabled();
		}
	}
	
	protected void dist(final String antCmd) {
		Runnable antRunnable = new Runnable() {
			public void run() {
				File resoptF = build.getOptimizedResFolder();
				if (resoptF.exists()) {
					ant(antCmd + " -Dres.dir=\"" + resoptF + "\"");
				} else {
					ant(antCmd);
				}
			}
		};
		
		File resoptF = build.getOptimizedResFolder();
		if (!resoptF.exists()) {
			MessageBox mb = BuildGUIUtil.newMessageBox("Optimize Resources", "Do you want to run the resource optimizer to decrease the file size of the release? The resource optimizer can recompress all images, audio and video with the click of a button.");
			mb.setIcons(getWindowIcons(this));
			int opt = mb.addButton("Optimize resources", "");
			mb.addButton("Skip this step", "");
			int cancel = mb.addButton("Cancel", "");
			
			int r = mb.showMessage(this);
			if (r == opt) {
				showResourceOptimizer(antRunnable);
				return;
			} else if (r < 0 || r == cancel) {
				return;
			}
		} else if (build.isOptimizedResOutdated()) {
			MessageBox mb = BuildGUIUtil.newMessageBox("Optimize Resources", "Optimized resources are outdated, what do you want to do?");
			mb.setIcons(getWindowIcons(this));
			int opt = mb.addOption("Optimize resources again", "Opens a resource optimizer window to allow you to create an up-to-date set of optimized resources.");
			int del = mb.addOption("Delete outdated resources and continue", "Delete the outdated optimized resources and continue without optimizing.");
			mb.addOption("Build using outdated resources", "Use the outdated optimized resources anyway (don't select this unless you know what you're doing).");
			int cancel = mb.addButton("Cancel", "");

			int r = mb.showMessage(this);
			if (r == opt) {
				showResourceOptimizer(antRunnable);
				return;
			} else if (r == del) {
				if (!FileUtil.deleteFolder(resoptF) && resoptF.exists()) {
					AwtUtil.showError("Unable to delete optimized resources folder: " + resoptF);
					return;
				}				
			} else if (r < 0 || r == cancel) {
				return;
			}			
		}
		
		antRunnable.run();
	}

	private void updateEnabled() {
		runPanel.setEnabled(!busy && build != null);
		optimizerAction.setEnabled(!busy && build != null);
		buildAppletAction.setEnabled(!busy && build != null);
		buildInstallerAction.setEnabled(!busy && build != null);
		buildInstallerCDAction.setEnabled(!busy && build != null);
		androidAction.setEnabled(!busy && build != null);
		rebuildButton.setEnabled(!busy && build != null);
		editButton.setEnabled(!busy);
		backupButton.setEnabled(!busy);					
	}
	
	// Getters

	// Setters
	public void setBuild(Build b) {
		if (build != b) {
			build = b;

			runPanel.setBuild(b);
		}

		updateEnabled();
	}
	
	public void setBusy(boolean b) {
		if (busy != b) {
			busy = b;
			
			updateEnabled();
		}
	}

}
