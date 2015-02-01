package nl.weeaboo.vn;

import java.io.File;

import nl.weeaboo.filesystem.IFileSystem;
import nl.weeaboo.filesystem.InMemoryFileSystem;
import nl.weeaboo.filesystem.LocalFileSystem;
import nl.weeaboo.filesystem.MultiFileSystem;

public final class TestFileSystem {

    public static IFileSystem newInstance() {
        IFileSystem readFileSystem = new LocalFileSystem(new File("res-test/"), true);
        IFileSystem inMemoryFileSystem = new InMemoryFileSystem(false);
        return new MultiFileSystem(readFileSystem, inMemoryFileSystem);
    }

}
