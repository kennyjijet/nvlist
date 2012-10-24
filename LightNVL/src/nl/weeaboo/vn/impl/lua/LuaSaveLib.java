package nl.weeaboo.vn.impl.lua;

import java.io.IOException;

import nl.weeaboo.lua2.LuaRunState;
import nl.weeaboo.lua2.io.LuaSerializable;
import nl.weeaboo.lua2.lib.LuaLibrary;
import nl.weeaboo.lua2.lib.LuajavaLib;
import nl.weeaboo.vn.ISaveInfo;
import nl.weeaboo.vn.IScreenshot;
import nl.weeaboo.vn.IStorage;
import nl.weeaboo.vn.impl.base.BaseStorage;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaThread;
import org.luaj.vm2.Varargs;

@LuaSerializable
public class LuaSaveLib extends LuaLibrary {

	private static final long serialVersionUID = LuaImpl.serialVersionUID;

	private static final String[] NAMES = {
		"getSaves",
		"save",
		"load",
		"getSavepointStorage",
		"getQuickSaveSlot",
		"getAutoSaveSlot",
		"getFreeSaveSlot"
	};

	private static final int INIT      = 0;
	private static final int GET_SAVES = 1;
	private static final int SAVE      = 2;
	private static final int LOAD      = 3;
	private static final int GET_SAVEPOINT_STORAGE = 4;
	private static final int GET_QUICK_SAVE_SLOT = 5;
	private static final int GET_AUTO_SAVE_SLOT = 6;
	private static final int GET_FREE_SAVE_SLOT = 7;
	
	private final LuaSaveHandler saveHandler;

	public LuaSaveLib(LuaSaveHandler sh) {		
		this.saveHandler = sh;
	}

	@Override
	protected LuaLibrary newInstance() {
		return new LuaSaveLib(saveHandler);
	}
	
	@Override
	public Varargs invoke(Varargs args) {
		switch (opcode) {
		case INIT: return initLibrary("Save", NAMES, 1);
		case GET_SAVES: return getSaves(args);
		case SAVE: return save(args);
		case LOAD: return load(args);
		case GET_SAVEPOINT_STORAGE: return getSavepointStorage(args);
		case GET_QUICK_SAVE_SLOT: return getQuickSaveSlot(args);
		case GET_AUTO_SAVE_SLOT: return getAutoSaveSlot(args);
		case GET_FREE_SAVE_SLOT: return getFreeSaveSlot(args);
		default: return super.invoke(args);
		}
	}
	
	protected Varargs getSaves(Varargs args) {
		int min = args.optint(1, Integer.MIN_VALUE);
		int max = args.optint(2, Integer.MAX_VALUE);
		if (max < min) max = min;
		
		ISaveInfo[] saves = saveHandler.getSaves(min, max);
		LuaTable table = new LuaTable(saves.length, 0);
		for (ISaveInfo si : saves) {
			table.rawset(si.getSlot(), LuajavaLib.toUserdata(si, si.getClass()));
		}
		return table;
	}
	
	protected Varargs save(Varargs args) {
		int slot = args.checkint(1);
		
		//Screenshot
		Object screenshotObj;
		if (args.istable(2)) {
			LuaTable t = args.checktable(2);
			screenshotObj = t.get("screenshot").touserdata();
		} else {
			screenshotObj = args.touserdata(2);
		}
		final IScreenshot ss = (screenshotObj instanceof IScreenshot ? (IScreenshot)screenshotObj : null);
		
		//Meta data
		IStorage metaData = new BaseStorage();
		if (args.istable(3)) {
			LuaTable table = args.checktable(3);
			metaData.set("", table);
		}
		
		//Save
		final LuaRunState lrs = LuaRunState.getCurrent();
		final LuaThread thread = lrs.getRunningThread();
		Varargs result = thread.yield(NONE);
		try {
			saveHandler.save(slot, ss, metaData, null);
		} catch (IOException e) {
			throw new LuaError(e);
		}
		return result;
	}

	protected Varargs load(Varargs args) {
		final LuaRunState lrs = LuaRunState.getCurrent();
		final LuaThread thread = lrs.getRunningThread();
		Varargs result = thread.yield(NONE);
		lrs.destroy();
		
		int slot = args.checkint(1);
		try {
			saveHandler.load(slot, null);
		} catch (IOException e) {
			throw new LuaError(e);
		}

		return result;
	}	
	
	protected Varargs getSavepointStorage(Varargs args) {
		return LuajavaLib.toUserdata(saveHandler.getSavepointStorage(), IStorage.class);
	}
	
	protected Varargs getQuickSaveSlot(Varargs args) {
		return valueOf(saveHandler.getQuickSaveSlot(args.optint(1, 1)));
	}
	
	protected Varargs getAutoSaveSlot(Varargs args) {
		return valueOf(saveHandler.getAutoSaveSlot(args.optint(1, 1)));
	}
	
	protected Varargs getFreeSaveSlot(Varargs args) {
		return valueOf(saveHandler.getNextFreeSlot());
	}
	
}
