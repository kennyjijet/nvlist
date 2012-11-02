package nl.weeaboo.vn;

import java.io.Serializable;

import nl.weeaboo.styledtext.StyledText;
import nl.weeaboo.styledtext.TextStyle;

public interface ITextRenderer extends Serializable {

	//Functions
	public void destroy();
	
	public void draw(IDrawBuffer buf, short z, boolean clipEnabled, BlendMode blendMode, int argb,
			double dx, double dy);
	
	//Getters
	public int getEndLine();
	
	public int getLineCount();
	
	public double getTextWidth();
	public double getTextWidth(int startLine, int endLine);
	
	public double getTextHeight();	
	public double getTextHeight(int startLine, int endLine);
	
	public int getCharOffset(int line);
	
	//Setters
	public void setMaxSize(double w, double h);

	public void setText(StyledText stext);
	
	public void setVisibleText(int startLine, double visibleChars);
	
	public void setDefaultStyle(TextStyle ts);
	
	public void setDisplayScale(double scale);
	
	public void setCursor(IDrawable cursor);
	
}
