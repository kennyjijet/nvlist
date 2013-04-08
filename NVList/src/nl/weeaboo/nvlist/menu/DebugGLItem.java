package nl.weeaboo.nvlist.menu;

import static nl.weeaboo.game.GameConfig.GL_DEBUG;

import java.awt.event.ActionEvent;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;

import nl.weeaboo.nvlist.Game;
import nl.weeaboo.settings.Preference;
import nl.weeaboo.vn.impl.nvlist.Novel;

public class DebugGLItem extends GameMenuAction {
	
	@Override
	public JMenuItem createItem(Game game, Novel nvl) {
		JCheckBoxMenuItem item = new JCheckBoxMenuItem("Check for OpenGL errors");
		item.setSelected(game.getConfig().get(GL_DEBUG));
		return item;
	}

	@Override
	public void actionPerformed(JMenuItem item, ActionEvent e, Game game, Novel nvl) {
		game.getConfig().set(GL_DEBUG, item.isSelected());
	}

	@Override
	public <T> void onPropertyChanged(JMenuItem item, Preference<T> pref, T oldval, T newval) {
		if (pref.getKey().equals(GL_DEBUG.getKey())) {
			item.setSelected(Boolean.TRUE.equals(newval));
		}
	}
}
