package nl.weeaboo.vn.impl.base;

import static org.luaj.vm2.LuaValue.valueOf;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

import nl.weeaboo.vn.IScriptLib;
import nl.weeaboo.vn.impl.lua.LuaNovel;

import org.luaj.vm2.LuaString;
import org.luaj.vm2.lib.PackageLib;

public abstract class BaseScriptLib implements IScriptLib, Serializable {

	private static final long serialVersionUID = BaseImpl.serialVersionUID;

	private static final LuaString PATH = valueOf("path");
	
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
	
	//Setters
	
}
