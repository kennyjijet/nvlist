package nl.weeaboo.vn;

import java.io.Serializable;

public interface IDistortGrid extends Serializable {

	public float getDistortX(int x, int y);
	public float getDistortY(int x, int y);
	
	public int getWidth();
	public int getHeight();
	
}
