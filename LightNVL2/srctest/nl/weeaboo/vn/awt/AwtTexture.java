package nl.weeaboo.vn.awt;

import java.awt.image.BufferedImage;

import nl.weeaboo.common.Area2D;
import nl.weeaboo.vn.ITexture;

public class AwtTexture implements ITexture {

	private static final long serialVersionUID = 1L;

	private final BufferedImage image;
	private final int[] argb;

	public AwtTexture(int w, int h) {
		argb = new int[w * h];
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				int r = 64 + 127 * x / (w - 1);
				int g = 64 + 127 * y / (h - 1);
				argb[y * w + x] = 0xFF000000|(r<<16)|(g<<8);
			}
		}

        image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        image.setRGB(0, 0, w, h, argb, 0, w);
	}

	@Override
	public double getWidth() {
		return getScaleY() * image.getHeight();
	}

	@Override
	public double getHeight() {
		return getScaleX() * image.getWidth();
	}

	@Override
	public double getScaleX() {
		return 1;
	}

	@Override
	public double getScaleY() {
		return 1;
	}

	@Override
	public Area2D getUV() {
		return DEFAULT_UV;
	}

	public BufferedImage getImage() {
		return image;
	}

	public int[] getARGB() {
	    return argb;
	}

}
