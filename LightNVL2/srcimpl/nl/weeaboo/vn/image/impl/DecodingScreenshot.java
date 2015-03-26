package nl.weeaboo.vn.image.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;

import nl.weeaboo.io.BufferUtil;

public abstract class DecodingScreenshot extends AbstractScreenshot {

	private static final long serialVersionUID = ImageImpl.serialVersionUID;

	private transient ByteBuffer data;
	private transient boolean isLoaded;

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
		if (!isLoaded) {
			tryLoad(data);
			isLoaded = true;
		}
		return super.getPixels();
	}

	@Override
	public int getPixelsWidth() {
		if (!isLoaded) {
			tryLoad(data);
			isLoaded = true;
		}
		return super.getPixelsWidth();
	}

	@Override
	public int getPixelsHeight() {
		if (!isLoaded) {
			tryLoad(data);
			isLoaded = true;
		}
		return super.getPixelsHeight();
	}

	protected void setPixels(int[] argb, int w, int h) {
		setPixels(argb, w, h, w, h);
	}

}
