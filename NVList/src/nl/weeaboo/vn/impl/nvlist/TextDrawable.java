package nl.weeaboo.vn.impl.nvlist;

import nl.weeaboo.lua2.io.LuaSerializable;
import nl.weeaboo.textlayout.LineElement;
import nl.weeaboo.textlayout.LineLayout;
import nl.weeaboo.textlayout.TextLayout;
import nl.weeaboo.vn.IInput;
import nl.weeaboo.vn.ILayer;
import nl.weeaboo.vn.ITextRenderer;
import nl.weeaboo.vn.impl.base.BaseTextDrawable;

@LuaSerializable
public class TextDrawable extends BaseTextDrawable {

	private static final long serialVersionUID = NVListImpl.serialVersionUID;
		
	public TextDrawable(ITextRenderer tr) {
		super(tr);
	}
	
	//Functions
	@Override
	public boolean update(ILayer layer, IInput input, double effectSpeed) {
		if (super.update(layer, input, effectSpeed)) {
			markChanged();
		}
		
		//System.out.println(Arrays.toString(consumePressedLinks()));
				
		return consumeChanged();
	}

	//Getters
	protected static double getLayoutLeading(boolean rtl, TextLayout layout, int startLine, int endLine) {
		startLine = Math.max(0, startLine);
		endLine = Math.min(layout.getNumLines(), endLine);
		if (endLine <= startLine) {
			return 0;
		}
		
		double w = Double.MAX_VALUE;
		for (int line = startLine; line < endLine; line++) {
			w = Math.min(w, rtl ? layout.getPadRight(line) : layout.getPadLeft(line));
		}		
		return w;
	}

	protected static double getLayoutTrailing(boolean rtl, TextLayout layout, int startLine, int endLine) {
		startLine = Math.max(0, startLine);
		endLine = Math.min(layout.getNumLines(), endLine);
		if (endLine <= startLine) {
			return 0;
		}

		double w = Double.MAX_VALUE;
		for (int line = startLine; line < endLine; line++) {
			w = Math.min(w, rtl ? layout.getPadLeft(line) : layout.getPadRight(line));
		}
		return w;
	}

	protected static int getEndLine(TextLayout layout, int startLine, double maxHeight) {
		final int lineCount = layout.getNumLines();
		startLine = Math.max(0, Math.min(lineCount, startLine));
		if (maxHeight <= 0) {
			return lineCount;
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
	
	protected static LineElement getLayoutHitElement(TextLayout layout, int startLine, int endLine, double cx, double cy) {
		int line = layout.getLineIndexAt((float)cy + layout.getLineTop(startLine));
		if (line >= startLine && line < endLine) {
			LineLayout ll = layout.getLine(line);
			int elem = ll.getElementIndexAt((float)cx);
			if (elem >= 0) {
				return ll.getElement(elem);
			}
		}
		return null;		
	}
	
	//Setters
	
}
