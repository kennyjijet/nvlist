package nl.weeaboo.vn.core.impl;

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
import nl.weeaboo.vn.script.IScriptLoader;
import nl.weeaboo.vn.script.lua.LuaScriptLoader;
import nl.weeaboo.vn.sound.ISoundModule;
import nl.weeaboo.vn.video.IVideoModule;

public class EnvironmentBuilder extends AbstractEnvironment {

    public IContextManager contextManager;
    public BasicPartRegistry partRegistry;
    public LuaScriptLoader scriptLoader;
    public IFileSystem fileSystem;
    public INotifier notifier;
    public IRenderEnv renderEnv;
    public ISystemEventHandler systemEventHandler;

    public IConfig preferences;

    public IImageModule imageModule;
    public ISoundModule soundModule;
    public IVideoModule videoModule;
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
    public IScriptLoader getScriptLoader() {
        return checkSet(scriptLoader);
    }

    @Override
    public IFileSystem getFileSystem() {
        return checkSet(fileSystem);
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
    public ISystemEventHandler getSystemEventHandler() {
        return checkSet(systemEventHandler);
    }

    @Override
    public <T> T getPreference(Preference<T> pref) {
        checkSet(preferences);
        return preferences.get(pref);
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
    public IVideoModule getVideoModule() {
        return checkSet(videoModule);
    }

    @Override
    public ISaveModule getSaveModule() {
        return checkSet(saveModule);
    }

}
