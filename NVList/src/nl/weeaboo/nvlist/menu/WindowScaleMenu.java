package nl.weeaboo.nvlist.menu;

import javax.swing.JMenuItem;

import nl.weeaboo.common.Dim;
import nl.weeaboo.game.IGameDisplay;
import nl.weeaboo.nvlist.Game;
import nl.weeaboo.vn.impl.nvlist.Novel;

public class WindowScaleMenu extends RangeMenu<Double> {

	private static final String labels[] = {
		" 25%",
		" 50%",
		"100%",
		"200%",
		"300%",
		"400%"
	};
	
	private static final Double values[] = {
		0.25,
		0.50,
		1.00,
		2.00,
		3.00,
		4.00
	};
	
	private Dim origSize;
	private double scale;
	
	public WindowScaleMenu() {
		super("Windowed mode scale", '\0', labels, values);
		scale = 1;
	}

	@Override
	public JMenuItem createItem(Game game, Novel nvl) {
		JMenuItem item = super.createItem(game, nvl);
		
		IGameDisplay display = game.getDisplay();
		item.setEnabled(!display.isEmbedded() && !display.isFullscreen());
		
		return item;
	}

	@Override
	protected JMenuItem createSubItem(Game game, Novel nvl, String lbl, Double val) {
		return new JMenuItem(lbl);
	}
	
	@Override
	protected int getSelectedIndex(Game game, Double[] values) {
		return getSelectedIndex(values, Double.valueOf(scale));
	}

	@Override
	protected void onItemSelected(Game game, Novel nvl, int index, String label, Double value) {
		if (value == null) {
			return;
		}
		
		scale = value;
		
		IGameDisplay display = game.getDisplay();		
		Dim size = display.getWindowedSize();
		if (origSize == null) {
			origSize = size;
		}
		
		int w = Math.max(1, (int)Math.round(origSize.w * scale));
		int h = Math.max(1, (int)Math.round(origSize.h * scale));
		display.setWindowedSize(w, h, true);
	}
	
}
