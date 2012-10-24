package nl.weeaboo.vn;

import java.io.Serializable;

public interface ILooper extends Serializable {

	public boolean update(double effectSpeed);
	
	public double getTime();
	public double getNormalizedTime();
	public double getDuration();
	
	public void setNormalizedTime(double time);
	public void setTime(double time);
	
}
