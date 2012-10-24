package nl.weeaboo.vn.impl.lua;

import static org.luaj.vm2.LuaValue.valueOf;

import java.io.Serializable;

import nl.weeaboo.lua2.io.LuaSerializable;
import nl.weeaboo.lua2.lib.LuajavaLib;
import nl.weeaboo.vn.IInterpolator;
import nl.weeaboo.vn.impl.base.LUTInterpolator;

import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaUserdata;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.VarArgFunction;

final class LuaInterpolators {

	static final LuaFunction GETTER = new GetterFunction();
	
	private LuaInterpolators() {		
	}
	
	public static IInterpolator getInterpolator(LuaValue lval, IInterpolator defaultValue) {
		if (lval.isuserdata()) {
			LuaUserdata udata = (LuaUserdata)lval;
			Object jval = udata.touserdata();
			if (jval instanceof IInterpolator) {
				return (IInterpolator)jval;
			}
		} else if (lval instanceof LuaFunction) {
			return getLuaInterpolator((LuaFunction)lval, 256);
		}
		return defaultValue;
	}
	
	public static IInterpolator getLuaInterpolator(LuaFunction func, int lutSize) {
		float[] lut = new float[lutSize];
		float scale = 1f / (lutSize - 1);
		for (int n = 0; n < lutSize; n++) {
			lut[n] = func.call(valueOf(n*scale)).tofloat();
		}
		return new LUTInterpolator(lut);
	}
	
	//Inner Classes
	@LuaSerializable
	private static final class GetterFunction extends VarArgFunction implements Serializable {
		
		private static final long serialVersionUID = 1L;

		@Override
		public Varargs invoke(Varargs args) {
			IInterpolator interpolator = getInterpolator(args.arg1(), null);
			if (interpolator == null) {
				return args.arg(2);
			}
			return LuajavaLib.toUserdata(interpolator, IInterpolator.class);
		}
		
	}
}
