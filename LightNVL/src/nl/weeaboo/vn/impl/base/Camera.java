package nl.weeaboo.vn.impl.base;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import nl.weeaboo.common.Dim2D;
import nl.weeaboo.common.Rect2D;
import nl.weeaboo.lua2.io.LuaSerializable;
import nl.weeaboo.vn.ICamera;
import nl.weeaboo.vn.ICameraImage;
import nl.weeaboo.vn.IGeometryShader;
import nl.weeaboo.vn.IImageDrawable;
import nl.weeaboo.vn.IImageFxLib;
import nl.weeaboo.vn.ILayer;
import nl.weeaboo.vn.ITexture;
import nl.weeaboo.vn.math.Matrix;
import nl.weeaboo.vn.math.MutableMatrix;

@LuaSerializable
public class Camera implements ICamera {

	private static final long serialVersionUID = BaseImpl.serialVersionUID;
	
	private final IImageFxLib fxlib;
	private final BlurImageCache blurImageCache;
	
	private List<Image> images;
	
	private Dim2D screen;
	private boolean perspectiveTransform;
	private double subjectDistance;
	private double znear, zfar;
	private Lens lens;
	private int blurLevels, blurKernelSize;
	private double blurScale;
	
	private Rect2D zoom;
	private double z;
	
	public Camera(BaseImageFxLib fxlib, ILayer layer) {
		this.fxlib = fxlib;
		this.blurImageCache = new BlurImageCache(fxlib);
		this.screen = new Dim2D(layer.getWidth(), layer.getHeight());
				
		images = new ArrayList<Image>();
		
		perspectiveTransform = true;
		subjectDistance = 1;
		znear = 0.001;
		zfar = Double.POSITIVE_INFINITY;
		lens = new Lens(.055, 16, .00002);
		blurLevels = 3;
		blurKernelSize = 4; //Prefer powers of two, they're slightly faster
		blurScale = 1;
		
		zoom = new Rect2D(0, 0, screen.w, screen.h);
	}

	//Functions
	@Override
	public void destroy() {
		for (Image image : images) {
			image.destroy();
		}
		images.clear();
	}

	public void reset() {
		blurImageCache.clear();
	}

	public ICameraImage add(IImageDrawable id) {
		return add(id, 1, false, false);
	}
	
	@Override
	public ICameraImage add(IImageDrawable id, double depth, boolean mipmapFast, boolean noAutoScale) {
		if (id == null) throw new IllegalArgumentException("Adding null is not allowed");
		
		BlurGS shader = new BlurGS(fxlib, blurImageCache);
		shader.setLevels(blurLevels);
		shader.setKernelSize(blurKernelSize);
		shader.setExtendDirs(2468);
		shader.setInterpolate(!mipmapFast);
		
		Image image = new Image(id, !noAutoScale, shader);
		image.setDepth(depth);
		images.add(image);
		return image;
	}
	
	@Override
	public void remove(IImageDrawable id) {
		List<ICameraImage> remove = new ArrayList<ICameraImage>();
		for (Image image : images) {
			if (image.getDrawable().equals(id)) {
				remove.add(image);
			}
		}
		for (ICameraImage image : remove) {
			remove(image);
		}
	}

	@Override
	public void remove(ICameraImage ci) {
		for (Iterator<Image> itr = images.iterator(); itr.hasNext(); ) {
			Image image = itr.next();
			if (image.equals(ci)) {
				image.destroy();
				itr.remove();
			}
		}
	}
	
	@Override
	public void setZoom3D(double depth, double x, double y) {
		setZoom(new Rect2D(x, y, screen.w, screen.h), depth);
	}
	
	@Override
	public void setZoom2D(double x, double y, double w, double h) {
		setZoom(new Rect2D(x, y, w, h), 0);
	}
	
	private void setZoom(Rect2D rect, double z) {
		this.zoom = rect;
		this.z = z;

		applyTransform();
	}
		
	public void applyTransform() {
		Matrix m = getTransformMatrix(zoom, screen);
		for (Image image : images) {
			applyTransform(image, m, z);
		}		
	}
	protected void applyTransform(Image image) {
		Matrix baseTransform = getTransformMatrix(zoom, screen);
		applyTransform(image, baseTransform, z);
	}
	protected void applyTransform(Image image, Matrix baseTransform, double cameraZ) {
		double z = image.getDepth();
		IImageDrawable drawable = image.getDrawable();

		MutableMatrix mm = new MutableMatrix();			

		if (Math.abs(blurScale) > 0.0001) {
			double baseScale = Math.min(baseTransform.getScaleX(), baseTransform.getScaleY());
			lens.setSubjectDistance(subjectDistance * baseScale);

			double blur = lens.getBlur(baseScale * Math.abs(z-cameraZ), blurScale);
			//System.out.println(blur + " " + (blurScale * blur) + " " + baseScale + " " + lens.getDOFNear() + "|" + lens.getDOFFar());
			image.setActiveTexture(blur);
		} else {
			image.setActiveTexture(0);
		}

		mm.mul(baseTransform);
		
		double sx = image.getExtraScaleX();
		double sy = image.getExtraScaleY();
		if (perspectiveTransform) {
			double scale = 0;
			if (z > cameraZ+znear && z < cameraZ+zfar) {
				scale = 1 / (z-cameraZ);
			}
			
			sx *= image.getScale() * scale;
			sy *= image.getScale() * scale;
			//System.out.println(z + " " + scale + " " + sx);
		}

		double px = screen.w/2; //image.getWidth()/2;
		double py = screen.h/2; //image.getHeight()/2;
		mm.translate(px, py);
		mm.scale(sx, sy);
		mm.translate(-px, -py);
		
		mm.mul(image.getInitialTransform());
		drawable.setBaseTransform(mm.immutableCopy());
	}

	protected void invalidateTransform() {
		applyTransform();
	}
	protected void invalidateBlur() {
		blurImageCache.clear(); //Prevent the blur image cache from growing too large
		applyTransform();
	}
	
	//Getters		
	protected Matrix getTransformMatrix(Rect2D zoom, Dim2D screen) {
		double tx = zoom.x;
		double ty = zoom.y;
		double sx = (zoom.w > 0 ? screen.w / zoom.w : 0);
		double sy = (zoom.h > 0 ? screen.h / zoom.h : 0);
		
		MutableMatrix mm = new MutableMatrix();
		mm.scale(Math.min(sx, sy));
		mm.translate(-tx, -ty);
		return mm.immutableCopy();		
	}
	
	@Override
	public Rect2D getZoom() {
		return zoom;
	}

	@Override
	public double getZ() {
		return z;
	}
	
	@Override
	public double getSubjectDistance() {
		return subjectDistance;
	}
	
	@Override
	public boolean getPerspectiveTransform() {
		return perspectiveTransform;
	}

	@Override
	public double getScreenWidth() {
		return screen.w;
	}

	@Override
	public double getScreenHeight() {
		return screen.h;
	}

	@Override
	public double getBlurScale() {
		return blurScale;
	}

	@Override
	public int getBlurLevels() {
		return blurLevels;
	}

	@Override
	public int getBlurKernelSize() {
		return blurKernelSize;
	}
	
	//Setters
	@Override
	public void setPerspectiveTransform(boolean p) {
		if (perspectiveTransform != p) {
			perspectiveTransform = p;
			invalidateTransform();
		}
	}

	@Override
	public void setScreenSize(double w, double h) {
		if (w <= 0 || h <= 0) {
			throw new IllegalArgumentException("Screen dimensions must be > 0 (" + w + "x" + h + ")");
		}
		
		if (screen.w != w || screen.h != h) {
			screen = new Dim2D(w, h);
			invalidateTransform();
		}
	}

	@Override
	public void setBlurLevels(int l) {
		if (blurLevels != l) {
			blurLevels = l;
			invalidateBlur();
		}
	}

	@Override
	public void setBlurKernelSize(int k) {
		if (blurKernelSize != k) {
			blurKernelSize = k;
			invalidateBlur();
		}
	}
	
	@Override
	public void setSubjectDistance(double dist) {
		if (subjectDistance != dist) {
			subjectDistance = dist;		
			invalidateBlur();
		}
	}
	
	public void setBlurScale(double s) {
		if (blurScale != s) {
			blurScale = s;
			invalidateBlur();
		}
	}
	
	//Inner Classes
	@LuaSerializable
	protected static class Image implements ICameraImage {
		
		private static final long serialVersionUID = Camera.serialVersionUID;
		
		private final IImageDrawable id;
		private final boolean autoScale;
		private final Matrix initialTransform;
		private final ITexture initialTexture;
		private final IGeometryShader initialGS;
		
		private final BlurGS shader;

		private double depth;
		
		public Image(IImageDrawable id, boolean autoScale, BlurGS blurShader) {
			this.id = id;
			this.autoScale = autoScale;
			this.initialTransform = id.getBaseTransform();
			this.initialTexture = id.getTexture();
			this.initialGS = id.getGeometryShader();
			this.shader = blurShader;
			
			id.setGeometryShader(shader);
			shader.setBaseTexture(initialTexture);
		}
		
		//Functions
		@Override
		public void destroy() {
			id.setBaseTransform(initialTransform);
			id.setTexture(initialTexture);
			id.setGeometryShader(initialGS);
		}
		
		//Getters
		@Override
		public IImageDrawable getDrawable() { return id; }

		@Override
		public double getDepth() { return depth; }

		protected double getWidth() { return id.getWidth(); }
		protected double getHeight() { return id.getHeight(); }
		protected double getScale() { return autoScale ? getDepth() : 1; }
		protected double getExtraScaleX() { return 1; /*shader.getPaddingScaleX();*/ }
		protected double getExtraScaleY() { return 1; /*shader.getPaddingScaleY();*/ }
		protected Matrix getInitialTransform() { return initialTransform; }
		
		//Setters
		protected void setActiveTexture(double index) {			
			shader.setActiveTexture(index);
		}
		
		@Override
		public void setDepth(double d) {
			depth = d;
		}
		
	}
	
	@LuaSerializable
	protected static class Lens implements Serializable {
	
		private static final long serialVersionUID = Camera.serialVersionUID;
		
		private final double focalLength;
		private final double hyperFocalDistance;
		
		private double dofNear, dofFar;
		private double subjectDistance;
		
		public Lens(double f, int fNumber, double circleOfConfusion) {
			this.focalLength = f;
			this.hyperFocalDistance = (f*f) / (fNumber*circleOfConfusion) + f;
			
			this.subjectDistance = 1;
			calculateDOF();
		}
		
		//Functions
		private void calculateDOF() {
			final double H = hyperFocalDistance;
			final double f = focalLength;
			final double s = subjectDistance;
			
			dofNear = ((H-f)*s) / (H+s-2*f);
			dofFar = (s < H ? ((H-f)*s) / (H-s) : Double.POSITIVE_INFINITY);
		}
		
		//Getters
		public double getDOFNear() { return dofNear; }
		public double getDOFFar() { return dofFar; }
		
		public double getBlur(double distance, double blurScale) {
			double near = getDOFNear();
			near = subjectDistance - (subjectDistance-near) / blurScale;
			
			double far = getDOFFar();
			far = subjectDistance + (far-subjectDistance) / blurScale;
			
			double blur;
			if (distance <= subjectDistance) {
				double range = subjectDistance - near;
				blur = (subjectDistance - distance) / range;
			} else {
				double range = far - subjectDistance;
				blur = (distance - subjectDistance) / range;
			}
			
			//Earlier calculation returns 1.0 for edge of DoF, but everything within DoF should return 0.0
			return Math.max(0, Math.abs(blur));
		}
		
		//Setters
		public void setSubjectDistance(double s) {
			if (subjectDistance != s) {
				subjectDistance = s;
				calculateDOF();				
			}
		}		
	}
	
}
