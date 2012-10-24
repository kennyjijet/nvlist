package nl.weeaboo.vn.impl.nvlist;

import java.awt.image.BufferedImage;
import java.io.ObjectStreamException;
import java.nio.IntBuffer;

import nl.weeaboo.awt.ImageUtil;
import nl.weeaboo.common.Dim;
import nl.weeaboo.common.Rect;
import nl.weeaboo.game.GameLog;
import nl.weeaboo.gl.texture.GLTexRect;
import nl.weeaboo.gl.texture.TextureException;
import nl.weeaboo.io.EnvironmentSerializable;
import nl.weeaboo.lua2.io.LuaSerializable;
import nl.weeaboo.vn.IImageFactory;
import nl.weeaboo.vn.ITexture;
import nl.weeaboo.vn.impl.base.BaseImageFxLib;

@LuaSerializable
public class ImageFxLib extends BaseImageFxLib {

	private final EnvironmentSerializable es;
	
	public ImageFxLib(IImageFactory imgfac) {
		super(imgfac);
		
		this.es = new EnvironmentSerializable(this);
	}
	
	//Functions
	private Object writeReplace() throws ObjectStreamException {	
		return es.writeReplace();
	}

	@Override
	protected Dim getBitmapSize(ITexture tex) {
		if (tex instanceof TextureAdapter) {
			TextureAdapter adapter = (TextureAdapter)tex;
			GLTexRect tr = adapter.getTexRect();
			if (tr != null) {
				return new Dim(tr.getWidth(), tr.getHeight());
			}
		}
		return new Dim(0, 0);
	}

	@Override
	protected Bitmap tryGetBitmap(ITexture tex, boolean logFailure, Rect r) {
		if (tex instanceof TextureAdapter) {
			TextureAdapter adapter = (TextureAdapter)tex;
			GLTexRect tr = adapter.getTexRect();
			if (tr != null) {
				try {
					BufferedImage image = tr.toBufferedImage(r);
					int[] argb = new int[image.getWidth() * image.getHeight()];
					ImageUtil.getPixelsPre(image, IntBuffer.wrap(argb), 0, image.getWidth());
					return new Bitmap(argb, image.getWidth(), image.getHeight());
				} catch (TextureException e) {
					GameLog.w("Error getting pixels from texture", e);
					return null;
				}
			}
		}
		
		if (logFailure) {
			GameLog.w("Unable to get pixels from texture: " + tex.toString());			
		}
		
		return null;
	}

	//Getters
	
	//Setters
	
}
