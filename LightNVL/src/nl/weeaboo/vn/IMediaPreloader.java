package nl.weeaboo.vn;


public interface IMediaPreloader {
	
	public MediaFile[] getFutureMedia(String filename, int startLine, int endLine, float minProbability);
	
}
