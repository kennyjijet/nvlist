package nl.weeaboo.nvlist;

import static nl.weeaboo.game.BaseGameConfig.HEIGHT;
import static nl.weeaboo.game.BaseGameConfig.TITLE;
import static nl.weeaboo.game.BaseGameConfig.WIDTH;
import static nl.weeaboo.vn.NovelPrefs.ENABLE_PROOFREADER_TOOLS;
import static nl.weeaboo.vn.NovelPrefs.*;
import static nl.weeaboo.vn.vnds.VNDSUtil.VNDS;

import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import nl.weeaboo.awt.AwtUtil;
import nl.weeaboo.common.Benchmark;
import nl.weeaboo.common.Dim;
import nl.weeaboo.common.StringUtil;
import nl.weeaboo.filemanager.FileManager;
import nl.weeaboo.game.BaseGame;
import nl.weeaboo.game.BaseGameConfig;
import nl.weeaboo.game.DebugPanel;
import nl.weeaboo.game.GameDisplay;
import nl.weeaboo.game.GameLog;
import nl.weeaboo.game.GameUpdater;
import nl.weeaboo.game.IGameDisplay;
import nl.weeaboo.game.Notifier;
import nl.weeaboo.game.RenderMode;
import nl.weeaboo.game.input.IKeyConfig;
import nl.weeaboo.game.input.UserInput;
import nl.weeaboo.gl.GLManager;
import nl.weeaboo.gl.GLResourceCache;
import nl.weeaboo.gl.shader.ShaderCache;
import nl.weeaboo.gl.text.FontManager;
import nl.weeaboo.gl.text.GLTextRendererStore;
import nl.weeaboo.gl.text.ParagraphRenderer;
import nl.weeaboo.gl.texture.TextureCache;
import nl.weeaboo.lua2.io.LuaSerializer;
import nl.weeaboo.nvlist.debug.BugReporter;
import nl.weeaboo.nvlist.debug.DebugImagePanel;
import nl.weeaboo.nvlist.debug.DebugLuaPanel;
import nl.weeaboo.nvlist.debug.DebugOutputPanel;
import nl.weeaboo.nvlist.menu.GameMenuFactory;
import nl.weeaboo.settings.IConfig;
import nl.weeaboo.sound.SoundManager;
import nl.weeaboo.vn.IAnalytics;
import nl.weeaboo.vn.IDrawable;
import nl.weeaboo.vn.IImageState;
import nl.weeaboo.vn.IInput;
import nl.weeaboo.vn.ILayer;
import nl.weeaboo.vn.INovelConfig;
import nl.weeaboo.vn.IPersistentStorage;
import nl.weeaboo.vn.ISaveHandler;
import nl.weeaboo.vn.ISeenLog;
import nl.weeaboo.vn.IStorage;
import nl.weeaboo.vn.ITimer;
import nl.weeaboo.vn.IVideoState;
import nl.weeaboo.vn.NovelPrefs;
import nl.weeaboo.vn.impl.base.BaseLoggingAnalytics;
import nl.weeaboo.vn.impl.base.BaseNovelConfig;
import nl.weeaboo.vn.impl.base.NullAnalytics;
import nl.weeaboo.vn.impl.base.RenderEnv;
import nl.weeaboo.vn.impl.base.RenderStats;
import nl.weeaboo.vn.impl.base.Timer;
import nl.weeaboo.vn.impl.lua.EnvLuaSerializer;
import nl.weeaboo.vn.impl.nvlist.Analytics;
import nl.weeaboo.vn.impl.nvlist.DrawBuffer;
import nl.weeaboo.vn.impl.nvlist.GUIFactory;
import nl.weeaboo.vn.impl.nvlist.Globals;
import nl.weeaboo.vn.impl.nvlist.ImageFactory;
import nl.weeaboo.vn.impl.nvlist.ImageFxLib;
import nl.weeaboo.vn.impl.nvlist.ImageState;
import nl.weeaboo.vn.impl.nvlist.InputAdapter;
import nl.weeaboo.vn.impl.nvlist.Movie;
import nl.weeaboo.vn.impl.nvlist.Novel;
import nl.weeaboo.vn.impl.nvlist.NovelNotifier;
import nl.weeaboo.vn.impl.nvlist.Renderer;
import nl.weeaboo.vn.impl.nvlist.SaveHandler;
import nl.weeaboo.vn.impl.nvlist.ScriptLib;
import nl.weeaboo.vn.impl.nvlist.SeenLog;
import nl.weeaboo.vn.impl.nvlist.SharedGlobals;
import nl.weeaboo.vn.impl.nvlist.SoundFactory;
import nl.weeaboo.vn.impl.nvlist.SoundState;
import nl.weeaboo.vn.impl.nvlist.SystemLib;
import nl.weeaboo.vn.impl.nvlist.TextState;
import nl.weeaboo.vn.impl.nvlist.TweenLib;
import nl.weeaboo.vn.impl.nvlist.VideoFactory;
import nl.weeaboo.vn.impl.nvlist.VideoState;

public class Game extends BaseGame {

	public static final int VERSION_MAJOR = 3;
	public static final int VERSION_MINOR = 0;
	public static final int VERSION = 10000 * VERSION_MAJOR + 100 * VERSION_MINOR;
	public static final String VERSION_STRING = VERSION_MAJOR + "." + VERSION_MINOR; //Our current engine version
	public static final String MIN_COMPAT_VERSION = "3.0"; //The oldest target engine version we still support
	
	private final ParagraphRenderer pr;
	
	private Novel novel;
	private LuaSerializer luaSerializer;
	private GameMenuFactory gmf;
	private BugReporter bugReporter;
	private Renderer renderer;
	private RenderStats renderStats = null; //new RenderStats();
	private Movie movie;
	
	public Game(IConfig cfg, ExecutorService e, GameDisplay gd, GameUpdater gu, FileManager fm,
			FontManager fontman, TextureCache tc, ShaderCache sc, GLResourceCache rc,
			GLTextRendererStore trs, SoundManager sm, UserInput in, IKeyConfig kc,
			String imageF, String videoF)
	{
		super(cfg, e, gd, gu, fm, fontman, tc, sc, rc, trs, sm, in, kc, imageF, videoF);
		
		gd.setJMenuBar(GameMenuFactory.createPlaceholderJMenuBar(gd)); //Forces GameDisplay to use a JFrame
		gd.setRenderMode(RenderMode.MANUAL);

		pr = trs.createParagraphRenderer();
	}

	//Functions
	@Override
	public void stop(final boolean force, final Runnable onStop) {
		super.stop(force, new Runnable() {
			public void run() {				
				if (novel != null) {
					novel.savePersistent();
					generatePreloaderData(); //Generate a preloader info from analytics					
					novel.reset();
					novel = null;
				}
				if (bugReporter != null) {
					bugReporter.dispose();
				}
				if (gmf != null) {
					gmf.dispose();
					gmf = null;
				}
				
				if (onStop != null) {
					onStop.run();
				}				
			}
		});
	}
	
	@Override
	public void start() {		
		IConfig config = getConfig();		
		if (StringUtil.compareVersion(VERSION_STRING, config.get(ENGINE_MIN_VERSION)) < 0) {
			//Our version number is too old to run the game
			AwtUtil.showError(String.format("NVList version number (%s) " +
				"is below the minimum acceptable version for this game (%s)",
				VERSION_STRING, config.get(ENGINE_MIN_VERSION)));			
		} else if (StringUtil.compareVersion(config.get(ENGINE_TARGET_VERSION), MIN_COMPAT_VERSION) < 0) {
			//The game version is too old
			AwtUtil.showError(String.format("vn.engineTargetVersion is too old (%s), minimum is (%s)",
				config.get(ENGINE_TARGET_VERSION), MIN_COMPAT_VERSION));			
		}
		
		//We're using the volume settings from NovelPrefs instead...
		config.set(BaseGameConfig.MUSIC_VOLUME, 1.0);
		config.set(BaseGameConfig.SOUND_VOLUME, 1.0);
		config.set(BaseGameConfig.VOICE_VOLUME, 1.0);
		
		if (bugReporter != null) {
			bugReporter.dispose();
			bugReporter = null;
		}
		if (config.get(ENABLE_PROOFREADER_TOOLS)) {
			try {
				bugReporter = new BugReporter(getFileManager());
			} catch (IOException ioe) {
				GameLog.w("Error creating bug reporter", ioe);
			}
		}
		
		if (gmf != null) {
			gmf.dispose();
		}
		gmf = new GameMenuFactory(this);		
		getDisplay().setJMenuBar(gmf.createJMenuBar());
		
		FileManager fm = getFileManager();
		TextureCache texCache = getTextureCache();
		GLResourceCache resCache = getGLResourceCache();
		ShaderCache shCache = getShaderCache();		
		GLTextRendererStore trStore = getTextRendererStore();
		SoundManager sm = getSoundManager();
		INovelConfig novelConfig = new BaseNovelConfig(config.get(TITLE), config.get(WIDTH), config.get(HEIGHT));
		Dim nvlSize = new Dim(novelConfig.getWidth(), novelConfig.getHeight());
				
		NovelNotifier notifier = new NovelNotifier(getNotifier());
		SaveHandler saveHandler = new SaveHandler(fm, notifier);
		
		IPersistentStorage sharedGlobals = new SharedGlobals(fm, "save-shared.bin", notifier);
		try {
			sharedGlobals.load();
		} catch (IOException ioe) {
			notifier.d("Error loading shared globals", ioe);
			try { sharedGlobals.save(); } catch (IOException e) { }
		}
		
		ITimer timer = new Timer();
		try {
			timer.load(sharedGlobals);
		} catch (IOException ioe) {
			notifier.d("Error loading timer", ioe);
			try { timer.save(sharedGlobals); } catch (IOException e) { }
		}
		
		ISeenLog seenLog = new SeenLog(fm, "seen.bin");
		try {
			seenLog.load();
		} catch (IOException ioe) {
			notifier.d("Error loading seenLog", ioe);
			try { seenLog.save(); } catch (IOException e) { }
		}
				
		IAnalytics an;
		if (!isDebug()) {
			an = new NullAnalytics();
		} else {
			an = new Analytics(fm, "analytics.bin", notifier);
			try {
				an.load();
			} catch (IOException ioe) {
				notifier.d("Error loading analytics", ioe);
				try { an.save(); } catch (IOException e) { }
			}
		}
				
		SystemLib syslib = new SystemLib(this, notifier);
		ImageFactory imgfac = new ImageFactory(texCache, shCache, trStore,
				an, seenLog, notifier, syslib.isTouchScreen(), nvlSize.w, nvlSize.h);
		ImageFxLib fxlib = new ImageFxLib(imgfac);
		SoundFactory sndfac = new SoundFactory(sm, an, seenLog, notifier);
		VideoFactory vidfac = new VideoFactory(fm, texCache, resCache, seenLog, notifier);
		GUIFactory guifac = new GUIFactory(imgfac, notifier);
		ScriptLib scrlib = new ScriptLib(fm, notifier);
		TweenLib tweenLib = new TweenLib(imgfac, notifier);
		
		if (isDebug() && !isVNDS()) {
			imgfac.setCheckFileExt(true);
			sndfac.setCheckFileExt(true);
			vidfac.setCheckFileExt(true);
		}
		
		ImageState is = new ImageState(imgfac, nvlSize.w, nvlSize.h);		
		SoundState ss = new SoundState(sndfac);
		VideoState vs = new VideoState();
		TextState ts = new TextState();
		IInput in = new InputAdapter(getInput());	
		IStorage globals = new Globals();
		
		novel = new Novel(novelConfig, imgfac, is, fxlib, sndfac, ss, vidfac, vs, guifac, ts,
				notifier, in, syslib, saveHandler, scrlib, tweenLib, sharedGlobals, globals,
				seenLog, an, timer,
				fm, getKeyConfig(), isVNDS());
		if (isVNDS()) {
			novel.setBootstrapScripts("builtin/vnds/main.lua");
		}
        luaSerializer = new EnvLuaSerializer();
        saveHandler.setNovel(novel, luaSerializer);
		onConfigPropertiesChanged();
   		
		restart("main");
		
		super.start();
   	}
	
	public void restart() {
		restart("titlescreen");
	}
	protected void restart(final String mainFunc) {		
		novel.restart(luaSerializer, getConfig(), mainFunc);

		onConfigPropertiesChanged();
	}
	
	@Override
	public boolean update(UserInput input, float dt) {
		boolean changed = super.update(input, dt);

		final IGameDisplay display = getDisplay();
		boolean allowMenuBarToggle = display.isEmbedded() || display.isFullscreen();
		
		IInput ninput = novel.getInput();
		boolean requestMenu = false;

		//Determine the visibility of the menu bar
		if (display.isMenuBarVisible()) {
			if (allowMenuBarToggle && display.isFullscreenExclusive()) {
				display.setMenuBarVisible(false);
			}
		} else {
			if (!allowMenuBarToggle) {
				if (display.isFullscreenExclusive()) {
					display.setFullscreen(false);
				}
				display.setMenuBarVisible(true);
			}
		}
		
		//Higher-than-game priority menu bar toggle listener
		if (allowMenuBarToggle) {
			if (display.isMenuBarVisible()) {
				if (ninput.consumeConfirm() || ninput.consumeTextContinue()) {
					requestMenu = true;
				}
			} else {
				if (input.consumeKey(KeyEvent.VK_ESCAPE)) {
					requestMenu = true;
				}
			}			
		}
		
		//Update novel
		changed |= novel.update();
		
		//Determine if we need to toggle the menu bar based on input
		if (requestMenu || ninput.consumeCancel()) {
			if (display.isMenuBarVisible() && allowMenuBarToggle) {
				display.setMenuBarVisible(false);
			} else {
				if (display.isFullscreenExclusive()) {
					display.setFullscreen(false);
				}

				GameMenuFactory gameMenu = new GameMenuFactory(this);
				display.setJMenuBar(gameMenu.createJMenuBar());
				display.setMenuBarVisible(true);
			}
		}
		
		//Debug functionality
		if (isDebug()) {
			Notifier ntf = getNotifier();

			if (input.consumeKey(KeyEvent.VK_MULTIPLY)) {
				int a = 0;
				a = 0 / a; //Boom shakalaka
			}
			
			ISaveHandler sh = novel.getSaveHandler();
			if (input.consumeKey(KeyEvent.VK_ADD)) {
				try {
					Benchmark.tick();
					int slot = sh.getQuickSaveSlot(1);
					String filename = String.format("save-%03d.sav", slot);
					sh.save(slot, null, null, null);
					long bytes = (getFileManager().getFileExists(filename) ? getFileManager().getFileSize(filename) : 0);
					ntf.addMessage(this, String.format("Quicksave took %s (%s)",
							StringUtil.formatTime(Benchmark.tock(false), TimeUnit.NANOSECONDS),
							StringUtil.formatMemoryAmount(bytes)));					
				} catch (Exception e) {
					GameLog.w("Error quicksaving", e);
				}
			} else if (input.consumeKey(KeyEvent.VK_SUBTRACT)) {
				try {
					int slot = sh.getQuickSaveSlot(1);
					novel.getSaveHandler().load(slot, null);
				} catch (Exception e) {
					GameLog.w("Error quickloading", e);
				}
			}
			
			if (input.consumeKey(KeyEvent.VK_F2)) {
				novel.printStackTrace(System.out);
			} else if (input.consumeKey(KeyEvent.VK_F3)) {
				ntf.addMessage(this, "Generating preloader data");
				generatePreloaderData();
			} else if (input.consumeKey(KeyEvent.VK_F5)) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						synchronized (Game.this) {
							String message = "All script files will be reloaded.\nWarning: unsaved progress will be lost.";
							String title = "Return to title screen?";
							int result = display.showConfirmDialog(message, title);		
							if (result == JOptionPane.OK_OPTION) {		
								restart();
							}
						}
					}
				});
			}
		}
		
		return changed;
	}
	
	@Override
	public void draw(GLManager glm) {
		IImageState is = novel.getImageState();
		IVideoState vs = novel.getVideoState();
		
		if (vs.isBlocking()) {
			Movie movie = (Movie)vs.getBlocking();
			movie.draw(glm, getWidth(), getHeight());
		} else {
			if (renderer == null) {
				RenderEnv env = new RenderEnv(getWidth(), getHeight(),
						getRealX(), getRealY(), getRealW(), getRealH(),
						getScreenW(), getScreenH());
				renderer = new Renderer(glm, pr, env, renderStats);
			}
			
			ILayer root = is.getRootLayer();
			DrawBuffer buffer = renderer.getDrawBuffer();
			root.draw(buffer);
			renderer.render(root, buffer);
			buffer.reset();
			
			if (renderStats != null) {
				renderStats.onFrameRenderDone();
			}
		}
		
		super.draw(glm);
	}
	
	@Override
	public void onConfigPropertiesChanged() {
		super.onConfigPropertiesChanged();

		IConfig config = getConfig();
		
		if (novel != null) {
			config.set(NovelPrefs.SCRIPT_DEBUG, isDebug()); //Use debug flag from Game
			config.set(BaseGameConfig.DEFAULT_TEXT_STYLE, config.get(NovelPrefs.TEXT_STYLE)); //Use text style from NovelPrefs
			novel.onPrefsChanged(config);
		}
		
		getDisplay().repaint();
	}
	
	@Override
	protected DebugPanel createDebugPanel() {
		DebugPanel debugPanel = super.createDebugPanel();
		debugPanel.addTab("Lua", new DebugLuaPanel(this, getNovel()));
		debugPanel.addTab("Image", new DebugImagePanel(this, getNovel()));
		debugPanel.addTab("Log", new DebugOutputPanel(this, getNovel()));
		return debugPanel;
	}	
	
	protected void generatePreloaderData() {
		IAnalytics an = novel.getAnalytics();
		if (an instanceof BaseLoggingAnalytics) {			
			BaseLoggingAnalytics ba = (BaseLoggingAnalytics)an;
			try {
				ba.optimizeLog(true);
			} catch (IOException ioe) {
				GameLog.w("Error dumping analytics", ioe);
			}
		}		
	}
	
	@Override
	public String generateOSDText() {
		StringBuilder sb = new StringBuilder(64);
		sb.append(super.generateOSDText());
		sb.append("\n");
		
		int visibleDrawables = 0;
		IImageState imageState = novel.getImageState();
		
		IDrawable[] drawablesTemp = new IDrawable[16];
		
		Queue<ILayer> workQ = new LinkedList<ILayer>();
		workQ.add(imageState.getRootLayer());
		while (!workQ.isEmpty()) {
			ILayer layer = workQ.remove();
			if (layer.isDestroyed() || !layer.isVisible()) continue;

			drawablesTemp = layer.getContents(drawablesTemp);
			for (IDrawable d : drawablesTemp) {
				if (d == null) break;
				
				if (!d.isDestroyed() && d.isVisible(.001)) {
					if (d instanceof ILayer) {
						workQ.add((ILayer)d);						
					} else {
						visibleDrawables++;
					}
				}
			}
		}		
		sb.append(String.format("drawables: %d\n", visibleDrawables));
		
		String callSite = novel.getCurrentCallSite();
		sb.append(String.format("script: %s\n", callSite != null ? callSite : "???"));

		return sb.toString();
	}
	
	//Getters
	public Novel getNovel() {
		return novel;
	}
	public BugReporter getBugReporter() {
		return bugReporter;
	}
	protected boolean isVNDS() {
		return getConfig().get(VNDS);
	}
	
	//Setters
	@Override
	public void setScreenBounds(int rx, int ry, int rw, int rh, int sw, int sh) {
		if (rx != getRealX()   || ry != getRealY() || rw != getRealW()   || rh != getRealH()
				|| sw != getScreenW() || sh != getScreenH())
		{
			super.setScreenBounds(rx, ry, rw, rh, sw, sh);
						
			renderer = null;
		}
	}
	
	@Override
	public void setImageFolder(String folder, int w, int h) throws IOException {
		super.setImageFolder(folder, w, h);
		
		if (novel != null) {
			ImageFactory imgfac = (ImageFactory)novel.getImageFactory();
			imgfac.setImageSize(w, h);
		}		
	}
	
	@Override
	public void setVideoFolder(String folder, int w, int h) throws IOException {
		super.setVideoFolder(folder, w, h);
		
		if (novel != null) {
			VideoFactory vfac = (VideoFactory)novel.getVideoFactory();
			vfac.setVideoFolder(folder, w, h);
		}
	}
	
	public void setMovie(Movie m) {
		if (movie != m) {
			if (movie != null) {
				movie.stop();
			}
			movie = m;
		}
	}
	
}
