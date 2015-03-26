package nl.weeaboo.vn.image.impl;

import java.io.Serializable;
import java.nio.IntBuffer;

import nl.weeaboo.common.Checks;
import nl.weeaboo.gl.tex.AbstractTextureStore;
import nl.weeaboo.gl.tex.GLTexRect;
import nl.weeaboo.gl.tex.GLWritableTexture;
import nl.weeaboo.gl.tex.ITextureData;

public class GLTextureFactory implements Serializable {

    private static final long serialVersionUID = 1L;

    private final AbstractTextureStore texStore;

    private double imageScale = 1.0;

    public GLTextureFactory(AbstractTextureStore texStore) {
        this.texStore = Checks.checkNotNull(texStore);
    }

    public GLWritableTexture newWritableTexture(int w, int h, int minF, int magF, int wrapS, int wrapT) {
        return texStore.newWritableTexture(w, h, minF, magF, wrapS, wrapT);
    }

    public GLTexRect newTexRect(int[] argb, int w, int h) {
        return texStore.newTexRect(argb, w, h);
    }

    public ITextureData newARGB8TextureData(int[] argb, int w, int h) {
        return texStore.newARGB8TextureData(argb, w, h);
    }

    public ITextureData newARGB8TextureData(IntBuffer argb, boolean isShared, int tw, int th) {
        return texStore.newARGB8TextureData(argb, isShared, tw, th);
    }

    public GLTexRect getTexRect(String filename) {
        return texStore.getTexRect(filename);
    }

    public double getImageScale() {
        return imageScale;
    }

    public void setImageScale(double s) {
        imageScale = Checks.checkRange(s, "imageScale", 0);
    }

}
