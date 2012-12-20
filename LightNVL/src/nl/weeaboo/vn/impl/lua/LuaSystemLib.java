package nl.weeaboo.vn.impl.lua;

import java.io.Externalizable;
import java.io.Serializable;

import nl.weeaboo.common.StringUtil;
import nl.weeaboo.lua2.LuaException;
import nl.weeaboo.lua2.LuaUtil;
import nl.weeaboo.lua2.io.LuaSerializable;
import nl.weeaboo.lua2.lib.LuaLibrary;
import nl.weeaboo.vn.INotifier;
import nl.weeaboo.vn.impl.base.BaseSystemLib;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

@LuaSerializable
public class LuaSystemLib extends LuaLibrary {

	private static final long serialVersionUID = LuaImpl.serialVersionUID;

	private static final String[] NAMES = {
		"exit",
		"canExit",
		"isLowEnd",
		"isTouchScreen",
		"setTextFullscreen",
		"openWebsite",
		"restart",
		"registerJavaClass",
		"compareVersion",
		"softExit"
	};

	private static final int INIT                = 0;
	private static final int EXIT                = 1;
	private static final int CAN_EXIT            = 2;
	private static final int IS_LOW_END          = 3;
	private static final int IS_TOUCH_SCREEN     = 4;
	private static final int SET_TEXT_FULLSCREEN = 5;
	private static final int OPEN_WEBSITE        = 6;
	private static final int RESTART             = 7;
	private static final int REGISTER_JAVA_CLASS = 8;
	private static final int COMPARE_VERSION     = 9;
	private static final int SOFT_EXIT           = 10;
	
	private final INotifier notifier;
	private final BaseSystemLib syslib;

	public LuaSystemLib(INotifier ntf, BaseSystemLib sl) {		
		this.notifier = ntf;
		this.syslib = sl;
	}
	
	@Override
	protected LuaLibrary newInstance() {
		return new LuaSystemLib(notifier, syslib);
	}
	
	@Override
	public Varargs invoke(Varargs args) {
		switch (opcode) {
		case INIT: return initLibrary("System", NAMES, 1);
		case EXIT: return exit(args);
		case CAN_EXIT: return canExit(args);
		case IS_LOW_END: return isLowEnd(args);
		case IS_TOUCH_SCREEN: return isTouchScreen(args);
		case SET_TEXT_FULLSCREEN: return setTextFullscreen(args);
		case OPEN_WEBSITE: return openWebsite(args);
		case RESTART: return restart(args);
		case REGISTER_JAVA_CLASS: return registerJavaClass(args);
		case COMPARE_VERSION: return compareVersion(args);
		case SOFT_EXIT: return softExit(args);
		default: return super.invoke(args);
		}
	}
	
	protected Varargs exit(Varargs args) {
		boolean force = args.optboolean(1, false);
		syslib.exit(force);
		return NONE;
	}
	
	protected Varargs softExit(Varargs args) {
		try {
			syslib.softExit();
		} catch (LuaException e) {
			throw new LuaError(e);
		}
		return NONE;
	}
	
	protected Varargs canExit(Varargs args) {
		return valueOf(syslib.canExit());
	}
	
	protected Varargs isLowEnd(Varargs args) {
		return valueOf(syslib.isLowEnd());
	}
	
	protected Varargs isTouchScreen(Varargs args) {
		return valueOf(syslib.isTouchScreen());
	}
	
	protected Varargs setTextFullscreen(Varargs args) {
		boolean fullscreen = args.optboolean(1, true);
		syslib.setTextFullscreen(fullscreen);
		return NONE;
	}
	
	protected Varargs openWebsite(Varargs args) {
		String url = args.optjstring(1, "");
		syslib.openWebsite(url);
		return NONE;
	}
	
	protected Varargs restart(Varargs args) {
		syslib.restart();
		return NONE;
	}
	
	protected Varargs registerJavaClass(Varargs args) {
		LuaValue env = args.arg(1);		
		String className = args.tojstring(2);
		try {
			Class<?> clazz = Class.forName(className);
			if (!Serializable.class.isAssignableFrom(clazz) && !Externalizable.class.isAssignableFrom(clazz)) {
				notifier.w("Java class must be Serializable or Externalizable to be allowed to be registered, " + className + " is neither.");
			} else {
				LuaUtil.registerClass(env, clazz);
			}
		} catch (ClassNotFoundException e) {
			notifier.w("Unable to register Java class (" + className + ")", e);
		}
		return NONE;
	}
	
	protected Varargs compareVersion(Varargs args) {
		String a = args.tojstring(1);		
		String b = args.tojstring(2);
		
		if (a == null) {
			return valueOf(b == null ? 0 : -1);
		} else if (b == null) {
			return valueOf(1);
		}
		
		return valueOf(StringUtil.compareVersion(a, b));
	}
	
}
