package nl.weeaboo.nvlist.build;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import nl.weeaboo.awt.DirectoryChooser;
import nl.weeaboo.awt.FileBrowseField;
import nl.weeaboo.awt.TableLayout;

@SuppressWarnings("serial")
public class HeaderPanel extends JPanel {

	private final JButton createProjectButton;
	private FileBrowseField engineBrowseField;
	private FileBrowseField projectBrowseField;
	
	public HeaderPanel(Color bg) {		
		createProjectButton = new JButton("Create New Project...");
		createProjectButton.setOpaque(false);
		createProjectButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				CreateProjectPanel panel = new CreateProjectPanel(projectBrowseField.getFile());
				int r = JOptionPane.showConfirmDialog(HeaderPanel.this.getParent(), panel,
						"Select a folder for the new project...",
						JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
				if (r == JOptionPane.OK_OPTION) {
					projectBrowseField.setFile(null);
					projectBrowseField.setFile(panel.getSelectedFolder());
				}
			}
		});
		createProjectButton.setEnabled(false);
		
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
		buttonPanel.setOpaque(false);
		buttonPanel.add(createProjectButton);
		
		engineBrowseField = new EngineBrowseField(bg);
		engineBrowseField.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				if ("file".equals(evt.getPropertyName())) {
					File engineF = (File)evt.getNewValue();
					createProjectButton.setEnabled(engineF != null && engineF.exists());
				}
			}
		});
		
		projectBrowseField = new ProjectBrowseField(bg);
		
		JPanel vPanel = new TranslucentPanel();
		vPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		vPanel.setLayout(new TableLayout(1, 5, 5));
		vPanel.add(buttonPanel);
		vPanel.add(Box.createVerticalStrut(5));
		vPanel.add(engineBrowseField);
		vPanel.add(projectBrowseField);
				
		JPanel rightPanel = new JPanel(new BorderLayout(0, 0));
		rightPanel.setOpaque(false);
		rightPanel.setMinimumSize(new Dimension(250, 100));
		rightPanel.setPreferredSize(new Dimension(360, 100));
		rightPanel.add(vPanel, BorderLayout.NORTH);
		rightPanel.add(Box.createGlue(), BorderLayout.CENTER);
		
		setOpaque(false);
		setLayout(new GridLayout(1, 2, 10, 10));
		add(Box.createRigidArea(new Dimension(315, 95)));
		add(rightPanel);
	}
	
	//Functions
	
	//Getters
	public FileBrowseField getEngineBrowseField() {
		return engineBrowseField;
	}
	public FileBrowseField getProjectBrowseField() {
		return projectBrowseField;
	}
	
	//Setters
	
	//Inner Classes
	private static class CreateProjectPanel extends JPanel {
		
		private final DirectoryChooser dc;
		
		public CreateProjectPanel(File initialFolder) {
			dc = new DirectoryChooser(true);
			if (initialFolder != null) {
				dc.setSelectedDirectory(initialFolder);
			}
			dc.setPreferredSize(new Dimension(350, 250));
			
			setLayout(new BorderLayout());
			add(dc, BorderLayout.CENTER);
		}
		
		public File getSelectedFolder() {
			return dc.getSelectedDirectory();
		}
	}
	
}
