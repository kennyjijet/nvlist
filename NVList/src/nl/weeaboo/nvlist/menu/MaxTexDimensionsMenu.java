package nl.weeaboo.nvlist.menu;

import static nl.weeaboo.game.BaseGameConfig.MAX_TEX_SIZE;

public class MaxTexDimensionsMenu extends PrefRangeMenu<Integer> {

	private static final String labels[] = {
		"64x64", "128x128", "256x256", "512x512", "1024x1024", "2048x2048", "4096x4096", "8192x8192"
	};
	
	private static final Integer values[] = {
		64, 128, 256, 512, 1024, 2048, 4096, 8192
	};
	
	public MaxTexDimensionsMenu() {
		super(MAX_TEX_SIZE, "Texture dimension limit", '\0', labels, values);
	}
	
}
