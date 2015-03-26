package nl.weeaboo.vn.image.impl;

import nl.weeaboo.common.Checks;
import nl.weeaboo.gl.GLManager;
import nl.weeaboo.gl.tex.TextureException;

public class ImageTextureAdapter extends TextureAdapter {

	private static final long serialVersionUID = ImageImpl.serialVersionUID;

	private final GLTextureFactory fac;
	private final String filename;

	public ImageTextureAdapter(GLTextureFactory fac, String filename) {
	    this.fac = Checks.checkNotNull(fac);
		this.filename = Checks.checkNotNull(filename);
	}

	//Functions
	@Override
	public void glLoad(GLManager glm) throws TextureException {
        if (tr == null) {
            tr = fac.getTexRect(filename);
        }
        if (tr != null) {
            tr = tr.glLoad(glm);
        }
		setTexRect(tr, fac.getImageScale());
	}

	@Override
    public void glTryLoad(GLManager glm) {
		if (tr == null) {
			tr = fac.getTexRect(filename);
		}
		if (tr != null) {
			tr = tr.glTryLoad(glm);
		}
        setTexRect(tr, fac.getImageScale());
	}

	//Getters
	public String getFilename() {
		return filename;
	}

	//Setters

}
