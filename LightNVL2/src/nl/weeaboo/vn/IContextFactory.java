package nl.weeaboo.vn;

public interface IContextFactory<C extends IContext> {

    public C newContext();

}
