package nl.weeaboo.vn.impl.base;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import nl.weeaboo.common.Dim;
import nl.weeaboo.common.Rect;
import nl.weeaboo.image.ImageBlur;
import nl.weeaboo.image.ImageBlur.BlurImage;
import nl.weeaboo.image.ImageFxUtil;
import nl.weeaboo.image.ImageResize;
import nl.weeaboo.vn.IImageFactory;
import nl.weeaboo.vn.IImageFxLib;
import nl.weeaboo.vn.ITexture;
import nl.weeaboo.vn.TextureCompositeInfo;

public abstract class BaseImageFxLib implements IImageFxLib, Serializable {

	private static final long serialVersionUID = BaseImpl.serialVersionUID;
		
	protected final IImageFactory imgfac;
	private transient byte[] _alphaBlendLUT;
	
	public BaseImageFxLib(IImageFactory ifac) {
		imgfac = ifac;
	}
	
	//Functions
	/**
	 * Returns the dimensions of the bitmap that would be returned by {@link #tryGetBitmap(ITexture, boolean, Rect)}.
	 */
	protected abstract Dim getBitmapSize(ITexture tex);
	
	/**
	 * Tries to get an premultiplied ARGB bitmap from a texture, returns
	 * <code>null</code> if impossible.
	 * 
	 * @param tex The texture to get the pixels from
	 * @param logFailure If <code>true</code> log any occurrences where this
	 *        function returns <code>null</code>
	 * @param subRect Optionally specifies a subrectangle (in bitmap
	 *        coordinates) to return instead of the entire bitmap.
	 * @return A {@link BaseImageFxLib.Bitmap} containing the pixels from
	 *         <code>tex</code>, or <code>null</code> if an error occurred while
	 *         trying to get the pixels.
	 */
	protected abstract Bitmap tryGetBitmap(ITexture tex, boolean logFailure, Rect subRect);
	
	protected Bitmap scaleBitmap(Bitmap bm, int w, int h) {
		int[] result = new int[w * h];
		ImageResize.bilinear(bm.argb, result, bm.w, bm.h, w, h);	
		return new Bitmap(result, w, h);
	}
	
	protected int scaleBlurStrength(int k, double w0, double h0, double w1, double h1) {
		double scale = Math.min(w1/w0, h1/h0);
		return (int)Math.round(k * scale);
	}
	
	protected final int directionsIntToImageBlurDirection(int i) {
		int result = ImageBlur.DIR_NONE;
		while (i > 0) {
			int digit = (i % 10);
			switch (digit) {
			case 8: result |= ImageBlur.DIR_NORTH; break;
			case 6: result |= ImageBlur.DIR_EAST;  break;
			case 2: result |= ImageBlur.DIR_SOUTH; break;
			case 4: result |= ImageBlur.DIR_WEST;  break;
			case 7: result |= ImageBlur.DIR_NORTH|ImageBlur.DIR_WEST; break;
			case 9: result |= ImageBlur.DIR_NORTH|ImageBlur.DIR_EAST; break;
			case 1: result |= ImageBlur.DIR_SOUTH|ImageBlur.DIR_WEST; break;
			case 3: result |= ImageBlur.DIR_SOUTH|ImageBlur.DIR_EAST; break;
			case 5: result |= ImageBlur.DIR_ALL; break;
			}
			i /= 10;
		}
		return result;
	}
	
	@Override
	public ITexture blur(ITexture itex, int k, int extendDirs, int cropDirs) {
		Bitmap bm = tryGetBitmap(itex, true, null);
		if (bm == null) {			
			return itex;
		}
		
		k = scaleBlurStrength(k, itex.getWidth(), itex.getHeight(), bm.w, bm.h);
		int[] argb = bm.argb;
		
		int extendBits = directionsIntToImageBlurDirection(extendDirs);
		int cropBits = directionsIntToImageBlurDirection(cropDirs);
		
		ExecutorService executor = getExecutor();
		BlurImage bi = ImageBlur.blur(executor, argb, bm.w, bm.h, k, extendBits, cropBits);		
		if (argb.length != bi.w * bi.h) {
			argb = new int[bi.w * bi.h];
		}
		ImageFxUtil.copyImageIntoData(bi.argb, bi.offset, bi.scansize,
				argb, 0, bi.w, 0, 0, bi.w, bi.h);
				
		return imgfac.createTexture(argb, bi.w, bi.h, itex.getScaleX(), itex.getScaleY());		
	}
	
	@Override
	public ITexture[] blurMultiple(ITexture itex, int minLevel, int levelCount, int k, int extendDirs) {
		if (levelCount <= 0) throw new IllegalArgumentException("Level count must be > 0");
		
		Bitmap bm = tryGetBitmap(itex, true, null);
		if (bm == null) {
			ITexture[] result = new ITexture[levelCount];
			Arrays.fill(result, itex);
			return result;
		}

		ExecutorService executor = getExecutor();
		
		//k = scaleBlurStrength(k, itex.getWidth(), itex.getHeight(), bm.w, bm.h);
		int extendBits = directionsIntToImageBlurDirection(extendDirs);
		
		Bitmap[] bitmaps = generateMipmaps(bm, levelCount);
		for (int n = minLevel; n < minLevel+levelCount; n++) {
			Bitmap b = bitmaps[n];
			
			int pad = getBlurMultiplePad(n, levelCount, k);
			if (n > 0) {
				BlurImage bi = ImageBlur.blur(executor, b.argb, b.w, b.h, k, extendBits, extendBits);
				
				int w = bi.w + pad*2;
				int h = bi.h + pad*2;
				int[] argb = new int[w * h];
				
				int dx = Math.max(0, (w-bi.w)/2);
				int dy = Math.max(0, (h-bi.h)/2);
				ImageFxUtil.copyImageIntoData(bi.argb, bi.offset, bi.scansize,
						argb, w*dy+dx, w, 0, 0, bi.w, bi.h);			

				bitmaps[n] = new Bitmap(argb, w, h);
			} else {
				int w = b.w + pad*2;
				int h = b.h + pad*2;
				int[] argb = new int[w * h];
				
				int dx = Math.max(0, (w-b.w)/2);
				int dy = Math.max(0, (h-b.h)/2);
				ImageFxUtil.copyImageIntoData(b.argb, 0, b.w,
						argb, w*dy+dx, w, 0, 0, b.w, b.h);			

				bitmaps[n] = new Bitmap(argb, w, h);
			}
		}
		
		int basePad = getBlurMultiplePad(0, levelCount, k);
		double scaleX = itex.getScaleX() * (bm.w+basePad*2);
		double scaleY = itex.getScaleY() * (bm.h+basePad*2);
		
		ITexture[] result = new ITexture[levelCount];
		for (int s = minLevel, d = 0; d < levelCount; s++, d++) {
			Bitmap b = bitmaps[s];
			result[d] = imgfac.createTexture(b.argb, b.w, b.h, scaleX / b.w, scaleY / b.h);
		}
		return result;
	}
	
	@Override
	public int getBlurMultiplePad(int level, int levelCount, int k) {
		int basePad = k << (levelCount-1);
		return basePad >> level;
	}

	protected Bitmap[] generateMipmaps(Bitmap base, int levels) {
		Bitmap[] bitmaps = new Bitmap[levels];
		bitmaps[0] = base;
		for (int n = 1; n < levels; n++) {
			Bitmap last = bitmaps[n-1];
			
			int w2 = Math.max(1, last.w>>1);
			int h2 = Math.max(1, last.h>>1);
			int[] dst = new int[w2 * h2];
			ImageResize.scaleHalf(last.argb, dst, last.w, last.h);
			bitmaps[n] = new Bitmap(dst, w2, h2);
		}
		return bitmaps;
	}
	
	@Override
	public ITexture mipmap(ITexture itex, int level) {
		Bitmap bm = tryGetBitmap(itex, true, null);
		if (bm == null) {			
			return itex;
		}

		int w = bm.w;
		int h = bm.h; 
		int[] a = bm.argb;
		int[] b = new int[Math.max(1, w>>1) * Math.max(1, h>>1)];		
		for (int l = 0; l < level; l++) {
			//Scale down
			ImageResize.scaleHalf(a, b, w, h);
			w = Math.max(1, w>>1);
			h = Math.max(1, h>>1);
			
			//Swap src and dst
			int[] temp = a;
			a = b;
			b = temp;
		}
		
		return imgfac.createTexture(a, w, h,
				(1<<level)*itex.getScaleX(), (1<<level)*itex.getScaleY());
	}
	
	@Override
	public ITexture brighten(ITexture itex, double addFraction) {
		Bitmap bm = tryGetBitmap(itex, true, null);
		if (bm == null) {			
			return itex;
		}
		
		int inc = (int)Math.round(255 * addFraction);
		brighten(bm.argb, inc, inc, inc, 0);
		
		return imgfac.createTexture(bm.argb, bm.w, bm.h, itex.getScaleX(), itex.getScaleY());		
	}
	
	protected void brighten(int[] argb, int ri, int gi, int bi, int ai) {
		for (int n = 0; n < argb.length; n++) {
			int c = argb[n];
						
			//Un-premultiply
			int a = ((c>>24) & 0xFF);
			int r, g, b;
			if (a > 0) {
				r = ((c>>16) & 0xFF) * 255 / a;
				g = ((c>> 8) & 0xFF) * 255 / a;
				b = ((c    ) & 0xFF) * 255 / a;
			} else {
				r = g = b = 0;
			}
			
			//Add & Re-premultiply
			r = a * (r+ri) / 255;
			g = a * (g+gi) / 255;
			b = a * (b+bi) / 255;
			a = a + ai;
			
			//Saturate
			a = Math.max(0, Math.min(255, a));
			r = Math.max(0, Math.min(a,   r));
			g = Math.max(0, Math.min(a,   g));
			b = Math.max(0, Math.min(a,   b));
			
			argb[n] = (a<<24)|(r<<16)|(g<<8)|(b);
		}
	}
	
	@Override
	public ITexture applyColorMatrix(ITexture tex, double[] rgba) {
		double[] r = new double[] {rgba[0], 0, 0, 0};
		double[] g = new double[] {0, rgba[1], 0, 0};
		double[] b = new double[] {0, 0, rgba[2], 0};
		double[] a = new double[] {0, 0, 0, rgba.length >= 4 ? rgba[3] : 1};
		return applyColorMatrix(tex, r, g, b, a);
	}

	private static int[] makeColorVector(double[] arr, int pos) {
		int[] out = new int[5];
		for (int n = 0; n < 5; n++) {
			if (arr != null && arr.length > n) {
				out[n] = (int)Math.round(256.0 * arr[n]);
			} else {
				out[n] = (n == pos ? 256 : 0);
			}
		}
		return out;
	}
	
	/**
	 * Multiplies each color channel with the specified vector:<br/>
	 * <code>r' = r[0]*r + r[1]*g + r[2]*b + r[3]*a + r[4]*a';</code><br/>
	 * Multiplication of <code>a'</code> with the <code>r[4]</code> factor is
	 * necessary because internally pre-multiplied alpha is used.
	 */
	@Override
	public ITexture applyColorMatrix(ITexture itex, double[] r, double[] g, double[] b, double[] a) {
		Bitmap bm = tryGetBitmap(itex, true, null);
		if (bm == null) {			
			return itex;
		}
		
		int[] mulR = makeColorVector(r, 0);
		int[] mulG = makeColorVector(g, 1);
		int[] mulB = makeColorVector(b, 2);
		int[] mulA = makeColorVector(a, 3);
		
		for (int n = 0; n < bm.argb.length; n++) {
			int c = bm.argb[n];
			int ca = (c>>24) & 0xFF;
			int cr = (c>>16) & 0xFF;
			int cg = (c>>8 ) & 0xFF;
			int cb = (c    ) & 0xFF;
			
			int da = (mulA[0]*cr) + (mulA[1]*cg) + (mulA[2]*cb) + (mulA[3]*ca) + (mulA[4]<<8);
			da = Math.max(0, Math.min(255, (da+127) >> 8));
			
			int dr = (mulR[0]*cr) + (mulR[1]*cg) + (mulR[2]*cb) + (mulR[3]*ca) + (mulR[4]*da);
			dr = Math.max(0, Math.min(255, (dr+127) >> 8));
			
			int dg = (mulG[0]*cr) + (mulG[1]*cg) + (mulG[2]*cb) + (mulG[3]*ca) + (mulG[4]*da);
			dg = Math.max(0, Math.min(255, (dg+127) >> 8));
			
			int db = (mulB[0]*cr) + (mulB[1]*cg) + (mulB[2]*cb) + (mulB[3]*ca) + (mulB[4]*da);
			db = Math.max(0, Math.min(255, (db+127) >> 8));
			
			bm.argb[n] = (da<<24)|(dr<<16)|(dg<<8)|(db);
		}
		
		return imgfac.createTexture(bm.argb, bm.w, bm.h, itex.getScaleX(), itex.getScaleY());
	}
	
	@Override
	public ITexture crop(ITexture itex, double x, double y, double w, double h) {
		Dim dim = getBitmapSize(itex);
		
		double sx = dim.w / itex.getWidth();
		double sy = dim.h / itex.getHeight();
		int ix = Math.max(0, Math.min(dim.w,    (int)Math.round(sx * x)));
		int iy = Math.max(0, Math.min(dim.h,    (int)Math.round(sy * y)));
		int iw = Math.max(0, Math.min(dim.w-ix, (int)Math.round(sx * w)));
		int ih = Math.max(0, Math.min(dim.h-iy, (int)Math.round(sy * h)));
		
		Bitmap bm = tryGetBitmap(itex, true, new Rect(ix, iy, iw, ih));
		if (bm == null) {			
			return itex;
		}
	
		return imgfac.createTexture(bm.argb, bm.w, bm.h, itex.getScaleX(), itex.getScaleY());
	}
	
	@Override
	public ITexture composite(double w, double h, List<? extends TextureCompositeInfo> infos) {
		if (infos.isEmpty()) {
			return null;
		}
		
		TextureCompositeInfo baseInfo = infos.get(0);
		ITexture baseTex = baseInfo.getTexture();
		if (w < 0) w = baseTex.getWidth();
		if (h < 0) h = baseTex.getHeight();
		double sx = baseTex.getScaleX();
		double sy = baseTex.getScaleY();
		double isx = 1.0 / sx;
		double isy = 1.0 / sy;
		
		int baseW = (int)Math.round(w * isx);
		int baseH = (int)Math.round(h * isy);
		
		Iterator<? extends TextureCompositeInfo> itr = infos.iterator();
		
		Bitmap baseBitmap = null;		
		if (new Dim(baseW, baseH).equals(getBitmapSize(baseTex)) && baseInfo.hasOffset()) {
			//If the base image is the same size as the output and positioned
			baseBitmap = tryGetBitmap(baseTex, false, null);
			itr.next(); //Use first texture as the base
		}		
		if (baseBitmap == null) {
			baseBitmap = new Bitmap(new int[baseW*baseH], baseW, baseH);
		}		
		
		while (itr.hasNext()) {
			TextureCompositeInfo info = itr.next();
			ITexture tex = info.getTexture();
			double dx = info.getOffsetX();
			double dy = info.getOffsetY();
			
			Bitmap bm = tryGetBitmap(tex, true, null);
			if (bm != null) {
				//Scale bitmap to same scale level as base texture
				if (Math.abs(isx-tex.getScaleX()) > .001 || Math.abs(isy-tex.getScaleY()) > .001) {
					int sw = Math.max(1, (int)Math.round(bm.w * isx / tex.getScaleX()));
					int sh = Math.max(1, (int)Math.round(bm.h * isy / tex.getScaleY()));
					bm = scaleBitmap(bm, sw, sh);
				}
				
				//Determine offset in bitmap coordinates
				int dix = (int)Math.round(dx * isx);
				int diy = (int)Math.round(dy * isy);
				
				//Composite
				if (info.getOverwrite()) {
					overwriteComposite(baseBitmap, bm, dix, diy);
				} else {
					alphaComposite(baseBitmap, bm, dix, diy);
				}
			}
		}
		
		return imgfac.createTexture(baseBitmap.argb, baseBitmap.w, baseBitmap.h, sx, sy);
	}
	
	protected void overwriteComposite(Bitmap dstBitmap, Bitmap srcBitmap, int x, int y) {		
		//Benchmark.tick();
		
		int srcOffset = Math.max(-y, 0) * srcBitmap.w + Math.max(-x, 0);
		int w = srcBitmap.w;
		int h = srcBitmap.h;
		if (x < 0) {
			w += x;
			x = 0;
		}
		if (y < 0) {
			h += y;
			y = 0;
		}
		w = Math.max(0, Math.min(dstBitmap.w-x, w));
		h = Math.max(0, Math.min(dstBitmap.h-y, h));
		
		ImageFxUtil.copyDataIntoImage(
				srcBitmap.argb, srcOffset, srcBitmap.w,
				dstBitmap.argb, 0, dstBitmap.w,
				x, y, w, h);
		
		//Benchmark.tock("Overwrite blending took: %s");
	}
	
	protected void alphaComposite(Bitmap dstBitmap, Bitmap srcBitmap, int x, int y) {				
		final int minX = Math.max(x, 0);
		final int minY = Math.max(y, 0);
		final int maxX = Math.min(dstBitmap.w, x+srcBitmap.w);
		final int maxY = Math.min(dstBitmap.h, y+srcBitmap.h);
		
		//Benchmark.tick();

		ExecutorService executor = getExecutor();	
		List<Future<?>> list = new ArrayList<Future<?>>();
		
		//Benchmark.tick();
		final byte[] lut = getAlphaBlendLUT();
		//Benchmark.tock("Init alpha blend LUT: %s");
		
		final int sliceHeight = Math.max(1, (64<<10) / (maxX-minX));
		for (int dy = minY; dy < maxY; dy += sliceHeight) {
			final int sliceMinY = dy;
			final int sliceMaxY = Math.min(maxY, dy+sliceHeight);
			
			Runnable task = new AlphaBlendTask(dstBitmap, srcBitmap, x, y,
					minX, maxX, sliceMinY, sliceMaxY, lut);
			//System.out.println("Enqueue " + task.hashCode());
			list.add(executor.submit(task));
		}
		waitFor(list);
		
		//Benchmark.tock("Alpha blending took: %s :: sliceHeight=" + sliceHeight);
	}
	
	private static void waitFor(Collection<Future<?>> futures) {
		for (Future<?> f : futures) {
			try {
				f.get();
			} catch (ExecutionException e) {
				throw new RuntimeException(e.getCause());
			} catch (InterruptedException ie) {
				throw new RuntimeException(ie);
			}
		}
		futures.clear();
	}	
	
	//Getters
	/**
	 * @return A <code>256 * 256</code> lookup table for the calculation <code>c * (255-f) / 255</code>
	 */
	protected byte[] getAlphaBlendLUT() {
		if (_alphaBlendLUT != null) {
			return _alphaBlendLUT;
		}
		
		byte[] lut = new byte[256 * 256];
		
		int t = 0;
		for (int f = 0; f < 256; f++) {
			for (int c = 0; c < 256; c++) {
				lut[t++] = (byte)(c * (255-f) / 255);
			}
		}
		
		return (_alphaBlendLUT = lut);
	}
	
	private static synchronized ExecutorService getExecutor() {
		int processors = Runtime.getRuntime().availableProcessors();
		return new ThreadPoolExecutor(processors, processors,
				5, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(),
			new ThreadFactory() {
				private final AtomicInteger counter = new AtomicInteger();
			
				public Thread newThread(Runnable r) {
					Thread t = new Thread(r, "ImageFx-SubThread-" + counter.incrementAndGet());
					t.setDaemon(true);
					return t;
				}
		});
	}
	
	//Setters
	
	//Inner Classes
	protected static class Bitmap {
		
		/**
		 * ARGB8, premultiplied
		 */
		public final int[] argb;
		public final int w;
		public final int h;
		
		public Bitmap(int[] argb, int w, int h) {
			this.argb = argb;
			this.w = w;
			this.h = h;
		}
		
	}
	
	private static class AlphaBlendTask implements Runnable {

		private final Bitmap srcBitmap, dstBitmap;
		private final int x, y;
		private final int minX, maxX, minY, maxY;
		private final byte[] lut;
		
		public AlphaBlendTask(Bitmap dstBitmap, Bitmap srcBitmap, int x, int y,
				int minX, int maxX, int minY, int maxY, byte[] lut)
		{
			this.dstBitmap = dstBitmap;
			this.srcBitmap = srcBitmap;
			this.x = x;
			this.y = y;
			this.minX = minX;
			this.maxX = maxX;
			this.minY = minY;
			this.maxY = maxY;
			this.lut = lut;
		}
		
		public void run() {
			final int[] dst = dstBitmap.argb;
			final int[] src = srcBitmap.argb;
			final int dScansize = dstBitmap.w;
			final int dSkip = dScansize - (maxX-minX);
			
			final int sScansize = srcBitmap.w;
			final int sSkip = sScansize - (maxX-minX);

			int d = minY * dScansize + minX;
			int s = (minY-y) * sScansize + (minX-x);

			for (int yspan = maxY-minY; yspan != 0; yspan--) {
				for (int xspan = maxX-minX; xspan != 0; xspan--) {
					int cs = src[s], cd = dst[d];
					
					//SrcOver (premultiplied) = src.argb + dst.argb * (1-src.a)
					/*//Straightforward implementation
					int f = 255 - ((cs>>24)&0xFF);
					int a = ((cs>>24)&0xFF) + ((cd>>24)&0xFF) * f / 255;
					int r = ((cs>>16)&0xFF) + ((cd>>16)&0xFF) * f / 255;
					int g = ((cs>> 8)&0xFF) + ((cd>> 8)&0xFF) * f / 255;
					int b = ((cs    )&0xFF) + ((cd    )&0xFF) * f / 255;
					dst[d] = (a<<24)|(r<<16)|(g<<8)|(b);
					*/
					
					//LUT implementation
					int f = ((cs>>16)&0xFF00);
					int a = ((cs>>24)&0xFF) + (lut[((cd>>24)&0xFF) | f]&0xFF);
					int r = ((cs>>16)&0xFF) + (lut[((cd>>16)&0xFF) | f]&0xFF);
					int g = ((cs>> 8)&0xFF) + (lut[((cd>> 8)&0xFF) | f]&0xFF);
					int b = ((cs    )&0xFF) + (lut[((cd    )&0xFF) | f]&0xFF);
					dst[d] = (a<<24)|(r<<16)|(g<<8)|(b);

					//Fancy LUT implementation (not significantly faster after JIT)
					/*int f = ((cs>>16)&0xFF00);
					int lutA = lut[((cd>>24)&0xFF) | f] & 0xFF;
					int lutG = lut[((cd>> 8)&0xFF) | f] & 0xFF;
					int ag = ((cs>>8) & 0x00FF00FF) + ((lutA<<16) | lutG);
					int lutR = lut[((cd>>16)&0xFF) | f] & 0xFF;
					int lutB = lut[((cd    )&0xFF) | f] & 0xFF;
					int rb = ((cs   ) & 0x00FF00FF) + ((lutR<<16) | lutB);
					dst[d] = (ag<<8) | rb;
					*/
					
					s++; d++;
				}
				s += sSkip; d += dSkip;
			}		
		}
		
	}
	
}
