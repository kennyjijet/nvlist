package nl.weeaboo.vn.impl.nvlist;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectStreamException;
import java.io.OutputStream;
import java.io.Serializable;

import nl.weeaboo.filesystem.SecureFileWriter;
import nl.weeaboo.io.EnvironmentSerializable;
import nl.weeaboo.lua2.io.LuaSerializable;
import nl.weeaboo.vn.INotifier;
import nl.weeaboo.vn.impl.base.BaseLoggingAnalytics;

@LuaSerializable
public class Analytics extends BaseLoggingAnalytics implements Serializable {
		
	private final SecureFileWriter fs;
	private final EnvironmentSerializable es;
	
	public Analytics(SecureFileWriter fs, String filename, INotifier ntf) {
		super(filename);
		
		this.fs = fs;
		this.es = new EnvironmentSerializable(this);
	}
	
	//Functions	
	private Object writeReplace() throws ObjectStreamException {	
		return es.writeReplace();
	}
	
	@Override
	protected InputStream openInputStream(String filename) throws IOException {
		return fs.newInputStream(filename);
	}

	@Override
	protected OutputStream openOutputStream(String filename, boolean append) throws IOException {
		return fs.newOutputStream(filename, append);
	}
	
	//Getters
	@Override
	protected long getFileSize(String filename) {
		try {
			return fs.getFileSize(filename);
		} catch (IOException e) {
			return 0L;
		}
	}
	
	//Setters
	
}
