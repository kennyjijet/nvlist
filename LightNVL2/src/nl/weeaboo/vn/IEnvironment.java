package nl.weeaboo.vn;

import nl.weeaboo.vn.save.ISaveModule;


/** Wrapper object that contains global engine state. */
public interface IEnvironment extends IDestructible {

    IContextManager getContextManager();

    BasicPartRegistry getPartRegistry();

    INotifier getNotifier();

    IRenderEnv getRenderEnv();

    IImageModule getImageModule();

    ISoundModule getSoundModule();

    ISaveModule getSaveModule();

}
