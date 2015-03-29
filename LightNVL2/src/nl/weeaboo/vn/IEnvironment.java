package nl.weeaboo.vn;

import nl.weeaboo.common.Dim;
import nl.weeaboo.common.Rect;
import nl.weeaboo.filesystem.IFileSystem;
import nl.weeaboo.settings.Preference;
import nl.weeaboo.vn.image.IImageModule;
import nl.weeaboo.vn.save.ISaveModule;
import nl.weeaboo.vn.script.IScriptLoader;
import nl.weeaboo.vn.sound.ISoundModule;
import nl.weeaboo.vn.video.IVideoModule;


/** Wrapper object that contains global engine state. */
public interface IEnvironment extends IDestructible {

    IContextManager getContextManager();
    BasicPartRegistry getPartRegistry();
    IScriptLoader getScriptLoader();
    IFileSystem getFileSystem();
    INotifier getNotifier();
    IRenderEnv getRenderEnv();
    ISystemEventHandler getSystemEventHandler();

    boolean isDebug();
    <T> T getPreference(Preference<T> pref);

    IImageModule getImageModule();
    ISoundModule getSoundModule();
    IVideoModule getVideoModule();
    ISaveModule getSaveModule();

    /**
     * @see IRenderEnv
     */
    void updateRenderEnv(Rect realClip, Dim realScreenSize);

}
