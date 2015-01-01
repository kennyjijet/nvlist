package nl.weeaboo.vn.script.impl;

import static org.luaj.vm2.LuaValue.valueOf;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import nl.weeaboo.filesystem.IFileSystem;
import nl.weeaboo.vn.script.IScriptLoader;

import org.luaj.vm2.LuaString;
import org.luaj.vm2.lib.PackageLib;

public class LuaScriptLoader implements IScriptLoader {

    private static final LuaString PATH = valueOf("path");

    private final IFileSystem fs;

    public LuaScriptLoader(IFileSystem fileSystem) {
        this.fs = fileSystem;
    }

    @Override
    public String findScriptFile(String name) {
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

    private static String toResourcePath(String filename) {
        return "/script/" + filename;
    }

    protected boolean getScriptExists(String normalizedFilename) {
        if (isBuiltInScript(normalizedFilename)) {
            return LuaScriptLoader.class.getResource(toResourcePath(normalizedFilename)) != null;
        }

        return fs.getFileExists(normalizedFilename);
    }

    public boolean isBuiltInScript(String filename) {
        return filename.startsWith("builtin/");
    }

    @Override
    public InputStream openScript(String normalizedFilename) throws IOException {
        if (isBuiltInScript(normalizedFilename)) {
            InputStream in = LuaScriptLoader.class.getResourceAsStream(toResourcePath(normalizedFilename));
            if (in == null) {
                throw new FileNotFoundException(normalizedFilename);
            }
            return in;
        }

        return fs.newInputStream(normalizedFilename);
    }

}
