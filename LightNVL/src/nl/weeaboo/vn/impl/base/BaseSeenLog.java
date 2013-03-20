package nl.weeaboo.vn.impl.base;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.Externalizable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import nl.weeaboo.collections.BloomFilter;
import nl.weeaboo.lua2.io.LuaSerializable;
import nl.weeaboo.vn.ISeenLog;

public abstract class BaseSeenLog implements ISeenLog {

	private static final long serialVersionUID = 1L;
	private static final double fpp = 1e-6;
	private static final int VERSION = 1;
	
	private final String filename;
	private boolean compressed = true;
	private long timestamp;

	private BaseImageFactory imageFactory;
	private BaseSoundFactory soundFactory;
	private BaseVideoFactory videoFactory;
	
	//--- Manual serialization, don't add/change properties ---------------
	private Map<String, ChoiceInfo> choiceSeen;
	private Map<String, LineInfo> linesSeen;
	private BloomFilter<String> imageSeen;
	private BloomFilter<String> soundSeen;
	private BloomFilter<String> videoSeen;
	//--- Manual serialization, don't add/change properties ---------------
	
	protected BaseSeenLog(String filename) {
		this.filename = filename;
		
		choiceSeen = new HashMap<String, ChoiceInfo>();
		linesSeen = new HashMap<String, LineInfo>();
		imageSeen = new BloomFilter<String>(fpp,  1000);
		soundSeen = new BloomFilter<String>(fpp, 10000);
		videoSeen = new BloomFilter<String>(fpp,   100);
	}
	
	//Functions	
	@Override
	public void load() throws IOException {		
		InputStream raw = null;
		ObjectInputStream in = null;
		try {
			raw = new BufferedInputStream(openInputStream(filename), 8<<10);
			readHeader(new DataInputStream(raw));
			if (compressed) {
				raw = new InflaterInputStream(raw);
			}			
			in = new ObjectInputStream(raw);
			load(in);
		} catch (ClassNotFoundException e) {
			throw new IOException("Class not found: " + e);
		} catch (FileNotFoundException fnfe) {
			//Ignore
		} finally {
			if (in != null) in.close();
			else if (raw != null) raw.close();
		}
	}
	
	protected void readHeader(DataInputStream din) throws IOException {
		int version = din.readInt();
		if (version != VERSION) {
			throw new IOException("Unsupported save version: " + version);
		}

		compressed = din.readBoolean();
		timestamp = din.readLong();
	}
			
	@SuppressWarnings("unchecked")
	protected void load(ObjectInputStream in) throws IOException, ClassNotFoundException {
		choiceSeen.clear();		
		int len = in.readInt();
		for (int n = 0; n < len; n++) {
			ChoiceInfo ci = (ChoiceInfo)in.readObject();
			choiceSeen.put(ci.getId(), ci);
		}
		
		linesSeen.clear();
		len = in.readInt();
		for (int n = 0; n < len; n++) {
			LineInfo li = (LineInfo)in.readObject();
			linesSeen.put(li.getFilename(), li);
		}
		
		imageSeen = (BloomFilter<String>)in.readObject();
		soundSeen = (BloomFilter<String>)in.readObject();
		videoSeen = (BloomFilter<String>)in.readObject();
	}

	@Override
	public void save() throws IOException {
		timestamp = System.currentTimeMillis();
		
		OutputStream raw = null;
		ObjectOutputStream out = null;
		try {
			raw = new BufferedOutputStream(openOutputStream(filename), 8<<10);
			writeHeader(new DataOutputStream(raw));
			if (compressed) {
				raw = new DeflaterOutputStream(raw);
			}
			
			out = new ObjectOutputStream(raw);
			save(out);
			out.flush();
		} catch (IOException ioe) {
			throw ioe;
		} finally {
			if (raw instanceof DeflaterOutputStream) {
				((DeflaterOutputStream)raw).finish();
			}

			if (out != null) out.close();
			else if (raw != null) raw.close();
		}
	}
	
	protected void writeHeader(DataOutputStream dout) throws IOException {
		dout.writeInt(VERSION);
		dout.writeBoolean(compressed);
		dout.writeLong(timestamp);		
	}
	
	protected void save(ObjectOutputStream out) throws IOException {
		out.writeInt(choiceSeen.size());
		for (ChoiceInfo ci : choiceSeen.values()) {
			out.writeObject(ci);
		}
		
		out.writeInt(linesSeen.size());
		for (LineInfo li : linesSeen.values()) {
			out.writeObject(li);
		}
		
		out.writeObject(imageSeen);
		out.writeObject(soundSeen);
		out.writeObject(videoSeen);
	}
	
	protected abstract InputStream openInputStream(String filename) throws IOException;	
	protected abstract OutputStream openOutputStream(String filename) throws IOException;
	
	@Override
	public void addImage(String filename) {
		String normalized = imageFactory.normalizeFilename(filename);
		if (normalized != null) {
			imageSeen.add(normalized);
		}
	}

	@Override
	public void addSound(String filename) {
		String normalized = soundFactory.normalizeFilename(filename);
		if (normalized != null) {
			soundSeen.add(normalized);
		}
	}

	@Override
	public void addVideo(String filename) {
		String normalized = videoFactory.normalizeFilename(filename);
		if (normalized != null) {
			videoSeen.add(normalized);
		}
	}
	
	@Override
	public void registerScriptFile(String filename, int numTextLines) {
		LineInfo li = linesSeen.get(filename);
		if (li != null && li.getNumTextLines() == numTextLines) {
			//File already registered and the registration is current
			return;
		}
		
		LineInfo newLineInfo = new LineInfo(filename, numTextLines);
		linesSeen.put(filename, newLineInfo);
		
		try {
			save();
		} catch (IOException e) {
			//Autosave failed, too bad
		}
	}
	
	//Getters
	@Override
	public boolean isChoiceSelected(String choiceId, int index) {
		if (index <= 0) throw new IllegalArgumentException("Choice selection index must be >= 1, given: " + index);
		if (index > 64) throw new IllegalArgumentException("Choice selection index must be <= 64, given: " + index);

		ChoiceInfo ci = choiceSeen.get(choiceId);
		return (ci != null ? ci.isSelected(index) : false);
	}
	
	@Override
	public boolean isTextLineRead(String filename, int textLineIndex) {
		LineInfo li = linesSeen.get(filename);
		if (li == null) {
			return false;
		}
		return li.isTextLineRead(textLineIndex);
	}
	
	@Override
	public boolean hasImage(String filename) {
		String normalized = imageFactory.normalizeFilename(filename);
		if (normalized == null) return false; 
		return imageSeen.contains(normalized);
	}

	@Override
	public boolean hasSound(String filename) {
		String normalized = soundFactory.normalizeFilename(filename);
		if (normalized == null) return false; 
		return soundSeen.contains(normalized);
	}

	@Override
	public boolean hasVideo(String filename) {
		String normalized = videoFactory.normalizeFilename(filename);
		if (normalized == null) return false; 
		return videoSeen.contains(normalized);
	}
	
	//Setters
	@Override
	public void setChoiceSelected(String choiceId, int index) {
		if (index <= 0) throw new IllegalArgumentException("Choice selection index must be >= 1, given: " + index);
		if (index > 64) throw new IllegalArgumentException("Choice selection index must be <= 64, given: " + index);
		
		ChoiceInfo ci = choiceSeen.get(choiceId);
		if (ci == null) {
			ci = new ChoiceInfo(choiceId);
			choiceSeen.put(choiceId, ci);
		}
		ci.setSelected(index);
	}
	
	@Override
	public void setTextLineRead(String filename, int textLineIndex) {
		LineInfo li = linesSeen.get(filename);
		if (li == null) {
			//throw new IllegalArgumentException("Script file ("+filename+") not registered in SeenLog, this should've happened automatically.");
			return;
		}
		li.setTextLineRead(textLineIndex);
	}
	
	void setImageFactory(BaseImageFactory imgfac) {
		imageFactory = imgfac;
	}
	void setSoundFactory(BaseSoundFactory sndfac) {
		soundFactory = sndfac;
	}
	void setVideoFactory(BaseVideoFactory vidfac) {
		videoFactory = vidfac;
	}
	
	//Inner Classes
	@LuaSerializable
	protected static class ChoiceInfo implements Externalizable {
		
		private static final long serialVersionUID = BaseSeenLog.serialVersionUID;

		//--- Manual serialization, don't add/change properties ---------------
		private String id;
		private long selected;
		//--- Manual serialization, don't add/change properties ---------------
		
		public ChoiceInfo() {			
		}
		public ChoiceInfo(String id) {
			this.id = id;			
		}
		
		//Functions
		@Override
		public void writeExternal(ObjectOutput out) throws IOException {
			out.writeUTF(id);
			out.writeLong(selected);
		}
		
		@Override
		public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
			id = in.readUTF();
			selected = in.readLong();
		}
		
		//Getters
		public String getId() {
			return id;
		}
		
		public boolean isSelected(int index) {
			if (index <= 0) throw new IllegalArgumentException("Choice selection index must be >= 1, given: " + index);
			if (index > 64) throw new IllegalArgumentException("Choice selection index must be <= 64, given: " + index);

			long bit = 1L << (index-1);
			return (selected & bit) != 0;
		}
		
		//Setters
		public void setSelected(int index) {			
			if (index <= 0) throw new IllegalArgumentException("Choice selection index must be >= 1, given: " + index);
			if (index > 64) throw new IllegalArgumentException("Choice selection index must be <= 64, given: " + index);
			
			long bit = 1L << (index-1);
			selected |= bit;
		}
		
	}

	@LuaSerializable
	protected static class LineInfo implements Externalizable {
		
		private static final long serialVersionUID = BaseSeenLog.serialVersionUID;
		private static final int MAX_SIZE = 64 << 10;
		
		//--- Manual serialization, don't add/change properties ---------------
		private String filename;
		private int numTextLines;
		private BitSet read;
		//--- Manual serialization, don't add/change properties ---------------
		
		public LineInfo() {
		}
		public LineInfo(String filename, int numTextLines) {
			this.filename = filename;
			this.numTextLines = numTextLines;
			
			read = new BitSet(numTextLines);
		}
		
		//Functions		
		@Override
		public void writeExternal(ObjectOutput out) throws IOException {
			out.writeUTF(filename);
			out.writeInt(numTextLines);
			out.writeObject(read);
		}
		
		@Override
		public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
			filename = in.readUTF();
			numTextLines = in.readInt();
			read = (BitSet)in.readObject();
		}
		
		@Override
		public String toString() {
			return String.format("%s [filename=%s, lines=%d, read=%d]",
				getClass().getSimpleName(), filename, numTextLines, read.cardinality());
		}
		
		//Getters
		public String getFilename() {
			return filename;
		}
		public int getNumTextLines() {
			return numTextLines;
		}
		
		public boolean isTextLineRead(int index) {
			if (index <= 0 || index > read.size()) {
				return true;
			}
			return read.get(index-1);
		}
		
		//Setters
		public void setTextLineRead(int index) {			
			if (index <= 0) throw new IllegalArgumentException("Line index must be >= 1, given: " + index);						
			assert numTextLines < 0 || index <= read.length();
			if (index >= MAX_SIZE) throw new IllegalArgumentException("Line index >= MAX_SIZE, given: " + index + ", MAX_SIZE: " + MAX_SIZE);
			
			if (index > read.size()) {
				read = new BitSet(Math.max(index, read.size() * 2));
			}
			read.set(index-1);
		}
		
	}
	
}
