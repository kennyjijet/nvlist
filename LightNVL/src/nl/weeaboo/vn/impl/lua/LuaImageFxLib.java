package nl.weeaboo.vn.impl.lua;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import nl.weeaboo.lua2.io.LuaSerializable;
import nl.weeaboo.lua2.lib.CoerceLuaToJava;
import nl.weeaboo.lua2.lib.LuaLibrary;
import nl.weeaboo.lua2.lib.LuajavaLib;
import nl.weeaboo.vn.IImageFxLib;
import nl.weeaboo.vn.ITexture;
import nl.weeaboo.vn.math.Vec2;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

@LuaSerializable
public class LuaImageFxLib extends LuaLibrary implements Serializable {

	private static final long serialVersionUID = LuaImpl.serialVersionUID;

	private static final String[] NAMES = {
		"blur",
		"brighten",
		"blurMultiple",
		"mipmap",
		"applyColorMatrix",
		"crop",
		"composite"
	};

	private static final int INIT          = 0;
	private static final int BLUR          = 1;
	private static final int BRIGHTEN      = 2;
	private static final int BLUR_MULTIPLE = 3;
	private static final int MIPMAP        = 4;
	private static final int APPLY_COLOR_MATRIX = 5;
	private static final int CROP          = 6;
	private static final int COMPOSITE     = 7;
	
	private final IImageFxLib imageFxFactory;

	public LuaImageFxLib(IImageFxLib fac) {
		this.imageFxFactory = fac;
	}

	@Override
	protected LuaLibrary newInstance() {
		return new LuaImageFxLib(imageFxFactory);
	}
	
	@Override
	public Varargs invoke(Varargs args) {
		switch (opcode) {
		case INIT: return initLibrary("ImageFx", NAMES, 1);
		case BLUR: return blur(args);
		case BRIGHTEN: return brighten(args);
		case BLUR_MULTIPLE: return blurMultiple(args);
		case MIPMAP: return mipmap(args);
		case APPLY_COLOR_MATRIX: return applyColorMatrix(args);
		case CROP: return crop(args);
		case COMPOSITE: return composite(args);
		default: return super.invoke(args);
		}
	}
	
	private int getDirectionsInt(Varargs args, int index, int defaultValue) {
		LuaValue val = args.arg(index);
		if (val.isboolean()) {
			return (val.toboolean() ? 5 : 0);
		}
		return val.optint(defaultValue);
	}
	
	protected Varargs blur(Varargs args) {
		ITexture tex = args.optuserdata(1, ITexture.class, null);
		int k = args.optint(2, 8);
		int extendInt = getDirectionsInt(args, 3, 0);
		int cropInt = getDirectionsInt(args, 4, 0);
		
		ITexture blurTex = null;
		if (tex != null) {
			blurTex = imageFxFactory.blur(tex, k, extendInt, cropInt);
		}
		
		if (blurTex == null) {
			return NIL;
		}
		return LuajavaLib.toUserdata(blurTex, blurTex.getClass());
	}
	
	protected Varargs blurMultiple(Varargs args) {
		ITexture tex = args.optuserdata(1, ITexture.class, null);
		int levels = args.optint(2, 1);
		int k = args.optint(3, 8);
		int extendInt = getDirectionsInt(args, 4, 0);
		
		ITexture blurTexs[] = new ITexture[0];
		if (tex != null) {
			blurTexs = imageFxFactory.blurMultiple(tex, 0, levels, k, extendInt);
		}
		
		LuaTable table = new LuaTable(blurTexs.length, 0);
		for (int n = 0; n < blurTexs.length; n++) {
			table.rawset(n+1, LuajavaLib.toUserdata(blurTexs[n], blurTexs[n].getClass()));
		}
		return table;
	}
	
	protected Varargs brighten(Varargs args) {
		ITexture tex = args.optuserdata(1, ITexture.class, null);
		double add = args.optdouble(2, 0.5);
		
		if (tex == null) {
			return NIL;
		}
		ITexture newTex = imageFxFactory.brighten(tex, add);
		return LuajavaLib.toUserdata(newTex, newTex.getClass());
	}
	
	protected Varargs mipmap(Varargs args) {
		ITexture tex = args.optuserdata(1, ITexture.class, null);
		int level = args.optint(2, 1);
		
		if (tex == null) {
			return NIL;
		}
		ITexture newTex = imageFxFactory.mipmap(tex, level);
		return LuajavaLib.toUserdata(newTex, newTex.getClass());
	}
	
	private double[] toDoubleArray(LuaValue val) {
		return CoerceLuaToJava.coerceArg(val, double[].class);		
	}
	
	protected Varargs applyColorMatrix(Varargs args) {		
		ITexture tex = args.optuserdata(1, ITexture.class, null);
		if (tex == null) {
			return NIL;
		}

		int arrayCount = args.narg() - 1;
		ITexture newTex;
		if (arrayCount == 1) {
			double[] mulRGBA = toDoubleArray(args.arg(2));
			newTex = imageFxFactory.applyColorMatrix(tex, mulRGBA);
		} else if (arrayCount == 3 || arrayCount == 4) {
			double[][] arrays = new double[Math.max(4, arrayCount)][];
			arrays[0] = toDoubleArray(args.arg(2));
			arrays[1] = toDoubleArray(args.arg(3));
			arrays[2] = toDoubleArray(args.arg(4));
			if (arrayCount >= 4) {
				arrays[3] = toDoubleArray(args.arg(5));
			}
			newTex = imageFxFactory.applyColorMatrix(tex, arrays[0], arrays[1], arrays[2], arrays[3]);				
		} else {
			throw new LuaError("Invalid number of array arguments: " + arrayCount);
		}
		
		return LuajavaLib.toUserdata(newTex, newTex.getClass());
	}
	
	protected Varargs crop(Varargs args) {
		ITexture tex = args.optuserdata(1, ITexture.class, null);
		if (tex == null) {
			return NIL;
		}

		double x = args.optdouble(2, 0.0);
		double y = args.optdouble(3, 0.0);
		double w = args.optdouble(4, tex.getWidth());
		double h = args.optdouble(5, tex.getHeight());
		ITexture newTex = imageFxFactory.crop(tex, x, y, w, h);
		
		return LuajavaLib.toUserdata(newTex, newTex.getClass());
	}
	
	/**
	 * Expects a single argument of the form:
	 * <pre>{
	 *     {tex=myTexture1},
	 *     {tex=myTexture2, pos={10, 10}}
	 *     {tex=myTexture3}
	 * }</pre>
	 * The <code>pos</code> field is optional and assumed <code>{0, 0}</code> when omitted.
	 */
	protected Varargs composite(Varargs args) {
		LuaTable table = args.opttable(1, new LuaTable());
		double w = args.optdouble(2, -1);
		double h = args.optdouble(3, -1);
		
		List<ITexture> itexs = new ArrayList<ITexture>();
		List<Vec2> offsets = new ArrayList<Vec2>();
		
		final LuaValue S_TEX = valueOf("tex");
		final LuaValue S_POS = valueOf("pos");
		LuaValue v;
		for (int n = 1; (v = table.get(n)) != NIL; n++) {
			ITexture tex = CoerceLuaToJava.coerceArg(v.get(S_TEX), ITexture.class);
			
			LuaTable posT = v.get(S_POS).opttable(new LuaTable());
			Vec2 offset = new Vec2(posT.get(1).todouble(), posT.get(2).todouble());
			
			itexs.add(tex);
			offsets.add(offset);
		}
		
		ITexture newTex = imageFxFactory.composite(w, h,
				itexs.toArray(new ITexture[itexs.size()]),
				offsets.toArray(new Vec2[offsets.size()]));
				
		return LuajavaLib.toUserdata(newTex, newTex.getClass());
	}
	
}
