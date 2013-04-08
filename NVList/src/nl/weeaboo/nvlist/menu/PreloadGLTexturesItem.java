package nl.weeaboo.nvlist.menu;

import static nl.weeaboo.game.GameConfig.PRELOAD_GL_TEXTURES;

import java.awt.event.ActionEvent;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;

import nl.weeaboo.nvlist.Game;
import nl.weeaboo.settings.Preference;
import nl.weeaboo.vn.impl.nvlist.Novel;

public class PreloadGLTexturesItem extends GameMenuAction {
	
	@Override
	public JMenuItem createItem(Game game, Novel nvl) {
		JCheckBoxMenuItem item = new JCheckBoxMenuItem("Async PBO texture upload");
		//item.setEnabled(false); item.setToolTipText("Disabled due to unreliable implementation.");
		item.setSelected(game.getConfig().get(PRELOAD_GL_TEXTURES));
		return item;
	}

	@Override
	public void actionPerformed(JMenuItem item, ActionEvent e, Game game, Novel nvl) {
		game.getConfig().set(PRELOAD_GL_TEXTURES, item.isSelected());
	}

	@Override
	public <T> void onPropertyChanged(JMenuItem item, Preference<T> pref, T oldval, T newval) {
		if (pref.getKey().equals(PRELOAD_GL_TEXTURES.getKey())) {
			item.setSelected(Boolean.TRUE.equals(newval));
		}
	}
}
