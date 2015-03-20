package nl.weeaboo.vn.impl;

import nl.weeaboo.vn.BasicPartRegistry;
import nl.weeaboo.vn.IContextManager;
import nl.weeaboo.vn.IEnvironment;
import nl.weeaboo.vn.INotifier;
import nl.weeaboo.vn.IRenderEnv;
import nl.weeaboo.vn.image.IImageModule;
import nl.weeaboo.vn.save.ISaveModule;
import nl.weeaboo.vn.sound.ISoundModule;

public class EnvironmentBuilder implements IEnvironment {

    public IContextManager contextManager;
    public BasicPartRegistry partRegistry;
    public INotifier notifier;
    public IRenderEnv renderEnv;
    public IImageModule imageModule;
    public ISoundModule soundModule;
    public ISaveModule saveModule;

    @Override
    public void destroy() {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public boolean isDestroyed() {
        return false;
    }

    private <T> T checkSet(T object) {
        if (object == null) {
            throw new IllegalStateException("Incorrect initialization order, field is null");
        }
        return object;
    }

    @Override
    public IContextManager getContextManager() {
        return checkSet(contextManager);
    }

    @Override
    public BasicPartRegistry getPartRegistry() {
        return checkSet(partRegistry);
    }

    @Override
    public INotifier getNotifier() {
        return checkSet(notifier);
    }

    @Override
    public IRenderEnv getRenderEnv() {
        return checkSet(renderEnv);
    }

    @Override
    public IImageModule getImageModule() {
        return checkSet(imageModule);
    }

    @Override
    public ISoundModule getSoundModule() {
        return checkSet(soundModule);
    }

    @Override
    public ISaveModule getSaveModule() {
        return checkSet(saveModule);
    }

}
