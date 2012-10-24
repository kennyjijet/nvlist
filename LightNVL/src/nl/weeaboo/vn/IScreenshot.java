package nl.weeaboo.vn;

import java.io.Serializable;

public interface IScreenshot extends Serializable {

	public void cancel();
	public void makeTransient();
	
	public boolean isAvailable();
	public boolean isCancelled();
	
	public int getScreenWidth();
	public int getScreenHeight();
	
	public int[] getARGB();
	public int getWidth();
	public int getHeight();
	public short getZ();
	
}
