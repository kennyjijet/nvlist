package nl.weeaboo.vn;


/** Wrapper object that contains global engine state. */
public interface IEnvironment extends IDestructible {

    IContextManager getContextManager();

    BasicPartRegistry getPartRegistry();

    INotifier getNotifier();

    IRenderEnv getRenderEnv();

    IImageModule getImageModule();

    ISoundModule getSoundModule();

}
