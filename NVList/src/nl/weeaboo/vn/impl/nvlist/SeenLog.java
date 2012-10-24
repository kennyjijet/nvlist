package nl.weeaboo.vn.impl.nvlist;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectStreamException;
import java.io.OutputStream;
import java.io.Serializable;

import nl.weeaboo.filemanager.FileManager;
import nl.weeaboo.io.EnvironmentSerializable;
import nl.weeaboo.lua2.io.LuaSerializable;
import nl.weeaboo.vn.impl.base.BaseSeenLog;

@LuaSerializable
public class SeenLog extends BaseSeenLog implements Serializable {

	private final EnvironmentSerializable es;
	private final FileManager fm;
	
	public SeenLog(FileManager fm, String filename) {
		super(filename);
		
		this.fm = fm;
		this.es = new EnvironmentSerializable(this);
	}
	
	//Functions
	private Object writeReplace() throws ObjectStreamException {	
		return es.writeReplace();
	}
	
	@Override
	protected InputStream openInputStream(String filename) throws IOException {
		if (fm.getFileExists(filename) && fm.getFileSize(filename) > 0) {
			//Open normal file if exists and non-empty
			return fm.getInputStream(filename);
		} else {
			//Go for the backup if the normal file was invalid
			return fm.getInputStream(filename+".bak");
		}
	}

	@Override
	protected OutputStream openOutputStream(String filename) throws IOException {
		return fm.getOutputStream(filename);
	}

	@Override
	protected boolean rename(String oldFilename, String newFilename) {
		return fm.rename(oldFilename, newFilename);
	}
	
	@Override
	protected boolean delete(String filename) {
		return fm.delete(filename);
	}
	
	//Getters
	
	//Setters
	
}
