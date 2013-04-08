package nl.weeaboo.nvlist.menu;

import java.awt.event.ActionEvent;

import javax.swing.JMenuItem;

import nl.weeaboo.game.desktop.AWTGameDisplay;
import nl.weeaboo.game.desktop.GameUpdater;
import nl.weeaboo.nvlist.Game;
import nl.weeaboo.vn.impl.nvlist.Novel;

public class CheckForUpdatesItem extends GameMenuAction {

	@Override
	public JMenuItem createItem(Game game, Novel nvl) {
		JMenuItem item = new JMenuItem("Check for updates...");
		item.setVisible(game.getGameUpdater() != null);			
		return item;
	}

	@Override
	public void actionPerformed(JMenuItem item, ActionEvent e, Game game, Novel novel) {
		GameUpdater gu = game.getGameUpdater();
		if (gu == null) return;
		
		AWTGameDisplay display = game.getDisplay();
		gu.checkForUpdatesAsync();
		display.showMessageDialog(gu.getComponent(), "Update");
	}
	
}
