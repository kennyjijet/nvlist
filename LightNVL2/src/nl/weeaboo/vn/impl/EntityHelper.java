package nl.weeaboo.vn.impl;

import nl.weeaboo.game.entity.Entity;
import nl.weeaboo.vn.BasicPartRegistry;
import nl.weeaboo.vn.ILayer;
import nl.weeaboo.vn.script.impl.ScriptPart;

public class EntityHelper {

    private final BasicPartRegistry pr;

    public EntityHelper(BasicPartRegistry pr) {
        this.pr = pr;
    }

    public Entity createScriptableEntity(ILayer layer) {
        Entity e = layer.createEntity();
        addImageParts(e);
        return e;
    }

    public Entity addScriptParts(Entity e) {
        ScriptPart script = new ScriptPart();

        e.setPart(pr.script, script);

        return e;
    }

    public Entity addImageParts(Entity e) {
        TransformablePart transformable = new TransformablePart();
        ImagePart image = new ImagePart(transformable);

        e.setPart(pr.drawable, transformable);
        e.setPart(pr.transformable, transformable);
        e.setPart(pr.image, image);

        return e;
    }

}
