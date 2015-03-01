package nl.weeaboo.vn.impl;

import nl.weeaboo.common.Checks;
import nl.weeaboo.vn.BasicPartRegistry;
import nl.weeaboo.vn.IContextManager;
import nl.weeaboo.vn.IEnvironment;
import nl.weeaboo.vn.IImageModule;
import nl.weeaboo.vn.INotifier;
import nl.weeaboo.vn.ISoundModule;

public class DefaultEnvironment implements IEnvironment {

    private final IContextManager contextManager;
    private final BasicPartRegistry pr;
    private final INotifier notifier;
    private final IImageModule imageModule;
    private final ISoundModule soundModule;

    private boolean destroyed;

    protected DefaultEnvironment(EnvironmentBuilder b) {
        this.contextManager = Checks.checkNotNull(b.contextManager);
        this.pr = Checks.checkNotNull(b.partRegistry);
        this.notifier = Checks.checkNotNull(b.notifier);
        this.imageModule = Checks.checkNotNull(b.imageModule);
        this.soundModule = Checks.checkNotNull(b.soundModule);
    }

    @Override
    public void destroy() {
        destroyed = true;
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
    public IImageModule getImageModule() {
        return imageModule;
    }

    @Override
    public ISoundModule getSoundModule() {
        return soundModule;
    }

}
