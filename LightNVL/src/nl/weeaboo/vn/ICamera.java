package nl.weeaboo.vn;

import java.io.Serializable;

import nl.weeaboo.common.Rect2D;

public interface ICamera extends Serializable {

	//Functions
	public void destroy();
	public ICameraImage add(IImageDrawable id, double depth, boolean mipmapFast, boolean noAutoScale);
	public void remove(ICameraImage ci);
	public void remove(IImageDrawable id);
	
	//Getters
	public Rect2D getZoom();
	public double getZ();
	public boolean getPerspectiveTransform();
	public double getSubjectDistance();
	public double getScreenWidth();
	public double getScreenHeight();
	public double getBlurScale();
	public int getBlurLevels();
	public int getBlurKernelSize();
	
	//Setters
	public void setZoom3D(double depth, double x, double y);
	public void setZoom2D(double x, double y, double w, double h);
	public void setPerspectiveTransform(boolean p);
	public void setSubjectDistance(double depth);
	public void setScreenSize(double w, double h);
	public void setBlurScale(double s);
	public void setBlurLevels(int l);
	public void setBlurKernelSize(int k);	
	
}
