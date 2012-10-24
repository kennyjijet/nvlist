package nl.weeaboo.vn.impl.nvlist;

import java.awt.Desktop;
import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.net.URI;

import nl.weeaboo.io.EnvironmentSerializable;
import nl.weeaboo.lua2.io.LuaSerializable;
import nl.weeaboo.nvlist.Game;
import nl.weeaboo.vn.IChoice;
import nl.weeaboo.vn.INotifier;
import nl.weeaboo.vn.ISaveLoadScreen;
import nl.weeaboo.vn.impl.base.BaseSystemLib;

@LuaSerializable
public class SystemLib extends BaseSystemLib implements Serializable {

	private final Game game;
	private final INotifier notifier;
	private final boolean isEmbedded;
	private final boolean isLowEnd;
	private final boolean isTouchScreen;
	
	private final EnvironmentSerializable es;
	
	public SystemLib(Game game, INotifier ntf) {
		this.game = game;
		this.notifier = ntf;
		this.isEmbedded = game.getDisplay().isEmbedded();
		this.isLowEnd = false;
		this.isTouchScreen = false;
		this.es = new EnvironmentSerializable(this);
	}
	
	//Functions
	private Object writeReplace() throws ObjectStreamException {	
		return es.writeReplace();
	}
	
	@Override
	public void restart() {
		game.restart();
	}
	
	@Override
	public void exit(boolean force) {
		game.stop(force, new Runnable() {
			public void run() {
				game.dispose();
			}
		});
	}
	
	@Override
	public IChoice createChoice(String... options) {
		return null;
	}

	@Override
	public ISaveLoadScreen createSaveScreen() {
		return null;
	}

	@Override
	public ISaveLoadScreen createLoadScreen() {
		return null;
	}
	
	@Override
	public void openWebsite(String url) {
		Desktop desktop = Desktop.getDesktop();
		try {
			desktop.browse(URI.create(url));
		} catch (IOException e) {
			notifier.w("Error opening URL: " + url, e);
		}
	}
	
	//Getters
	@Override
	public boolean canExit() {
		return !isEmbedded;
	}

	@Override
	public boolean isTouchScreen() {
		return isTouchScreen;
	}

	@Override
	public boolean isLowEnd() {
		return isLowEnd;
	}

	
	//Setters
	@Override
	public void setTextFullscreen(boolean fullscreen) {
		// This is a hint for a system-supplied textbox that the game wants a
		// fullscreen textbox. The normal PC implementation doesn't provide a
		// system textbox, currently only the Android version does that.
		// Therefore we can just ignore the hint.
	}
	
}
