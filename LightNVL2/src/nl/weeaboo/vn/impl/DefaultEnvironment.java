package nl.weeaboo.vn.impl;

import nl.weeaboo.common.Checks;
import nl.weeaboo.vn.BasicPartRegistry;
import nl.weeaboo.vn.IContextManager;
import nl.weeaboo.vn.IEnvironment;
import nl.weeaboo.vn.INotifier;
import nl.weeaboo.vn.IRenderEnv;
import nl.weeaboo.vn.image.IImageModule;
import nl.weeaboo.vn.save.ISaveModule;
import nl.weeaboo.vn.sound.ISoundModule;

public class DefaultEnvironment implements IEnvironment {

    private final IContextManager contextManager;
    private final BasicPartRegistry pr;
    private final INotifier notifier;
    private final IRenderEnv renderEnv;
    private final IImageModule imageModule;
    private final ISoundModule soundModule;
    private final ISaveModule saveModule;

    private boolean destroyed;

    public DefaultEnvironment(EnvironmentBuilder b) {
        this.contextManager = Checks.checkNotNull(b.contextManager);
        this.pr = Checks.checkNotNull(b.partRegistry);
        this.notifier = Checks.checkNotNull(b.notifier);
        this.renderEnv = Checks.checkNotNull(b.renderEnv);
        this.imageModule = b.imageModule;
        this.soundModule = b.soundModule;
        this.saveModule = b.saveModule;
    }

    @Override
    public void destroy() {
        destroyed = true;

        ContextUtil.setCurrentContext(null);
    }

    @Override
    public boolean isDestroyed() {
        return destroyed;
    }

    @Override
    public IContextManager getContextManager() {
        return contextManager;
    }

    @Override
    public BasicPartRegistry getPartRegistry() {
        return pr;
    }

    @Override
    public INotifier getNotifier() {
        return notifier;
    }

    @Override
    public IRenderEnv getRenderEnv() {
        return renderEnv;
    }

    @Override
    public IImageModule getImageModule() {
        return imageModule;
    }

    @Override
    public ISoundModule getSoundModule() {
        return soundModule;
    }

    @Override
    public ISaveModule getSaveModule() {
        return saveModule;
    }

}
