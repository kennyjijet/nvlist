package nl.weeaboo.vn.layout;

import java.io.Serializable;

public interface ILayoutComponent extends Serializable {
	
	public double getX();
	public double getY();
	public double getWidth();
	public double getHeight();
	public ILayoutConstraints getConstraints();
	
	public void setX(double x);
	public void setY(double y);
	public void setWidth(double w);
	public void setHeight(double h);
	public void setPos(double x, double y);
	public void setSize(double w, double h);
	public void setBounds(double x, double y, double w, double h);
	
}
