package nl.weeaboo.nvlist;

import static nl.weeaboo.game.BaseGameConfig.HEIGHT;
import static nl.weeaboo.game.BaseGameConfig.SCALE;
import static nl.weeaboo.game.BaseGameConfig.WIDTH;

import java.awt.Container;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import nl.weeaboo.filemanager.FileManager;
import nl.weeaboo.game.BaseLauncher;
import nl.weeaboo.game.GameDisplay;
import nl.weeaboo.game.GameUpdater;
import nl.weeaboo.game.IGame;
import nl.weeaboo.game.input.IKeyConfig;
import nl.weeaboo.game.input.UserInput;
import nl.weeaboo.gl.GLResourceCache;
import nl.weeaboo.gl.shader.ShaderCache;
import nl.weeaboo.gl.text.FontManager;
import nl.weeaboo.gl.text.GLTextRendererStore;
import nl.weeaboo.gl.texture.TextureCache;
import nl.weeaboo.settings.IConfig;
import nl.weeaboo.sound.SoundManager;
import nl.weeaboo.vn.impl.base.Obfuscator;
import nl.weeaboo.vn.vnds.VNDSUtil;

public class Launcher extends BaseLauncher {
	
	public Launcher() {
		super();
		
		setObfuscator(Obfuscator.getInstance());
	}
	
	//Functions
	public static void main(String args[]) {		
		//nl.weeaboo.game.GameLog.getLogger().setLevel(java.util.logging.Level.FINE);
		
		main(new Launcher(), args);
	}
	
	@Override
	protected IGame startGame(Container container) throws InitException {
		if (isVNDS()) {
			setPreference(WIDTH.getKey(), "256");
			setPreference(HEIGHT.getKey(), "192");
			setPreference(SCALE.getKey(), "3");
//			setPreference(WIDTH.getKey(), "1024");
//			setPreference(HEIGHT.getKey(), "768");
		}
		
		return super.startGame(container);
	}

	@Override
	protected FolderSet newFolderSet(URI rootURI) {
		FolderSet folders = super.newFolderSet(rootURI);
		if (isVNDS()) {
			folders.image = "";
			folders.sound = "";
			folders.video = "";
			folders.font  = "";
			folders.read  = "";
			folders.write = "nvlist_save";
		}
		return folders;
	}
	
	@Override
	protected IGame newGame(IConfig config, ExecutorService executor, GameDisplay display, GameUpdater gu,
			FileManager fm, FontManager fontManager, TextureCache tc, ShaderCache sc,
			GLResourceCache rc, GLTextRendererStore trs, SoundManager sm, UserInput in,
			IKeyConfig kc, FolderSet folders)
	{
		return new Game(config, executor, display, gu, fm, fontManager, tc, sc, rc, trs, sm, in, kc,
				folders.image, folders.video);
	}
	
	//Getters
	@Override
	protected Class<?>[] getJarArchiveSources() {
		List<Class<?>> result = new ArrayList<Class<?>>();
		for (Class<?> c : super.getJarArchiveSources()) {
			result.add(c);
		}
		//result.add(LuaNovel.class);
		return result.toArray(new Class<?>[result.size()]);
	}
	
	@Override
	protected String[] getZipFilenames(String gameId) {
		List<String> files = new ArrayList<String>();
		if (isVNDS()) {
			for (String filename : VNDSUtil.getZipFilenames()) {
				files.add(filename);
			}
		} else {
			files.add("res.zip");
			files.add(gameId+".nvl");
		}
		//files.add("lightnvl.jar");
		return files.toArray(new String[files.size()]);
	}

	protected boolean isVNDS() {
		return "true".equalsIgnoreCase(getPreference(VNDSUtil.VNDS.getKey()));
	}
	
	//Setters
	public void setVNDS(boolean vnds) {
		setPreference(VNDSUtil.VNDS.getKey(), Boolean.toString(vnds));
	}
	
}
