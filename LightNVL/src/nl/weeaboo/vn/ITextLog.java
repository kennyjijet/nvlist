package nl.weeaboo.vn;

import java.io.Serializable;

import nl.weeaboo.styledtext.StyledText;

public interface ITextLog extends Serializable {

	// === Functions ===========================================================	
	/**
	 * Remove all pages
	 */
	public void clear();
	
	// === Getters =============================================================
	/**
	 * @return The text of the page specified by index. The most recent page is
	 *         numbered <code>-1</code>, older pages have progressively lower
	 *         numbers.
	 */
	public StyledText getPage(int index);
	
	/**
	 * @return All pages stored, oldest first.
	 */
	public StyledText[] getPages();
	
	/**
	 * @return The total number of pages stored.
	 */
	public int getPageCount();
	
	// === Setters =============================================================
	/**
	 * Limits the page history to the specified number.
	 */
	public void setPageLimit(int numPages);
	
	public void setText(StyledText text);
	
	public void appendText(StyledText text);
	
}
