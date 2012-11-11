package nl.weeaboo.nvlist.build.android;

import static nl.weeaboo.nvlist.build.BuildGUIUtil.*;
import static nl.weeaboo.nvlist.build.android.AndroidConfig.*;
import static nl.weeaboo.nvlist.build.android.AndroidConfig.LVL_KEY_BASE64;
import static nl.weeaboo.nvlist.build.android.AndroidConfig.PACKAGE;
import static nl.weeaboo.nvlist.build.android.AndroidConfig.XAPK_MAIN_FILE;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import nl.weeaboo.awt.AwtUtil;
import nl.weeaboo.awt.DirectoryChooser;
import nl.weeaboo.awt.MessageBox;
import nl.weeaboo.io.FileUtil;
import nl.weeaboo.nvlist.build.Build;
import nl.weeaboo.nvlist.build.BuildGUIUtil;
import nl.weeaboo.nvlist.build.ConsoleOutputPanel;
import nl.weeaboo.nvlist.build.LogoPanel;
import nl.weeaboo.nvlist.build.ProcessCallback;
import nl.weeaboo.nvlist.build.TranslucentPanel;

@SuppressWarnings("serial")
public class AndroidGUI extends LogoPanel {

	private enum AntMode {
		UPDATE("update-android-project"),
		CREATE("create-android-project");
		
		public final String target;
		
		private AntMode(String tgt) {
			target = tgt;
		}
	}
	
	private static final String VERSION_INI = "version.ini";
	private static final String TEMPLATE_PATH = "build-res/android-template.zip";
	
	private final Build build;
	private final AndroidPropertyPanel androidProperties;
	private final ConsoleOutputPanel consoleOutput;
	private final JButton updateButton, createButton;
	
	private boolean busy;
	
	public AndroidGUI(Build b) {
		super("header.png");
		
		build = b;
		
		consoleOutput = new ConsoleOutputPanel();
		
		updateButton = new JButton("Update Existing Android Project...");
		updateButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doUpdate();
			}
		});
		
		createButton = new JButton("Create New Android Project...");
		createButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doCreate();				
			}
		});
		
		JPanel buildPanel = new JPanel();
		buildPanel.setLayout(new GridLayout(-1, 1, 5, 5));
		buildPanel.setOpaque(false);
		buildPanel.add(updateButton);
		buildPanel.add(createButton);
		
		JPanel commandPanel = new TranslucentPanel();
		commandPanel.setLayout(new BoxLayout(commandPanel, BoxLayout.X_AXIS));
		commandPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		commandPanel.add(Box.createHorizontalGlue());
		commandPanel.add(buildPanel);
		commandPanel.add(Box.createHorizontalGlue());
		
		JPanel rightPanel = new JPanel(new BorderLayout(10, 10));
		rightPanel.setOpaque(false);
		rightPanel.add(consoleOutput, BorderLayout.CENTER);
		rightPanel.add(commandPanel, BorderLayout.SOUTH);
		
		androidProperties = new AndroidPropertyPanel(getBackground());
		androidProperties.setPropertyDefinitions(b.getAndroidDefs());
		androidProperties.setBuild(b);
		
		JPanel mainPanel = new JPanel(new GridLayout(-1, 2, 10, 10));
		mainPanel.setOpaque(false);
		mainPanel.add(androidProperties);
		mainPanel.add(rightPanel);
		
		setPreferredSize(new Dimension(650, 450));
		add(Box.createRigidArea(new Dimension(315, 95)), BorderLayout.NORTH);
		add(mainPanel, BorderLayout.CENTER);
	}
	
	//Functions	
	public static JFrame createFrame(AndroidGUI agui) {
		JFrame frame = new JFrame("Android Build Config");
		//frame.setResizable(false);
		frame.setMinimumSize(new Dimension(600, 350));
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.add(agui, BorderLayout.CENTER);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		
		AwtUtil.setFrameIcon(frame, getImageRes("icon.png"));
		return frame;
	}
			
	protected boolean checkProperties(AndroidConfig config) {		
		List<String> warnings = new ArrayList<String>();
		
		String pkg = config.get(PACKAGE);
		if (pkg.equals(PACKAGE.getDefaultValue())) {
			warnings.add("Invalid package name: " + pkg);
		}
		
		String xapkMainFile = config.get(XAPK_MAIN_FILE);
		String lvlKeyBase64 = config.get(LVL_KEY_BASE64);
		if (xapkMainFile != null && xapkMainFile.trim().length() > 0
				&& lvlKeyBase64.equals(LVL_KEY_BASE64.getDefaultValue()))
		{
			warnings.add("XAPK file specified, but not a valid LVL key (required to download the XAPK file).");
		}
		
		if (!warnings.isEmpty()) {
			StringBuilder messageS = new StringBuilder("<html><div width=350>");
			messageS.append("<p>One of more warnings were triggered:</p>");

			messageS.append("<div><br>");
			for (String warning : warnings) {				
				messageS.append("<p style='color: #dd6600'>").append(warning).append("</p><br>");
			}
			messageS.append("</div>");
			
			messageS.append("<p>The application may not function with the current configuration, continue anyway?</p>");
			messageS.append("</div></html>");
			
			int r = JOptionPane.showConfirmDialog(this, messageS, "Warning", JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.WARNING_MESSAGE);
			if (r != JOptionPane.OK_OPTION) {
				return false;
			}
		}
		
		return true;
	}
		
	protected void createNVL(final ProcessCallback callback) {		
		//Show resource optimizer
		try {
			JFrame frame = BuildGUIUtil.createOptimizerGUI(build, true, true);
			frame.addWindowListener(new WindowAdapter() {
				@Override
			    public void windowClosed(WindowEvent e) {
					File resFolder = new File(build.getProjectFolder(), "res");
					File optimizedFolder = new File(build.getProjectFolder(), Build.getOptimizerOutputName(build, true));
					if (optimizedFolder.exists()) {
						resFolder = optimizedFolder;
					}

					StringBuilder cmd = new StringBuilder("dist");
					cmd.append(" -Dobfuscate=true"); //Obfuscation required for XAPK files
					cmd.append(" -Dres.dir=\"").append(resFolder.toString()).append("\"");
					ant(cmd.toString(), callback);		
			    }
			});
		} catch (Exception e) {
			e.printStackTrace();
			StringBuilder sb = new StringBuilder();
			while (e != null) {
				if (sb.length() > 0) sb.append(" :: ");
				sb.append(e.toString());
			}
			AwtUtil.showError(sb.toString());
		}
	}
	
	protected void doUpdate() {	
		AndroidConfig config = loadConfig();		
		if (config.get(AUTO_INCLUDE_NVL)) {
			config.set(XAPK_MAIN_FILE, getXAPKMainPath(config));
			androidProperties.setProperty(XAPK_MAIN_FILE, getXAPKMainPath(config));
		}
		if (!checkProperties(config)) {
			return;
		}

		File updateFolder = new File(build.getProjectFolder(), "android-project");
		DirectoryChooser dc = new DirectoryChooser(true);
		dc.setSelectedDirectory(updateFolder);
		if (dc.showDialog(AndroidGUI.this, "Select Android project to update...")) {
			updateFolder = dc.getSelectedDirectory();
			if (!updateFolder.exists()) {
				AwtUtil.showError("Selected folder doesn't exist or can't be read: " + updateFolder);
				return;
			}
			
			if (checkForUpgrade(updateFolder)) {
				doCreate(updateFolder);
			} else {			
				doUpdate(updateFolder);
			}
		}		
	}
	protected void doUpdate(final File folder) {
		AndroidConfig config = loadConfig();			

		ProcessCallback cb = new ProcessCallback() {
			@Override
			public void run(int exitCode) {
				if (exitCode == 0) {
					createAndroidProject(AntMode.UPDATE, folder, folder);
				}
			}
		};
		
		if (config.get(AUTO_INCLUDE_NVL)) {
			updateMainXAPK(folder, cb);
		} else {
			cb.run(0);
		}
	}
	
	protected void doCreate() {
		File createFolder = new File(build.getProjectFolder(), "android-project");
		DirectoryChooser dc = new DirectoryChooser(true);
		dc.setSelectedDirectory(createFolder);
		if (dc.showDialog(AndroidGUI.this, "Select a folder for the new project...")) {
			createFolder = dc.getSelectedDirectory();
			if (createFolder != null) {
				doCreate(createFolder);
			}
		}		
	}
	
	protected void doCreate(final File folder) {
		createAndroidProject(AntMode.CREATE, null, folder, new ProcessCallback() {
			@Override
			public void run(int exitCode) {				
				if (exitCode == 0) {
					AndroidConfig config = loadConfig();					
					if (config.get(AUTO_INCLUDE_NVL)) {
						config.set(XAPK_MAIN_FILE, getXAPKMainPath(config));
						androidProperties.setProperty(XAPK_MAIN_FILE, getXAPKMainPath(config));
					}
					if (checkProperties(config)) {
						doUpdate(folder);
					}
				}
			}
		});		
	}
	
	protected boolean checkForUpgrade(File folder) {
		TemplateVersion stored = new TemplateVersion();
		try {
			stored.load(new File(folder, VERSION_INI));
		} catch (IOException ioe) {
			//Doesn't matter
		}
		
		TemplateVersion template = new TemplateVersion();
		template.initFromTemplateFile(new File(build.getEngineFolder(), TEMPLATE_PATH));
		
		if (stored.compareTo(template) < 0) {
			MessageBox mb = BuildGUIUtil.newMessageBox("Engine Upgrade Available", "The existing Android project appears to be built using an older version of NVList. Do you want to upgrade it to the current version?");
			mb.setIcons(getWindowIcons(this));
			mb.addButton("Upgrade", "");
			mb.addButton("Not Now", "");
			if (mb.showMessage(this) == 0) {
				return true;
			}			
		}
		
		return false;
	}
	
	protected void updateMainXAPK(final File androidWorkspaceF, final ProcessCallback callback) {
		createNVL(new ProcessCallback() {
			@Override
			public void run(int exitCode) {
				if (exitCode == 0) {
					AndroidConfig config = loadConfig();
					File nvlF = new File(build.getProjectFolder(), "dist/" + build.getGameId() + ".nvl");
					File mainF = new File(androidWorkspaceF, AndroidProjectCompiler.F_ANDROID_NVLIST
							+ File.separator + getXAPKMainPath(config));
					File xapkFolder = mainF.getParentFile();
					try {						
						FileUtil.copyFile(nvlF, mainF);
						FileUtil.write(new File(xapkFolder, "what-are-these-files.txt"),
							"The .nvl/.obb files in this folder are APK Expansion files used by the Google play store.\n" +
							"These allow you to create applications of over 50MB in size (some older Android devices have even lower limits).\n" +
							"AndroidNVList uses these to store the visual novel's resources (contents of the res folder or .nvl file).");
						Desktop.getDesktop().open(mainF.getParentFile());
					} catch (IOException ioe) {
						AwtUtil.showError("Error copying XAPK to AndroidNVList folder: " + nvlF + " -> " + mainF);
						ioe.printStackTrace();
					}
				}
				if (callback != null) callback.run(exitCode);
			}
		});				
	}
	
	private void createAndroidProject(AntMode mode, File templateFolder, File outputFolder,
			final ProcessCallback... postBuildCallbacks)
	{
		if (outputFolder == null) {
			throw new IllegalArgumentException("Output folder should never be null.");
		}
		
		if (mode == AntMode.CREATE) {			
			//Write version info
			TemplateVersion tv = new TemplateVersion();
			if (templateFolder != null) {
				tv.initFromTemplateFile(templateFolder);
			} else {
				tv.initFromTemplateFile(new File(build.getEngineFolder(), TEMPLATE_PATH));				
			}
			try {
				outputFolder.mkdirs();
				tv.save(new File(outputFolder, VERSION_INI));
			} catch (IOException ioe) {
				System.err.println("Error writing version info: " + ioe);
			}
		}
		
		StringBuilder sb = new StringBuilder(mode.target);
		if (templateFolder != null) {
			sb.append(" -Dandroid-template=\"" + templateFolder + "\"");
		}
		if (outputFolder != null) {
			sb.append(" -Dandroid-dist.dir=\"" + outputFolder + "\"");
		}
		ant(sb.toString(), postBuildCallbacks);
	}
	
	public void ant(String args, final ProcessCallback... postBuildCallbacks) {
		setBusy(true);
		try {
			System.out.println("ANT: " + args);

			consoleOutput.process(build.ant(args), new ProcessCallback() {
				public void run(int exitCode) {
					setBusy(false);
					
					if (postBuildCallbacks != null) {
						for (ProcessCallback r : postBuildCallbacks) {
							r.run(exitCode);
						}
					}
				}
			});
		} catch (IOException e) {
			AwtUtil.showError("Error starting ant with args: " + args + " :: " + e);
			setBusy(false);
		}
	}	
	
	private AndroidConfig loadConfig() {
		try {
			return AndroidConfig.fromFile(new File(build.getProjectFolder(), Build.PATH_ANDROID_INI));
		} catch (IOException ioe) {
			System.err.println(ioe);
			return new AndroidConfig();
		}
	}
	
	//Getters
	protected String getXAPKMainPath(AndroidConfig config) {
		return String.format("xapk/main.%d.%s.obb", config.get(XAPK_MAIN_VERSION), config.get(PACKAGE));
		//return "xapk/" + build.getGameId() + ".nvl";
	}
	
	//Setters
	public void setBusy(boolean b) {
		if (busy != b) {
			busy = b;
			
			updateButton.setEnabled(!busy);
			createButton.setEnabled(!busy);
		}
	}
		
}
