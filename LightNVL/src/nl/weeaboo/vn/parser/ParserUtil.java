package nl.weeaboo.vn.parser;


public class ParserUtil {

	private static final char ZERO_WIDTH_SPACE = 0x200B;
	
	//Functions
	public static String concatLines(String[] lines) {
		StringBuilder sb = new StringBuilder();
		for (String line : lines) {
			sb.append(line);
			sb.append('\n');
		}
		return sb.toString();		
	}
	
	static final char escapeList[] = new char[] {
		'n', '\n', 'r', '\r', 't', '\t', 'f', '\f', '\"', '\"', '\'', '\'', '\\', '\\'
	};
	
	public static String escape(String s) {
		if (s == null) return null;
		
		StringBuilder sb = new StringBuilder(s.length());
		for (int n = 0; n < s.length(); n++) {
			char c = s.charAt(n);
			
			int t;
			for (t = 0; t < escapeList.length; t+=2) {
				if (c == escapeList[t+1]) {
					sb.append('\\');
					sb.append(escapeList[t]);
					break;
				}
			}			
			if (t >= escapeList.length) {
				sb.append(c);
			}
		}
		return sb.toString();
	}
	
	public static String unescape(String s) {
		char chars[] = new char[s.length()];
		s.getChars(0, chars.length, chars, 0);
		
		int t = 0;
		for (int n = 0; n < chars.length; n++) {
			if (chars[n] == '\\') {
				n++;
				chars[t] = unescape(chars[n]);
			} else {
				chars[t] = chars[n];
			}
			t++;
		}
		return new String(chars, 0, t);
	}
	
	public static char unescape(char c) {
		for (int n = 0; n < escapeList.length; n+=2) {
			if (c == escapeList[n]) {
				return escapeList[n+1];
			}
		}
		return c;
	}
	
	private static boolean isCollapsibleSpace(char c) {
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
	
}
