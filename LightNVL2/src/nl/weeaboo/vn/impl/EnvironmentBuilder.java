package nl.weeaboo.vn.impl;

import nl.weeaboo.vn.BasicPartRegistry;
import nl.weeaboo.vn.IContextManager;
import nl.weeaboo.vn.IImageModule;
import nl.weeaboo.vn.INotifier;
import nl.weeaboo.vn.IRenderEnv;
import nl.weeaboo.vn.ISoundModule;

public class EnvironmentBuilder {

    public IContextManager contextManager;
    public BasicPartRegistry partRegistry;
    public INotifier notifier;
    public IRenderEnv renderEnv;
    public IImageModule imageModule;
    public ISoundModule soundModule;

}
