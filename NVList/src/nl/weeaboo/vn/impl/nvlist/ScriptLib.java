package nl.weeaboo.vn.impl.nvlist;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Collection;

import nl.weeaboo.filemanager.FileManagerView;
import nl.weeaboo.filemanager.IFileManager;
import nl.weeaboo.io.EnvironmentSerializable;
import nl.weeaboo.lua2.io.LuaSerializable;
import nl.weeaboo.vn.INotifier;
import nl.weeaboo.vn.impl.lua.BaseScriptLib;
import nl.weeaboo.vn.impl.lua.LuaNovel;

@LuaSerializable
public class ScriptLib extends BaseScriptLib implements Serializable {

	private final FileManagerView fm;
	private final EnvironmentSerializable es;
	
	public ScriptLib(IFileManager fm, INotifier ntf) {
		super(ntf);
		
		this.fm = new FileManagerView(fm, "script/");
		this.es = new EnvironmentSerializable(this);
	}
	
	//Functions
	private Object writeReplace() throws ObjectStreamException {	
		return es.writeReplace();
	}
	
	protected boolean getScriptExists(String normalizedFilename) {
		return LuaNovel.isBuiltInScript(normalizedFilename)
			|| fm.getFileExists(normalizedFilename);
	}
	
	@Override
	protected InputStream openExternalScriptFile(String filename) throws IOException {
		return fm.getInputStream(filename);
	}
	
	//Getters
	@Override
	protected long getExternalScriptModificationTime(String filename) throws IOException {
		return fm.getFileModifiedTime(filename);
	}

	@Override
	protected void getExternalScriptFiles(Collection<String> out, String folder) {
		try {
			out.addAll(fm.getFolderContents(folder, true));
		} catch (IOException e) {
			notifier.d("Folder doesn't exist or can't be read: " + folder, e);
		}
	}
	
	//Setters
	
}
