package nl.weeaboo.vn;

import nl.weeaboo.styledtext.StyledText;
import nl.weeaboo.styledtext.TextStyle;

public interface ITextDrawable extends IDrawable {

	// === Functions ===========================================================
	
	// === Getters =============================================================
	
	public StyledText getText();
	
	/**
	 * @return The number of visible characters
	 */
	public double getVisibleChars();
	
	/**
	 * @return The first line currently displayed
	 */
	public int getStartLine();
	
	/**
	 * @return The line index following the last visible line.
	 */
	public int getEndLine();
	
	/**
	 * @return The total number of lines
	 */
	public int getLineCount();
	
	/**
	 * @return The speed at which characters fade in. 
	 */
	public double getTextSpeed();
	
	/**
	 * @return <code>true</code> if all characters between
	 *         <code>startLine</code> and <code>endLine</code> are visible.
	 */
	public boolean getCurrentLinesFullyVisible();
	
	/**
	 * @return <code>true</code> if <code>getEndLine() == getLineCount() && getCurrentLinesFullyVisible()</code>.
	 */
	public boolean getFinalLineFullyVisible();
	
	/**
	 * @return The difference between the charoffset of the end of the last
	 *         visible line, and the start of the first visible line.
	 */
	public int getMaxVisibleChars();
	
	/**
	 * @param line The line number (starting at 0)
	 * @return The starting character for <code>line</code>, or the total number
	 *         of characters for <code>line >= getLineCount()</code>.
	 */
	public int getCharOffset(int line);
	
	/**
	 * @return The width of the visible text.
	 */
	public double getTextWidth();
	
	/**
	 * @return The height of the visible text.
	 */
	public double getTextHeight();
	
	/**
	 * @return The height of the visible text between lines <code>start</code>
	 *         and <code>end</code> (exclusive).
	 */
	public double getTextHeight(int start, int end);
		
	public double getPadding();
	
	/**
	 * @return width minus the internal padding
	 */
	public double getInnerWidth();
	
	/**
	 * @return height minus the internal padding
	 */
	public double getInnerHeight();
		
	public int getBackgroundColorRGB();
	public int getBackgroundColorARGB();
	public double getBackgroundRed();
	public double getBackgroundGreen();
	public double getBackgroundBlue();
	public double getBackgroundAlpha();
	
	public TextStyle getDefaultStyle();
	
	/**
	 * @return The text anchor
	 * @see #setAnchor(int)
	 */
	public int getAnchor();
	
	public IDrawable getCursor();
	
	// === Setters =============================================================
	
	public void setText(String text);
	
	public void setText(StyledText text);
	
	/**
	 * @param sl The new starting line
	 */
	public void setStartLine(int sl);
	
	/**
	 * @param vc The number of visible characters
	 */
	public void setVisibleChars(double vc);
	
	/**
	 * @param ts The speed at which characters fade in, must be
	 *        <code>&gt;0</code>
	 */
	public void setTextSpeed(double ts);
	
	/**
	 * Specifies the width/height of the text bounds. Text is automatically
	 * word-wrapped to fit the width.
	 */
	public void setSize(double w, double h);
	
	/**
	 * Calls both {@link #setPos(double, double)} and {@link #setSize(double, double)}.
	 */
	public void setBounds(double x, double y, double w, double h);
	
	/**
	 * Sets the internal padding of the text compared to the outer bounds.
	 */
	public void setPadding(double p);
	
	public void setBackgroundColor(double r, double g, double b);
	public void setBackgroundColor(double r, double g, double b, double a);
	public void setBackgroundColorRGB(int rgb);
	public void setBackgroundColorARGB(int argb);
	public void setBackgroundAlpha(double a);
	
	public void setDefaultStyle(TextStyle ts);
	
	/**
	 * Sets the relative position of the text within the textbox's bounds
	 * @param a The anchor, uses numpad number positions as directions
	 */
	public void setAnchor(int a);
	
	public void setCursor(IDrawable d, boolean autoConfig, boolean autoPos);
	
}
