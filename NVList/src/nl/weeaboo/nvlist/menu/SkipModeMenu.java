package nl.weeaboo.nvlist.menu;

import static nl.weeaboo.vn.NovelPrefs.SKIP_UNREAD;

import java.awt.event.ActionEvent;

import javax.swing.ButtonGroup;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;

import nl.weeaboo.nvlist.Game;
import nl.weeaboo.settings.IConfig;
import nl.weeaboo.settings.Preference;
import nl.weeaboo.vn.impl.nvlist.Novel;

public class SkipModeMenu extends GameMenuAction {
	
	private JGameMenu gmenu;
	
	public SkipModeMenu() {
	}
	
	//Functions
	@Override
	protected JMenuItem createItem(Game game, Novel nvl) {
		if (gmenu == null) {		
			gmenu = new JGameMenu(game, "Skip mode", '\0');
			
			IConfig config = game.getConfig();			
			
			ButtonGroup skipModeGroup = new ButtonGroup();
			
			JRadioButtonMenuItem skipAllItem = new JRadioButtonMenuItem("Skip all");
			skipAllItem.addActionListener(new SubItemActionListener(gmenu, 0));
			skipAllItem.setSelected(config.get(SKIP_UNREAD));
			gmenu.add(skipAllItem);
			skipModeGroup.add(skipAllItem);
			
			JRadioButtonMenuItem button = new JRadioButtonMenuItem("Skip read text");
			button.addActionListener(new SubItemActionListener(gmenu, 1));
			button.setSelected(!config.get(SKIP_UNREAD));
			gmenu.add(button);
			skipModeGroup.add(button);
		}
		return gmenu;
	}

	@Override
	public void actionPerformed(JMenuItem item, ActionEvent e, Game game, Novel nvl) {
		IConfig config = game.getConfig();
		
		int index = (e.getSource() instanceof Integer ? (Integer)e.getSource() : -1);
		if (index >= 0 && index <= 1) {
			config.set(SKIP_UNREAD, index == 0);
		}
	}
	
	@Override
	public <T> void onPropertyChanged(JMenuItem item, Preference<T> pref, T oldval, T newval) {
		gmenu.onPropertyChanged(pref, oldval, newval);
	}	
	
	//Getters
	
	//Setters
	
}
