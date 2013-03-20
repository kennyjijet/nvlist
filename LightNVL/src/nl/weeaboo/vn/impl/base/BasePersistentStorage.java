package nl.weeaboo.vn.impl.base;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import nl.weeaboo.vn.IPersistentStorage;

public abstract class BasePersistentStorage extends BaseStorage implements IPersistentStorage {

	private static final long serialVersionUID = BaseImpl.serialVersionUID;

	private static final int VERSION = 257;

	private final String filename;
	private boolean compressed = true;
	private long timestamp;
	
	protected BasePersistentStorage(String filename) {
		this.filename = filename;
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
			throw new IOException("Unsupported persistent storage save version: " + version);
		}
		
		compressed = din.readBoolean();
		timestamp = din.readLong();
	}
	
	@Override
	public final void save() throws IOException {
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
			ioe.printStackTrace();
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
	
	protected abstract InputStream openInputStream(String filename) throws IOException;	
	protected abstract OutputStream openOutputStream(String filename) throws IOException;
	
	//Getters
	
	//Setters
	
}
