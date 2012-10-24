package nl.weeaboo.vn;

import java.io.Serializable;

public interface ICameraImage extends Serializable {

	//Functions
	public void destroy();
	
	//Getters
	public IDrawable getDrawable();
	public double getDepth();
	
	//Setters
	public void setDepth(double d);
	
}
