package nl.weeaboo.vn.core.impl;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import nl.weeaboo.common.StringUtil;
import nl.weeaboo.obfuscator.AbstractXORObfuscator;

public final class Obfuscator extends AbstractXORObfuscator {

	private static final Set<String> shouldCompress = new HashSet<String>(Arrays.asList(
		"ktx", "ttf"
	));
	private static final Set<String> unencryptables = new HashSet<String>(Arrays.asList(
		"wav", "ogg", "oga", "mp3", "m4a", "aac", "wma", "mka",
		"avi", "mpg", "mpeg", "mp4", "mov", "ogv", "wmv", "mkv", "webm", "flv"
	));

	private static Obfuscator instance;

	private Obfuscator() {
		super();
	}

	//Functions
	public static synchronized Obfuscator getInstance() {
		if (instance == null) {
			instance = new Obfuscator();
		}
		return instance;
	}

	//Getters
	@Override
	public boolean shouldCompress(String filename) {
		String fext = StringUtil.getExtension(filename).toLowerCase();
		return shouldCompress.contains(fext);
	}

	@Override
	public boolean allowEncrypt(String filename) {
		String fext = StringUtil.getExtension(filename).toLowerCase();
		return !unencryptables.contains(fext);
	}

	//Setters

}
