package nl.weeaboo.vn.impl.lua;

import nl.weeaboo.lua2.io.LuaSerializable;
import nl.weeaboo.lua2.lib.LuaLibrary;
import nl.weeaboo.lua2.lib.LuajavaLib;
import nl.weeaboo.vn.IChoice;
import nl.weeaboo.vn.IImageState;
import nl.weeaboo.vn.ILayer;
import nl.weeaboo.vn.INotifier;
import nl.weeaboo.vn.IPanel;
import nl.weeaboo.vn.ISaveLoadScreen;
import nl.weeaboo.vn.impl.base.BaseGUIFactory;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.Varargs;

@LuaSerializable
public class LuaGUILib extends LuaLibrary {

	private static final long serialVersionUID = LuaImpl.serialVersionUID;

	private static final String[] NAMES = {
		"createChoice",
		"createSaveScreen",
		"createLoadScreen",
	};

	private static final int INIT                = 0;
	private static final int CREATE_CHOICE       = 1;
	private static final int CREATE_SAVE_SCREEN  = 2;
	private static final int CREATE_LOAD_SCREEN  = 3;
	private static final int CREATE_PANEL        = 4;
	
	private final INotifier notifier;
	private final BaseGUIFactory guifac;
	private final IImageState imageState;
	
	public LuaGUILib(INotifier ntf, BaseGUIFactory gl, IImageState is) {		
		this.notifier = ntf;
		this.guifac = gl;
		this.imageState = is;
	}
	
	@Override
	protected LuaLibrary newInstance() {
		return new LuaGUILib(notifier, guifac, imageState);
	}
	
	@Override
	public Varargs invoke(Varargs args) {
		switch (opcode) {
		case INIT: return initLibrary("GUI", NAMES, 1);
		case CREATE_CHOICE: return createChoice(args);
		case CREATE_SAVE_SCREEN: return createSaveScreen(args);
		case CREATE_LOAD_SCREEN: return createLoadScreen(args);
		case CREATE_PANEL: return createPanel(args);
		default: return super.invoke(args);
		}
	}
	
	protected Varargs createChoice(Varargs args) {
		String[] opts = {};
		if (args.narg() >= 1) {
			if (args.istable(1)) {
				LuaTable table = (LuaTable)args.checktable(1);
				opts = new String[table.getn().toint()];
				for (int n = table.length(); n > 0; n--) {
					opts[n-1] = table.get(n).tojstring();
				}
			} else {
				opts = new String[args.narg()];
				for (int n = 0; n < opts.length; n++) {
					opts[n] = args.tojstring(n+1);
				}
			}
		}
		IChoice choice = guifac.createChoice(opts);
		if (choice == null) {
			return NIL;
		}
		return LuajavaLib.toUserdata(choice, IChoice.class);
	}

	protected Varargs createSaveScreen(Varargs args) {
		ISaveLoadScreen screen = guifac.createSaveScreen();
		if (screen == null) {
			return NIL;
		}
		return LuajavaLib.toUserdata(screen, ISaveLoadScreen.class);
	}


	protected Varargs createLoadScreen(Varargs args) {
		ISaveLoadScreen screen = guifac.createLoadScreen();
		if (screen == null) {
			return NIL;
		}
		return LuajavaLib.toUserdata(screen, ISaveLoadScreen.class);
	}
	
	protected ILayer getLayerArg(Varargs args, int index) {		
		if (args.isuserdata(index) && args.touserdata(index) instanceof ILayer) {
			return (ILayer)args.touserdata(index);
		}		
		throw new LuaError("Invalid layer arg: " + args.tojstring(1));
	}
	
	protected Varargs createPanel(Varargs args) {
		ILayer layer = getLayerArg(args, 1);

		IPanel panel = guifac.createPanel();
		layer.add(panel);
		return LuajavaLib.toUserdata(panel, panel.getClass());
	}
	
}
