package nl.weeaboo.vn.impl.lua;

import static nl.weeaboo.vn.impl.base.Interpolators.SMOOTH;
import static nl.weeaboo.vn.impl.lua.LuaInterpolators.getInterpolator;
import nl.weeaboo.lua2.io.LuaSerializable;
import nl.weeaboo.lua2.lib.LuaLibrary;
import nl.weeaboo.lua2.lib.LuajavaLib;
import nl.weeaboo.vn.IImageFactory;
import nl.weeaboo.vn.IImageTween;
import nl.weeaboo.vn.IInterpolator;
import nl.weeaboo.vn.INotifier;
import nl.weeaboo.vn.IShaderFactory;
import nl.weeaboo.vn.ITweenLib;

import org.luaj.vm2.LuaBoolean;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.OneArgFunction;

@LuaSerializable
public abstract class LuaTweenLib extends OneArgFunction implements ITweenLib {

	private static final long serialVersionUID = LuaImpl.serialVersionUID;
	
	protected final INotifier notifier;
	
	public LuaTweenLib(INotifier ntf, IImageFactory imgfac, IShaderFactory shfac) {
		this.notifier = ntf;
	}
	
	//Functions
	@Override
	public LuaValue call(LuaValue arg) {
		env.load(new CrossFadeTweenLib(this));
		env.load(new BitmapTweenLib(this));
		return NONE;
	}
	
	protected Varargs newCrossFadeTween(Varargs args) {
		if (!isCrossFadeTweenAvailable() && !isBitmapTweenAvailable()) {
			return NIL;
		}
		
		double duration = args.todouble(1);
		IInterpolator i = getInterpolator(args.arg(2), SMOOTH);
		IImageTween tween;
		if (isCrossFadeTweenAvailable()) {
			tween = newCrossFadeTween(duration, i);
		} else {
			tween = newBitmapTween(null, duration, 0.5, i, false);
		}
		return LuajavaLib.toUserdata(tween, tween.getClass());
	}
	
	protected abstract IImageTween newCrossFadeTween(double duration, IInterpolator i);

	protected Varargs newBitmapTween(Varargs args) {
		if (!isBitmapTweenAvailable()) {
			return NIL;
		}
		
		String fadeFilename = args.optjstring(1, null);
		double duration = args.todouble(2);
		double range = args.todouble(3);
		IInterpolator i = getInterpolator(args.arg(4), SMOOTH);
		IImageTween tween = newBitmapTween(fadeFilename, duration, range, i, false);
		return LuajavaLib.toUserdata(tween, tween.getClass());
	}

	protected abstract IImageTween newBitmapTween(String fadeFilename, double duration, double range,
			IInterpolator i, boolean fadeTexTile);
	
	//Getters

	//Setters
	
	//Inner Classes
	@LuaSerializable
	private static class CrossFadeTweenLib extends LuaLibrary {
				
		private static final long serialVersionUID = LuaImpl.serialVersionUID;

		private static final String[] NAMES = {
			"new",
			"isAvailable"
		};

		private static final int INIT         = 0;
		private static final int NEW          = 1;
		private static final int IS_AVAILABLE = 2;
				
		private final LuaTweenLib tweenLib;
		
		public CrossFadeTweenLib(LuaTweenLib lib) {
			this.tweenLib = lib;
		}
		
		@Override
		protected LuaLibrary newInstance() {
			return new CrossFadeTweenLib(tweenLib);
		}
		
		@Override
		public Varargs invoke(Varargs args) {
			switch (opcode) {
			case INIT: return initLibrary("CrossFadeTween", NAMES, 1);
			case NEW: return tweenLib.newCrossFadeTween(args);
			case IS_AVAILABLE: return isAvailable(args);
			default: return super.invoke(args);
			}
		}
		
		protected Varargs isAvailable(Varargs args) {
			return LuaBoolean.valueOf(tweenLib.isCrossFadeTweenAvailable());
		}
	}

	@LuaSerializable
	private static class BitmapTweenLib extends LuaLibrary {
		
		private static final long serialVersionUID = LuaImpl.serialVersionUID;

		private static final String[] NAMES = {
			"new",
			"isAvailable"
		};

		private static final int INIT         = 0;
		private static final int NEW          = 1;
		private static final int IS_AVAILABLE = 2;
				
		private final LuaTweenLib tweenLib;
		
		public BitmapTweenLib(LuaTweenLib lib) {
			this.tweenLib = lib;
		}
		
		@Override
		protected LuaLibrary newInstance() {
			return new BitmapTweenLib(tweenLib);
		}
		
		@Override
		public Varargs invoke(Varargs args) {
			switch (opcode) {
			case INIT: return initLibrary("BitmapTween", NAMES, 1);
			case NEW: return tweenLib.newBitmapTween(args);
			case IS_AVAILABLE: return isAvailable(args);
			default: return super.invoke(args);
			}
		}
		
		protected Varargs isAvailable(Varargs args) {
			return LuaBoolean.valueOf(tweenLib.isBitmapTweenAvailable());
		}
	}
	
}
