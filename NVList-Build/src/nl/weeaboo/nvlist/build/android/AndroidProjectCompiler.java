package nl.weeaboo.nvlist.build.android;

import static nl.weeaboo.nvlist.build.android.AndroidConfig.FOLDER;
import static nl.weeaboo.nvlist.build.android.AndroidConfig.ICON;
import static nl.weeaboo.nvlist.build.android.AndroidConfig.LVL_KEY_BASE64;
import static nl.weeaboo.nvlist.build.android.AndroidConfig.OUYA_ICON;
import static nl.weeaboo.nvlist.build.android.AndroidConfig.SPLASH_IMAGE;
import static nl.weeaboo.nvlist.build.android.AndroidConfig.TITLE;
import static nl.weeaboo.nvlist.build.android.AndroidConfig.VERSION_CODE;
import static nl.weeaboo.nvlist.build.android.AndroidConfig.VERSION_NAME;
import static nl.weeaboo.nvlist.build.android.AndroidConfig.XAPK_MAIN_FILE;
import static nl.weeaboo.nvlist.build.android.AndroidConfig.XAPK_MAIN_VERSION;
import static nl.weeaboo.nvlist.build.android.AndroidConfig.XAPK_PATCH_FILE;
import static nl.weeaboo.nvlist.build.android.AndroidConfig.XAPK_PATCH_VERSION;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import nl.weeaboo.io.FileUtil;
import nl.weeaboo.io.StreamUtil;
import nl.weeaboo.nvlist.build.BuildUtil;

public class AndroidProjectCompiler {
	
	static final String F_ANDROID_NVLIST = "Workspace/AndroidNVList/";
	
	private final File gameFolder;
	private final File templateF;
	private final File dstFolder;
	private final Map<String, FileHandler> handlers;
	        
	public AndroidProjectCompiler(File gameF, File templateF, File dstF, AndroidConfig config) {
		this.gameFolder = gameF;
		this.templateF = templateF;
		this.dstFolder = dstF;
		
		handlers = new HashMap<String, FileHandler>();
		
		handlers.put(null, Handlers.getDefault());
		
		String mainXAPKPath = config.get(XAPK_MAIN_FILE);
		final File mainXAPKFile = (mainXAPKPath != null && mainXAPKPath.trim().length() > 0
				? new File(new File(dstF, F_ANDROID_NVLIST), mainXAPKPath)
				: null);
		
		String patchXAPKPath = config.get(XAPK_PATCH_FILE);
		final File patchXAPKFile = (patchXAPKPath != null && patchXAPKPath.trim().length() > 0
				? new File(new File(dstF, F_ANDROID_NVLIST), patchXAPKPath)
				: null);
		
		handlers.put(F_ANDROID_NVLIST + "src/nl/weeaboo/android/nvlist/ExpansionConstants.java",
			Handlers.expansionConstants(config.get(BuildUtil.PACKAGE), config.get(LVL_KEY_BASE64), config.get(XAPK_MAIN_VERSION),
				mainXAPKFile, config.get(XAPK_PATCH_VERSION), patchXAPKFile));
				
		handlers.put(F_ANDROID_NVLIST + "AndroidManifest.xml",
			Handlers.androidManifestHandler(config.get(BuildUtil.PACKAGE), config.get(VERSION_CODE),
				config.get(VERSION_NAME)));
		
		handlers.put(F_ANDROID_NVLIST + "res/values/strings.xml",
			Handlers.stringResHandler(config.get(TITLE), config.get(FOLDER)));				
		
		handlers.put("java",
			Handlers.javaHandler(config.get(BuildUtil.PACKAGE)));
		
		File iconF = new File(gameFolder, config.get(ICON));
		File splashF = new File(gameFolder, config.get(SPLASH_IMAGE));
		File ouyaIconF = new File(gameFolder, config.get(OUYA_ICON));
		handlers.put("res/drawable", Handlers.drawableHandler(iconF, splashF, ouyaIconF));
	}
	
	//Functions
	private static void printUsage() {
		System.err.println("Usage: java -cp Build.jar " + AndroidProjectCompiler.class.getName() + " <game> <template> <config> <dst>");
	}
	
	public static void main(String[] args) throws IOException {
		File gameF = new File(args[0]);
		File templateF = new File(args[1]);
		File buildPropertiesF = new File(args[2]);
		File androidConfigF = new File(args[3]);
		File dstF = new File(args[4]);
		
		if (!gameF.exists() || !buildPropertiesF.exists() || !androidConfigF.exists() || !templateF.exists()) {
			if (!gameF.exists()) System.err.println("Folder doesn't exist: " + gameF);
			if (!buildPropertiesF.exists()) System.err.println("File doesn't exist: " + buildPropertiesF);
			if (!androidConfigF.exists()) System.err.println("File doesn't exist: " + androidConfigF);
			if (!templateF.exists()) System.err.println("Folder doesn't exist: " + templateF);
			printUsage();
			System.exit(1);
			return;
		}
		
		AndroidConfig config = AndroidConfig.fromFile(buildPropertiesF, androidConfigF);
		
		AndroidProjectCompiler compiler = new AndroidProjectCompiler(gameF, templateF, dstF, config);
		compiler.compile();
	}
	
	public void compile() throws IOException {
		if (!templateF.exists()) {
			throw new FileNotFoundException("Template doesn't exist: " + templateF);
		}
		if (!dstFolder.exists() && !dstFolder.mkdirs()) {
			throw new IOException("Unable to create destination folder: " + dstFolder);
		}
		
		File tempFolder = new File(dstFolder, "_temp");
		try {
			Map<String, File> files = new TreeMap<String, File>();
			if (!templateF.isDirectory() && templateF.exists()) {
				tempFolder.mkdirs();
				unzip(templateF, tempFolder);
				FileUtil.collectFiles(files, tempFolder, false, true, true);
			} else {
				FileUtil.collectFiles(files, templateF, false, true, true);				
			}
			
			for (Entry<String, File> entry : files.entrySet()) {
				String relpath = entry.getKey();
				File templateF = entry.getValue();
				File dstF = new File(dstFolder, relpath);
				
				//System.out.println(relpath + " " + templateF);
				
				if (templateF.isDirectory()) {
					dstF.mkdirs();
				} else {
					FileHandler handler = handlers.get(relpath);
					if (handler == null) {
						if (relpath.contains("res/drawable")) {
							handler = handlers.get("res/drawable");
						} else if (relpath.endsWith(".java")) {
							handler = handlers.get("java");
						}
						
						if (handler == null) {
							handler = handlers.get(null);
						}
					}
					handler.process(relpath, templateF, dstF);
				}
			}
		} finally {
			if (tempFolder.exists()) {
				FileUtil.deleteFolder(tempFolder);
			}
		}
	}
	
	private static void unzip(File zipF, File dstFolder) throws IOException {
		dstFolder.mkdirs();
		
		ZipFile zip = new ZipFile(zipF);
		try {
			Enumeration<? extends ZipEntry> e = zip.entries();
			while (e.hasMoreElements()) {
				ZipEntry entry = e.nextElement();
				File f = new File(dstFolder, entry.getName());
				if (entry.isDirectory()) {
					f.mkdirs();
				} else {
					f.getParentFile().mkdirs();					

					InputStream in = zip.getInputStream(entry);
					try {
						OutputStream out = new FileOutputStream(f);
						try {
							StreamUtil.writeFully(out, in);
						} finally {
							out.close();
						}
					} finally {
						in.close();
					}
				}				
			}
		} finally {
			zip.close();
		}
	}
	
	//Getters
	
	//Setters
	
}
