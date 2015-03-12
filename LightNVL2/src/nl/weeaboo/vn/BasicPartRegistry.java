package nl.weeaboo.vn;

import nl.weeaboo.game.entity.PartRegistry;
import nl.weeaboo.game.entity.PartType;
import nl.weeaboo.vn.entity.IButtonPart;
import nl.weeaboo.vn.impl.ButtonPart;
import nl.weeaboo.vn.impl.DrawablePart;
import nl.weeaboo.vn.impl.ImagePart;
import nl.weeaboo.vn.impl.InputHandlerPart;
import nl.weeaboo.vn.impl.TransformablePart;
import nl.weeaboo.vn.script.impl.ScriptPart;

public class BasicPartRegistry extends PartRegistry {

	private static final long serialVersionUID = 1L;

    public final PartType<ScriptPart> script;

    public final PartType<DrawablePart> drawable;
	public final PartType<TransformablePart> transformable;
    public final PartType<ImagePart> image;

    public final PartType<? extends IInputHandler> input;

    public final PartType<? extends IButtonPart> button;
    // public final PartType<? extends IButtonDrawablePart> buttonDrawable;

	public BasicPartRegistry() {
        script = register("script", ScriptPart.class);

        drawable = register("drawable", DrawablePart.class);
		transformable = register("transformable", TransformablePart.class);
        image = register("image", ImagePart.class);

        input = register("input", InputHandlerPart.class);

        button = register("button", ButtonPart.class);
        //buttonDrawable = register("buttonDrawable", ButtonDrawable.class);
	}

}
