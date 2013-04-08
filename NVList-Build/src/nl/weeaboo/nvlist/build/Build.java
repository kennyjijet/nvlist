package nl.weeaboo.nvlist.build;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import nl.weeaboo.awt.AwtUtil;
import nl.weeaboo.common.StringUtil;
import nl.weeaboo.io.FileCopyListener;
import nl.weeaboo.io.FileUtil;
import nl.weeaboo.nvlist.build.android.AndroidConfig;
import nl.weeaboo.settings.INIFile;
import nl.weeaboo.settings.Preference;
import nl.weeaboo.system.ProcessUtil;
import nl.weeaboo.system.SystemUtil;

public class Build {

	public static final String PATH_BUILD_INI     = "build-res/build.properties";
	public static final String PATH_GAME_INI      = "res/game.ini";
	public static final String PATH_PREFS_INI     = "res/prefs-default.ini";
	public static final String PATH_INSTALLER_INI = "build-res/installer-config.ini";
	public static final String PATH_ANDROID_INI   = "build-res/android-config.ini";
	
	private final File engineFolder;
	private final File projectFolder;
	private final FileFilter jarFilter;
	private final URLClassLoader loader;
	private final String gameId;
	
	public Build(File engineF, File projectF) {
		engineFolder = engineF;
		projectFolder = projectF;
		
		jarFilter = new FileFilter() {
			public boolean accept(File file) {
				return file.getName().endsWith(".jar");
			}
		};
		
		if (!engineF.exists() || !projectF.exists()) {
			throw new IllegalArgumentException("Invalid folder(s): " + engineF + " " + projectF);
		}
		
		List<URL> list = new ArrayList<URL>();
		
		//Causes ${project-name.jar} to become undeletable as the URLClassLoader keeps
		//the JAR file open.		
		//for (File f : root.listFiles(jarFilter)) {
		//	try {
		//		list.add(f.toURI().toURL());
		//	} catch (MalformedURLException e) {
		//		System.err.println("Unexpected error accessing JAR: " + f + " :: " + e);
		//	}
		//}
		
		File libF = new File(engineFolder, "lib");
		if (!libF.exists()) {
			throw new IllegalArgumentException("Invalid engine folder, it doesn't contain a lib subfolder. Are you sure NVList is installed in " + engineF + "?");
		}
		
		for (File f : libF.listFiles(jarFilter)) {
			try {
				list.add(f.toURI().toURL());
			} catch (MalformedURLException e) {
				System.err.println("Unexpected error accessing JAR: " + f + " :: " + e);
			}
		}

		File installerF = new File(engineFolder, "tools/MakeInstaller.jar");
		try {
			list.add(installerF.toURI().toURL());
		} catch (MalformedURLException e) {
			System.err.println("Unexpected error accessing installer JAR: " + installerF + " :: " + e);
		}
		
		final URL[] urls = list.toArray(new URL[list.size()]);
		final ClassLoader parentLoader = getClass().getClassLoader();
		loader = AccessController.doPrivileged(new PrivilegedAction<URLClassLoader>() {
			@Override
			public URLClassLoader run() {
				return new URLClassLoader(urls, parentLoader);
			}
			
		});
		
		INIFile buildProperties = new INIFile();
		try {
			buildProperties.read(new File(projectFolder, "build-res/build.properties"));
		} catch (IOException e) {
			System.err.println(e);
		}
		
		gameId = buildProperties.getString("project-name", null);
	}
	
	//Functions
	public static void main(String args[]) {
		AwtUtil.setDefaultLAF();		
	}

	public Process ant(String target) throws IOException {
		String engineF = engineFolder.getAbsolutePath().replace('\\', '/');
		String projectF = projectFolder.getAbsolutePath().replace('\\', '/');
		if (engineF.equals(projectF)) {
			projectF = ".";
		}
		
		String prefix = "", suffix = "";
		String exe;
		if (SystemUtil.isWindowsOS()) {
			prefix = "cmd /C \"";
			suffix = "\"";			
			exe = "\"" + engineF + "/tools/ant/bin/ant\"";
		} else {
			exe = "\"" + engineF + "/tools/ant/bin/ant\"";
		}
		
		String cmd = String.format("%s%s -Dgamedata.dir=\"%s\" %s%s",
				prefix, exe, projectF, target, suffix);
		//System.out.println(cmd);
		//AwtUtil.showError(cmd);
		return ProcessUtil.execInDir(cmd, engineFolder.getAbsolutePath());
	}
	
	public static void createEmptyProject(File engineFolder, File projectFolder) throws IOException {
		createEmptyProject(engineFolder, projectFolder);
	}
	public static void createEmptyProject(File engineFolder, File projectFolder, FileCopyListener cl)
			throws IOException
	{	
		if (projectExists(projectFolder)) {
			throw new IOException("Project folder already exists");
		}
		
		if (!projectFolder.exists() && !projectFolder.mkdirs()) {
			throw new IOException("Unable to create project folder: " + projectFolder);
		}
		
		File srcRes = new File(engineFolder, "res");
		File srcBuildRes = new File(engineFolder, "build-res");
		FileUtil.copyFolder(srcRes, projectFolder, false, cl);
		FileUtil.copyFolder(srcBuildRes, projectFolder, false, cl);
		
		//Remove files that should stay in the engine root only
		File buildResF = new File(projectFolder, "build-res");
		File[] buildResContents = buildResF.listFiles();
		if (buildResContents != null) {
			Set<String> nonCopy = new HashSet<String>(Arrays.asList(
				"android-template.zip"
			));
			
			for (File file : buildResContents) {
				String name = file.getName();
				if (name.startsWith("build") && name.endsWith(".xml")) {
					if (!file.delete()) {
						System.err.println("Unable to delete build scripts from project build folder");
					}
				} else if (nonCopy.contains(name) || name.matches("jre(-installer)?(-\\w+)*\\.\\w+")) {
					if (!file.delete()) {
						System.err.println("Unable to delete JRE installer from project build folder");
					}
				}
			}
		}
		FileUtil.deleteFolder(new File(projectFolder, "build-res/launcher"));		
	}
		
	//Getters
	public static boolean projectExists(File projectFolder) {
		File buildRes = new File(projectFolder, "build-res");
		File res = new File(projectFolder, "res");
		return buildRes.exists() && res.exists();
	}
	
	public ClassLoader getClassLoader() {
		return loader;
	}
	
	public String getGameId() {
		return gameId;
	}
	public File getEngineFolder() {
		return engineFolder;
	}
	public File getProjectFolder() {
		return projectFolder;
	}
	
	public String getCleanTarget() { return "clean"; }
	public String getBuildTarget() { return "main"; }
	public String getRebuildTarget() { return "clean main"; }
	
	public List<Preference<?>> getBuildDefs() {
		List<Preference<?>> result = new ArrayList<Preference<?>>();
		getPropertyDefinitions(result, "nl.weeaboo.game.desktop.BuildConfig");
		return result;
	}
	public List<Preference<?>> getGameDefs() {
		List<Preference<?>> result = new ArrayList<Preference<?>>();
		getPropertyDefinitions(result, "nl.weeaboo.game.GameConfig");
		getPropertyDefinitions(result, "nl.weeaboo.game.GameUpdaterPrefs");
		getPropertyDefinitions(result, "nl.weeaboo.vn.NovelPrefs");
		return result;
	}
	public List<Preference<?>> getPrefsDefaultDefs() {
		List<Preference<?>> result = new ArrayList<Preference<?>>();
		getPropertyDefinitions(result, "nl.weeaboo.game.GameConfig");
		getPropertyDefinitions(result, "nl.weeaboo.game.GameUpdaterPrefs");
		getPropertyDefinitions(result, "nl.weeaboo.vn.NovelPrefs");
		return result;
	}
	public List<Preference<?>> getInstallerConfigDefs() {
		List<Preference<?>> result = new ArrayList<Preference<?>>();
		getPropertyDefinitions(result, "nl.weeaboo.installer.InstallerConfig");
		return result;
	}
	public List<Preference<?>> getAndroidDefs() {
		List<Preference<?>> result = new ArrayList<Preference<?>>();
		getPropertyDefinitions(result, AndroidConfig.class);
		return result;
	}
	
	protected void getPropertyDefinitions(Collection<Preference<?>> out, String className) {
		try {
			getPropertyDefinitions(out, Class.forName(className, true, loader));
		} catch (ClassNotFoundException e) {
			System.err.println("Unexpected error accessing class: " + e);
		}
	}
	protected void getPropertyDefinitions(Collection<Preference<?>> out, Class<?> clazz) {
		for (Field field : clazz.getFields()) {
			if ((field.getModifiers() & Modifier.STATIC) != 0) {
				try {
					Object value = field.get(null);
					if (value instanceof Preference<?>) {
						out.add((Preference<?>)value);
					}
				} catch (IllegalArgumentException e) {
					System.err.println("Unexpected error accessing field: " + field.getName() + " :: " + e);
				} catch (IllegalAccessException e) {
					System.err.println("Unexpected error accessing field: " + field.getName() + " :: " + e);
				}
			}
		}
	}
	
	public INIFile getProperties() {
		INIFile ini = new INIFile();
		try {
			ini.read(new File(getProjectFolder(), Build.PATH_GAME_INI));
		} catch (IOException ioe) {
			//Ignore missing INI file
		}
		try {
			ini.read(new File(getProjectFolder(), Build.PATH_PREFS_INI));
		} catch (IOException ioe) {
			//Ignore missing INI file
		}
		return ini;
	}
	
	private static void stripResourceExts(Map<String, File> map) {
		Map<String, File> copy = new HashMap<String, File>(map);
		map.clear();
		
		for (Entry<String, File> entry : copy.entrySet()) {
			String relpath = entry.getKey();
			if (relpath.startsWith("img") || relpath.startsWith("snd") || relpath.startsWith("video")) {
				relpath = StringUtil.stripExtension(relpath);
			}
			map.put(relpath, entry.getValue());
		}
	}
	
	/**
	 * @return <code>true</code> if files in the res folder have been changed
	 *         since the optimized res folder was made (determined by comparing
	 *         the modified time of each file).<br/>
	 *         Also returns <code>true</code> when no optimized res folder
	 *         exists.
	 */
	public boolean isOptimizedResOutdated() {
		File resF = getResFolder();
		File resoptF = getOptimizedResFolder();
		if (!resoptF.exists()) {
			return true;
		}		
		
		Map<String, File> resFiles = new HashMap<String, File>();
		FileUtil.collectFiles(resFiles, resF, false);
		stripResourceExts(resFiles);
		
		Map<String, File> resoptFiles = new HashMap<String, File>();
		FileUtil.collectFiles(resoptFiles, resoptF, false);
		stripResourceExts(resoptFiles);
		
		for (Entry<String, File> entry : resFiles.entrySet()) {
			String relpath = entry.getKey();
			File oldFile = entry.getValue();
			File newFile = resoptFiles.get(relpath);
			if (newFile == null) {
				System.out.printf("Optimized file doesn't exist (%s)\n", relpath);
				return true;
			}

			long oldTime = oldFile.lastModified();
			long newTime = newFile.lastModified();
			if (newTime < oldTime) {
				System.out.printf("Optimized file outdated (%s), %d < %d\n", relpath, newTime, oldTime);
				return true;
			}
			
			//Optimized file is acceptable
		}
		return false;
	}	
	
	public File getResFolder() {
		return new File(getProjectFolder(), "res");
	}
	public File getOptimizedResFolder() {
		return new File(getProjectFolder(), getOptimizerOutputName(this, false));
	}

	public static String getOptimizerOutputName(Build build, boolean isAndroid) {
		try {
			ClassLoader cl = build.getClassLoader();
			Class<?> clazz = cl.loadClass("nl.weeaboo.game.optimizer.Optimizer");
			return String.valueOf(clazz.getDeclaredMethod("getDefaultOutputName", Boolean.TYPE).invoke(null, isAndroid));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return (isAndroid ? "res-optimized-android" : "res-optimized");
	}
	
	//Setters
	
}
