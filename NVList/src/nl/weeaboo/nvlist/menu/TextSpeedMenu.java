package nl.weeaboo.nvlist.menu;

import static nl.weeaboo.vn.NovelPrefs.TEXT_SPEED;

public class TextSpeedMenu extends PrefRangeMenu<Double> {

	private static final double baseSpeed = 0.5;
	
	private static final String labels[] = {
		"Slower",
		"Slow",
		"Normal",
		"Fast",
		"Faster",
		"Instant"
	};
	
	private static final Double values[] = {
		0.25 * baseSpeed,
		0.50 * baseSpeed,
		1.00 * baseSpeed,
		2.00 * baseSpeed,
		4.00 * baseSpeed,
		999999.0
	};
	
	public TextSpeedMenu() {
		super(TEXT_SPEED, "Text speed", '\0', labels, values);
	}
	
}
