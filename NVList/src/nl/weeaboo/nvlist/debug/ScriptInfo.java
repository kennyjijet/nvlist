package nl.weeaboo.nvlist.debug;

import java.io.IOException;
import java.io.InputStream;

import nl.weeaboo.vn.IScriptLib;
import nl.weeaboo.vn.impl.lua.LuaNovel;
import nl.weeaboo.vn.parser.LVNFile;
import nl.weeaboo.vn.parser.LVNParser;
import nl.weeaboo.vn.parser.ParseException;

class ScriptInfo {

	private final String path;
	private final int lineCount, wordCount;
	
	public ScriptInfo(String path, int lines, int words) {
		this.path = path;
		this.lineCount = lines;
		this.wordCount = words;
	}
	
	//Functions
	public static ScriptInfo fromScript(IScriptLib scriptLib, String path) throws ParseException, IOException {
		LVNFile file = null;
		
		if (path.endsWith(".lvn")) {
			InputStream in = scriptLib.openScriptFile(path);
			if (in != null) {
				try {
					LVNParser parser = new LVNParser();
					file = parser.parseFile(path, in);
				} finally {
					in.close();
				}
			}
		}
		
		int lines = 0;
		int words = 0;
		if (file != null) {
			lines += file.countTextLines(false);
			words += file.countTextWords();
		}
		
		return new ScriptInfo(path, lines, words);
	}
	
	//Getters
	public String getFilename() {
		return path;
	}
	public boolean isBuiltIn() {
		return LuaNovel.isBuiltInScript(path);
	}
	public int getLineCount() {
		return lineCount;
	}
	public int getWordCount() {
		return wordCount;
	}
		
	//Setters
	
}
