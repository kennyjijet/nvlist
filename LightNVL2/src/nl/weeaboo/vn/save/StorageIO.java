package nl.weeaboo.vn.save;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Properties;

import nl.weeaboo.filesystem.IFileSystem;
import nl.weeaboo.filesystem.SecureFileWriter;
import nl.weeaboo.vn.core.impl.Storage;

/** Helper class for reading/writing {@link IStorage} objects to files */
public final class StorageIO {

    private static final String VERSION_STRING = "Version 1";

    private StorageIO() {
    }

    public static IStorage read(IFileSystem fs, String filename) throws IOException {
        InputStream in = fs.newInputStream(filename);
        try {
            return read(in);
        } finally {
            in.close();
        }
    }

    public static IStorage read(SecureFileWriter fs, String filename) throws IOException {
        InputStream in = fs.newInputStream(filename);
        try {
            return read(in);
        } finally {
            in.close();
        }
    }

    public static IStorage read(InputStream in) throws IOException {
        Properties properties = new Properties();
        properties.load(new InputStreamReader(in, "UTF-8"));

        Storage storage = new Storage();
        for (String key : properties.stringPropertyNames()) {
            String str = properties.getProperty(key);
            if (str != null) {
                storage.set(key, StoragePrimitive.fromJson(str));
            }
        }
        return storage;
    }

    public static void write(IStorage storage, IFileSystem fs, String filename) throws IOException {
        OutputStream out = fs.newOutputStream(filename, false);
        try {
            write(out, storage);
        } finally {
            out.close();
        }
    }

    public static void write(IStorage storage, SecureFileWriter fs, String filename) throws IOException {
        OutputStream out = fs.newOutputStream(filename, false);
        try {
            write(out, storage);
        } finally {
            out.close();
        }
    }

    public static void write(OutputStream out, IStorage storage) throws IOException {
        Properties properties = new Properties();
        for (String key : storage.getKeys()) {
            StoragePrimitive val = storage.get(key);
            if (val != null) {
                properties.setProperty(key, val.toJson());
            }
        }
        properties.store(new OutputStreamWriter(out, "UTF-8"), VERSION_STRING);
    }
}
