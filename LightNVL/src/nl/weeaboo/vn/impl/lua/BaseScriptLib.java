package nl.weeaboo.vn.impl.lua;

import static org.luaj.vm2.LuaValue.valueOf;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import nl.weeaboo.vn.INotifier;
import nl.weeaboo.vn.IScriptLib;
import nl.weeaboo.vn.parser.LVNParser;
import nl.weeaboo.vn.parser.ParserUtil;

import org.luaj.vm2.LuaString;
import org.luaj.vm2.lib.PackageLib;

public abstract class BaseScriptLib implements IScriptLib, Serializable {

	private static final long serialVersionUID = LuaImpl.serialVersionUID;

	private static final LuaString PATH = valueOf("path");
	
	protected final INotifier notifier;
	private String engineVersion = "1.0";
	
	public BaseScriptLib(INotifier ntf) {
		this.notifier = ntf;
	}
	
	//Functions
	public String normalizeFilename(String name) {
		String path = PackageLib.getCurrent().PACKAGE.get(PATH).optjstring("?.lua;?.lvn");

		if (getScriptExists(name)) {
			return name;
		}
		
		for (String pattern : path.split(";")) {
			String filename = pattern.replaceFirst("\\?", name);
			if (getScriptExists(filename)) {
				return filename;
			}
		}		
		return name;
	}

	protected abstract boolean getScriptExists(String normalizedFilename);
	
	@Override
	public final InputStream openScriptFile(String filename) throws IOException {
		if (LuaNovel.isBuiltInScript(filename)) {
			return LuaNovel.openBuiltInScript(filename);
		}
		return openExternalScriptFile(filename);
	}
	
	protected abstract InputStream openExternalScriptFile(String filename) throws IOException;
		
	//Getters
	@Override
	public long getScriptModificationTime(String filename) throws IOException {
		if (LuaNovel.isBuiltInScript(filename)) {
			return 0;
		}		
		return getExternalScriptModificationTime(filename);
	}

	protected abstract long getExternalScriptModificationTime(String filename) throws IOException;
	
	@Override
	public List<String> getScriptFiles(String folder, boolean includeBuiltIn) {
		List<String> result = new ArrayList<String>();
		if (includeBuiltIn) {
			try {
				LuaNovel.getBuiltInScripts(result, folder);
			} catch (IOException ioe) {
				notifier.w("Error retrieving a list of built-in script files", ioe);
			}
		}
		getExternalScriptFiles(result, folder);
		return result;
	}
	
	protected abstract void getExternalScriptFiles(Collection<String> out, String folder);
	
	@Override
	public LVNParser getLVNParser() {
		return ParserUtil.getParser(engineVersion);
	}
	
	//Setters
	public void setEngineVersion(String version) {
		engineVersion = version;
	}
	
}
