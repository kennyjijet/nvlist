package nl.weeaboo.nvlist.menu;

import java.awt.event.ActionEvent;

import javax.swing.JMenuItem;

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
			nvl.getSystemLib().exit(false);
		} else if (game != null) {
			game.stop(false, new Runnable() {
				public void run() {
					game.dispose();
				}
			});
		}
	}

}
