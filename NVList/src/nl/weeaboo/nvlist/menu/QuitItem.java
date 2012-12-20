package nl.weeaboo.nvlist.menu;

import java.awt.event.ActionEvent;

import javax.swing.JMenuItem;

import nl.weeaboo.lua2.LuaException;
import nl.weeaboo.nvlist.Game;
import nl.weeaboo.vn.impl.nvlist.Novel;

public class QuitItem extends GameMenuAction {
	
	@Override
	public JMenuItem createItem(Game game, Novel nvl) {
		JMenuItem item = new JMenuItem("Quit");
		item.setMnemonic('Q');
		return item;
	}

	@Override
	public void actionPerformed(JMenuItem item, ActionEvent e, final Game game, Novel nvl) {
		if (nvl != null) {
			try {
				nvl.getSystemLib().softExit();
			} catch (LuaException le) {
				nvl.getNotifier().e("Error calling onExit function", le);
			}
		} else if (game != null) {
			game.nativeStop(false);
		}
	}

}
