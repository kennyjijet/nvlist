package nl.weeaboo.nvlist.menu;

import static nl.weeaboo.game.BaseGameConfig.TRUE_FULLSCREEN;

import java.awt.event.ActionEvent;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;

import nl.weeaboo.game.IGameDisplay;
import nl.weeaboo.nvlist.Game;
import nl.weeaboo.settings.Preference;
import nl.weeaboo.vn.impl.nvlist.Novel;

public class TrueFullscreenItem extends GameMenuAction {
	
	@Override
	public JMenuItem createItem(Game game, Novel nvl) {
		JCheckBoxMenuItem item = new JCheckBoxMenuItem("Heavyweight fullscreen");
		item.setSelected(game.getConfig().get(TRUE_FULLSCREEN));
		return item;
	}

	@Override
	public void actionPerformed(JMenuItem item, ActionEvent e, Game game, Novel nvl) {
		game.getConfig().set(TRUE_FULLSCREEN, item.isSelected());
		
		IGameDisplay display = game.getDisplay();
		if (display.isFullscreen()) {
			display.setFullscreen(false);
			display.setFullscreen(true);
		}
	}

	@Override
	public <T> void onPropertyChanged(JMenuItem item, Preference<T> pref, T oldval, T newval) {
		if (pref.getKey().equals(TRUE_FULLSCREEN.getKey())) {
			item.setSelected(Boolean.TRUE.equals(newval));
		}
	}
}
