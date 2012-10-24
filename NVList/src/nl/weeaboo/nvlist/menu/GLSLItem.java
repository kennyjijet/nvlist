package nl.weeaboo.nvlist.menu;

import static nl.weeaboo.game.BaseGameConfig.*;
import java.awt.event.ActionEvent;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;

import nl.weeaboo.nvlist.Game;
import nl.weeaboo.settings.Preference;
import nl.weeaboo.vn.impl.nvlist.Novel;

public class GLSLItem extends GameMenuAction {
	
	@Override
	public JMenuItem createItem(Game game, Novel nvl) {
		JCheckBoxMenuItem item = new JCheckBoxMenuItem("Enable GLSL shaders");
		item.setSelected(game.getConfig().get(ENABLE_SHADERS));
		return item;
	}

	@Override
	public void actionPerformed(JMenuItem item, ActionEvent e, Game game, Novel nvl) {
		game.getConfig().set(ENABLE_SHADERS, item.isSelected());
	}

	@Override
	public <T> void onPropertyChanged(JMenuItem item, Preference<T> pref, T oldval, T newval) {
		if (pref.getKey().equals(ENABLE_SHADERS.getKey())) {
			item.setSelected(Boolean.TRUE.equals(newval));
		}
	}
}
