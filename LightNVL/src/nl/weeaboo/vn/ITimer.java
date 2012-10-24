package nl.weeaboo.vn;

import java.io.IOException;

public interface ITimer {

	public void update(IInput input);	
	public void load(IStorage storage) throws IOException;
	public void save(IStorage storage) throws IOException;
	public String formatTime(int seconds);
	
	public int getTotalTime();
	public int getIdleTime();
	
	public void setIdleTimeout(int seconds);
	
}
