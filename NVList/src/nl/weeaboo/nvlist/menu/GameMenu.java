package nl.weeaboo.nvlist.menu;

import nl.weeaboo.settings.ConfigPropertyListener;

interface GameMenu extends ConfigPropertyListener {
	
	public void addSeparator();
	public void add(GameMenuAction a);
	
}