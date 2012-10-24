package nl.weeaboo.nvlist.menu;

import static nl.weeaboo.vn.NovelPrefs.PRELOADER_LOOK_AHEAD;

public class PreloaderMenu extends PrefRangeMenu<Integer> {

	private static final String labels[] = {
		"Off", "1 line", "3 lines", "5 lines", "8 lines", "15 lines", "30 lines", "50 lines"
	};
	
	private static final Integer values[] = {
		0, 1, 3, 5, 8, 15, 30, 50
	};
	
	public PreloaderMenu() {
		super(PRELOADER_LOOK_AHEAD, "Preloader look-ahead", '\0', labels, values);
	}
	
}
