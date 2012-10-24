package nl.weeaboo.nvlist.menu;

import java.awt.event.ActionEvent;

import javax.swing.JMenuItem;

import nl.weeaboo.lua2.LuaException;
import nl.weeaboo.nvlist.Game;
import nl.weeaboo.vn.impl.nvlist.Novel;

public class SaveLoadItem extends GameMenuAction {

	private final boolean isSave;
	
	protected SaveLoadItem(boolean isSave) {
		this.isSave = isSave;
	}
	
	//Functions
	public static SaveLoadItem createSaveItem() {
		return new SaveLoadItem(true);
	}
	public static SaveLoadItem createLoadItem() {
		return new SaveLoadItem(false);
	}

	@Override
	public JMenuItem createItem(Game game, Novel nvl) {
		JMenuItem item = new JMenuItem(isSave ? "Save" : "Load");
		item.setMnemonic(isSave ? 'S' : 'L');
		
		return item;
	}

	@Override
	public void actionPerformed(JMenuItem item, ActionEvent e, Game game, Novel nvl) {
		if (isSave) {
			try {
				nvl.openSaveScreen();
				game.getDisplay().setMenuBarVisible(false);				
			} catch (LuaException le) {
				nvl.getNotifier().e("Error opening save screen", le);
			}
		} else {
			try {
				nvl.openLoadScreen();
				game.getDisplay().setMenuBarVisible(false);				
			} catch (LuaException le) {
				nvl.getNotifier().e("Error opening load screen", le);
			}
		}
	}
	
}
