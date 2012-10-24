package nl.weeaboo.nvlist.menu;

import java.awt.event.ActionEvent;

import javax.swing.JMenuItem;

import nl.weeaboo.nvlist.Game;
import nl.weeaboo.vn.impl.nvlist.Novel;

public class FullscreenItem extends GameMenuAction {

	@Override
	public JMenuItem createItem(Game game, Novel nvl) {
		JMenuItem item = new JMenuItem("Toggle fullscreen");
		return item;
	}

	@Override
	public void actionPerformed(JMenuItem item, ActionEvent e, Game game, Novel nvl) {
		game.getDisplay().toggleFullscreen();
	}

}
