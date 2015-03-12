package nl.weeaboo.vn.entity;

import java.io.Serializable;

public interface INovelPart extends Serializable {

    /**
     * This method is called every frame while the part is attached to an active Entity.
     */
    public void update();

}
