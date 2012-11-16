package nl.weeaboo.vn.impl.base;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;

import nl.weeaboo.io.BufferUtil;

public abstract class DecodingScreenshot extends BaseScreenshot {

	private static final long serialVersionUID = BaseImpl.serialVersionUID;

	private transient ByteBuffer data;
	
	public DecodingScreenshot(ByteBuffer b) {
		super((short)0, false);
		
		data = b;
		
		if (data == null) {
			cancel();
		} else {
			isAvailable = true;
		}
	}
	
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
		
		if (!isTransient()) {
			byte[] b = BufferUtil.toArray(data);
			out.writeInt(b.length);
			out.write(b);			
		} else {
			out.writeInt(-1);
		}
	}
	
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();

		int len = in.readInt();
		if (len >= 0) {
			byte[] b = new byte[len];
			in.readFully(b);
			data = ByteBuffer.wrap(b);
		}
	}
	
	protected abstract void tryLoad(ByteBuffer data);
	
	@Override
	public void cancel() {
		data = null; //Allows garbage collection of data and makes isAvailable() return false 
		
		super.cancel();
	}
	
	@Override
	public int[] getPixels() {
		if (!isAvailable) {
			tryLoad(data);
			isAvailable = true;
		}		
		return super.getPixels();
	}

	@Override
	public int getPixelsWidth() {
		if (!isAvailable) {
			tryLoad(data);
			isAvailable = true;
		}		
		return super.getPixelsWidth();
	}

	@Override
	public int getPixelsHeight() {
		if (!isAvailable) {
			tryLoad(data);
			isAvailable = true;
		}		
		return super.getPixelsHeight();
	}
	
}
