package nl.weeaboo.vn.impl.base;

import java.io.IOException;
import java.io.Serializable;

import nl.weeaboo.vn.INotifier;
import nl.weeaboo.vn.ISeenLog;
import nl.weeaboo.vn.IVideo;
import nl.weeaboo.vn.IVideoFactory;

public abstract class BaseVideoFactory extends BaseMediaFactory implements IVideoFactory, Serializable {

	private static final long serialVersionUID = BaseImpl.serialVersionUID;
	
	public BaseVideoFactory(ISeenLog sl, INotifier ntf) {
		super(new String[] {"ogv"}, sl, ntf);
		
		if (sl instanceof BaseSeenLog) {
			((BaseSeenLog)sl).setVideoFactory(this);
		}
	}
	
	//Functions
	@Override
	public final IVideo movie(String filename) throws IOException {
		String normalized = normalizeFilename(filename);
		if (normalized == null) {
			notifier.d("Unable to find video file: " + filename);
			return null;
		}
		seenLog.addVideo(normalized);		
		return movieNormalized(normalized);
	}
	
	protected abstract IVideo movieNormalized(String filename) throws IOException;

	//Getters
	
	//Setters
	
}
