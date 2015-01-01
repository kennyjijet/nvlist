package nl.weeaboo.vn.script.lvn;

import nl.weeaboo.common.Checks;
import nl.weeaboo.common.StringUtil;

class CompiledLvnFile implements ICompiledLvnFile {

	private final String filename;
	private final String[] srcLines;
	private final String[] compiledLines;
	private final LvnMode[] compiledModes;

	public CompiledLvnFile(String filename, String[] srcLines, String[] compiledLines,
			LvnMode[] compiledModes)
	{
        Checks.checkArgument(srcLines.length == compiledLines.length, "source line count != compiled line count");
	    Checks.checkArgument(compiledLines.length == compiledModes.length, "compiled lines length != compiles modes length");

		this.filename = filename;
		this.srcLines = srcLines.clone();
		this.compiledLines = compiledLines.clone();
		this.compiledModes = compiledModes.clone();
	}

	//Functions
	@Override
	public int countTextLines(boolean countEmptyLines) {
		int count = 0;
		for (int n = 0; n < compiledLines.length; n++) {
			if (compiledModes[n] == LvnMode.TEXT) {
				if (countEmptyLines || !StringUtil.isWhitespace(srcLines[n])) {
					count++;
				}
			}
		}
		return count;
	}

	//Getters
	@Override
	public String getFilename() {
		return filename;
	}

    @Override
    public String getCompiledContents() {
        return ParserUtil.concatLines(compiledLines);
    }

	String[] getSourceLines() {
		return srcLines;
	}
	String[] getCompiledLines() {
		return compiledLines;
	}
	LvnMode[] getCompiledModes() {
		return compiledModes;
	}

	//Setters

}
