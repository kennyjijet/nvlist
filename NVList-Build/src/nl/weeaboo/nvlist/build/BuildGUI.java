package nl.weeaboo.nvlist.build;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

import nl.weeaboo.awt.AwtUtil;
import nl.weeaboo.awt.FileBrowseField;
import nl.weeaboo.awt.MessageBox;
import nl.weeaboo.awt.ProgressDialog;
import nl.weeaboo.common.StringUtil;
import nl.weeaboo.io.DefaultFileCopyListener;
import nl.weeaboo.io.FileUtil;
import nl.weeaboo.settings.INIFile;

@SuppressWarnings("serial")
public class BuildGUI extends LogoPanel {

	private enum CreateProjectResult {
		ERROR, UNABLE, REFUSED, EXISTS, CREATED;
	}
	
	private final File buildIniF;
	private final INIFile iniFile;
	
	private Build build;
	private final HeaderPanel headerPanel;
	private final FileBrowseField engineBrowseField, projectBrowseField;
	private ProjectPropertyPanel projectProperties;
	private BuildCommandPanel buildCommandPanel;
	
	public BuildGUI() {
		super("header.png");

		buildIniF = new File("build.ini");
		iniFile = new INIFile();
				
		headerPanel = new HeaderPanel(getBackground());

		engineBrowseField = headerPanel.getEngineBrowseField();
		engineBrowseField.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				if ("file".equals(evt.getPropertyName())) {
					setEngineFolder((File)evt.getNewValue());
				}
			}
		});

		projectBrowseField = headerPanel.getProjectBrowseField();		
		projectBrowseField.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				if ("file".equals(evt.getPropertyName())) {
					setProjectFolder((File)evt.getNewValue());
				}
			}
		});
		
		ConsoleOutputPanel consoleOutput = new ConsoleOutputPanel();

		JPanel outputPanel = new JPanel(new BorderLayout());
		outputPanel.add(consoleOutput);
		
		projectProperties = new ProjectPropertyPanel(consoleOutput, getBackground());
		buildCommandPanel = new BuildCommandPanel(projectProperties, outputPanel, consoleOutput);
		
		JPanel rightPanel = new JPanel(new BorderLayout(10, 10));
		rightPanel.setOpaque(false);
		rightPanel.add(buildCommandPanel, BorderLayout.NORTH);
		rightPanel.add(outputPanel, BorderLayout.CENTER);
		
		JPanel mainPanel = new JPanel(new GridLayout(-1, 2, 10, 10));
		mainPanel.setOpaque(false);
		mainPanel.add(projectProperties);
		mainPanel.add(rightPanel);
		
		setBorder(new EmptyBorder(10, 10, 10, 10));
		setPreferredSize(new Dimension(750, 550));
		setLayout(new BorderLayout(5, 5));
		add(headerPanel, BorderLayout.NORTH);
		add(mainPanel, BorderLayout.CENTER);
	}
	
	//Functions
	
	public static void main(String args[]) {
		AwtUtil.setDefaultLAF();
		
		final BuildGUI buildGui = new BuildGUI();
		if (args.length >= 2) {
			buildGui.createBuild(new File(args[0]), new File(args[1]));
		} else {
			if (new File("build-res").exists()) {
				buildGui.setProjectFolder(new File("").getAbsoluteFile());
			}
			
			try {
				buildGui.loadSettings();
			} catch (IOException e) {
				System.err.println(e);
			}
		}
				
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				final JFrame frame = new JFrame("NVList Build Config");
				//frame.setResizable(false);
				frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
				frame.setMinimumSize(new Dimension(700, 350));
				frame.add(buildGui, BorderLayout.CENTER);
				frame.pack();
				frame.setLocationRelativeTo(null);
				frame.setVisible(true);
				frame.addWindowListener(new WindowAdapter() {
					public void windowClosing(WindowEvent event) {
						if (buildGui.askDispose()) {						
							frame.dispose();
						}
					}
					public void windowClosed(WindowEvent event) {
						try {
							buildGui.saveSettings();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				});
				
				AwtUtil.setFrameIcon(frame, getImageRes("icon.png"));			
				buildGui.createBuild(buildGui.engineBrowseField.getFile(), buildGui.projectBrowseField.getFile());
			}
		});
	}
	
	public boolean askDispose() {
		if (buildCommandPanel.isBusy()) {
			MessageBox mbox = new MessageBox("Confirm Exit", "A build operation or resource optimizer is still active in the background.");
			int leave = mbox.addOption("Exit anyway", "Keep the build operation running in the background and exit anyway.");
			mbox.addButton("Cancel", "Cancel the close operation.");
			int r = mbox.showMessage(this);
			if (r == leave) {
				//Continue
			} else {
				return false; //Cancel
			}			
		}
		
		return true;
	}
	
	public boolean askCreateProject(File projectFolder) {
		int r = JOptionPane.showConfirmDialog(this, "Project folder (" + projectFolder
				+ ") doesn't exist or is not a valid project folder.\nCreate a new project in that location?",
				"Confirm Create Project", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		return r == JOptionPane.OK_OPTION;
	}

	private void tryCreateProject(File engineFolder, File projectFolder, CreateProjectCallback callback) {
		doTryCreateProject(engineFolder, projectFolder, callback);
	}
	
	private CreateProjectResult doTryCreateProject(File engineFolder, File projectFolder,
			final CreateProjectCallback callback)
	{
		if (engineFolder == null || !engineFolder.exists()) {
			if (callback != null) callback.run(CreateProjectResult.UNABLE);
			return CreateProjectResult.UNABLE;
		} else if (projectFolder == null) {
			if (callback != null) callback.run(CreateProjectResult.UNABLE);
			return CreateProjectResult.UNABLE;			
		} else if (new File(projectFolder, "res").exists()) {
			if (callback != null) callback.run(CreateProjectResult.EXISTS);
			return CreateProjectResult.EXISTS;
		}
		
		if (!askCreateProject(projectFolder)) {
			if (callback != null) callback.run(CreateProjectResult.REFUSED);
			return CreateProjectResult.REFUSED;
		}
		
		final File src = engineFolder, dst = projectFolder;
		File srcRes = new File(src, "res"), srcBuildRes = new File(src, "build-res");
		final long batchTotal = FileUtil.getRecursiveSize(srcRes) + FileUtil.getRecursiveSize(srcBuildRes);
		final ProgressDialog dialog = new ProgressDialog();
		dialog.setMessage(String.format("Copying %s, please wait...",
			StringUtil.formatMemoryAmount(batchTotal)));
		
		SwingWorker<File, ?> worker = new SwingWorker<File, Void>() {
			protected File doInBackground() throws Exception {
				Build.createEmptyProject(src, dst, new DefaultFileCopyListener() {
					private long batchWritten;
					private long progress;
					
					@Override
					public void onProgress(File file, long written, long total) {
						if (batchTotal > 0) {
							progress = batchWritten + written;
							setProgress(Math.max(0, Math.min(100, Math.round(100 * progress / batchTotal))));
						}
					}
					
					@Override
					public void onEnd(File file) {
						super.onEnd(file);
						batchWritten += file.length();
					}
				});
				return dst;
			}
			protected void done() {
				dialog.dispose();
				if (callback != null) callback.run(CreateProjectResult.CREATED);
				super.done();
			}
		};
		dialog.setTask(worker);
		worker.execute();
		dialog.setVisible(true);
		
		return null;
	}
	
	protected void createBuild(File engineFolder, File projectFolder) {
		if (engineFolder == null) {
			engineFolder = (build != null ? build.getEngineFolder() : projectFolder);
		}
		if (projectFolder == null) {
			File engineBuildJAR = new File(engineFolder, "Build.jar");
			if (engineBuildJAR.exists()) {
				projectFolder = (build != null ? build.getProjectFolder() : engineFolder);
			} else {
				projectFolder = null;
			}
		}
		
		if (engineFolder != null && engineBrowseField != null) {
			engineBrowseField.setFile(engineFolder);
		}
		if (projectFolder != null && projectBrowseField != null) {
			projectBrowseField.setFile(projectFolder);
		}
		
		if (getParent() == null || engineFolder == null || projectFolder == null) {
			return;
		}

		final File engineF = engineFolder;
		final File projectF = projectFolder;
		
		tryCreateProject(engineFolder, projectFolder, new CreateProjectCallback() {
			@Override
			public void run(CreateProjectResult cpr) {
				if (cpr != CreateProjectResult.EXISTS && cpr != CreateProjectResult.CREATED) {
					return;
				}
				
				try {						
					build = new Build(engineF, projectF);
					projectProperties.setPropertyDefinitions(build.getBuildDefs(),
							build.getGameDefs(), build.getPrefsDefaultDefs(),
							build.getInstallerConfigDefs());
					projectProperties.setBuild(build);
					projectProperties.update();
					buildCommandPanel.setBuild(build);
				} catch (RuntimeException re) {
					re.printStackTrace();
					AwtUtil.showError(re.getMessage());
				} catch (LinkageError e) {
					e.printStackTrace();
					AwtUtil.showError(e.getMessage());
				} finally {
					engineBrowseField.setFile(engineF);
					projectBrowseField.setFile(projectF);			
				}		
				
				//Trigger an automatic rebuild after creating a new project
				if (cpr == CreateProjectResult.CREATED) {
					buildCommandPanel.rebuild();
				}
			}
		});
	}
		
	protected void loadSettings() throws IOException {
		iniFile.clear();
		if (buildIniF.exists()) {
			iniFile.read(buildIniF);
		}
		
		if (iniFile.containsKey("engineFolder")) {
			setEngineFolder(new File(iniFile.getString("engineFolder", "")));
		}
		if (iniFile.containsKey("projectFolder")) {
			setProjectFolder(new File(iniFile.getString("projectFolder", "")));
		}
	}
	protected void saveSettings() throws IOException {
		if (build != null) {
			iniFile.put("engineFolder", build.getEngineFolder().toString());
			iniFile.put("projectFolder", build.getProjectFolder().toString());
		}
		iniFile.write(buildIniF);
	}
			
	//Getters
	protected static BufferedImage getImageRes(String filename) {
		try {
			return ImageIO.read(BuildGUI.class.getResource("res/" + filename));
		} catch (IOException e) {
			return new BufferedImage(16, 16, BufferedImage.TYPE_INT_RGB);
		}		
	}
	
	//Setters
	public void setEngineFolder(File folder) {
		if (folder == null) return;
		
		//System.out.println("Engine folder: \"" + folder + "\"");
		if (build == null || !build.getEngineFolder().equals(folder)) {
			createBuild(folder, projectBrowseField.getFile());
		}
	}
	public void setProjectFolder(File folder) {
		if (folder == null) {
			build = null;
		} else {		
			//System.out.println("Project folder: \"" + folder + "\"");
			if (build == null || !build.getProjectFolder().equals(folder)) {
				createBuild(engineBrowseField.getFile(), folder);
			}
		}
	}
	
	//Inner Classes
	private static interface CreateProjectCallback {
		public void run(CreateProjectResult cpr);
	}
		
}
