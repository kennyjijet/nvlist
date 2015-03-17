package nl.weeaboo.vn;

import java.io.Serializable;

public interface INovelPart extends Serializable, IUpdateable {

    /**
     * This method is called every frame while the part is attached to an active Entity.
     */
    @Override
    public void update();

}
