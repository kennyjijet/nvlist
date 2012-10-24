package nl.weeaboo.nvlist.menu;

import java.awt.event.ActionEvent;

import javax.swing.JMenuItem;

import nl.weeaboo.lua2.LuaException;
import nl.weeaboo.nvlist.Game;
import nl.weeaboo.vn.impl.nvlist.Novel;

public class ViewCGItem extends GameMenuAction {

	@Override
	public JMenuItem createItem(Game game, Novel nvl) {
		JMenuItem item = new JMenuItem("View CG");
		item.setMnemonic('V');
		return item;
	}

	@Override
	public void actionPerformed(JMenuItem item, ActionEvent e, Game game, Novel nvl) {
		try {
			nvl.openViewCG();
			game.getDisplay().setMenuBarVisible(false);				
		} catch (LuaException le) {
			nvl.getNotifier().e("Error opening viewCG screen", le);
		}
	}

}
