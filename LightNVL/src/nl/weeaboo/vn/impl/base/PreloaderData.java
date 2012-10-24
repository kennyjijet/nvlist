package nl.weeaboo.vn.impl.base;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;

import nl.weeaboo.lua2.io.LuaSerializable;
import nl.weeaboo.vn.IMediaPreloader;
import nl.weeaboo.vn.MediaFile;
import nl.weeaboo.vn.MediaFile.MediaType;

@LuaSerializable
public class PreloaderData implements Externalizable, IMediaPreloader {
	
	private static final int VERSION = 2;
	private static final long serialVersionUID = VERSION;

	private static final MediaFile[] EMPTY = new MediaFile[0];
	
	//--- Manual serialization, don't add/change properties ---------------
	private Map<String, FileInfo> fileMeta;
	//--- Manual serialization, don't add/change properties ---------------
	
	public PreloaderData() {		
		fileMeta = new HashMap<String, FileInfo>();
	}
	
	//Functions
	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeInt(VERSION);
		out.writeInt(fileMeta.size());
		for (Entry<String, FileInfo> entry : fileMeta.entrySet()) {
			out.writeUTF(entry.getKey());
			out.writeObject(entry.getValue());
		}
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		int version = in.readInt();
		if (version != VERSION) {
			throw new IOException("Invalid file version, expected " + VERSION + ", got " + version);
		}
		
		fileMeta.clear();
		int fileMetaLength = in.readInt();
		for (int n = 0; n < fileMetaLength; n++) {
			String key = in.readUTF();
			FileInfo value = (FileInfo)in.readObject();
			fileMeta.put(key, value);
		}
	}
		
	public void addScript(String filename, int line, String image, float probability) {
		addMedia(filename, line, MediaType.SCRIPT, image, probability);
	}	
	public void addImage(String filename, int line, String image, float probability) {
		addMedia(filename, line, MediaType.IMAGE, image, probability);
	}	
	public void addSound(String filename, int line, String sound, float probability) {
		addMedia(filename, line, MediaType.SOUND, sound, probability);
	}
	public void addMedia(String filename, int line, MediaType type, String mediaFilename, float probability) {
		addMedia(filename,  new MediaFile(type, mediaFilename, line, probability));
	}	
	protected void addMedia(String filename, MediaFile m) {
		FileInfo info = fileMeta.get(filename);
		if (info == null) {
			info = new FileInfo();
			fileMeta.put(filename, info);
		}
		info.add(m);
	}
	
	public void addAll(PreloaderData data) {
		for (Entry<String, FileInfo> entry : data.fileMeta.entrySet()) {
			String filename = entry.getKey();
			FileInfo info = entry.getValue();
			for (MediaFile mf : info.media) {
				addMedia(filename, mf);
			}
		}
	}
	
	//Getters
	@Override
	public MediaFile[] getFutureMedia(String filename, int startLine, int endLine, float minProbability) {
		return getFutureMedia(null, filename, startLine, endLine, minProbability);
	}
	
	/**
	 * @param type Only return mediafiles of this type. Use <code>null</code> to
	 *        return all types of media file.
	 */
	protected MediaFile[] getFutureMedia(MediaType type, String filename, int startLine, int endLine,
			float minProbability)
	{
		FileInfo info = fileMeta.get(filename);
		if (info == null) return EMPTY;
		
		Collection<MediaFile> c = new PriorityQueue<MediaFile>(16, MediaFile.COMPARATOR);
		for (int line = startLine; line < endLine; line++) {
			info.get(c, type, line, minProbability);
		}
		return c.toArray(new MediaFile[c.size()]);
	}
	
	//Setters
	
	//Inner Classes
	@LuaSerializable
	private static class FileInfo implements Externalizable {
		
		private static final long serialVersionUID = 1L;
		
		//--- Manual serialization, don't add/change properties ---------------
		private Collection<MediaFile> media;
		//--- Manual serialization, don't add/change properties ---------------
		
		public FileInfo() {
			media = new ArrayList<MediaFile>();
		}
		
		public void add(MediaFile m) {
			//Remove existing entries for the same file, take their probability if it's higher.
			for (MediaFile stored : media) {
				if (m.getType() == stored.getType() && m.getFilename().equals(stored.getFilename())
						&& m.getLine() == stored.getLine())
				{
					m = MediaFile.merge(m, stored);					
					media.remove(stored);
					break; //Must break after modification, our iterator's invalid now!
				}
			}
			
			//Add media file to the collection
			media.add(m);
		}

		public int get(Collection<? super MediaFile> out, MediaType type, int line, float minProbability) {
			int t = 0;
			for (MediaFile m : media) {
				if (m.getLine() == line && (type == null || m.getType().equals(type))) {
					if (m.getProbability() >= minProbability) {
						out.add(m);
						t++;
					}
				}
			}
			return t;
		}
		
		@Override
		public void writeExternal(ObjectOutput out) throws IOException {
			out.writeInt(media.size());
			for (MediaFile mf : media) {
				out.writeObject(mf);
			}
		}

		@Override
		public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
			media.clear();

			int mediaLength = in.readInt();
			for (int n = 0; n < mediaLength; n++) {
				media.add((MediaFile)in.readObject());
			}
		}
	}
	
}
