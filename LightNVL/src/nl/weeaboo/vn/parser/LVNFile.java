package nl.weeaboo.vn.parser;

import nl.weeaboo.vn.parser.LVNParser.Mode;

public class LVNFile {

	private final String filename;
	private final String[] srcLines;
	private final String[] compiledLines;
	private final Mode[] compiledModes;
	
	public LVNFile(String filename, String[] srcLines, String[] compiledLines,
			Mode[] compiledModes)
	{
		this.filename = filename;
		this.srcLines = srcLines;
		this.compiledLines = compiledLines;
		this.compiledModes = compiledModes;
	}
	
	//Functions
	public String compile() {
		return ParserUtil.concatLines(getCompiledLines());		
	}
	
	public int countTextLines(boolean countEmptyLines) {
		int count = 0;
		for (int n = 0; n < compiledLines.length; n++) {
			if (compiledModes[n] == Mode.TEXT) {
				if (countEmptyLines || compiledLines[n].trim().length() > 0) {
					count++;
				}
			}
		}
		return count;
	}
	
	//Getters
	public String getFilename() {
		return filename;
	}
	public String[] getSourceLines() {
		return srcLines;
	}
	public String[] getCompiledLines() {
		return compiledLines;
	}
	public Mode[] getCompiledModes() {
		return compiledModes;
	}
	
	//Setters
	
}
