package nl.weeaboo.vn;

public interface IInputHandler {

    /**
     * Allows this input handler to process the given input. The coordinates of the given input objects are
     * generally normalized to the coordinate system used by this object.
     */
    public void handleInput(IInput input);

}
