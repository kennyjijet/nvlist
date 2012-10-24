package nl.weeaboo.vn.impl.nvlist;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import nl.weeaboo.io.ByteChunkOutputStream;
import nl.weeaboo.lua2.io.LuaSerializable;
import nl.weeaboo.tga.TGAUtil;
import nl.weeaboo.vn.impl.base.BaseScreenshot;

@LuaSerializable
public class Screenshot extends BaseScreenshot {

	private static final long serialVersionUID = NVListImpl.serialVersionUID;
	
	public Screenshot(short z) {
		super(z);
	}
	
	//Functions				
	@Override
	protected void serializePixels(ObjectOutputStream out, int[] argb) throws IOException {
		if (argb != null && getWidth() > 0 && getHeight() > 0) {
			ByteChunkOutputStream bout = new ByteChunkOutputStream();
			TGAUtil.writeTGA(bout, argb, getWidth(), getHeight(), true, true);
			out.writeInt(bout.size());
			bout.writeContentsTo((OutputStream)out);
		} else {
			out.writeInt(argb != null ? 0 : -1);
		}
	}
	
	@Override
	protected int[] deserializePixels(ObjectInputStream in) throws IOException, ClassNotFoundException {
		int len = in.readInt();
		if (len > 0) {
			byte data[] = new byte[len];
			in.readFully(data);			
			BufferedImage image = TGAUtil.readTGA(new ByteArrayInputStream(data));
			return image.getRGB(0, 0, image.getWidth(), image.getHeight(), new int[len], 0, image.getWidth());
		} else {
			return (len < 0 ? null : new int[0]);
		}
	}
	
	//Getters
	
	//Setters
	
}
