package nl.weeaboo.vn.impl.lua;

import static nl.weeaboo.vn.NovelPrefs.AUTO_READ;
import static nl.weeaboo.vn.NovelPrefs.AUTO_READ_WAIT;
import static nl.weeaboo.vn.NovelPrefs.FPS;
import static nl.weeaboo.vn.NovelPrefs.PRELOADER_LOOK_AHEAD;
import static nl.weeaboo.vn.NovelPrefs.PRELOADER_MAX_PER_LINE;
import static nl.weeaboo.vn.NovelPrefs.SCRIPT_DEBUG;
import static nl.weeaboo.vn.NovelPrefs.SKIP_UNREAD;
import static org.luaj.vm2.LuaString.valueOf;
import static org.luaj.vm2.LuaValue.INDEX;
import static org.luaj.vm2.LuaValue.NIL;
import static org.luaj.vm2.LuaValue.NONE;
import static org.luaj.vm2.LuaValue.valueOf;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nl.weeaboo.common.Benchmark;
import nl.weeaboo.common.StringUtil;
import nl.weeaboo.lua2.LuaException;
import nl.weeaboo.lua2.LuaRunState;
import nl.weeaboo.lua2.LuaUtil;
import nl.weeaboo.lua2.io.LuaSerializable;
import nl.weeaboo.lua2.io.LuaSerializer;
import nl.weeaboo.lua2.io.ObjectDeserializer;
import nl.weeaboo.lua2.io.ObjectSerializer;
import nl.weeaboo.lua2.lib.CoerceJavaToLua;
import nl.weeaboo.lua2.lib.CoerceLuaToJava;
import nl.weeaboo.lua2.lib.LuajavaLib;
import nl.weeaboo.lua2.link.LuaFunctionLink;
import nl.weeaboo.settings.IConfig;
import nl.weeaboo.settings.Preference;
import nl.weeaboo.vn.BlendMode;
import nl.weeaboo.vn.ErrorLevel;
import nl.weeaboo.vn.IAnalytics;
import nl.weeaboo.vn.IImageState;
import nl.weeaboo.vn.IInput;
import nl.weeaboo.vn.IInterpolator;
import nl.weeaboo.vn.INotifier;
import nl.weeaboo.vn.INovelConfig;
import nl.weeaboo.vn.IPersistentStorage;
import nl.weeaboo.vn.IScriptLib;
import nl.weeaboo.vn.ISeenLog;
import nl.weeaboo.vn.ISoundState;
import nl.weeaboo.vn.IStorage;
import nl.weeaboo.vn.ITextState;
import nl.weeaboo.vn.ITimer;
import nl.weeaboo.vn.IVideoState;
import nl.weeaboo.vn.NovelPrefs;
import nl.weeaboo.vn.SoundType;
import nl.weeaboo.vn.impl.base.BaseGUIFactory;
import nl.weeaboo.vn.impl.base.BaseImageFactory;
import nl.weeaboo.vn.impl.base.BaseImageFxLib;
import nl.weeaboo.vn.impl.base.BaseNotifier;
import nl.weeaboo.vn.impl.base.BaseNovel;
import nl.weeaboo.vn.impl.base.BaseShaderFactory;
import nl.weeaboo.vn.impl.base.BaseSoundFactory;
import nl.weeaboo.vn.impl.base.BaseSystemLib;
import nl.weeaboo.vn.impl.base.BaseVideoFactory;
import nl.weeaboo.vn.impl.base.BlendGS;
import nl.weeaboo.vn.impl.base.BlurGS;
import nl.weeaboo.vn.impl.base.DistortGS;
import nl.weeaboo.vn.impl.base.Interpolators;
import nl.weeaboo.vn.impl.base.LoopMode;
import nl.weeaboo.vn.impl.base.Looper;
import nl.weeaboo.vn.impl.base.ShaderImageTween;
import nl.weeaboo.vn.impl.base.ShutterGS;
import nl.weeaboo.vn.impl.base.WipeGS;
import nl.weeaboo.vn.layout.FlowLayout;
import nl.weeaboo.vn.layout.GridLayout;
import nl.weeaboo.vn.layout.NullLayout;
import nl.weeaboo.vn.math.MutableMatrix;
import nl.weeaboo.vn.parser.LVNFile;
import nl.weeaboo.vn.parser.LVNParser;
import nl.weeaboo.vn.parser.ParseException;

import org.luaj.vm2.LuaBoolean;
import org.luaj.vm2.LuaClosure;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaUserdata;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.BaseLib;
import org.luaj.vm2.lib.PackageLib;
import org.luaj.vm2.lib.ResourceFinder;
import org.luaj.vm2.lib.ResourceFinder.Resource;
import org.luaj.vm2.lib.VarArgFunction;

public abstract class LuaNovel extends BaseNovel {
	
	private static final LuaString S_EFFECTSPEED = valueOf("effectSpeed");
	private static final LuaString S_MAINTHREAD  = valueOf("_mainThread");
	private static final LuaString S_EDT         = valueOf("edt");
	private static final LuaString S_UPDATE      = valueOf("update");
	
	private transient LuaMediaPreloader preloader;
	
	// !!WARNING!! Do not add properties without adding code for saving/loading
	
	private List<LuaInitializer> luaInitializers;
	private String[] bootstrapScripts;
	private LuaRunState luaRunState;
	private LuaFunctionLink mainThread;
	private LuaUserdata mainThreadUserdata;
	private LuaLinkedProperties linkedProperties;
	private PrefsMetaFunction prefsGetterFunction, prefsSetterFunction;
	
	private transient IConfig prefs;
	private transient String lastCallSite;
	private transient int wait;
	
	protected LuaNovel(INovelConfig gc, BaseImageFactory imgfac, IImageState is, BaseImageFxLib imgfxlib,
			BaseSoundFactory sndfac, ISoundState ss, BaseVideoFactory vidfac, IVideoState vs, BaseGUIFactory guifac,
			ITextState ts, BaseNotifier n, IInput in, BaseShaderFactory shfac, BaseSystemLib syslib, LuaSaveHandler sh,
			final BaseScriptLib scrlib, LuaTweenLib tl, IPersistentStorage sharedGlobals, IStorage globals,
			ISeenLog seenLog, IAnalytics analytics, ITimer tmr)
	{
		super(gc, imgfac, is, imgfxlib, sndfac, ss, vidfac, vs, guifac, ts, n, in, shfac, syslib, sh, scrlib,
				tl, sharedGlobals, globals, seenLog, analytics, tmr);
		
		luaInitializers = new ArrayList<LuaInitializer>();
		bootstrapScripts = new String[] {"main.lua"};
		preloader = new LuaMediaPreloader(imgfac, sndfac);
		linkedProperties = new LuaLinkedProperties();

		//Init Lua global state
		BaseLib.FINDER = new ResourceFinder() {
			@Override
			public Resource findResource(String filename) {
				try {
					return openScriptFile(filename);
				} catch (FileNotFoundException e) {
					return null;
				} catch (IOException e) {
					onScriptError(e);
					return null;
				}
			}
		};
	}

	//Functions
	@Override
	public void reset() {
		super.reset();
		
		if (luaRunState != null) {
			luaRunState.destroy();
			luaRunState = null;
		}
		mainThread = null;
		mainThreadUserdata = null;
		//linkedProperties.clear(); //Retain properties when (re)starting
		
		initPreloader(preloader);
	}
	
	protected abstract void initPreloader(LuaMediaPreloader preloader);
	
	public void restart(LuaSerializer ls, IConfig prefs, String mainFuncName) {
		reset();
		
		onPrefsChanged(prefs);
		
		luaRunState = new LuaRunState();
		initLuaRunState();		
		mainThread = luaRunState.newThread(mainFuncName);
		mainThread.setPersistent(true);
		mainThreadUserdata = LuajavaLib.toUserdata(mainThread, mainThread.getClass());		
	}
	
	private Resource openScriptFile(String filename) throws IOException {
		IScriptLib scrlib = getScriptLib();
		String normalized = scrlib.normalizeFilename(filename);
		if (normalized == null) {
			throw new FileNotFoundException(filename);
		}
		InputStream in = scrlib.openScriptFile(normalized);
		long modTime = scrlib.getScriptModificationTime(normalized);		
		in = compileScriptFile(normalized, in, modTime);
		if (in == null) {
			throw new FileNotFoundException(filename);
		}
		return new Resource(normalized, in);
	}
	
	public void addLuaInitializer(LuaInitializer li) {
		if (!luaInitializers.contains(li)) {
			luaInitializers.add(li);
		}
	}
	public void removeLuaInitializer(LuaInitializer li) {
		luaInitializers.remove(li);
	}
		
	@Override
	public void readAttributes(ObjectInputStream in) throws ClassNotFoundException, IOException {
		String className = in.readUTF();
		if (!className.equals(getClass().getName())) {
			throw new IOException("Stored Novel object is of a different class, expected " + getClass().getName() + ", got " + className);
		}
		
		super.readAttributes(in);

		int bootstrapScriptsLen = in.readInt();
		bootstrapScripts = new String[bootstrapScriptsLen];
		for (int n = 0; n < bootstrapScriptsLen; n++) {
			bootstrapScripts[n] = in.readUTF();
		}
		
		if (in instanceof ObjectDeserializer) {
			ObjectDeserializer ds = (ObjectDeserializer)in;
			luaRunState = (LuaRunState)ds.readObjectOnNewThread(256<<10);
		} else {
			luaRunState = (LuaRunState)in.readObject();
		}
		luaRunState.registerOnThread();
		mainThread = (LuaFunctionLink)in.readObject();
		mainThreadUserdata = LuajavaLib.toUserdata(mainThread, mainThread.getClass());
		linkedProperties = (LuaLinkedProperties)in.readObject();
		
		int luaInitializersLen = in.readInt();
		luaInitializers.clear();
		for (int n = 0; n < luaInitializersLen; n++) {
			luaInitializers.add((LuaInitializer)in.readObject());
		}
	}
	
	@Override
	public void writeAttributes(ObjectOutputStream out) throws IOException {
		out.writeUTF(getClass().getName());
		
		super.writeAttributes(out);

		out.writeInt(bootstrapScripts.length);
		for (String script : bootstrapScripts) {
			out.writeUTF(script);
		}
		
		if (out instanceof ObjectSerializer) {
			ObjectSerializer os = (ObjectSerializer)out;
			os.writeObjectOnNewThread(luaRunState, 256<<10);
			//os.writeObject(luaRunState);
		} else {
			out.writeObject(luaRunState);
		}
		out.writeObject(mainThread);		
		out.writeObject(linkedProperties);
		
		out.writeInt(luaInitializers.size());
		for (LuaInitializer li : luaInitializers) {
			out.writeObject(li);
		}
	}
	
	protected InputStream compileScriptFile(String filename, InputStream in, long modificationTime)
			throws IOException
	{
		if (in != null) {
			if (!filename.endsWith(".lvn")) {
				//Read as buffered file
				in = new BufferedInputStream(in, 4096);
			} else {
				LVNFile file;
				try {
					LVNParser parser = new LVNParser();
					file = parser.parseFile(filename, in);					
				} catch (ParseException e) {
					throw new IOException(e.toString());
				} finally {
					in.close();
				}
				
				IAnalytics analytics = getAnalytics();
				if (analytics != null) {
					analytics.logScriptCompile(filename, modificationTime);
				}
				
				ISeenLog seenLog = getSeenLog();
				if (seenLog != null) {
					seenLog.registerScriptFile(filename, file.countTextLines(false));
				}
				
				in = new ByteArrayInputStream(StringUtil.toUTF8(file.compile()));
			}
		}	
		return in;
	}
		
	@Override
	protected boolean updateScript(IInput input) {
		if (wait != 0) {
			if (wait > 0) wait--;
			return false;
		}
		
		boolean changed = false;

		luaRunState.registerOnThread();		
		LuaValue globals = luaRunState.getGlobalEnvironment();
		globals.rawset(S_EFFECTSPEED, valueOf(getEffectSpeed(input)));
		globals.rawset(S_MAINTHREAD, mainThreadUserdata != null ? mainThreadUserdata : NIL);
		
		try {
			//Call event-dispatch thread's update function
			LuaValue edt = globals.rawget(S_EDT);
			if (!edt.isnil()) {
				LuaClosure edtUpdate = edt.rawget(S_UPDATE).checkclosure();
				Varargs result = mainThread.call(edtUpdate);			
				if (result.istable(1)) {
					LuaTable table = result.checktable(1);
					
					Varargs args = mainThread.call("edt.prePushEvents", mainThread);
					args = LuaValue.varargsOf(mainThreadUserdata, args);
					
					mainThread.pushCall("edt.postPushEvents", args);
					for (int n = table.length(); n > 0; n--) {
						LuaValue val = table.rawget(n);
						if (val.isclosure()) {
							mainThread.pushCall(val.checkclosure(), NONE);
						} else {
							mainThread.pushCall(val.tojstring());
						}
					}
					
					changed = true;
				} else if (!result.isnil(1)) {
					throw new LuaError("Invalid value returned from edt.update: " + result);
				}
			}
			
			lastCallSite = LuaNovelUtil.getNearestLVNSrcloc(mainThread.getThread());
			
			IAnalytics analytics = getAnalytics();
			if (analytics != null) {
				analytics.logScriptLine(lastCallSite);
			}
			
			preloader.update(lastCallSite);
			
			if (luaRunState.update()) {
				changed = true;
			}
		} catch (Exception e) {
			onScriptError(e);			
		}
		return changed;
	}
	
	public static boolean isBuiltInScript(String filename) {
		return filename.startsWith("builtin/");
	}
	
	/**
	 * @return <code>null</code> if the specified file could not be found.
	 */
	public static InputStream openBuiltInScript(String filename) {
		if (!isBuiltInScript(filename)) {
			return null;
		}
		return LuaNovel.class.getResourceAsStream("/script/" + filename);
	}
	
	public static void getBuiltInScripts(Collection<String> out, String folder) throws IOException {
		if (!isBuiltInScript(folder) && !"".equals(folder) && !"/".equals(folder)) {
			//Not in builtin/ (treat root folder as builtin)
			return;
		}
		
		//Strip 'builtin/' from the start, make sure it ends in '/'
		if (folder.startsWith("builtin/")) {
			folder = folder.substring(8);
		}
		if (!folder.endsWith("/")) {
			folder = folder + "/";
		}

		ClassLoader cl = LuaNovel.class.getClassLoader();
		if (cl == null) {
			return;
		}
		
		Enumeration<URL> e = cl.getResources("builtin/script/" + folder);
		while (e.hasMoreElements()) {
			String filename = "builtin/" + folder + e.nextElement().getFile();
			System.out.println(filename);
			if (isBuiltInScript(filename)) {
				out.add(filename);
			}
		}
	}
	
	protected abstract void onScriptError(Exception e);
	
	protected void initLuaRunState() {
		Benchmark.tick();
		Benchmark.tick();
				
		LuaRunState lrs = luaRunState;
		lrs.registerOnThread();
		LuaValue globals = lrs.getGlobalEnvironment();
		PackageLib.getCurrent().setLuaPath("?.lvn;?.lua");
		
		BaseNotifier ntf = getNotifier();
		BaseImageFactory imgfac = getImageFactory();
		BaseImageFxLib fxlib = getImageFxLib();
		IImageState is = getImageState();
		BaseSoundFactory sndfac = getSoundFactory();
		ISoundState ss = getSoundState();
		BaseVideoFactory vidfac = getVideoFactory();
		IVideoState vs = getVideoState();
		BaseGUIFactory guifac = getGUIFactory();
		ITextState ts = getTextState();
		IInput input = getInput();
		BaseShaderFactory shfac = getShaderFactory();
		BaseSystemLib syslib = getSystemLib();
		LuaSaveHandler sh = (LuaSaveHandler)getSaveHandler();
		ITimer timer = getTimer();
		IStorage gs = getGlobals();
		IPersistentStorage sg = getSharedGlobals();
		ISeenLog sl = getSeenLog();
		IAnalytics an = getAnalytics();
		LuaTweenLib tweenLib = (LuaTweenLib)getTweenLib();
		try {
			//--- Register globals ---
			globals.rawset("screenWidth",  is.getWidth());
			globals.rawset("screenHeight", is.getHeight());
			globals.rawset("imageState",   LuajavaLib.toUserdata(is, IImageState.class));
			globals.rawset("soundState",   LuajavaLib.toUserdata(ss, ISoundState.class));
			globals.rawset("videoState",   LuajavaLib.toUserdata(vs, IVideoState.class));
			globals.rawset("textState",    LuajavaLib.toUserdata(ts, ITextState.class));
			globals.rawset("input",        LuajavaLib.toUserdata(input, IInput.class));
			globals.rawset("notifier",     LuajavaLib.toUserdata(ntf, INotifier.class));
			globals.rawset("globals",      LuajavaLib.toUserdata(gs, IStorage.class));
			globals.rawset("sharedGlobals",LuajavaLib.toUserdata(sg, IPersistentStorage.class));						
			globals.rawset("seenLog",      LuajavaLib.toUserdata(sl, ISeenLog.class));		
			globals.rawset("analytics",    LuajavaLib.toUserdata(an, IAnalytics.class));
			globals.rawset("timer",        LuajavaLib.toUserdata(timer, ITimer.class));
			linkedProperties.flush(globals);
			
			//--- Register types ---
			LuaUtil.registerClass(globals, MutableMatrix.class, "Matrix");
			LuaUtil.registerClass(globals, Looper.class);
			// Enums
			LuaUtil.registerClass(globals, ErrorLevel.class);
			LuaUtil.registerClass(globals, BlendMode.class);			
			LuaUtil.registerClass(globals, SoundType.class);
			LuaUtil.registerClass(globals, LoopMode.class);
			// Layouts
			LuaUtil.registerClass(globals, NullLayout.class);
			LuaUtil.registerClass(globals, FlowLayout.class);
			LuaUtil.registerClass(globals, GridLayout.class);
			// Shaders
			LuaUtil.registerClass(globals, ShutterGS.class);
			LuaUtil.registerClass(globals, WipeGS.class);
			LuaUtil.registerClass(globals, BlendGS.class);
			LuaUtil.registerClass(globals, DistortGS.class);
			LuaUtil.registerClass(globals, BlurGS.class);
			// Tweens
			LuaUtil.registerClass(globals, ShaderImageTween.class);

			//--- Register special libraries ---			
			LuaTable blurGSTable = (LuaTable)globals.get(BlurGS.class.getSimpleName());
			//Replace BlurGS constructor with custom version not requiring image fx lib arg			
			blurGSTable.set("new", new BlurGS.LuaConstructorFunction(fxlib));
			
			LuaTable keysTable = new LuaTable();
			addKeyCodeConstants(keysTable);
			globals.rawset("Keys", keysTable);

			LuaTable prefsTable = new LuaTable();
			initPrefsTable(prefsTable);
			globals.rawset("prefs", prefsTable);
			
			LuaTable interpolatorsTable = new LuaTable();
			interpolatorsTable.rawset("get", LuaInterpolators.GETTER);
			addInterpolators(interpolatorsTable);
			globals.rawset("Interpolators", interpolatorsTable);
			
			//--- Register libraries ---
			globals.load(new LuaImageLib(imgfac, fxlib, is));
			globals.load(new LuaImageFxLib(fxlib));
			globals.load(new LuaSoundLib(ntf, sndfac, ss));
			globals.load(new LuaVideoLib(ntf, vidfac, vs));
			globals.load(new LuaGUILib(ntf, guifac, is));
			globals.load(new LuaTextLib(ts));
			globals.load(new LuaShaderLib(ntf, shfac));
			globals.load(new LuaSystemLib(ntf, syslib));
			globals.load(new LuaSaveLib(sh));
			globals.load(tweenLib);
			
			//Register backwards-compatibilty hacks
			IConfig prefs = getPrefs();
			String targetVersion = prefs.get(nl.weeaboo.vn.NovelPrefs.ENGINE_TARGET_VERSION);
			if (StringUtil.compareVersion(targetVersion, "3.1") < 0) {
				LuaValue shaderTable = globals.rawget("Shader");
				LuaTable glsl = new LuaTable();				
				glsl.rawset("new", shaderTable.rawget("createGLSLShader"));
				glsl.rawset("getVersion", shaderTable.rawget("getGLSLVersion"));
				glsl.rawset("isVersionSupported", shaderTable.rawget("isGLSLVersionSupported"));
				globals.rawset("GLSL", glsl);
			}
			
			//Register Lua initializers
			for (LuaInitializer li : luaInitializers) {
				li.init(lrs);
			}
		} catch (LuaException le) {
			onScriptError(le);
		}
		Benchmark.tock("Lua register time=%s");
		
		for (String filename : bootstrapScripts) {
			Varargs loadResult = BaseLib.loadFile(filename);
			if (loadResult.arg1() == LuaValue.NIL) {
				onScriptError(new LuaException("Error loading script: " + loadResult.arg(2)));
			} else {
				loadResult.arg1().invoke();
			}
		}
		
		Benchmark.tock("Lua total init time=%s");
	}

	protected void registerVNDSLib() throws LuaException {
		BaseScriptLib scrfac = getScriptLib();
		BaseNotifier ntf = getNotifier();
		
		LuaValue globals = luaRunState.getGlobalEnvironment();
		globals.set("isVNDS", LuaBoolean.TRUE);
		globals.load(new nl.weeaboo.vn.vnds.VNDSLib(scrfac, ntf));
	}
	
	protected abstract void addKeyCodeConstants(LuaTable table) throws LuaException;
	
	protected void addKeyCodeConstants(LuaTable table, LuaFunction meta) throws LuaException {
		LuaTable mt = new LuaTable();
		mt.rawset(INDEX, meta);
		table.setmetatable(mt);
	}
	
	protected void addInterpolators(LuaTable table) throws LuaException {		
		int m = Modifier.PUBLIC | Modifier.STATIC;
		for (Field field : Interpolators.class.getFields()) {
			if ((field.getModifiers() & m) == m) {
				try {
					Object val = field.get(null);
					if (val instanceof IInterpolator) {
						table.rawset(field.getName(), LuajavaLib.toUserdata(val, IInterpolator.class));
					}
				} catch (IllegalArgumentException e) {
					throw new LuaException(e);
				} catch (IllegalAccessException e) {
					//Too bad...
					//throw new LuaException(e);
				}
			}
		}
	}
	
	protected void initPrefsTable(LuaTable table) throws LuaException {
		LuaTable mt = new LuaTable();
		
		prefsGetterFunction = new PrefsMetaFunction(false, NovelPrefs.class);
		mt.rawset(LuaValue.INDEX, prefsGetterFunction);

		//prefsSetterFunction = new PrefsMetaFunction(true, NovelPrefs.class);
		//mt.rawset(LuaValue.NEWINDEX, prefsSetterFunction);
		
		table.setmetatable(mt);
	}
	
	@Override
	public void onPrefsChanged(IConfig config) {
		if (config == null) return;
		
		prefs = config;
		if (prefsGetterFunction != null) {
			prefsGetterFunction.setPrefs(config);
		}
		if (prefsSetterFunction != null) {
			prefsSetterFunction.setPrefs(config);
		}

		super.onPrefsChanged(config);		
		
		int fps = config.get(FPS);
				
		setScriptDebug(config.get(SCRIPT_DEBUG));
		setSkipUnread(config.get(SKIP_UNREAD));
		setAutoRead(config.get(AUTO_READ), fps * config.get(AUTO_READ_WAIT) / 1000);

		LuaMediaPreloader preloader = getPreloader();
		if (preloader != null) {
			preloader.setLookAhead(config.get(PRELOADER_LOOK_AHEAD));
			preloader.setMaxItemsPerLine(config.get(PRELOADER_MAX_PER_LINE));
		}		
	}
	
	public void onExit() throws LuaException { luaCall("onExit"); }
	protected void setMode(String funcName) { luaCall("edt.addEvent", funcName); }	
	public void openTextLog() throws LuaException { setMode("textLog"); }
	public void openViewCG() throws LuaException { setMode("viewCG"); }	
	public void openSaveScreen() throws LuaException { setMode("saveScreen"); }	
	public void openLoadScreen() throws LuaException { setMode("loadScreen"); }
	
	public Varargs eval(String code) throws LuaException {
		if (luaRunState == null) throw new LuaException("LuaRunState is null");
		
		luaRunState.registerOnThread();
		return LuaUtil.eval(luaRunState, mainThread, code);
	}
	
	protected Varargs luaCall(String function, Object... args) {
		Varargs result = NONE;
		if (mainThread != null) {
			luaRunState.registerOnThread();
			try {
				result = mainThread.call(function, args);
			} catch (LuaException e) {
				onScriptError(e);
			}
		}
		return result;
	}
	
	public void printStackTrace(PrintStream pout) {
		luaRunState.registerOnThread();
		luaRunState.printStackTrace(pout);
	}
	
	//Getters
	protected LuaRunState getLuaRunState() {
		return luaRunState;
	}
	
	public LuaMediaPreloader getPreloader() {
		return preloader;
	}
	
	public String getCurrentCallSite() {
		return lastCallSite;
	}
	
	public String[] getStackTrace() {
		if (mainThread == null) return new String[0];
		return LuaNovelUtil.getLuaStack(mainThread.getThread());
	}
	
	protected LuaValue getLuaGlobal(String key) {
		LuaValue globals = (luaRunState != null ? luaRunState.getGlobalEnvironment() : null);
		return linkedProperties.readFromLua(globals, key);
	}
	
	IConfig getPrefs() {
		return prefs;
	}
	
	//Setters	
	/**
	 * Warning: Don't change the bootstrap scripts while the novel is running
	 * unless you know what you're doing.
	 */
	public void setBootstrapScripts(String... bootstrap) {
		if (bootstrap == null) {
			bootstrap = new String[0];
		}
		
		for (String script : bootstrap) {
			if (script == null) throw new NullPointerException();
		}
		
		bootstrapScripts = bootstrap;
	}
	
	protected void setLuaGlobal(String key, LuaValue value) {
		LuaValue globals = (luaRunState != null ? luaRunState.getGlobalEnvironment() : null);
		linkedProperties.writeToLua(globals, key, value);
	}
	
	protected void setWait(int w) {
		wait = w;
	}
	
	protected void setWaitClick(boolean wait) {
		luaCall("setWaitClick", wait);
	}
	
	protected void setScriptDebug(boolean debug) {
		setLuaGlobal("scriptDebug", valueOf(debug));
	}
	
	protected void setAutoRead(boolean enable, int delay) {
		setLuaGlobal("autoRead", valueOf(enable));
		setLuaGlobal("autoReadWait", valueOf(delay));
		
		if (enable) {
			setWaitClick(false);
		}
	}

	protected void setSkipUnread(boolean s) {
		setLuaGlobal("skipUnread", valueOf(s));
	}
			
	//Inner Classes
	@LuaSerializable
	private static class PrefsMetaFunction extends VarArgFunction implements Serializable {
		
		private static final long serialVersionUID = 1L;
		
		private final String[] preferenceHolderClasses;
		private final boolean set;
		
		private transient IConfig prefs;
		private transient Map<String, Preference<?>> all;
		
		public PrefsMetaFunction(boolean setter, Class<?>... preferenceHolders) {			
			set = setter;

			preferenceHolderClasses = new String[preferenceHolders.length];
			for (int n = 0; n < preferenceHolders.length; n++) {
				preferenceHolderClasses[n] = preferenceHolders[n].getName();
			}
						
			initTransients();
		}
		
		private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
			in.defaultReadObject();
			
			initTransients();
		}	
		
		private void initTransients() {
			all = new HashMap<String, Preference<?>>();
			
			int m = Modifier.PUBLIC | Modifier.STATIC;
			for (String className : preferenceHolderClasses) {
				try {
					Class<?> clazz = Class.forName(className);
					for (Field field : clazz.getFields()) {
						if ((field.getModifiers() & m) == m) {
							try {
								Object val = field.get(null);
								if (val instanceof Preference<?>) {
									Preference<?> pref = (Preference<?>)val;
									
									String key = pref.getKey();
									if (key.startsWith("vn.")) {
										key = key.substring(3); //Remove "vn." prefix
									}
									all.put(key, pref);
								}
							} catch (IllegalArgumentException e) {
								//Too bad...
							} catch (IllegalAccessException e) {
								//Too bad...
							}
						}
					}		
				} catch (ClassNotFoundException cnfe) {
					//Ignore
				}
			}
		}
		
		@Override
		public Varargs invoke(Varargs args) {
			if (prefs == null) return NIL;

			String key = args.tojstring(2);
			
			Preference<?> pref = all.get(key);
			if (pref == null) return NIL;
			
			if (set) {
				doSet(prefs, pref, args.arg(3));
				return NONE;
			} else {
				return CoerceJavaToLua.coerce(prefs.get(pref));
			}
		}
		
		//Needs to be in a different function to work with generics
		private static <T> void doSet(IConfig prefs, Preference<T> pref, LuaValue val) {
			prefs.set(pref, CoerceLuaToJava.coerceArg(val, pref.getType()));
		}
		
		public void setPrefs(IConfig p) {
			prefs = p;
		}
		
	}
}
