package nl.weeaboo.nvlist.menu;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import nl.weeaboo.game.GameLog;
import nl.weeaboo.nvlist.Game;
import nl.weeaboo.settings.ConfigPropertyListener;
import nl.weeaboo.settings.Preference;

@SuppressWarnings("serial")
public class JGameMenu extends JMenu implements GameMenu, ConfigPropertyListener {

	private final Game game;
	
	private List<GameMenuAction> actions;
	
	public JGameMenu(Game g, String label, char mnemonic) {
		super(label);
		
		game = g;
		actions = new ArrayList<GameMenuAction>();
		
		if (mnemonic != '\0') {
			setMnemonic(mnemonic);
		}
	}
	
	@Override
	public void add(GameMenuAction a) {
		actions.add(a);
		JMenuItem item = a.getItem(game, (game != null ? game.getNovel() : null));
		item.addActionListener(new GameActionListener(game, a));
		add(item);
	}

	@Override
	public <T> void onPropertyChanged(Preference<T> pref, T oldval, T newval) {
		try {
			for (GameMenuAction a : actions) {
				a.onPropertyChanged(pref, oldval, newval);
			}
		} catch (ConcurrentModificationException e) {
			GameLog.w("JGameMenu.onPropertyChanged()", e);
		}
	}
	
}