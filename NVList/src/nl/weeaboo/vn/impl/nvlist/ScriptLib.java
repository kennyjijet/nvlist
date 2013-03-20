package nl.weeaboo.vn.impl.nvlist;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Collection;

import nl.weeaboo.filesystem.FileSystemView;
import nl.weeaboo.filesystem.IFileSystem;
import nl.weeaboo.io.EnvironmentSerializable;
import nl.weeaboo.lua2.io.LuaSerializable;
import nl.weeaboo.vn.INotifier;
import nl.weeaboo.vn.impl.lua.BaseScriptLib;
import nl.weeaboo.vn.impl.lua.LuaNovel;

@LuaSerializable
public class ScriptLib extends BaseScriptLib implements Serializable {

	private final IFileSystem fs;
	private final EnvironmentSerializable es;
	
	public ScriptLib(IFileSystem fs, INotifier ntf) {
		super(ntf);
		
		this.fs = new FileSystemView(fs, "script/", true);
		this.es = new EnvironmentSerializable(this);
	}
	
	//Functions
	private Object writeReplace() throws ObjectStreamException {	
		return es.writeReplace();
	}
	
	protected boolean getScriptExists(String normalizedFilename) {
		return LuaNovel.isBuiltInScript(normalizedFilename)
			|| fs.getFileExists(normalizedFilename);
	}
	
	@Override
	protected InputStream openExternalScriptFile(String filename) throws IOException {
		return fs.newInputStream(filename);
	}
	
	//Getters
	@Override
	protected long getExternalScriptModificationTime(String filename) throws IOException {
		return fs.getFileModifiedTime(filename);
	}

	@Override
	protected void getExternalScriptFiles(Collection<String> out, String folder) {
		try {
			fs.getFiles(out, folder, true);
		} catch (IOException e) {
			notifier.d("Folder doesn't exist or can't be read: " + folder, e);
		}
	}
	
	//Setters
	
}
