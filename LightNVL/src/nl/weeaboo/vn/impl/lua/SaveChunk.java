package nl.weeaboo.vn.impl.lua;

import java.io.DataInput;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.Deflater;
import java.util.zip.InflaterInputStream;

import nl.weeaboo.io.ByteBufferInputStream;

public final class SaveChunk {

	private final long id;
	private final int version;
	private final boolean deflate;
	private final int uncompressedLength;
	private final ByteBuffer bytes;
	
	private SaveChunk(long id, int version, boolean deflate, int uncompressedLength, byte[] b) {
		this.id = id;
		this.version = version;
		this.deflate = deflate;
		this.uncompressedLength = uncompressedLength;
		this.bytes = ByteBuffer.wrap(b);
	}
	
	//Functions
	public static void checkId(SaveChunk chunk, long expected) throws IOException {
		if (chunk.getId() != expected) {
			throw new IOException(String.format(
					"Invalid chunk identifier: %08x, expected: %08x",
					chunk.getId(), expected));
		}
	}
	
	public static void checkVersion(SaveChunk chunk, int expected) throws IOException {
		if (chunk.getVersion() != expected) {		
			throw new IOException(String.format("Invalid chunk version: %d, expected: %d",
					chunk.getVersion(), expected));
		}
	}
	
	public static long createId(char a, char b, char c, char d, char e, char f, char g, char h) {
		if (a > 0x7F || b > 0x7F || c > 0x7F || d > 0x7F
				|| e > 0x7F || f > 0x7F || g > 0x7F || h > 0x7F)
		{
			throw new IllegalArgumentException("All chars must be ASCII");
		}
		
		long part1 = d|((c&0xFF)<<8)|((b&0xFF)<<16)|((a&0xFF)<<24);
		long part2 = h|((g&0xFF)<<8)|((f&0xFF)<<16)|((e&0xFF)<<24);
		return (part1<<32L)|part2;
	}
	
	public static SaveChunk read(DataInput din) throws IOException {
		long id;
		int version, ulen, clen;
		boolean deflate;
		
		try {
			id = din.readLong();
			version = din.readInt();
			deflate = din.readBoolean();
			ulen = din.readInt();
			clen = din.readInt();
		} catch (EOFException eofe) {
			return null;
		}
		
		if (ulen < 0 || clen < 0) {
			//Damaged
			return null;
		}
		
		try {
			byte[] b = new byte[clen];
			din.readFully(b);		
			return new SaveChunk(id, version, deflate, ulen, b);
		} catch (OutOfMemoryError oome) {
			System.err.println("Out of memory reading chunk: " + clen);
			throw oome;
		}
	}
	
	public static void write(DataOutputStream dout, long id, int version, boolean deflate,
			byte[] b, int off, int len) throws IOException
	{
		dout.writeLong(id);
		dout.writeInt(version);
		dout.writeBoolean(deflate);
		dout.writeInt(len);
		
		if (deflate) {
			Deflater defl = new Deflater();
			defl.setInput(b, off, len);
			defl.finish();
			
			int written = 0;
			List<byte[]> chunks = new ArrayList<byte[]>();
			List<Integer> lengths = new ArrayList<Integer>();
			while (true) {
				byte[] temp = new byte[8192];
				int w = defl.deflate(temp);
				if (w <= 0) break;
				
				chunks.add(temp);
				lengths.add(w);
				written += w;
			}
			
			dout.writeInt(written);
			for (int n = 0; n < chunks.size(); n++) {
				dout.write(chunks.get(n), 0, lengths.get(n));
			}
		} else {
			dout.writeInt(len);
			dout.write(b, off, len);
		}
	}
	
	//Getters
	public long getId() {
		return id;
	}
	public int getVersion() {
		return version;
	}
	public int getDataLength() {
		return uncompressedLength;
	}
	public InputStream getData() {
		InputStream in = new ByteBufferInputStream(bytes);
		if (deflate) {
			in = new InflaterInputStream(in);
		}
		return in;
	}
	
}
