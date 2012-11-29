package nl.weeaboo.vn.impl.base;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nl.weeaboo.styledtext.MutableStyledText;
import nl.weeaboo.styledtext.MutableTextStyle;
import nl.weeaboo.styledtext.StyledText;
import nl.weeaboo.styledtext.TextStyle;
import nl.weeaboo.vn.ITextDrawable;
import nl.weeaboo.vn.ITextLog;
import nl.weeaboo.vn.ITextState;
import nl.weeaboo.vn.NovelPrefs;

public abstract class BaseTextState implements ITextState {
	
	private static final long serialVersionUID = BaseImpl.serialVersionUID;
	
	public static int ansiPalette[] = {
		//Regular colors
		0xFF000000, 0xFFBB0000, 0xFF00BB00, 0xFFBBBB00,
		0xFF0000BB, 0xFFBB00BB, 0xFF00BBBB, 0xFFBBBBBB,
		//Bright colors
		0xFF555555, 0xFFFF5555, 0xFF55FF55, 0xFFFFFF55,
		0xFF5555FF, 0xFFFF55FF, 0xFF55FFFF, 0xFFFFFFFF
	};
	
	private StyledText stext;
	private ITextLog textLog;
	private double baseTextSpeed;
	private double textSpeed;
	private ITextDrawable textDrawable;		
	
	protected BaseTextState() {
		this(new TextLog());
	}
	protected BaseTextState(ITextLog tl) {
		stext = new StyledText("");
		textLog = tl;
		reset0();
	}

	//Functions
	private void reset0() {
		baseTextSpeed = NovelPrefs.TEXT_SPEED.getDefaultValue();
		textSpeed = 1.0;
	}
	
	@Override
	public void reset() {
		textDrawable = null;
		
		if (textLog != null) {
			textLog.clear();
		}
		setText("");
		reset0();
	}
	
	public static StyledText convertAnsiCodes(StyledText stext) {
		char chars[] = new char[stext.length()];
		TextStyle styles[] = new TextStyle[stext.length()];
		int colors[] = new int[chars.length]; //Color overrides
		
		int len = 0;
		char[] textChars = new char[stext.length()];
		TextStyle[] textStyles = new TextStyle[stext.length()];
		stext.getText(textChars, 0, textChars.length);
		stext.getStyles(textStyles, 0, textStyles.length);
		
		//Find escape sequences and convert them to color changes
		Pattern ansiPattern = Pattern.compile("\\\\x1[bB]\\[(\\d+)(;(\\d+))?m");
		Matcher ansiMatcher = ansiPattern.matcher(stext.toString());
		int last = 0;
		int color = 0;
		while (ansiMatcher.find(last)) {
			int start = ansiMatcher.start();
			int end = ansiMatcher.end();
			if (start > last) {
				int skipped = start - last;
				System.arraycopy(textChars, last, chars, len, skipped);
				System.arraycopy(textStyles, last, styles, len, skipped);
				Arrays.fill(colors, len, len+skipped, color);
				len += skipped;
			}
			
			int cs = Integer.parseInt(ansiMatcher.group(1));			
			int brightness = 0;
			if (ansiMatcher.group(3) != null) {
				brightness = Integer.parseInt(ansiMatcher.group(3));
			}

			if (cs >= 30 && cs <= 37 && brightness >= 0 && brightness <= 1) {
				color = ansiPalette[(brightness<<3)+(cs-30)];
			} else {
				color = 0;
			}
			
			//System.out.println(text + " : " + start + " " + end + " " + Integer.toHexString(color));
			
			last = end;
		}
		
		int skipped = textChars.length - last;
		System.arraycopy(textChars, last, chars, len, skipped);
		System.arraycopy(textStyles, last, styles, len, skipped);
		Arrays.fill(colors, len, len+skipped, color);
		len += skipped;
		
		//Output time
		MutableStyledText result = new MutableStyledText(chars, 0, styles, 0, len);
		
		//Add color spans to stext
		int start = 0;
		int currentColor = 0;
		for (int n = 0; n < len; n++) {
			if (colors[n] != currentColor) {
				if (currentColor != 0) {
					MutableTextStyle ts = new MutableTextStyle();
					ts.setColor(currentColor);
					result.extendStyle(start, n, ts.immutableCopy());
				}
				
				currentColor = colors[n];
				start = n;
			}
		}
		if (start < len && currentColor != 0) {
			MutableTextStyle ts = new MutableTextStyle();
			ts.setColor(currentColor);
			result.extendStyle(start, len, ts.immutableCopy());
		}
		
		return result.immutableCopy();
	}
	
	protected void onTextSpeedChanged() {
		if (textDrawable != null) {
			textDrawable.setTextSpeed(getTextSpeed());
		}		
	}
	
	//Getters
	@Override
	public ITextLog getTextLog() {
		return textLog;
	}
	
	@Override
	public ITextDrawable getTextDrawable() {
		return textDrawable;
	}
	
	@Override
	public double getTextSpeed() {
		if (baseTextSpeed <= 0 || textSpeed <= 0) {
			return -1;
		}
		return baseTextSpeed * textSpeed;
	}
		
	//Setters		
	@Override
	public void setText(String t) {
		setText(new StyledText(t));
	}
	
	@Override
	public void setText(StyledText st) {
		st = convertAnsiCodes(st);
		
		stext = st;		
		if (textDrawable != null) {
			textDrawable.setStartLine(0);
		}
		setTextDrawableText(st);
	}
	
	@Override
	public void appendText(String t) {
		appendText(new StyledText(t));
	}
	
	@Override
	public void appendText(StyledText st) {
		st = convertAnsiCodes(st);
		
		stext = stext.concat(st);
		setTextDrawableText(stext);
	}
	
	@Override
	public void appendTextLog(String t, boolean newPage) {
		appendTextLog(new StyledText(t), newPage);
	}
	
	@Override
	public void appendTextLog(StyledText st, boolean newPage) {
		st = convertAnsiCodes(st);
		
		if (newPage) {
			textLog.setText(StyledText.EMPTY_STRING);
		}
		textLog.appendText(st);
	}
	
	private void setTextDrawableText(StyledText stext) {
		if (textDrawable == null) {
			return;
		}
		
		int sl = textDrawable.getStartLine();
		double vc = textDrawable.getVisibleChars();
		if (vc < 0 || vc >= textDrawable.getMaxVisibleChars()) {
			vc = textDrawable.getMaxVisibleChars();
		}
		
		textDrawable.setText(stext);
		textDrawable.setStartLine(sl);
		if (vc > textDrawable.getVisibleChars()) {
			textDrawable.setVisibleChars(vc);
		}
	}
	
	@Override
	public void setTextDrawable(ITextDrawable td) {
		if (textDrawable != td) {
			textDrawable = td;
			
			if (textDrawable != null) {
				textDrawable.setText(stext);
				textDrawable.setTextSpeed(getTextSpeed());
			}
		}
	}
	
	@Override
	public void setBaseTextSpeed(double ts) {
		if (baseTextSpeed != ts) {
			baseTextSpeed = ts;
			onTextSpeedChanged();
		}
	}
	
	@Override
	public void setTextSpeed(double ts) {
		if (textSpeed != ts) {
			textSpeed = ts;
			onTextSpeedChanged();
		}
	}
	
}
