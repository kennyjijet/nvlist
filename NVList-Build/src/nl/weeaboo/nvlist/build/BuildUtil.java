package nl.weeaboo.nvlist.build;

import nl.weeaboo.settings.Preference;


public final class BuildUtil {

	/**
	 * Local redefinition for BuildConfig.PACKAGE, should only be used in config.get()
	 */
	public static Preference<String> PACKAGE = Preference.newPreference("package", "com.example", "", "");
	
	private BuildUtil() {
	}
	
	public static boolean isValidPackage(String pkg) {
		if (pkg == null || pkg.equals("") || pkg.startsWith("com.example")) {
			return false;
		}
		
		String[] parts = pkg.split("\\.");
		if (parts.length == 0) {
			return false;
		}
		
		for (String part : parts) {
			if (part.length() == 0) return false;
			
			for (int n = 0; n < part.length(); ) {
				int c = part.codePointAt(n);
				if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || c == '_') {
					//pass
				} else if (n > 0 && c >= '0' && c <= '9') {
					//pass
				} else {
					return false; //fail
				}
				n += Character.charCount(c);
			}
		}
		return true;
	}
	
}
