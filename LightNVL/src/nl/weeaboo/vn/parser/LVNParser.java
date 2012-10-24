package nl.weeaboo.vn.parser;

import static nl.weeaboo.vn.parser.ParserUtil.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import nl.weeaboo.io.StreamUtil;

public class LVNParser {

	protected enum Mode {
		TEXT(false),
		CODE(true), MULTILINE_CODE(false),
		COMMENT(true), MULTILINE_COMMENT(false);
		
		private boolean singleLine;
		
		private Mode(boolean singleLine) {
			this.singleLine = singleLine;
		}
		
		public boolean isSingleLine() { return singleLine; }
	}
	
	private String[] compiledLines;
	private Mode[] compiledModes;
	
	public LVNParser() {
	}
	
	//Functions
	public LVNFile parseFile(String filename, InputStream in) throws ParseException, IOException {
		byte bytes[] = StreamUtil.readFully(in);
		int off = StreamUtil.skipBOM(bytes, 0, bytes.length);
		String[] src = StreamUtil.readLines(bytes, off, bytes.length-off);
		parseLines(filename, src, 1, 1);
		
//		int t = 1;
//		for (String line : compiledLines) {
//			System.out.println(t + ": " + line);
//			t++;
//		}
		
		return new LVNFile(filename, src, compiledLines, compiledModes);
	}
	
	private void parseLines(String filename, String[] lines, int startLineNum,
			int textLineNum) throws ParseException
	{
		compiledLines = new String[lines.length];
		compiledModes = new Mode[lines.length];
		
		Mode mode = Mode.TEXT;		
		for (int n = 0; n < lines.length; n++) {
			String line = lines[n].trim();
			
			//End single-line modes
			if (mode.isSingleLine()) mode = Mode.TEXT;
			
			//Look for comment starts				
			if (mode == Mode.TEXT && line.startsWith("#")) mode = Mode.COMMENT;
			if (mode == Mode.TEXT && line.startsWith("@")) mode = Mode.CODE;
			
			//Process line			
			if (mode == Mode.TEXT) {
				compiledModes[n] = mode;
				if (line.length() > 0) {
					line = parseTextLine(filename, line, textLineNum);
					textLineNum++;
				}
			} else if (mode == Mode.CODE || mode == Mode.MULTILINE_CODE) {
				if (line.startsWith("@@")) {
					compiledModes[n] = Mode.MULTILINE_CODE;
					
					line = line.substring(2);
					mode = (mode == Mode.MULTILINE_CODE ? Mode.TEXT : Mode.MULTILINE_CODE);					
				} else {
					compiledModes[n] = mode;
					if (mode == Mode.CODE && line.startsWith("@")) {					
						line = line.substring(1);
					}
				}

				if (mode == Mode.CODE || mode == Mode.MULTILINE_CODE) {
					line = parseCodeLine(line);
				} else {
					line = "";
				}
			} else if (mode == Mode.COMMENT || mode == Mode.MULTILINE_COMMENT) {
				if (line.startsWith("##")) {
					compiledModes[n] = Mode.MULTILINE_COMMENT;
					
					mode = (mode == Mode.MULTILINE_COMMENT ? Mode.TEXT : Mode.MULTILINE_COMMENT);					
				} else {
					compiledModes[n] = mode;					
				}
				
				//Ignore commented lines
				line = "";
			} else {
				throw new ParseException(filename, startLineNum+n, "Invalid mode: " + mode);
			}
			
			compiledLines[n] = line;
		}
	}
	
	protected String parseTextLine(String filename, String line, int textLineNum)
		throws ParseException
	{
		if (line.length() == 0) {
			return line; //Ignore empty lines
		}
		
		List<String> out = new ArrayList<String>(8);
		out.add(beginParagraphCommand(filename, textLineNum));
		
		StringBuilder sb = new StringBuilder(line.length());
		
		for (int n = 0; n < line.length(); n++) {
			char c = line.charAt(n);
			if (c == '\\') {
				n++;
				c = line.charAt(n);
				sb.append(ParserUtil.unescape(c));
			} else if (c == '[' || c == '$') { //Read [lua code] or $stringify or ${stringify}
				int start = n;
				int end = start;

				char startChar = c;
				char endChar = ' ';
				if (startChar == '[') {
					endChar = ']';
				} else if (startChar == '$' && start+1 < line.length() && line.charAt(start+1) == '{') {
					start++;
					endChar = '}';
				}
						
				if (sb.length() > 0) { //Flush buffered chars
					String ln = appendTextCommand(sb.toString());
					if (ln.length() > 0) out.add(ln);
					sb.delete(0, sb.length());
				}
				
				boolean inQuotes = false;
				int brackets = (startChar == '[' ? 1 : 0);
				for (int x = n+1; x < line.length(); x++) {
					int d = line.charAt(x);
					if (d == '\\') {
						x++;
					} else if (d == '\"') {
						inQuotes = !inQuotes;
					} else if (!inQuotes) {
						if (d == '[') brackets++;
						else if (d == ']') brackets--;
						
						if (brackets <= 0 && d == endChar) {
							end = x;
							break;
						}
					}
				}
				
				if (end > start+1) {
					String str = line.substring(start+1, end);
					if (startChar == '$') {
						out.add(parseStringifier(str));
					} else {
						out.add(parseCodeLine(str));
					}
				}
				
				n = end;
			} else {
				sb.append(c);
			}
		}
		
		if (sb.length() > 0) { //Flush buffered chars
			String ln = appendTextCommand(sb.toString());
			if (ln.length() > 0) out.add(ln);
			sb.delete(0, sb.length());
		}
		out.add(endParagraphCommand());
		
		//Merge out lines into a String
		for (String outLine : out) {
			if (sb.length() > 0) sb.append("; ");
			sb.append(outLine);
		}
		return sb.toString();
	}
	
	protected String parseStringifier(String str) {
		return String.format("paragraph.stringify(\"%s\")", escape(str));
	}	
	protected String parseCodeLine(String line) throws ParseException {
		return line.trim();
	}

	protected String beginParagraphCommand(String filename, int textLineNum) {
		return String.format("paragraph.start(\"%s\", %d)", escape(filename), textLineNum);
	}
	protected String appendTextCommand(String line) {
		if (line.length() == 0) return "";
		line = collapseWhitespace(escape(line), false);
		if (line.length() == 0) return "";
		return String.format("paragraph.append(\"%s\")", line);
	}
	protected String endParagraphCommand() {
		return String.format("paragraph.finish()");
	}
	
}
