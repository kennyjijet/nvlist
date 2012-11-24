package nl.weeaboo.vn.parser;

import java.text.BreakIterator;

import nl.weeaboo.common.StringUtil;
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
	
	protected boolean isEmptyLine(String line) {
		final int L = line.length();
		for (int n = 0; n < L; n++) {
			if (!Character.isWhitespace(line.charAt(n))) {
				return false;
			}
		}
		return true;
	}
	
	public int countTextLines(boolean countEmptyLines) {
		int count = 0;
		for (int n = 0; n < compiledLines.length; n++) {
			if (compiledModes[n] == Mode.TEXT) {
				if (countEmptyLines || !isEmptyLine(srcLines[n])) {
					count++;
				}
			}
		}
		return count;
	}
	
	public int countTextWords() {
		return countTextWords(BreakIterator.getWordInstance(StringUtil.LOCALE));
	}
	public int countTextWords(BreakIterator wordBreakIterator) {
		int count = 0;
		for (int n = 0; n < compiledLines.length; n++) {
			if (compiledModes[n] == Mode.TEXT) {
				if (!isEmptyLine(srcLines[n])) {
					wordBreakIterator.setText(srcLines[n]);
					
					int index = wordBreakIterator.first();
					while (index != BreakIterator.DONE) {
						index = wordBreakIterator.next();
						if (index != BreakIterator.DONE) {
							count++;
						}
					}
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
