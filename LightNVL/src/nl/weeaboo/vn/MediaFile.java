package nl.weeaboo.vn;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Comparator;

import nl.weeaboo.lua2.io.LuaSerializable;

@LuaSerializable
public final class MediaFile implements Externalizable {

	private static final long serialVersionUID = 1L;
	
	public static enum MediaType {
		IMAGE, SOUND, SCRIPT;
	}
	
	public static final Comparator<MediaFile> COMPARATOR = new Comparator<MediaFile>() {
		@Override
		public int compare(MediaFile a, MediaFile b) {
			if (a == null) return 1;
			if (b == null) return -1;
			
			if (a.line < b.line) return -1;
			if (a.line > b.line) return 1;
			
			if (a.probability > b.probability) return -1;
			if (a.probability < b.probability) return 1;
			
			int c = a.type.compareTo(b.type);
			if (c != 0) return c;
			
			return a.filename.compareTo(b.filename);
		}
	};
	
	//--- Manual serialization, don't add/change properties ---------------
	private MediaType type;
	private String filename;
	private int line;
	private float probability;
	//--- Manual serialization, don't add/change properties ---------------
	
	@Deprecated
	public MediaFile() {			
	}
	public MediaFile(MediaType t, String fn, int l, float p) {
		type = t;
		filename = fn;
		line = l;
		probability = p;
	}
	
	//Functions
	public static MediaFile merge(MediaFile a, MediaFile b) {
		MediaType type = a.type;
		String filename = a.filename;
		int line = a.line;
		float probability = Math.max(a.probability, b.probability);
		return new MediaFile(type, filename, line, probability);
	}
	
	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(type);
		out.writeUTF(filename);
		out.writeInt(line);
		out.writeFloat(probability);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		type = (MediaType)in.readObject();
		filename = in.readUTF();
		line = in.readInt();
		probability = in.readFloat();
	}
	
	@Override
	public String toString() {
		return String.format("%s(type=%s, file=%s, p=%.2f)", getClass().getSimpleName(),
				type, filename, probability);
	}
	
	//Getters
	public MediaType getType() {
		return type;
	}
	public String getFilename() {
		return filename;
	}
	public int getLine() {
		return line;
	}
	public float getProbability() {
		return probability;
	}
	
}