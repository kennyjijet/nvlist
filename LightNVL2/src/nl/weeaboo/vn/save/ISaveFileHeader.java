package nl.weeaboo.vn.save;


public interface ISaveFileHeader {

	public String getTitle();
	public String getLabel();

	public long getTimestamp();
    public String getDateString();

    public IStorage getUserData();

}
