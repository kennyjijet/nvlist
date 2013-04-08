package nl.weeaboo.nvlist.menu;

import static nl.weeaboo.game.GameConfig.LEGACY_GPU_EMULATION;

import java.awt.event.ActionEvent;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;

import nl.weeaboo.nvlist.Game;
import nl.weeaboo.settings.Preference;
import nl.weeaboo.vn.impl.nvlist.Novel;

public class LegacyGPUItem extends GameMenuAction {
	
	@Override
	public JMenuItem createItem(Game game, Novel nvl) {
		JCheckBoxMenuItem item = new JCheckBoxMenuItem("Emulate legacy GPU");
		item.setSelected(game.getConfig().get(LEGACY_GPU_EMULATION));
		return item;
	}

	@Override
	public void actionPerformed(JMenuItem item, ActionEvent e, Game game, Novel nvl) {
		game.getConfig().set(LEGACY_GPU_EMULATION, item.isSelected());
	}

	@Override
	public <T> void onPropertyChanged(JMenuItem item, Preference<T> pref, T oldval, T newval) {
		if (pref.getKey().equals(LEGACY_GPU_EMULATION.getKey())) {
			item.setSelected(Boolean.TRUE.equals(newval));
		}
	}
}
