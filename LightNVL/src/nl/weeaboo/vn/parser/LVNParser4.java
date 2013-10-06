package nl.weeaboo.vn.parser;

import static nl.weeaboo.lua2.LuaUtil.escape;
import static nl.weeaboo.lua2.LuaUtil.unescape;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import nl.weeaboo.io.StreamUtil;
import nl.weeaboo.vn.parser.LVNFile.Mode;
import nl.weeaboo.vn.parser.TextParser.Token;

class LVNParser4 implements LVNParser {
	
	private final TextParser textParser;
	
	private String[] compiledLines;
	private Mode[] compiledModes;
	
	public LVNParser4() {
		textParser = new TextParser();
	}
	
	//Functions
	@Override
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
		StringBuilder temp = new StringBuilder();
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
					parseTextLine(temp, filename, line, textLineNum);
					line = temp.toString();
					temp.delete(0, temp.length());
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
	
	protected void parseTextLine(StringBuilder out, String filename, String line, int textLineNum)
		throws ParseException
	{
		if (line.length() == 0) {
			return; //Empty line
		}
		
		line = unescape(line);
		
		textParser.setInput(line);
		textParser.tokenize();
		
		List<String> triggers = new ArrayList<String>(4);
		for (Token token : textParser.tokens()) {
			if (token.getType() == TextParser.TOKEN_COMMAND) {
				triggers.add(token.getText());
			}
		}
		
		out.append("text(\"").append(escape(line)).append("\"");
		if (triggers.isEmpty()) {
			out.append(", nil");
		} else {
			out.append(", {");
			for (String trigger : triggers) {
				out.append("function() ").append(trigger).append(" end, ");
			}
			out.append("}");
		}
		out.append(", {")
			.append("filename=\"").append(escape(filename)).append("\", ")
			.append("line=").append(textLineNum).append(",")
			.append("}");
		out.append(")");
	}

	protected String parseCodeLine(String line) throws ParseException {
		return line.trim();
	}
	
}
