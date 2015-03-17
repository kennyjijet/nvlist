package nl.weeaboo.vn;

import java.io.Serializable;

public interface INovel extends Serializable, IUpdateable {

    public void start();
    public void stop();

    public IEnvironment getEnv();

}
