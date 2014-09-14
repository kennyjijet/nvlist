package nl.weeaboo.vn.awt;

import java.awt.Composite;
import java.awt.CompositeContext;
import java.awt.RenderingHints;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

class AdditiveComposite implements Composite {

	public static final AdditiveComposite INSTANCE = new AdditiveComposite();

	private static final CompositeContext CONTEXT = new AdditiveContext();

	private AdditiveComposite() {
	}

	@Override
	public CompositeContext createContext(ColorModel srcColorModel, ColorModel dstColorModel,
			RenderingHints hints)
	{
		return CONTEXT;
	}

	private static class AdditiveContext implements CompositeContext {

		@Override
		public void dispose() {
		}

		@Override
		public void compose(Raster src, Raster dstIn, WritableRaster dstOut) {
			final int w = Math.min(src.getWidth(), dstIn.getWidth());
	        final int h = Math.min(src.getHeight(), dstIn.getHeight());

	        final int[] srcRGBA = new int[4];
	        final int[] dstRGBA = new int[4];
	        for (int x = 0; x < w; x++) {
	            for (int y = 0; y < h; y++) {
	                src.getPixel(x, y, srcRGBA);
	                dstIn.getPixel(x, y, dstRGBA);
	                for (int n = 0; n < 3; n++) {
	                	dstRGBA[n] = Math.min(255, dstRGBA[n] + srcRGBA[n]);
	                }
	                dstOut.setPixel(x, y, dstRGBA);
	            }
	        }
		}

	}

}
