package nl.weeaboo.vn.core.impl;

import nl.weeaboo.common.Checks;
import nl.weeaboo.filesystem.IFileSystem;
import nl.weeaboo.settings.IConfig;
import nl.weeaboo.settings.Preference;
import nl.weeaboo.vn.BasicPartRegistry;
import nl.weeaboo.vn.IContextManager;
import nl.weeaboo.vn.INotifier;
import nl.weeaboo.vn.IRenderEnv;
import nl.weeaboo.vn.ISystemEventHandler;
import nl.weeaboo.vn.image.IImageModule;
import nl.weeaboo.vn.save.ISaveModule;
import nl.weeaboo.vn.script.lua.LuaScriptLoader;
import nl.weeaboo.vn.sound.ISoundModule;
import nl.weeaboo.vn.video.IVideoModule;

public class DefaultEnvironment extends AbstractEnvironment {

    private final IContextManager contextManager;
    private final BasicPartRegistry pr;
    private final LuaScriptLoader scriptLoader;
    private final IFileSystem fileSystem;
    private final INotifier notifier;
    private final IRenderEnv renderEnv;
    private final ISystemEventHandler systemEventHandler;

    private final IConfig preferences;

    private final IImageModule imageModule;
    private final ISoundModule soundModule;
    private final IVideoModule videoModule;
    private final ISaveModule saveModule;

    private boolean destroyed;

    public DefaultEnvironment(EnvironmentBuilder b) {
        this.contextManager = Checks.checkNotNull(b.contextManager);
        this.pr = Checks.checkNotNull(b.partRegistry);
        this.scriptLoader = Checks.checkNotNull(b.scriptLoader);
        this.fileSystem = b.fileSystem;
        this.notifier = Checks.checkNotNull(b.notifier);
        this.renderEnv = Checks.checkNotNull(b.renderEnv);
        this.systemEventHandler = Checks.checkNotNull(b.systemEventHandler);

        this.preferences = b.preferences;

        this.imageModule = b.imageModule;
        this.soundModule = b.soundModule;
        this.videoModule = b.videoModule;
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
    public LuaScriptLoader getScriptLoader() {
        return scriptLoader;
    }

    @Override
    public IFileSystem getFileSystem() {
        return fileSystem;
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
    public ISystemEventHandler getSystemEventHandler() {
        return systemEventHandler;
    }

    @Override
    public <T> T getPreference(Preference<T> pref) {
        return preferences.get(pref);
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
    public IVideoModule getVideoModule() {
        return videoModule;
    }

    @Override
    public ISaveModule getSaveModule() {
        return saveModule;
    }

}
