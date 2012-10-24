package nl.weeaboo.nvlist.menu;

import static nl.weeaboo.vn.NovelPrefs.AUTO_READ_WAIT;

public class AutoReadWaitMenu extends PrefRangeMenu<Integer> {

	private static final String labels[] = {
		"0 ms",
		"500 ms",
		"1000 ms",
		"2000 ms",
		"4000 ms"
	};
	
	private static final Integer values[] = {
		0,
		500,
		1000,
		2000,
		4000
	};
	
	public AutoReadWaitMenu() {
		super(AUTO_READ_WAIT, "Auto read delay", '\0', labels, values);
	}
	
}
