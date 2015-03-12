package nl.weeaboo.vn.impl;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import nl.weeaboo.vn.IInput;
import nl.weeaboo.vn.IInputHandler;
import nl.weeaboo.vn.entity.IInputHandlerPart;

public class InputHandlerPart extends NovelPart implements IInputHandlerPart {

    private static final long serialVersionUID = BaseImpl.serialVersionUID;

    private List<IInputHandler> inputHandlers = new CopyOnWriteArrayList<IInputHandler>();

    @Override
    public void handleInput(IInput input) {
        for (IInputHandler handler : inputHandlers) {
            handler.handleInput(input);
        }
    }

    @Override
    public <I extends IInputHandler & Serializable> void addInputListener(I handler) {
        inputHandlers.add(handler);
    }

    @Override
    public void removeInputListener(IInputHandler handler) {
        inputHandlers.remove(handler);
    }

}
