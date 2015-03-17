package nl.weeaboo.vn.impl;

import nl.weeaboo.vn.IContext;
import nl.weeaboo.vn.IContextManager;
import nl.weeaboo.vn.IEnvironment;
import nl.weeaboo.vn.INovel;
import nl.weeaboo.vn.save.ISaveModule;

public class AbstractNovel implements INovel {

    private static final long serialVersionUID = BaseImpl.serialVersionUID;

    private final IEnvironment env;

    public AbstractNovel(IEnvironment env) {
        this.env = env;
    }

    //Functions
    @Override
    public void start() {
    }

    @Override
    public void stop() {
        ISaveModule saveModule = getSaveModule();
        saveModule.savePersistent();
    }

    @Override
    public void update() {
        IContextManager contextManager = getContextManager();
        for (IContext context : contextManager.getActiveContexts()) {
            context.updateScreen();
        }
    }

    //Getters
    @Override
    public IEnvironment getEnv() {
        return env;
    }

    protected IContextManager getContextManager() {
        return env.getContextManager();
    }

    protected ISaveModule getSaveModule() {
        return env.getSaveModule();
    }

    //Setters

}
