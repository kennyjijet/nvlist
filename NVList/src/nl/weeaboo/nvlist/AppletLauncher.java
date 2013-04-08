package nl.weeaboo.nvlist;

import nl.weeaboo.game.desktop.AWTAppletLauncher;
import nl.weeaboo.game.desktop.AWTLauncher;

@SuppressWarnings("serial")
public class AppletLauncher extends AWTAppletLauncher {

	//Functions
	@Override	
	protected AWTLauncher newLauncher() {
		return new Launcher();
	}
	
	//Getters
	
	//Setters
	
}
