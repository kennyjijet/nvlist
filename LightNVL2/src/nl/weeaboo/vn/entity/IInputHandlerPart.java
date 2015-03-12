package nl.weeaboo.vn.entity;

import java.io.Serializable;

import nl.weeaboo.vn.IInputHandler;

/**
 * Interface for the part of an entity that is responsible for distributing input events (keyboard, mouse,
 * gamepad) to listeners.
 */
public interface IInputHandlerPart extends INovelPart, IInputHandler {

    /** Adds an instance of the given input handler */
    public <I extends IInputHandler & Serializable> void addInputListener(I handler);

    /** Tries to remove the first instance of the given input handler */
    public void removeInputListener(IInputHandler handler);

}
