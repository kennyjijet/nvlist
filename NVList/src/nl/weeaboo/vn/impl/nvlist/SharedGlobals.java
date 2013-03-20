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
import nl.weeaboo.vn.impl.base.BasePersistentStorage;

@LuaSerializable
public class SharedGlobals extends BasePersistentStorage implements Serializable {

	private final SecureFileWriter fs;
	private final INotifier notifier;	
	private final EnvironmentSerializable es;
	
	private boolean autoSave;
	
	public SharedGlobals(SecureFileWriter fs, String filename, INotifier ntf) {
		super(filename);
		
		this.fs = fs;
		this.notifier = ntf;
		this.autoSave = true;
		
		this.es = new EnvironmentSerializable(this);
	}
	
	//Functions
	private Object writeReplace() throws ObjectStreamException {	
		return es.writeReplace();
	}
	
	@Override
	protected void onChanged() {
		if (autoSave) {
			try {
				save();
			} catch (IOException ioe) {
				notifier.w("Error saving persistent storage", ioe);
			}
		}
	}

	@Override
	protected InputStream openInputStream(String filename) throws IOException {
		return fs.newInputStream(filename);
	}

	@Override
	protected OutputStream openOutputStream(String filename) throws IOException {
		return fs.newOutputStream(filename, false);
	}

	//Getters
	public boolean getAutoSave() {
		return autoSave;
	}
	
	//Setters
	public void setAutoSave(boolean as) {
		autoSave = as;
	}
	
}
