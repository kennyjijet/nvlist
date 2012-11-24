package nl.weeaboo.vn.impl.nvlist;


import nl.weeaboo.lua2.io.LuaSerializable;
import nl.weeaboo.textlayout.TextLayout;
import nl.weeaboo.vn.ITextRenderer;
import nl.weeaboo.vn.impl.base.BaseTextDrawable;


@LuaSerializable
public class TextDrawable extends BaseTextDrawable {

	private static final long serialVersionUID = NVListImpl.serialVersionUID;
		
	public TextDrawable(ITextRenderer tr) {
		super(tr);
	}
	
	//Functions	
		
	//Getters
	protected static double getLayoutRight(TextLayout layout, int startLine, int endLine) {
		startLine = Math.max(0, startLine);
		endLine = Math.min(layout.getNumLines(), endLine);
		
		double w = 0;
		for (int line = startLine; line < endLine; line++) {
			w = Math.max(w, layout.getLineRight(line));
		}
		return w;
	}
	
	protected static int getEndLine(TextLayout layout, int startLine, double maxHeight) {
		final int lineCount = layout.getNumLines();
		startLine = Math.max(0, Math.min(lineCount, startLine));
		if (maxHeight <= 0) {
			return layout.getNumLines();
		}
		
		final double startTop = layout.getLineTop(startLine);
		final double limit = startTop + maxHeight;
		int endLine = startLine;
		while (endLine < lineCount && layout.getLineBottom(endLine) <= limit) {
			endLine++;
		}
		
		//Always show at least one line (prevents text from disappearing if its bounds are too small)
		return Math.max(startLine+1, endLine);
	}
	
	//Setters
	
}
