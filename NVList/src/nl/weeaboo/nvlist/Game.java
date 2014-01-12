package nl.weeaboo.nvlist;

import static nl.weeaboo.game.GameConfig.HEIGHT;
import static nl.weeaboo.game.GameConfig.TITLE;
import static nl.weeaboo.game.GameConfig.WIDTH;
import static nl.weeaboo.vn.NovelPrefs.ENABLE_PROOFREADER_TOOLS;
import static nl.weeaboo.vn.NovelPrefs.ENGINE_MIN_VERSION;
import static nl.weeaboo.vn.NovelPrefs.ENGINE_TARGET_VERSION;
import static nl.weeaboo.vn.vnds.VNDSUtil.VNDS;

import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.xml.parsers.ParserConfigurationException;

import nl.weeaboo.awt.AwtUtil;
import nl.weeaboo.common.Benchmark;
import nl.weeaboo.common.Dim;
import nl.weeaboo.common.Rect;
import nl.weeaboo.common.StringUtil;
import nl.weeaboo.filesystem.IFileSystem;
import nl.weeaboo.filesystem.SecureFileWriter;
import nl.weeaboo.game.GameConfig;
import nl.weeaboo.game.GameLog;
import nl.weeaboo.game.Notifier;
import nl.weeaboo.game.RenderMode;
import nl.weeaboo.game.desktop.AWTGame;
import nl.weeaboo.game.desktop.AWTGameBuilder;
import nl.weeaboo.game.desktop.AWTGameDisplay;
import nl.weeaboo.game.desktop.DebugPanel;
import nl.weeaboo.game.input.IUserInput;
import nl.weeaboo.gl.GLManager;
import nl.weeaboo.gl.GLResCache;
import nl.weeaboo.gl.jogl.JoglTextureStore;
import nl.weeaboo.gl.shader.IShaderStore;
import nl.weeaboo.gl.text.GlyphManager;
import nl.weeaboo.gl.text.ParagraphRenderer;
import nl.weeaboo.lua2.LuaException;
import nl.weeaboo.lua2.io.LuaSerializer;
import nl.weeaboo.nvlist.debug.BugReporter;
import nl.weeaboo.nvlist.debug.DebugImagePanel;
import nl.weeaboo.nvlist.debug.DebugLuaPanel;
import nl.weeaboo.nvlist.debug.DebugOutputPanel;
import nl.weeaboo.nvlist.debug.DebugScriptPanel;
import nl.weeaboo.nvlist.menu.GameMenuFactory;
import nl.weeaboo.settings.IConfig;
import nl.weeaboo.sound.SoundManager;
import nl.weeaboo.vn.IAnalytics;
import nl.weeaboo.vn.IDrawable;
import nl.weeaboo.vn.IImageState;
import nl.weeaboo.vn.IInput;
import nl.weeaboo.vn.ILayer;
import nl.weeaboo.vn.INovelConfig;
import nl.weeaboo.vn.ISaveHandler;
import nl.weeaboo.vn.ISeenLog;
import nl.weeaboo.vn.IStorage;
import nl.weeaboo.vn.ISystemLib;
import nl.weeaboo.vn.ITimer;
import nl.weeaboo.vn.IVideoState;
import nl.weeaboo.vn.NovelPrefs;
import nl.weeaboo.vn.RenderEnv;
import nl.weeaboo.vn.impl.base.BaseLoggingAnalytics;
import nl.weeaboo.vn.impl.base.BaseNovelConfig;
import nl.weeaboo.vn.impl.base.NullAnalytics;
import nl.weeaboo.vn.impl.base.RenderStats;
import nl.weeaboo.vn.impl.base.Timer;
import nl.weeaboo.vn.impl.lua.EnvLuaSerializer;
import nl.weeaboo.vn.impl.lua.LuaEventHandler;
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
import nl.weeaboo.vn.impl.nvlist.ShaderFactory;
import nl.weeaboo.vn.impl.nvlist.SharedGlobals;
import nl.weeaboo.vn.impl.nvlist.SoundFactory;
import nl.weeaboo.vn.impl.nvlist.SoundState;
import nl.weeaboo.vn.impl.nvlist.SystemLib;
import nl.weeaboo.vn.impl.nvlist.TextState;
import nl.weeaboo.vn.impl.nvlist.TweenLib;
import nl.weeaboo.vn.impl.nvlist.VideoFactory;
import nl.weeaboo.vn.impl.nvlist.VideoState;
import nl.weeaboo.vn.vnds.VNDSUtil;

import org.xml.sax.SAXException;

public class Game extends AWTGame {

	public static final int VERSION_MAJOR = 4;
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
	
	public Game(AWTGameBuilder b) {
		super(b);
		
		AWTGameDisplay gd = b.getDisplay();
		gd.setJMenuBar(GameMenuFactory.createPlaceholderJMenuBar(gd)); //Forces GameDisplay to use a JFrame
		gd.setRenderMode(RenderMode.MANUAL);

		GlyphManager gman = b.getGlyphManager();
		pr = gman.createParagraphRenderer();
	}

	//Functions
	@Override
	public void stop(boolean force, Runnable onStop) {
		try {
			novel.onExit();
		} catch (LuaException le) {
			GameLog.e("Error calling onExit", le);
			nativeStop(force);
		}
	}
	
	public void nativeStop(final boolean force) {
		final Runnable onStop = new Runnable() {
			public void run() {
				Game.this.dispose();
			}
		};
		
		super.stop(force, new Runnable() {
			public void run() {
				try {
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
				} finally {				
					if (onStop != null) {
						onStop.run();
					}
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
		config.set(GameConfig.MUSIC_VOLUME, 1.0);
		config.set(GameConfig.SOUND_VOLUME, 1.0);
		config.set(GameConfig.VOICE_VOLUME, 1.0);		

		IFileSystem fs = getFileSystem();
		
		if (bugReporter != null) {
			bugReporter.dispose();
			bugReporter = null;
		}
		if (config.get(ENABLE_PROOFREADER_TOOLS)) {
			try {
				bugReporter = new BugReporter(fs);
			} catch (IOException ioe) {
				GameLog.w("Error creating bug reporter", ioe);
			}
		}
		
		if (gmf != null) {
			gmf.dispose();
		}
		gmf = new GameMenuFactory(this);		
		getDisplay().setJMenuBar(gmf.createJMenuBar());
		
		SecureFileWriter sfw = new SecureFileWriter(fs);
		JoglTextureStore texStore = getTextureStore();
		GLResCache resCache = getGLResCache();
		IShaderStore shStore = getShaderStore();		
		GlyphManager glyphManager = getGlyphManager();
		SoundManager sm = getSoundManager();
		INovelConfig novelConfig = new BaseNovelConfig(config.get(TITLE), config.get(WIDTH), config.get(HEIGHT));
		Dim nvlSize = new Dim(novelConfig.getWidth(), novelConfig.getHeight());
				
		NovelNotifier notifier = new NovelNotifier(getNotifier());
		LuaEventHandler eventHandler = new LuaEventHandler();
		SaveHandler saveHandler = new SaveHandler(fs, notifier);
		
		SharedGlobals sharedGlobals = new SharedGlobals(sfw, "save-shared.bin", notifier);
		try {
			if (isVNDS()) readVNDSGlobalSav(sharedGlobals, fs);
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
		
		ISeenLog seenLog = new SeenLog(sfw, "seen.bin");
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
			an = new Analytics(sfw, "analytics.bin", notifier);
			try {
				an.load();
			} catch (IOException ioe) {
				notifier.d("Error loading analytics", ioe);
				try {
					an.save();
				} catch (IOException e) {
					notifier.d("Error saving analytics", e);
				}
			}
		}
				
		SystemLib syslib = new SystemLib(this, notifier);
		boolean renderTextToTexture = false; //isVNDS();
		ShaderFactory shfac = new ShaderFactory(notifier, shStore);
		ImageFactory imgfac = new ImageFactory(texStore, glyphManager, an, eventHandler,
				seenLog, notifier, nvlSize.w, nvlSize.h, renderTextToTexture);
		ImageFxLib fxlib = new ImageFxLib(imgfac);
		SoundFactory sndfac = new SoundFactory(sm, an, seenLog, notifier);
		VideoFactory vidfac = new VideoFactory(fs, texStore, shStore, resCache, seenLog, notifier);
		GUIFactory guifac = new GUIFactory(imgfac, notifier);
		ScriptLib scrlib = new ScriptLib(fs, notifier);
		TweenLib tweenLib = new TweenLib(notifier, imgfac, shfac);
		
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
				notifier, in, shfac, syslib, saveHandler, scrlib, tweenLib, sharedGlobals, globals,
				seenLog, an, timer, eventHandler,
				fs, getInput().getKeyConfig(), isVNDS());
		if (isVNDS()) {
			novel.setBootstrapScripts("builtin/vnds/main.lua");
		}
        luaSerializer = new EnvLuaSerializer();
        saveHandler.setNovel(novel, luaSerializer);
		onNovelCreated(novel);
		onConfigChanged();
   		
		restart("main");
		
		super.start();
   	}
	
	public void restart() {
		restart("titlescreen");
	}
	protected void restart(final String mainFunc) {		
		novel.restart(luaSerializer, getConfig(), mainFunc);

		onConfigChanged();
	}
	
	private static void readVNDSGlobalSav(SharedGlobals out, IFileSystem fs) {
		try {
			InputStream in = null;
			if (fs.getFileExists("save/global.sav")) {
				in = fs.newInputStream("save/global.sav");					
			} else if (fs.getFileExists("save/GLOBAL.sav")) {
				in = fs.newInputStream("save/GLOBAL.sav");					
			}
			
			if (in != null) {
				boolean wasAutoSave = out.getAutoSave();
				try {
					out.setAutoSave(false);
					VNDSUtil.readDSGlobalSav(out, in);
				} catch (ParserConfigurationException e) {
					GameLog.w("Error reading global.sav", e);
				} catch (SAXException e) {
					GameLog.w("Error reading global.sav", e);
				} finally {
					out.setAutoSave(wasAutoSave);
					in.close();
				}
			}
		} catch (IOException ioe) {
			GameLog.v("Error reading global.sav", ioe);
		}
		
	}
	
	@Override
	public boolean update(IUserInput input, float dt) {
		boolean changed = super.update(input, dt);

		final AWTGameDisplay display = getDisplay();
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
					sh.save(slot, null, null, null, null);
					IFileSystem fs = getFileSystem();
					long bytes = (fs.getFileExists(filename) ? fs.getFileSize(filename) : 0);
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
				try {
					novel.printStackTrace(System.out);
				} catch (LuaException e) {
					GameLog.w("Error printing stack trace", e);
				}
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
		
		Dim vsize = getVirtualSize();
		if (vs.isBlocking()) {
			Movie movie = (Movie)vs.getBlocking();
			movie.draw(glm, vsize.w, vsize.h);
		} else {
			ILayer root = is.getRootLayer();
			
			if (renderer == null) {
				ImageFactory imgfac = (ImageFactory)novel.getImageFactory();
				ISystemLib syslib = novel.getSystemLib();
				Rect realBounds = getRealBounds();
				Dim realScreenSize = getRealScreenSize();
				RenderEnv env = new RenderEnv(vsize.w, vsize.h,
						realBounds.x, realBounds.y, realBounds.w, realBounds.h,
						realScreenSize.w, realScreenSize.h, syslib.isTouchScreen());
			
				is.setRenderEnv(env);
				
				renderer = new Renderer(glm, pr, imgfac, env, renderStats);				
			}
			
			DrawBuffer buffer = renderer.getDrawBuffer();
			buffer.reset();
			root.draw(buffer);
			renderer.render(root, buffer);
			buffer.reset();
			
			if (renderStats != null) {
				renderStats.onFrameRenderDone();
			}
		}
		
		super.draw(glm);
	}
	
	protected void onNovelCreated(Novel novel) {
		
	}
	
	@Override
	public void onConfigChanged() {
		super.onConfigChanged();

		IConfig config = getConfig();		
		if (novel != null) {
			config.set(NovelPrefs.SCRIPT_DEBUG, isDebug()); //Use debug flag from Game
			config.set(GameConfig.DEFAULT_TEXT_STYLE, config.get(NovelPrefs.TEXT_STYLE)); //Use text style from NovelPrefs
			novel.onPrefsChanged(config);
		}
		
		getDisplay().repaint();
	}
	
	@Override
	protected DebugPanel newDebugPanel() {
		DebugPanel debugPanel = super.newDebugPanel();
		debugPanel.addTab("Lua", new DebugLuaPanel(this, getNovel()));
		debugPanel.addTab("Image", new DebugImagePanel(this, getNovel()));
		debugPanel.addTab("Script", new DebugScriptPanel(this, getNovel()));
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
	protected void onSizeChanged() {
		super.onSizeChanged();
		
		renderer = null;
	}
	
	@Override
	public void setImageFolder(String imgF, Dim size) throws IOException {
		super.setImageFolder(imgF, size);
		
		if (novel != null) {
			ImageFactory imgfac = (ImageFactory)novel.getImageFactory();
			imgfac.setImageSize(size.w, size.h);
		}		
	}
	
	@Override
	public void setVideoFolder(String videoF, Dim size) throws IOException {
		super.setVideoFolder(videoF, size);
		
		if (novel != null) {
			VideoFactory vfac = (VideoFactory)novel.getVideoFactory();
			vfac.setVideoFolder(videoF, size.w, size.h);
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
