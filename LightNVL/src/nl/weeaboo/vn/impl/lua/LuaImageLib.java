package nl.weeaboo.vn.impl.lua;

import java.io.Serializable;
import java.util.Arrays;

import nl.weeaboo.lua2.io.LuaSerializable;
import nl.weeaboo.lua2.lib.LuaLibrary;
import nl.weeaboo.lua2.lib.LuajavaLib;
import nl.weeaboo.styledtext.StyledText;
import nl.weeaboo.vn.IButtonDrawable;
import nl.weeaboo.vn.IImageDrawable;
import nl.weeaboo.vn.IImageState;
import nl.weeaboo.vn.ILayer;
import nl.weeaboo.vn.IScreenshot;
import nl.weeaboo.vn.ITextDrawable;
import nl.weeaboo.vn.ITexture;
import nl.weeaboo.vn.impl.base.BaseImageFactory;
import nl.weeaboo.vn.impl.base.BaseImageFxLib;
import nl.weeaboo.vn.impl.base.Camera;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.Varargs;

@LuaSerializable
public class LuaImageLib extends LuaLibrary implements Serializable {

	private static final long serialVersionUID = LuaImpl.serialVersionUID;

	private static final String[] NAMES = {
		"createImage",
		"createText",
		"createButton",
		"screenshot",
		"getTexture",
		"preload",
		"getImageFiles",
		"createLayer",
		"createCamera",
		"createColorTexture",
	};

	private static final int INIT            = 0;
	private static final int CREATE_IMAGE    = 1;
	private static final int CREATE_TEXT     = 2;
	private static final int CREATE_BUTTON   = 3;
	private static final int SCREENSHOT      = 4;
	private static final int GET_TEXTURE     = 5;
	private static final int PRELOAD         = 6;
	private static final int GET_IMAGE_FILES = 7;
	private static final int CREATE_LAYER    = 8;
	private static final int CREATE_CAMERA   = 9;
	private static final int CREATE_COLOR_TEXTURE = 10;
	
	private final BaseImageFactory imageFactory;
	private final BaseImageFxLib imageFxLib;
	private final IImageState imageState;

	public LuaImageLib(BaseImageFactory fac, BaseImageFxLib fxlib, IImageState is) {
		this.imageFactory = fac;
		this.imageFxLib = fxlib;
		this.imageState = is;
	}
	
	@Override
	protected LuaLibrary newInstance() {
		return new LuaImageLib(imageFactory, imageFxLib, imageState);
	}
	
	@Override
	public Varargs invoke(Varargs args) {
		switch (opcode) {
		case INIT:            return initLibrary("Image", NAMES, 1);
		case CREATE_IMAGE:    return createImage(args);
		case CREATE_TEXT:     return createText(args);
		case CREATE_BUTTON:   return createButton(args);
		case SCREENSHOT:      return screenshot(args);
		case GET_TEXTURE:     return getTexture(args);
		case PRELOAD:         return preload(args);
		case GET_IMAGE_FILES: return getImageFiles(args);
		case CREATE_LAYER:    return createLayer(args);
		case CREATE_CAMERA:   return createCamera(args);
		case CREATE_COLOR_TEXTURE: return createColorTexture(args);
		default: return super.invoke(args);
		}
	}
			
	protected Varargs createImage(Varargs args) {
		ILayer layer = getLayerArg(args, 1);

		IImageDrawable d = imageFactory.createImageDrawable();
		if (args.isstring(2)) {
			d.setTexture(imageFactory.getTexture(args.tojstring(2), LuaNovelUtil.getLuaStack(), false));
		} else if (args.isuserdata(2)) {
			Object obj = args.touserdata(2);
			if (obj instanceof IScreenshot) {
				IScreenshot ss = (IScreenshot)obj;
				if (!ss.isAvailable()) {
					throw new LuaError("Screenshot data isn't available yet");
				}
				d.setTexture(imageFactory.createTexture(ss));
			} else if (obj instanceof ITexture) {
				d.setTexture((ITexture)obj);
			} else {
				throw new LuaError("Invalid arguments");
			}
		} else if (!args.isnil(2)) {
			throw new LuaError("Invalid arguments");
		}
		
		layer.add(d);
		return LuajavaLib.toUserdata(d, d.getClass());
	}
	
	protected Varargs createText(Varargs args) {
		ILayer layer = getLayerArg(args, 1);

		ITextDrawable d = imageFactory.createTextDrawable();
		if (args.isstring(2)) {
			d.setText(args.tojstring(2));
		} else if (args.isuserdata(2)) {
			Object obj = args.touserdata(2);
			if (obj instanceof StyledText) {
				d.setText((StyledText)obj);
			}
		}

		layer.add(d);
		return LuajavaLib.toUserdata(d, d.getClass());
	}
	
	protected Varargs createButton(Varargs args) {		
		String[] luaStack = LuaNovelUtil.getLuaStack(); 
		
		ILayer layer = getLayerArg(args, 1);

		IButtonDrawable b = imageFactory.createButtonDrawable();
		
		if (args.isuserdata(2) && args.touserdata(2) instanceof ITexture) {
			ITexture tex = args.checkuserdata(2, ITexture.class);
			b.setNormalTexture(tex);
		} else {
			String filename = null;
			if (args.isstring(2)) {
				filename = args.tojstring(2);			
			}
			if (filename != null) {
				ITexture[] texs = getButtonTextures(filename, luaStack);
				if (texs[0] == null) {
					texs = getButtonTextures(filename + "#", luaStack);
				}
				
				int t = 0;
				b.setNormalTexture(texs[t++]);
				b.setRolloverTexture(texs[t++]);
				b.setPressedTexture(texs[t++]);
				b.setPressedRolloverTexture(texs[t++]);
				b.setDisabledTexture(texs[t++]);
				b.setDisabledPressedTexture(texs[t++]);
			}
		}
		
		if (b.getNormalTexture() == null) {
			ITexture normalI   = createColorTexture(0xFF333333, 300, 60);
			ITexture rolloverI = createColorTexture(0xFF999999, 300, 60);
			ITexture pressedI  = createColorTexture(0xFF111111, 300, 60);
			ITexture disabledI = createColorTexture(0x80222222, 300, 60);
			b.setNormalTexture(normalI);
			b.setRolloverTexture(rolloverI);
			b.setPressedTexture(pressedI);
			b.setDisabledTexture(disabledI);
		}
		
		layer.add(b);
		return LuajavaLib.toUserdata(b, b.getClass());
	}
	
	protected Varargs screenshot(Varargs args) {
		ILayer layer = null;
		int z;
		boolean clip;
		if (!args.isnil(1)) {
			layer = getLayerArg(args, 1);
			z = args.optint(2, -999);
			clip = args.optboolean(3, true);
		} else {
			layer = imageState.getRootLayer();
			z = Short.MIN_VALUE;
			clip = false;
		}
		
		if (layer == null) {
			return NIL;
		}
		
		IScreenshot ss = imageFactory.screenshot((short)z);		
		layer.getScreenshotBuffer().add(ss, clip);
		return LuajavaLib.toUserdata(ss, ss.getClass());
	}

	protected Varargs getTexture(Varargs args) {
		ITexture tex = imageFactory.getTexture(args.tojstring(1), LuaNovelUtil.getLuaStack(), args.toboolean(2));
		if (tex == null) {
			return NIL;
		}
		return LuajavaLib.toUserdata(tex, tex.getClass());
	}

	protected Varargs preload(Varargs args) {
		for (int n = 1; n <= args.narg(); n++) {
			if (args.isstring(n)) {
				imageFactory.preload(args.tojstring(n));
			}
		}
		return NONE;
	}
	
	protected Varargs getImageFiles(Varargs args) {
		String folder = args.optjstring(1, "");
		
		LuaTable table = new LuaTable();
		int t = 1;
		for (String filename : imageFactory.getImageFiles(folder)) {
			table.rawset(t++, filename);
		}
		return table;
	}
	
	protected ILayer getLayerArg(Varargs args, int index) {		
		if (args.isuserdata(index) && args.touserdata(index) instanceof ILayer) {
			return (ILayer)args.touserdata(index);
		}		
		throw new LuaError("Invalid layer arg: " + args.tojstring(1));
	}

	protected Varargs createLayer(Varargs args) {
		ILayer parentLayer = getLayerArg(args, 1);
		
		ILayer layer = imageState.createLayer(parentLayer);		
		return LuajavaLib.toUserdata(layer, layer.getClass());
	}
	
	protected Varargs createCamera(Varargs args) {
		ILayer layer = getLayerArg(args, 1);
		Camera camera = new Camera(imageFxLib, layer);		
		return LuajavaLib.toUserdata(camera, camera.getClass());
	}
	
	private ITexture[] getButtonTextures(String prefix, String[] luaStack) {
		return new ITexture[] {
			imageFactory.getTexture(prefix + "normal", luaStack, true),		
			imageFactory.getTexture(prefix + "rollover", luaStack, true),		
			imageFactory.getTexture(prefix + "pressed", luaStack, true),		
			imageFactory.getTexture(prefix + "pressed-rollover", luaStack, true),		
			imageFactory.getTexture(prefix + "disabled", luaStack, true),
			imageFactory.getTexture(prefix + "disabled-pressed", luaStack, true)
		};
	}
	

	protected Varargs createColorTexture(Varargs args) {
		int argb = args.optint(1, 0xFFFFFFFF);
		int w = args.optint(2, 64);
		int h = args.optint(3, 64);
		
		ITexture tex = null;
		if (w > 0 && h > 0) {
			tex = createColorTexture(argb, w, h);
		}
		
		if (tex == null) {
			return NIL;
		}
		return LuajavaLib.toUserdata(tex, tex.getClass());
	}
	
	protected ITexture createColorTexture(int argb, int w, int h) {
		int[] pixels = new int[w * h];
		Arrays.fill(pixels, argb);
		return imageFactory.createTexture(pixels, w, h, 1, 1);		
	}
	
}
