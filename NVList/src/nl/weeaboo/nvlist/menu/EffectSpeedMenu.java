package nl.weeaboo.nvlist.menu;

import static nl.weeaboo.vn.NovelPrefs.EFFECT_SPEED;

public class EffectSpeedMenu extends PrefRangeMenu<Double> {

	private static final String labels[] = {
		"Slower",
		"Slow",
		"Normal",
		"Fast",
		"Faster",
		"Instant"
	};
	
	private static final Double values[] = {
		0.25,
		0.50,
		1.00,
		2.00,
		4.00,
		999.0
	};
	
	public EffectSpeedMenu() {
		super(EFFECT_SPEED, "Effect speed", '\0', labels, values);
	}
	
}
