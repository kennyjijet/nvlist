package nl.weeaboo.nvlist.menu;

import nl.weeaboo.nvlist.Game;
import nl.weeaboo.settings.IConfig;
import nl.weeaboo.settings.Preference;
import nl.weeaboo.vn.impl.nvlist.Novel;

public class PrefRangeMenu<T extends Comparable<T>> extends RangeMenu<T> {

	private final Preference<T> pref;
	
	public PrefRangeMenu(Preference<T> pref, String label, char mnemonic, String[] labels, T[] values) {
		super(label, mnemonic, labels, values);
		this.pref = pref;
	}

	@Override
	protected int getSelectedIndex(Game game, T[] values) {
		IConfig config = game.getConfig();
		return getSelectedIndex(values, config.get(pref));
	}

	@Override
	protected void onItemSelected(Game game, Novel nvl, int index, String label, T value) {
		game.getConfig().set(pref, value);
	}
	
}
