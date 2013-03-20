package nl.weeaboo.vn.vnds;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;

import nl.weeaboo.io.StreamUtil;
import nl.weeaboo.lua2.LuaException;
import nl.weeaboo.lua2.io.LuaSerializable;
import nl.weeaboo.lua2.lib.LuaLibrary;
import nl.weeaboo.lua2.lib.LuajavaLib;
import nl.weeaboo.vn.INotifier;
import nl.weeaboo.vn.IScriptLib;

import org.luaj.vm2.LuaBoolean;
import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

@LuaSerializable
public class VNDSLib extends LuaLibrary {

	private static final long serialVersionUID = VNDSImpl.serialVersionUID;

	private static final String[] NAMES = {
		"readScript"
	};

	private static final int INIT        = 0;
	private static final int READ_SCRIPT = 1;
	
	private final IScriptLib scrfac;
	private final INotifier notifier;
	
	public VNDSLib(IScriptLib scrfac, INotifier ntf) {
		this.scrfac = scrfac;
		this.notifier = ntf;
	}

	public static void register(LuaValue globals, IScriptLib scrlib, INotifier ntf) throws LuaException {
		globals.set("isVNDS", LuaBoolean.TRUE);
		globals.load(new VNDSLib(scrlib, ntf));
	}	
	
	@Override
	protected LuaLibrary newInstance() {
		return new VNDSLib(scrfac, notifier);
	}
	
	@Override
	public Varargs invoke(Varargs args) {
		switch (opcode) {
		case INIT: return initLibrary("VNDS", NAMES, 1);
		case READ_SCRIPT: return readScript(args);
		default: return super.invoke(args);
		}
	}
	
	protected Varargs readScript(Varargs args) {
		String filename = args.arg1().checkjstring();
		
		InputStream in = null;
		try {
			in = scrfac.openScriptFile(filename);
			if (in == null) {
				throw new FileNotFoundException(filename);
			}
		} catch (FileNotFoundException fnfe) {
			notifier.d("Script file not found: " + filename, fnfe);
			return NIL;
		} catch (IOException ioe) {
			notifier.w("Error opening script: " + filename, ioe);
			return NIL;
		} finally {
			try {
				if (in != null) in.close();
			} catch (IOException ioe) {
				//Ignore
			}
		}
		
		VNDSFile file = new VNDSFile(this, filename);
		return LuajavaLib.toUserdata(file, file.getClass());
	}
	
	protected LStringReader open(String filename) throws IOException {
		InputStream in = scrfac.openScriptFile(filename);
		if (in == null) {
			throw new FileNotFoundException(filename);
		}

		byte bytes[] = StreamUtil.readFully(in);
		int off = StreamUtil.skipBOM(bytes, 0, bytes.length);
		
		return new LStringReader(bytes, off, bytes.length-off);
	}
			
	//Inner Classes
	@LuaSerializable
	protected static final class LStringReader implements Serializable {

		private static final long serialVersionUID = VNDSImpl.serialVersionUID;
		
		private byte[] data;
		private int off;
		private int end;
		
		public LStringReader(byte[] b, int o, int len) {
			data = b;
			off = o;
			end = o + len;
		}
		
		public void close() throws IOException {
			data = null;
			off = end = 0;
		}
		
		public LuaString readLine() throws IOException {
			if (off >= end) {
				return null;
			}
			
			int a = off;
			while (off < end && data[off] != '\n') {
				off++;
			}
			
			int b;
			if (off > a && data[off-1] == '\r') {
				b = off-1;
			} else {
				b = off;
			}
			
			if (off < end && data[off] == '\n') {
				off++;
			}
			
			//System.out.println(off + " " + end + " " + a + " " + b);
			
			return LuaString.valueOf(data, a, b - a);
		}
	}
	
	@LuaSerializable
	public static final class VNDSFile implements Serializable {

		private static final long serialVersionUID = 1L;
		
		//Warning: Serializable object, don't add fields unless necessary
		private final VNDSLib lib;
		private final String filename;
		private int lineNumber;
		//----------
		
		private transient LStringReader in;
		
		public VNDSFile(VNDSLib l, String fn) {
			lib = l;
			filename = fn;
			
			lineNumber = 1;
			
			initTransients();
		}
		
		private void initTransients() {
			try {
				in = lib.open(filename);
				
				//Skip lines to desired lineNumber
				for (int n = 1; n < lineNumber; n++) {
					in.readLine();
				}
			} catch (FileNotFoundException fnfe) {
				lib.notifier.w("Script file not found: " + filename, fnfe);
			} catch (IOException ioe) {
				lib.notifier.w("Error opening script: " + filename, ioe);
			}
		}
		
		private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
			in.defaultReadObject();
			
			initTransients();
		}	
		
		public void close() {
			try {
				in.close();
			} catch (IOException e) {
				//Ignore
			} finally {
				in = null;
			}			
		}
		
		/**
		 * Reads a single line from the script.
		 * 
		 * @return A string representing the script line, or <code>null</code>
		 *         if either there are no more lines in the script or some type
		 *         of error occurs.
		 */
		public LuaString readLine() {
			lineNumber++;
			
			LuaString line = null;
			if (in != null) {
				try {
					line = in.readLine();
				} catch (IOException ioe) {
					lib.notifier.w("Error reading line from file (" + filename + ")", ioe);
					close();
				}
			}
			return line;
		}
		
		/**
		 * Starts skipping script lines until the script ends or the specified
		 * label is reached.
		 * 
		 * @param targetLabel The label to skip to
		 * @return <code>true</code> if the label is reached, <code>false</code>
		 *         if the end of the script is reached before encountering
		 *         <code>targetLabel</code>.
		 */
		public boolean skipToLabel(String targetLabel) {
			LuaString cmd = LuaString.valueOf("label");
			
			LuaString line;
			while ((line = readLine()) != null) {
				int index = 0;
				int len = line.length();
				
				//Skip whitespace prefix
				while (index < len && Character.isWhitespace(line.charAt(index))) {
					index++;
				}
				
				//Check if command matches "label"
				int t = 0;
				for (t = 0; t < cmd.length() && index < len; t++, index++) {
					if (line.charAt(index) != cmd.charAt(t)) {
						break;
					}
				}				
				
				if (t < cmd.length() || index >= line.length()) {
					//No match, or no arg
					continue;
				}
				
				//Check if the label targets match
				String label = line.substring(index, line.length()).tojstring().trim();
				if (targetLabel.equals(label)) {
					//System.out.println(targetLabel + " " + label);
					return true;
				} else {
					//System.out.println(targetLabel + " " + label);
				}
			}
			return false;
		}
		
		/**
		 * Skips lines until <code>getLineNumber() == targetLine</code> or the
		 * end of the script is reached.
		 * 
		 * @param targetLine
		 * @return <code>true</code> if the current line is now
		 *         <code>targetLine</code>.
		 */
		public boolean skipToLine(int targetLine) {
			while (getLineNumber() < targetLine) {
				LuaString line = readLine();
				if (line == null) {
					break;
				}
			}
			return getLineNumber() == targetLine;
		}
		
		public String getFilename() {
			return filename;
		}
		
		public int getLineNumber() {
			return lineNumber;
		}
		
	}
	
}
