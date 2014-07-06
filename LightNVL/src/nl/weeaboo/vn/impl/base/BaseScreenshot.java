package nl.weeaboo.vn.impl.base;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import nl.weeaboo.vn.IScreenshot;
import nl.weeaboo.vn.ITexture;

public abstract class BaseScreenshot implements IScreenshot {

	private static final long serialVersionUID = BaseImpl.serialVersionUID;
	
	private final short z;
	private final boolean isVolatile;
	
	private boolean cancelled;	
	private transient int[] pixels;
	private int pixelsWidth, pixelsHeight;
	private transient ITexture volatilePixels;
	private int screenWidth, screenHeight;
	protected boolean isAvailable;
	private boolean isTransient;
	
	protected BaseScreenshot(short z, boolean isVolatile) {
		this.z = z;
		this.isVolatile = isVolatile;
	}
	
	//Functions
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
		
		if (!isTransient && !isVolatile) {
			serializePixels(out, pixels, pixelsWidth, pixelsHeight);
		}
	}
	
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		
		if (!isTransient && !isVolatile) {
			pixels = deserializePixels(in, pixelsWidth, pixelsHeight);
		}
	}
	
	protected void serializePixels(ObjectOutputStream out, int argb[], int w, int h) throws IOException {
		out.writeObject(argb);
	}
	
	protected int[] deserializePixels(ObjectInputStream in, int w, int h) throws IOException, ClassNotFoundException {
		return (int[])in.readObject();
	}
	
	@Override
	public void markTransient() {
		isTransient = true;
	}
	
	@Override
	@Deprecated
	public final void makeTransient() {
		markTransient();
	}
	
	@Override
	public void cancel() {
		cancelled = true;
	}
	
	@Override
	public String toString() {
		return String.format("%s[%dx%d]", getClass().getSimpleName(), pixelsWidth, pixelsHeight);
	}
	
	//Getters
	@Override
	public boolean isAvailable() {
		return !isCancelled() && isAvailable;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public short getZ() {
		return z;
	}

	@Override
	public int getScreenWidth() {
		return screenWidth;
	}

	@Override
	public int getScreenHeight() {
		return screenHeight;
	}
	
	@Override
	public final boolean isTransient() {
		return isTransient;
	}
	
	@Override
	public final boolean isVolatile() {
		return isVolatile;
	}
	
	@Override
	public int[] getPixels() {
		return pixels;
	}	

	@Override
	public int getPixelsWidth() {
		return pixelsWidth;
	}

	@Override
	public int getPixelsHeight() {
		return pixelsHeight;
	}
	
	@Override
	public ITexture getVolatilePixels() {
		return volatilePixels;
	}
	
	//Setters
	public void setPixels(int[] argb, int w, int h, int screenWidth, int screenHeight) {
		if (isVolatile) throw new IllegalStateException("Can't set non-volatile pixels on a volatile screenshot");
		
		this.pixels = argb;
		this.pixelsWidth = w;
		this.pixelsHeight = h;
		this.volatilePixels = null;
		this.screenWidth = screenWidth;
		this.screenHeight = screenHeight;
		this.isAvailable = true;
	}
	
	public void setVolatilePixels(ITexture tex, int screenWidth, int screenHeight) {
		if (!isVolatile) throw new IllegalStateException("Can't set volatile pixels on a non-volatile screenshot");

		this.pixels = null;
		this.pixelsWidth = 0;
		this.pixelsHeight = 0;
		this.volatilePixels = tex;
		this.screenWidth = screenWidth;
		this.screenHeight = screenHeight;
		this.isAvailable = true;
	}
	
}
