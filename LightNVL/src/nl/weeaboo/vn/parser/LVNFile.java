package nl.weeaboo.vn.parser;

import java.text.BreakIterator;

import nl.weeaboo.common.StringUtil;

public class LVNFile {

	public enum Mode {
		TEXT(false),
		CODE(true), MULTILINE_CODE(false),
		COMMENT(true), MULTILINE_COMMENT(false);
		
		private boolean singleLine;
		
		private Mode(boolean singleLine) {
			this.singleLine = singleLine;
		}
		
		public boolean isSingleLine() { return singleLine; }
	}
	
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
	
	protected boolean isWhitespace(String text) {
		return isWhitespace(text, 0, text.length());
	}
	protected boolean isWhitespace(String text, int from, int to) {
		int n = from;
		while (n < to) {
			int c = text.codePointAt(n);			
			if (!Character.isWhitespace(c)) {
				return false;
			}
			n += Character.charCount(c);
		}
		return true;
	}
	protected boolean isWord(String text) {
		return isWord(text, 0, text.length());
	}
	protected boolean isWord(String text, int from, int to) {
		int n = from;
		while (n < to) {
			int c = text.codePointAt(n);			
			if (Character.isLetterOrDigit(c)) {
				return true;
			}
			n += Character.charCount(c);
		}
		return false;
	}
	
	public int countTextLines(boolean countEmptyLines) {
		int count = 0;
		for (int n = 0; n < compiledLines.length; n++) {
			if (compiledModes[n] == Mode.TEXT) {
				if (countEmptyLines || !isWhitespace(srcLines[n])) {
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
		int totalWords = 0;
		for (int n = 0; n < compiledLines.length; n++) {
			int lineWords = 0;
			
			String line = srcLines[n];
			if (compiledModes[n] == Mode.TEXT) {
				if (!isWhitespace(line)) {
					wordBreakIterator.setText(line);
					
					int index = wordBreakIterator.first();
					while (index != BreakIterator.DONE) {
						int lastIndex = index;
						index = wordBreakIterator.next();
						if (index != BreakIterator.DONE && isWord(line, lastIndex, index)) {							
							lineWords++;
						}
					}
					
					//System.out.printf("%03d: %s\n", lineWords, line);
				}
			}
			totalWords += lineWords;
		}
		return totalWords;
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
