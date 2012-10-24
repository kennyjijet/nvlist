package nl.weeaboo.vn.impl.base;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import nl.weeaboo.vn.IScreenshot;

public abstract class BaseScreenshot implements IScreenshot {

	private static final long serialVersionUID = BaseImpl.serialVersionUID;
	
	private final short z;
	private boolean cancelled;	
	private transient int[] argb;	
	private int width, height;
	private int screenWidth, screenHeight;
	private boolean isTransient;
	
	protected BaseScreenshot(short z) {
		this.z = z;
	}
	
	//Functions
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
		
		if (!isTransient) {
			serializePixels(out, argb);
		}
	}
	
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		
		if (!isTransient) {
			argb = deserializePixels(in);
		}
	}
	
	protected void serializePixels(ObjectOutputStream out, int argb[]) throws IOException {
		out.writeObject(argb);
	}
	
	protected int[] deserializePixels(ObjectInputStream in) throws IOException, ClassNotFoundException {
		return (int[])in.readObject();
	}
	
	@Override
	public void makeTransient() {
		isTransient = true;
	}
	
	@Override
	public void cancel() {
		cancelled = true;
	}
	
	@Override
	public String toString() {
		return String.format("%s[%dx%d]", getClass().getSimpleName(), width, height);
	}
	
	//Getters
	@Override
	public boolean isAvailable() {
		return !isCancelled() && argb != null;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}
	
	@Override
	public int getWidth() { return width; }
	
	@Override
	public int getHeight() { return height; }
	
	@Override
	public short getZ() { return z; }
	
	@Override
	public int getScreenWidth() { return screenWidth; }
	
	@Override
	public int getScreenHeight() { return screenHeight; }

	@Override
	public int[] getARGB() { return argb; }
	
	protected final boolean isTransient() {
		return isTransient;
	}
	
	//Setters
	public void set(int[] argb, int w, int h, int screenWidth, int screenHeight) {
		this.argb = argb;
		this.width = w;
		this.height = h;
		this.screenWidth = screenWidth;
		this.screenHeight = screenHeight;
	}
	
}
