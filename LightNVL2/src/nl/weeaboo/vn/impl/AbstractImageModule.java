package nl.weeaboo.vn.impl;

import java.util.Collection;

import nl.weeaboo.game.entity.Entity;
import nl.weeaboo.vn.IEnvironment;
import nl.weeaboo.vn.IImageModule;
import nl.weeaboo.vn.ILayer;
import nl.weeaboo.vn.INotifier;
import nl.weeaboo.vn.IRenderEnv;
import nl.weeaboo.vn.IScreenshot;
import nl.weeaboo.vn.ITexture;

public abstract class AbstractImageModule implements IImageModule {

    protected final IEnvironment env;
    protected final INotifier notifier;
    protected final ResourceLoader resourceLoader;
    protected final EntityHelper entityHelper;

    public AbstractImageModule(IEnvironment env, ResourceLoader resourceLoader) {
        this.env = env;
        this.notifier = env.getNotifier();
        this.resourceLoader = resourceLoader;
        this.entityHelper = new EntityHelper(env.getPartRegistry());
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
    protected abstract ITexture getTextureNormalized(String filename, String normalized, String[] callStack);

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

}
