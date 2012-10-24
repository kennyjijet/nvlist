package nl.weeaboo.vn.impl.base;

import nl.weeaboo.lua2.io.LuaSerializable;
import nl.weeaboo.styledtext.StyledText;
import nl.weeaboo.vn.ITextLog;
import nl.weeaboo.vn.NovelPrefs;

@LuaSerializable
public class TextLog implements ITextLog {

	private static final long serialVersionUID = BaseImpl.serialVersionUID;
	
	private int maxPages;
	private StyledText[] pages;
	private int off, len;
	
	public TextLog() {
		maxPages = NovelPrefs.TEXTLOG_PAGE_LIMIT.getDefaultValue();
		pages = new StyledText[nextpow2(maxPages)];
	}
	
	//Functions
	private int nextpow2(int num) {
		long n = 1;
		while (n < num) {
			n <<= 1;
		}
		if (n >= Integer.MAX_VALUE) {
			throw new IllegalArgumentException("num is too large: " + num);
		}
		return (int)n;
	}
	
	@Override
	public void clear() {
		for (int n = 0; n < len; n++) {
			pages[(off+n) & (pages.length-1)] = null;
		}
		off = len = 0;
	}
	
	protected void addPage(StyledText stext) {
		while (len >= maxPages) {
			removePage();
		}
		pages[(off + len) & (pages.length - 1)] = stext;
		len++;
	}
	
	protected void removePage() {
		pages[off] = null;
		off = (off + 1) & (pages.length - 1);
		len--;
	}
	
	//Getters
	@Override
	public StyledText getPage(int delta) {
		return pages[(off+len+delta) & (pages.length-1)];
	}
	
	@Override
	public StyledText[] getPages() {
		return getPages(null);
	}
	
	protected StyledText[] getPages(StyledText[] out) {
		if (out == null || out.length < len) {
			out = new StyledText[len];
		}
		
		if (off + len <= pages.length) {
			System.arraycopy(pages, off, out, 0, len);
		} else {
			int len0 = pages.length-off;
			System.arraycopy(pages, off, out, 0, len0);
			System.arraycopy(pages, 0, out, len0, len-len0);
		}
		
		return out;
	}

	@Override
	public int getPageCount() {
		return len;
	}
	
	//Setters
	@Override
	public void setPageLimit(int numPages) {
		if (maxPages != numPages) {
			maxPages = numPages;
			
			StyledText[] newPages = new StyledText[nextpow2(maxPages)];
			for (int n = 0; n < len; n++) {
				newPages[n] = pages[(off+n) & (pages.length-1)];
			}			
			pages = newPages;
			off = 0;
		}
	}
	
	@Override
	public void setText(StyledText text) {
		if (getPageCount() > 0) {
			int index = (off+len-1) & (pages.length-1);
			StyledText current = pages[index];
			if (current == null || current.length() == 0) {
				pages[index] = text;
			} else {
				addPage(text);
			}
		} else {
			addPage(text);
		}
	}
	
	@Override
	public void appendText(StyledText text) {
		if (getPageCount() == 0) {
			addPage(text);
		} else {
			int index = (off+len-1) & (pages.length-1);
			StyledText current = pages[index];
			if (current != null && current.length() > 0) {
				pages[index] = current.concat(text);
			} else {
				pages[index] = text;
			}
		}
	}
	
}
