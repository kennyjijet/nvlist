package nl.weeaboo.vn.impl.nvlist;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectStreamException;
import java.io.OutputStream;
import java.io.Serializable;

import nl.weeaboo.filemanager.FileManager;
import nl.weeaboo.io.EnvironmentSerializable;
import nl.weeaboo.lua2.io.LuaSerializable;
import nl.weeaboo.vn.INotifier;
import nl.weeaboo.vn.impl.base.BasePersistentStorage;

@LuaSerializable
public class SharedGlobals extends BasePersistentStorage implements Serializable {

	private final FileManager fm;
	private final INotifier notifier;
	private final EnvironmentSerializable es;
	
	public SharedGlobals(FileManager fm, String filename, INotifier ntf) {
		super(filename);
		
		this.fm = fm;
		this.notifier = ntf;	
		
		this.es = new EnvironmentSerializable(this);
	}
	
	//Functions
	private Object writeReplace() throws ObjectStreamException {	
		return es.writeReplace();
	}
	
	@Override
	protected void onChanged() {
		try {
			save();
		} catch (IOException ioe) {
			notifier.w("Error saving persistent storage", ioe);
		}
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
