package nl.weeaboo.vn.script.lua;

import static org.luaj.vm2.LuaValue.valueOf;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import nl.weeaboo.common.StringUtil;
import nl.weeaboo.filesystem.IFileSystem;
import nl.weeaboo.vn.script.IScriptLoader;
import nl.weeaboo.vn.script.IScriptThread;
import nl.weeaboo.vn.script.ScriptException;
import nl.weeaboo.vn.script.lvn.ICompiledLvnFile;
import nl.weeaboo.vn.script.lvn.ILvnParser;
import nl.weeaboo.vn.script.lvn.LvnParseException;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaString;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.BaseLib;
import org.luaj.vm2.lib.PackageLib;
import org.luaj.vm2.lib.ResourceFinder;
import org.luaj.vm2.lib.ResourceFinder.Resource;

public class LuaScriptLoader implements IScriptLoader {

    private static final int INPUT_BUFFER_SIZE = 4096;
    private static final LuaString PATH = valueOf("path");

    private final ILvnParser lvnParser;
    private final IFileSystem fs;

    LuaScriptLoader(ILvnParser lvnParser, IFileSystem fileSystem) {
        this.lvnParser = lvnParser;
        this.fs = fileSystem;
    }

    void initEnv() {
        PackageLib.getCurrent().setLuaPath("?.lvn;?.lua");

        BaseLib.FINDER = new ResourceFinder() {
            @Override
            public Resource findResource(String filename) {
                try {
                    return luaOpenScript(filename);
                } catch (FileNotFoundException e) {
                    return null;
                } catch (IOException e) {
                    throw new LuaError(e);
                }
            }
        };
    }

    @Override
    public String findScriptFile(String name) {
        String path = PackageLib.getCurrent().PACKAGE.get(PATH).tojstring();

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

    protected long getScriptModificationTime(String normalizedFilename) throws IOException {
        if (isBuiltInScript(normalizedFilename)) {
            return 0L;
        }

        return fs.getFileModifiedTime(normalizedFilename);
    }

    public ICompiledLvnFile compileScript(String normalizedFilename, InputStream in) throws IOException {
        ICompiledLvnFile file;
        try {
            file = lvnParser.parseFile(normalizedFilename, in);
        } catch (LvnParseException e) {
            throw new IOException(e.toString());
        }

//TODO Re-enable analytics
//        IAnalytics analytics = getAnalytics();
//        if (analytics != null) {
//            long modTime = getScriptModificationTime(normalizedFilename);
//            analytics.logScriptCompile(filename, modificationTime);
//        }

//TODO Re-enable seen logging
//        ISeenLog seenLog = getSeenLog();
//        if (seenLog != null) {
//            seenLog.registerScriptFile(filename, file.countTextLines(false));
//        }

        return file;
    }

    Resource luaOpenScript(String filename) throws IOException {
        filename = findScriptFile(filename);

        InputStream in = openScript(filename);

        if (!filename.endsWith(".lvn")) {
            //Read as a normal file
            if (INPUT_BUFFER_SIZE > 0) {
                in = new BufferedInputStream(in, INPUT_BUFFER_SIZE);
            }
        } else {
            ICompiledLvnFile file;
            try {
                file = compileScript(filename, in);
            } finally {
                in.close();
            }
            in = new ByteArrayInputStream(StringUtil.toUTF8(file.getCompiledContents()));
        }

        return new Resource(filename, in);
    }

    @Override
    public void loadScript(IScriptThread thread, String filename) throws IOException, ScriptException {
        filename = findScriptFile(filename);

        LuaScriptThread luaThread = (LuaScriptThread)thread;

        Varargs loadResult = BaseLib.loadFile(filename);
        if (!loadResult.arg1().isclosure()) {
            throw new ScriptException("Error loading script, " + filename + ": " + loadResult.arg(2));
        } else {
            luaThread.call(loadResult.checkclosure(1));
        }
    }

}
