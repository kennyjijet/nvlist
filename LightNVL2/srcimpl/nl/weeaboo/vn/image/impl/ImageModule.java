package nl.weeaboo.vn.image.impl;

import java.util.Collection;

import nl.weeaboo.common.Checks;
import nl.weeaboo.common.Dim;
import nl.weeaboo.game.entity.Entity;
import nl.weeaboo.gl.tex.GLTexRect;
import nl.weeaboo.gl.tex.GLTexture;
import nl.weeaboo.gl.tex.GLWritableTexture;
import nl.weeaboo.vn.IEnvironment;
import nl.weeaboo.vn.ILayer;
import nl.weeaboo.vn.INotifier;
import nl.weeaboo.vn.IRenderEnv;
import nl.weeaboo.vn.core.impl.EntityHelper;
import nl.weeaboo.vn.core.impl.ResourceLoader;
import nl.weeaboo.vn.image.IImageModule;
import nl.weeaboo.vn.image.IScreenshot;
import nl.weeaboo.vn.image.ITexture;

public class ImageModule implements IImageModule {

    protected final IEnvironment env;
    protected final INotifier notifier;
    protected final ResourceLoader resourceLoader;
    protected final EntityHelper entityHelper;

    private final GLTextureFactory texFactory;

    /** Max size to try and put in a GLPackedTexture instead of generating a whole new texture. */
    private final int packRectLimit = 128;

    private Dim imageResolution;

    public ImageModule(IEnvironment env, ResourceLoader resourceLoader, GLTextureFactory texStore) {
        this.env = env;
        this.notifier = env.getNotifier();
        this.resourceLoader = resourceLoader;
        this.entityHelper = new EntityHelper(env.getPartRegistry());

        this.texFactory = texStore;

        IRenderEnv renderEnv = env.getRenderEnv();
        imageResolution = new Dim(renderEnv.getWidth(), renderEnv.getHeight());
    }

    @Override
    public Entity createImage(ILayer layer) {
        Entity e = entityHelper.createScriptableEntity(layer);
        entityHelper.addImageParts(e);
        return e;
    }

    @Override
    public Entity createTextDrawable(ILayer layer) {
        Entity e = entityHelper.createScriptableEntity(layer);
        //TODO Add text parts
        return e;
    }

    @Override
    public Entity createButton(ILayer layer) {
        Entity e = entityHelper.createScriptableEntity(layer);
        //TODO Add button parts
        return e;
    }

    @Override
    public ITexture getTexture(String filename, String[] callStack, boolean suppressErrors) {
        resourceLoader.checkRedundantFileExt(filename);

        String normalized = resourceLoader.normalizeFilename(filename);
        if (normalized == null) {
            if (!suppressErrors) {
                notifier.debug("Unable to find image file: " + filename);
            }
            return null;
        }

        // seenLog.addImage(filename); // TODO Enable seen log
        return getTextureNormalized(filename, normalized, callStack);
    }

    /**
     * Is called from {@link #getTexture(String, String[], boolean)}
     */
    protected ITexture getTextureNormalized(String filename, String normalized, String[] luaStack) {
        GLTexRect tr = getTexRectNormalized(filename, normalized, luaStack);

        TextureAdapter ta = new ImageTextureAdapter(texFactory, normalized);
        double scale = getImageScale();
        ta.setTexRect(tr, scale, scale);
        return ta;
    }

    private GLTexRect getTexRectNormalized(String filename, String normalized, String[] luaStack) {
        // TODO LVN-011 Track stack traces, log texture load times
        return texFactory.getTexRect(normalized);
    }

    public ITexture createTexture(GLTexture tex, double sx, double sy) {
        GLTexRect tr = null;
        if (tex != null) {
            tr = tex.getSubRect(null);
        }
        return createTexture(tr, sx, sy);
    }

    public ITexture createTexture(GLTexRect tr, double sx, double sy) {
        if (tr == null) {
            return null;
        }

        TextureAdapter ta = new TextureAdapter();
        ta.setTexRect(tr, sx, sy);
        return ta;
    }

    @Override
    public ITexture createTexture(int[] argb, int w, int h, double sx, double sy) {
        if (w <= packRectLimit && h <= packRectLimit) {
            return createTexture(texFactory.newTexRect(argb, w, h), sx, sy);
        } else {
            GLWritableTexture tex = texFactory.newWritableTexture(w, h, 0, 0, 0, 0);
            if (argb != null) {
                tex.setPixels(texFactory.newARGB8TextureData(argb, w, h));
            }
            return createTexture(tex, sx, sy);
        }
    }

    @Override
    public ITexture createTexture(IScreenshot ss) {
        if (ss.isVolatile()) {
            return ss.getVolatilePixels();
        } else {
            IRenderEnv renderEnv = env.getRenderEnv();
            return createTexture(ss.getPixels(), ss.getPixelsWidth(), ss.getPixelsHeight(),
                    renderEnv.getWidth() / (double)ss.getScreenWidth(),
                    renderEnv.getHeight() / (double)ss.getScreenHeight());
        }
    }

    @Override
    public Collection<String> getImageFiles(String folder) {
        return resourceLoader.getMediaFiles(folder);
    }

    protected void onImageScaleChanged() {
        texFactory.setImageScale(getImageScale());
    }

    public GLTextureFactory getGLTextureFactory() {
        return texFactory;
    }

    protected double getImageScale() {
        IRenderEnv renderEnv = env.getRenderEnv();
        return Math.min(renderEnv.getWidth() / (double)imageResolution.w,
                renderEnv.getHeight() / (double)imageResolution.h);
    }

    @Override
    public void setImageResolution(Dim size) {
        size = Checks.checkNotNull(size);
        if (!imageResolution.equals(size)) {
            imageResolution = size;
            onImageScaleChanged();
        }
    }

}
