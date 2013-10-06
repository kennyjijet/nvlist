package nl.weeaboo.vn.parser;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

import nl.weeaboo.common.StringUtil;

public class ParserUtil {

	private static final char ZERO_WIDTH_SPACE = 0x200B;
	
	//Functions
	public static LVNParser getParser(String engineVersion) {
		if (StringUtil.compareVersion(engineVersion, "4.0") < 0) {
			return new LVNParser3();
		}
		return new LVNParser4();
	}
	
	public static String concatLines(String[] lines) {
		StringBuilder sb = new StringBuilder();
		for (String line : lines) {
			sb.append(line);
			sb.append('\n');
		}
		return sb.toString();		
	}
	
	public static boolean isCollapsibleSpace(char c) {
		return c == ' ' || c == '\t' || c == '\f' || c == ZERO_WIDTH_SPACE;		
	}
	
	public static String collapseWhitespace(String s, boolean trim) {
		char chars[] = new char[s.length()];
		s.getChars(0, chars.length, chars, 0);
		
		int r = 0;
		int w = 0;
		while (r < chars.length) {
			char c = chars[r++];
			
			if (isCollapsibleSpace(c)) {
				//Skip any future characters if they're whitespace
				while (r < chars.length && isCollapsibleSpace(chars[r])) {
					r++;
				}
				
				if (w == 0 && trim) {
					continue; //Starts with space
				} else if (r >= chars.length && trim) {
					continue; //Ends with space
				}
			}
			
			chars[w++] = c;
		}
		
		return new String(chars, 0, w);
	}

	public static String getSrclocFilename(String srcloc) {
		return getSrclocFilename(srcloc, "?");
	}
	public static String getSrclocFilename(String srcloc, String defaultFilename) {
		if (srcloc == null) {
			return defaultFilename;
		}
		
		int index = srcloc.indexOf(':');
		if (index >= 0) {
			srcloc = srcloc.substring(0, index);
		}
		
		srcloc = srcloc.trim();
		
		if (srcloc.equals("?") || srcloc.equals("???") || srcloc.equals("undefined")) {
			return defaultFilename;
		}
		
		return srcloc;
	}
	
	public static int getSrclocLine(String srcloc) {
		if (srcloc == null) {
			return -1;
		}
		
		int index = srcloc.indexOf(':');
		
		if (index < 0) {
			return -1;
		}
		
		srcloc = srcloc.substring(index+1);
		
		try {
			return Integer.parseInt(srcloc);
		} catch (NumberFormatException nfe) {
			return -1;
		}
	}

	static int findBlockEnd(String str, int off, char endChar) {
		CharacterIterator itr = new StringCharacterIterator(str, off);
		return findBlockEnd(itr, endChar, null);
	}
	static int findBlockEnd(CharacterIterator itr, char endChar, StringBuilder out) {
		boolean inQuotes = false;
		int brackets = 0;
		
		for (char c = itr.current(); c != CharacterIterator.DONE; c = itr.next()) {			
			if (c == '\\') {
				if (out != null) out.append(c);
				c = itr.next();
			} else if (c == '\"') {
				inQuotes = !inQuotes;
			} else if (!inQuotes) {
				if (brackets <= 0 && c == endChar) {
					break;
				}
				else if (c == '[') brackets++;
				else if (c == ']') brackets--;
			}
			
			if (out != null && c != CharacterIterator.DONE) {
				out.append(c);			
			}
		}
		return itr.getIndex();
	}
	
}
