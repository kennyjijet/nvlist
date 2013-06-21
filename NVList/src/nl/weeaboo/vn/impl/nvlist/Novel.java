package nl.weeaboo.vn.impl.nvlist;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidClassException;

import nl.weeaboo.filesystem.IFileSystem;
import nl.weeaboo.game.input.IKeyConfig;
import nl.weeaboo.game.input.IKeys;
import nl.weeaboo.lua2.LuaException;
import nl.weeaboo.lua2.LuaRunState;
import nl.weeaboo.lua2.io.LuaSerializable;
import nl.weeaboo.vn.IAnalytics;
import nl.weeaboo.vn.IImageState;
import nl.weeaboo.vn.IInput;
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
import nl.weeaboo.vn.impl.lua.AbstractKeyCodeMetaFunction;
import nl.weeaboo.vn.impl.lua.LuaMediaPreloader;
import nl.weeaboo.vn.impl.lua.LuaNovel;
import nl.weeaboo.vn.vnds.VNDSLib;

import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;

public class Novel extends LuaNovel {

	private transient IFileSystem fs;
	private transient IKeyConfig keyConfig;
	private transient boolean isVNDS;
	private transient boolean preloaderBroken;
	
	// !!WARNING!! Do not add properties without adding code for saving/loading
	
	public Novel(INovelConfig nc, ImageFactory imgfac, IImageState is, ImageFxLib fxlib,
			SoundFactory sndfac, ISoundState ss, VideoFactory vidfac, IVideoState vs, GUIFactory guifac,
			ITextState ts, NovelNotifier n, IInput in, ShaderFactory shfac, SystemLib syslib, SaveHandler sh,
			ScriptLib scrlib, TweenLib tl, IPersistentStorage sharedGlobals, IStorage globals,
			ISeenLog seenLog, IAnalytics analytics, ITimer tmr,
			IFileSystem fs, IKeyConfig kc, boolean isVNDS)
	{
		super(nc, imgfac, is, fxlib, sndfac, ss, vidfac, vs, guifac, ts, n, in, shfac, syslib, sh, scrlib, tl,
				sharedGlobals, globals, seenLog, analytics, tmr);
		
		this.fs = fs;
		this.keyConfig = kc;
		this.isVNDS = isVNDS;
	}
	
	//Functions	
	@Override
	protected void initPreloader(LuaMediaPreloader preloader) {
		preloader.clear();
		
		Throwable preloaderError = null;
		try {
			try {
				InputStream in = new BufferedInputStream(fs.newInputStream("preloader-default.bin"), 4096);
				try {
					preloader.load(in);
				} finally {
					in.close();
				}
			} catch (InvalidClassException ice) {
				preloaderError = ice;
			} catch (FileNotFoundException fnfe) {
				//Ignore
			}
			
			try {
				InputStream in = new BufferedInputStream(fs.newInputStream("preloader.bin"), 4096);
				try {
					preloader.load(in);
				} finally {
					in.close();
				}
			} catch (InvalidClassException ice) {
				fs.copy("preloader.bin", "preloader.bin.old");
				try {
					fs.delete("preloader.bin");
				} catch (IOException ioe) {
					//Oh well, nothing we can do about it then
				}
				preloaderError = ice;
			} catch (FileNotFoundException fnfe) {
				//Ignore
			}
			
			if (!preloaderBroken && preloaderError != null) {
				preloaderBroken = true;
				getNotifier().d("Error initializing preloader", preloaderError);
			}
		} catch (IOException ioe) {
			if (!preloaderBroken) {
				preloaderBroken = true;
				getNotifier().d("Error initializing preloader", ioe);
			}
		}
	}
	
	@Override
	public void initLuaRunState() {		
		LuaRunState lrs = getLuaRunState();
		LuaValue globals = lrs.getGlobalEnvironment();
		try {
			//Register types
			//LuaUtil.registerClass(globals, FreeRotationGS.class);

			//Register libraries
			IScriptLib scrlib = getScriptLib();
			INotifier ntf = getNotifier();
			if (isVNDS) {
				VNDSLib.register(globals, scrlib, ntf);
			}
		} catch (LuaException e) {
			onScriptError(e);
		}
		
		super.initLuaRunState();
	}

	@Override
	protected void onScriptError(Exception e) {		
		Throwable t = e;
		String msg = t.getMessage();
		
		if (t instanceof LuaException && t.getCause() != null) {
			t = t.getCause();
		}

		StringBuilder message = new StringBuilder("Script Error");
		StackTraceElement[] stack = t.getStackTrace();
		if (stack != null) {
			StackTraceElement bestEntry = null;
			for (StackTraceElement entry : stack) {
				if (entry.getClassName().equals("Lua")) {
					bestEntry = entry;
					if (!bestEntry.getFileName().startsWith("?")) {
						break; //Quit when a real filename is found
					}
				}
			}
			
			if (bestEntry != null) {
				message.append(" at ");
				message.append(bestEntry.getFileName()+":"+bestEntry.getLineNumber());
			}
		}		
		message.append(" :: ");
		message.append(msg);
		
		getNotifier().e(message.toString(), t);		
		
		setWait(60);
	}

	@Override
	protected void addKeyCodeConstants(LuaTable table) throws LuaException {
		IKeys keys = keyConfig.getKeys();
		//for (Entry<String, Integer> entry : keys) {
		//	table.put(valueOf(entry.getKey()), valueOf(entry.getValue()));
		//}
		addKeyCodeConstants(table, new KeyCodeMetaFunction(keys, table));
	}
	
	//Getters
	
	//Setters
	
	//Inner classes
	@LuaSerializable
	private static class KeyCodeMetaFunction extends AbstractKeyCodeMetaFunction {

		private static final long serialVersionUID = 1L;
		
		private static IKeys keys;
		
		public KeyCodeMetaFunction(IKeys k, LuaTable t) {
			super(t);
			
			keys = k;
		}

		@Override
		protected LuaValue getKeyCode(String name) {
			int retval = keys.get(name);
			return (retval == 0 ? NIL : valueOf(retval));
		}
		
	}
	
}
