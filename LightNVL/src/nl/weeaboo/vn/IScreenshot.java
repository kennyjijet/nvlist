package nl.weeaboo.vn;

import java.io.Serializable;

public interface IScreenshot extends Serializable {

	public void cancel();
	
	/**
	 * Marks this screenshot object as transient; its pixels won't be serialized.
	 */
	public void markTransient();
	
	/**
	 * Marks this screenshot object as volatile; its backing pixels may disappear at any time.
	 */
	public void markVolatile();
	
	public boolean isAvailable();
	public boolean isCancelled();
	
	public int getScreenWidth();
	public int getScreenHeight();
	
	public int getWidth();
	public int getHeight();
	public short getZ();
	
}
