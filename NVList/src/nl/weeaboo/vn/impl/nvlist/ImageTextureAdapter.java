package nl.weeaboo.vn.impl.nvlist;

import nl.weeaboo.gl.GLManager;
import nl.weeaboo.lua2.io.LuaSerializable;

@LuaSerializable
public class ImageTextureAdapter extends TextureAdapter {

	private static final long serialVersionUID = NVListImpl.serialVersionUID;

	private final String filename;
	
	public ImageTextureAdapter(ImageFactory fac, String filename) {
		super(fac);
		
		this.filename = filename;
	}

	//Functions
	@Override
	public void forceLoad(GLManager glm) {
		if (tr == null || tr.isDisposed()) {
			tr = imgfac.getTexRect(filename, null);
		}
		if (tr != null) {
			tr = tr.forceLoad(glm);
		}
		
		double scale = imgfac.getImageScale();
		setTexRect(tr, scale, scale);
	}
	
	//Getters
	public String getFilename() {
		return filename;
	}
	
	//Setters
	
}
