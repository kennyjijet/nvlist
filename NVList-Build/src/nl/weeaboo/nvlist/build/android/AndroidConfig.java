package nl.weeaboo.nvlist.build.android;

import static nl.weeaboo.settings.Preference.newPreference;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Map;
import java.util.Map.Entry;

import nl.weeaboo.settings.BaseConfig;
import nl.weeaboo.settings.INIFile;
import nl.weeaboo.settings.Preference;

public class AndroidConfig extends BaseConfig {

	public static final Preference<String>  PACKAGE            = newPreference("package", "com.example", "Package Name", "Unique Java package name for your application. Should start with your website's domain name reversed (blah.example.com -> com.example.blah), followed by some part specific to this specific app (example: com.example.blah.myapp).");
	public static final Preference<String>  TITLE              = newPreference("title", "App Title", "Title", "Display name for your app.");
	public static final Preference<Integer> VERSION_CODE       = newPreference("versionCode", 1, "Version Code", "Version code, must be an integer. Each new release should have a higher versionCode than the last.");
	public static final Preference<String>  VERSION_NAME       = newPreference("versionName", "1.0", "Version Name", "The application version as seen by the user. Can be anything you want: <code>1.0</code>, <code>pancake edition</code>, whatever.");
	public static final Preference<String>  ICON               = newPreference("icon", "res/icon.png", "Icon", "Application icon, should be at least 96x96. Will be downscaled automatically as needed.");
	public static final Preference<Boolean> AUTO_INCLUDE_NVL   = newPreference("autoIncludeNVL", true, "Auto Include NVL", "Controls if the build system should automatically include an updated .nvl file containing your visual novel's resources when creating/updating an Android project.");	
	public static final Preference<String>  FOLDER             = newPreference("folder", "data/nvlist/", "Resource Folder", "Path to a folder on the SD card to search for resources. Not needed when resources are packed into an APK expansion file (XAPK), or if all resources are compiled into the app (by including them into the assets folder). On some devices, part of the internal memory counts as the 'SD card'.");
	public static final Preference<String>  LVL_KEY_BASE64     = newPreference("lvlKeyBase64", "REPLACE THIS WITH YOUR PUBLIC KEY", "LVL Public Key", "Google Play license verification public key encoded in base 64. Can be found under edit profile -> public key within the Android Developer Console webpage.");
	public static final Preference<Integer> XAPK_MAIN_VERSION  = newPreference("xapk.main.version", 1, "Main XAPK Version", "Version code for the current 'main' APK expansion file.");
	public static final Preference<String>  XAPK_MAIN_FILE     = newPreference("xapk.main.file", "", "Main XAPK File", "Points to the main APK expansion file.");
	public static final Preference<Integer> XAPK_PATCH_VERSION = newPreference("xapk.patch.version", 1, "Patch XAPK Version", "Version code for the current 'patch' APK expansion file.");
	public static final Preference<String>  XAPK_PATCH_FILE    = newPreference("xapk.patch.file", "", "Patch XAPK File", "Points to the patch APK expansion file.");
	
	public static AndroidConfig fromFile(File file) throws IOException {
		AndroidConfig config = new AndroidConfig();
		if (file.exists() && file.isFile()) {
			FileInputStream fin = new FileInputStream(file);
			try {
				config.load(fin);
			} finally {
				fin.close();
			}
		}
		config.init(null);
		return config;
	}
		
	private void load(InputStream in) throws IOException {		
		INIFile iniFile = new INIFile();
		iniFile.read(new BufferedReader(new InputStreamReader(in, "UTF-8"), 8192));			
		initProperties(iniFile.entrySet());
	}
	
	public void save(OutputStream out) throws IOException {
		INIFile iniFile = new INIFile();
		for (Entry<String, Var> entry : map.entrySet()) {
			iniFile.put(entry.getKey(), entry.getValue().getRaw());
		}
		iniFile.write(out);
	}

	@Override
	public void init(Map<String, String> overrides) throws IOException {
		loadVariables();
		if (overrides != null) {
			initProperties(overrides.entrySet());
		}
	}

	@Override
	public void loadVariables() throws IOException {
		//throw new RuntimeException("Not implemented");
	}

	@Override
	public void saveVariables() throws IOException {
		throw new RuntimeException("Not implemented");
	}
	
}
