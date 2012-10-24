package nl.weeaboo.nvlist.menu;

import static nl.weeaboo.vn.NovelPrefs.AUTO_READ;

import java.awt.event.ActionEvent;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;

import nl.weeaboo.nvlist.Game;
import nl.weeaboo.settings.Preference;
import nl.weeaboo.vn.impl.nvlist.Novel;

public class AutoReadItem extends GameMenuAction {

	public AutoReadItem() {
	}
	
	@Override
	public JMenuItem createItem(Game game, Novel nvl) {
		JCheckBoxMenuItem item = new JCheckBoxMenuItem("Auto read");
		item.setSelected(game.getConfig().get(AUTO_READ));
		return item;
	}

	@Override
	public void actionPerformed(JMenuItem item, ActionEvent e, Game game, Novel nvl) {
		game.getConfig().set(AUTO_READ, item.isSelected());
	}

	@Override
	public <T> void onPropertyChanged(JMenuItem item, Preference<T> p, T oldval, T newval) {
		if (AUTO_READ.getKey().equals(p.getKey())) {
			if (item instanceof JCheckBoxMenuItem) {
				JCheckBoxMenuItem checkItem = (JCheckBoxMenuItem)item;
				checkItem.setSelected(Boolean.TRUE.equals(newval));
			}
		}
	}

}
