package nl.weeaboo.vn;

import java.io.Serializable;

import nl.weeaboo.styledtext.StyledText;

public interface ITextState extends Serializable {

	// === Functions ===========================================================
	public void reset();
	
	// === Getters =============================================================

	public ITextDrawable getTextDrawable();
	public ITextLog getTextLog();
	public double getTextSpeed();
	
	// === Setters =============================================================
	
	public void setText(String str);
	public void setText(StyledText st);
	public void appendText(String str);
	public void appendText(StyledText st);
	public void appendTextLog(String str, boolean newPage);
	public void appendTextLog(StyledText st, boolean newPage);
	public void setTextDrawable(ITextDrawable td);
	public void setBaseTextSpeed(double ts);
	public void setTextSpeed(double ts);
	
}
