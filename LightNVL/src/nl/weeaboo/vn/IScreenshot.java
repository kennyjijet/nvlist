package nl.weeaboo.vn;

import java.io.Serializable;

public interface IScreenshot extends Serializable {

	public void cancel();
	
	/**
	 * Marks this screenshot object as transient; its pixels won't be serialized.
	 */
	public void markTransient();
	
	@Deprecated
	public void makeTransient();
	
	public boolean isAvailable();
	public boolean isVolatile();
	public boolean isTransient();
	public boolean isCancelled();
	
	public int getScreenWidth();
	public int getScreenHeight();
	
	/**
	 * Warning: May return <code>null</code>. 
	 */
	public int[] getPixels();
	public int getPixelsWidth();
	public int getPixelsHeight();
	
	/**
	 * Warning: May return <code>null<code>. 
	 */
	public ITexture getVolatilePixels();
	
	public short getZ();
	
}
