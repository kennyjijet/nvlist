package nl.weeaboo.vn;

import java.io.Serializable;

public interface IVideoState extends Serializable {

	// === Functions ===========================================================
	
	public void add(IVideo d, boolean fullscreen);
	
	public void stopAll();
		
	public boolean update(IInput input);
		
	// === Getters =============================================================
	public boolean isPaused();
		
	public boolean isBlocking();
	
	public IVideo getBlocking();
	
	// === Setters =============================================================
	public void setMasterVolume(double vol);

	public void setPaused(boolean p);
	
}
