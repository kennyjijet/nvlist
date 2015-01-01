package nl.weeaboo.vn;

public interface IContextListener {

    public void onContextActivated(IContext context);

    public void onContextDeactivated(IContext context);

    public void onContextDestroyed(IContext context);

}
