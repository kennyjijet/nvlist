package nl.weeaboo.nvlist.menu;

import java.awt.event.ActionEvent;

import javax.swing.JMenuItem;

import nl.weeaboo.nvlist.Game;
import nl.weeaboo.settings.ConfigPropertyListener;
import nl.weeaboo.settings.Preference;
import nl.weeaboo.vn.impl.nvlist.Novel;

public abstract class GameMenuAction implements ConfigPropertyListener {

	private JMenuItem jmenuItem;
	
	public GameMenuAction() {
	}
	
	//Functions
	protected abstract JMenuItem createItem(Game game, Novel nvl);

	public void actionPerformed(ActionEvent e, Game game, Novel nvl) {
		actionPerformed(jmenuItem, e, game, nvl);
	}
	
	public abstract void actionPerformed(JMenuItem item, ActionEvent e, Game game, Novel nvl);
	
	public final <T> void onPropertyChanged(Preference<T> pref, T oldval, T newval) {
		if (jmenuItem != null) {
			onPropertyChanged(jmenuItem, pref, oldval, newval);
		}
	}

	public <T> void onPropertyChanged(JMenuItem item, Preference<T> pref, T oldval, T newval) {
		
	}
	
	//Getters
	public JMenuItem getItem(Game game, Novel nvl) {
		if (jmenuItem == null) {
			jmenuItem = createItem(game, nvl);
		}
		return jmenuItem;
	}
	
	//Setters
	
}
