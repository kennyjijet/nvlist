package nl.weeaboo.vn.core.impl;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import nl.weeaboo.vn.IInput;
import nl.weeaboo.vn.IInputHandlerPart;
import nl.weeaboo.vn.IInputListener;

public class InputHandlerPart extends NovelPart implements IInputHandlerPart {

    private static final long serialVersionUID = BaseImpl.serialVersionUID;

    private List<IInputListener> inputHandlers = new CopyOnWriteArrayList<IInputListener>();

    @Override
    public void handleInput(IInput input, boolean mouseContains) {
        for (IInputListener handler : inputHandlers) {
            handler.handleInput(input, mouseContains);
        }
    }

    @Override
    public <I extends IInputListener & Serializable> void addInputListener(I handler) {
        inputHandlers.add(handler);
    }

    @Override
    public void removeInputListener(IInputListener handler) {
        inputHandlers.remove(handler);
    }

}
