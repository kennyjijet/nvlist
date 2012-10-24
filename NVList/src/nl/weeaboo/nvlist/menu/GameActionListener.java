package nl.weeaboo.nvlist.menu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import nl.weeaboo.nvlist.Game;

public class GameActionListener implements ActionListener {
	
	private Game game;
	private GameMenuAction action;
	
	public GameActionListener(Game g, GameMenuAction a) {
		game = g;
		action = a;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (game != null) {
			synchronized (game) {
				action.actionPerformed(e, game, game.getNovel());
			}
		} else {
			action.actionPerformed(e, null, null);
		}
	}
	
}