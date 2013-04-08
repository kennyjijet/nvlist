package nl.weeaboo.nvlist;

import static nl.weeaboo.game.GameConfig.FBO;
import static nl.weeaboo.game.GameConfig.HEIGHT;
import static nl.weeaboo.game.GameConfig.TITLE;
import static nl.weeaboo.game.GameConfig.WIDTH;

import java.awt.Container;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nl.weeaboo.filesystem.IFileSystem;
import nl.weeaboo.game.IGame;
import nl.weeaboo.game.desktop.AWTGameBuilder;
import nl.weeaboo.game.desktop.AWTLauncher;
import nl.weeaboo.settings.IConfig;
import nl.weeaboo.settings.INIFile;
import nl.weeaboo.vn.impl.base.Obfuscator;
import nl.weeaboo.vn.vnds.VNDSUtil;

public class Launcher extends AWTLauncher {
	
	public Launcher() {
		super();
		
		setObfuscator(Obfuscator.getInstance());
	}
	
	//Functions
	public static void main(String args[]) {		
		//nl.weeaboo.game.GameLog.getInstance().setLevel(java.util.logging.Level.FINE);
		
		main(new Launcher(), args);
	}
	
	@Override
	protected IGame startGame(Container container) throws InitException {
		if (getPreference(VNDSUtil.VNDS.getKey()) == null) {
			//Auto-determine if we should run in VNDS mode if preference not explicitly set
			URI uri = getRootURI();
			if (uri != null) {
				try {
					//Support auto-detection for local URIs only
					File folder = new File(uri);
					if (new File(folder, "info.txt").exists() && new File(folder, "icon.png").exists()) {
						//It's probably a VNDS folder
						setPreference(VNDSUtil.VNDS.getKey(), "true");
					}
				} catch (IllegalArgumentException iae) {
					//Unable to determine
				}
			}
		}
		
		return super.startGame(container);
	}

	@Override
	protected IConfig loadConfig(IFileSystem fs, Map<String, String> preferenceOverrides) {
		if (isVNDS()) {
			INIFile ini = new INIFile();			
			BufferedReader in = null;
			try {
				in = new BufferedReader(new InputStreamReader(fs.newInputStream("info.txt"), "UTF-8"));
				ini.read(in);
			} catch (IOException e) {
				//Ignore
			} finally {
				try {
					if (in != null) in.close();
				} catch (IOException e) {
					//Ignore
				}
			}
			
			setPreference(TITLE.getKey(), ini.getString("title", "VNDS"));
			//setPreference(WIDTH.getKey(), "256");
			//setPreference(HEIGHT.getKey(), "192");
			//setPreference(SCALE.getKey(), "3");
			setPreference(WIDTH.getKey(), "1024");
			setPreference(HEIGHT.getKey(), "768");
			setPreference(FBO.getKey(), "false");
			//setPreference(CHOICE_STYLE.getKey(), new TextStyle(null, FontStyle.PLAIN, 16).toString());
			//setPreference(SELECTED_CHOICE_STYLE.getKey(), new TextStyle(null, FontStyle.PLAIN, 16).toString());
		}
		
		return super.loadConfig(fs, preferenceOverrides);
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
	protected AWTGameBuilder newGameBuilder() {
		return new AWTGameBuilder() {
			@Override
			public Game build() throws IllegalStateException {
				return new Game(this);
			}
		};
	}
	
	//Getters
	@Override
	protected Set<String> getJarArchiveSources() {
		Set<String> result = new LinkedHashSet<String>(super.getJarArchiveSources());
		if (isVNDS()) {
			result.add("vnds.jar");
		}
		return result;
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
