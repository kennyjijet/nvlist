package nl.weeaboo.nvlist.menu;

import java.awt.event.ActionEvent;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import nl.weeaboo.game.IGameDisplay;
import nl.weeaboo.nvlist.Game;
import nl.weeaboo.vn.impl.nvlist.Novel;

public class RestartItem extends GameMenuAction {

	@Override
	public JMenuItem createItem(Game game, Novel nvl) {
		JMenuItem item = new JMenuItem("Return to title");
		return item;
	}

	@Override
	public void actionPerformed(JMenuItem item, ActionEvent e, Game game, Novel nvl) {
		IGameDisplay display = game.getDisplay();
		String message = "Warning: unsaved progress will be lost.";
		String title = "Return to title screen?";
		
		if (display.isFullscreenExclusive()) {
			display.setFullscreen(false);
		}
		
		int result = display.showConfirmDialog(message, title);		
		if (result == JOptionPane.OK_OPTION) {		
			game.restart();
		}
	}

}
