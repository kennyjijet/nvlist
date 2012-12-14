package nl.weeaboo.vn;

public interface ISaveInfo {

	public int getSlot();
	public String getTitle();
	public String getLabel();
	public String getDateString();
	public long getTimestamp();	
	public IScreenshot getScreenshot(int maxW, int maxH);
	public IStorage getMetaData();
	
}
