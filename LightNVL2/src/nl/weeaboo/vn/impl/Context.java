package nl.weeaboo.vn.impl;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import nl.weeaboo.common.Checks;
import nl.weeaboo.game.entity.Entity;
import nl.weeaboo.game.entity.PartType;
import nl.weeaboo.game.entity.Scene;
import nl.weeaboo.vn.IContext;
import nl.weeaboo.vn.IContextListener;
import nl.weeaboo.vn.IScreen;
import nl.weeaboo.vn.script.IScriptContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Context implements IContext {

	private static final long serialVersionUID = BaseImpl.serialVersionUID;
	private static final Logger LOG = LoggerFactory.getLogger(Context.class);

	private final Scene scene;
	private final PartType<? extends DrawablePart> drawablePart;
	private final IScreen screen;
	private final IScriptContext scriptContext;

	private final List<IContextListener> contextListeners = new CopyOnWriteArrayList<IContextListener>();

	private boolean active;
	private boolean destroyed;

    public Context(ContextArgs contextArgs) {
		this.scene = Checks.checkNotNull(contextArgs.scene);
		this.drawablePart = Checks.checkNotNull(contextArgs.drawablePart);
		this.screen = Checks.checkNotNull(contextArgs.screen);
		this.scriptContext = contextArgs.scriptContext;
	}

	//Functions
	@Override
	public final void destroy() {
		if (!destroyed) {
			destroyed = true;

            LOG.debug("Context destroyed: {}", this);
			fireDestroyed();
		}
	}

    @Override
    public void addContextListener(IContextListener contextListener) {
        contextListeners.add(contextListener);
    }

    @Override
    public void removeContextListener(IContextListener contextListener) {
        contextListeners.remove(contextListener);
    }

	@Override
	public void add(Entity e) {
		e.moveToScene(scene);
		DrawablePart.moveToLayer(e.getPart(drawablePart), null);
	}

	@Override
	public boolean contains(Entity e) {
		return scene.contains(e);
	}

	private void fireDestroyed() {
        for (IContextListener cl : contextListeners) {
            cl.onContextDestroyed(this);
        }
	}

	private void fireActiveStateChanged(final boolean activated) {
	    for (IContextListener cl : contextListeners) {
	        if (activated) {
	            cl.onContextActivated(this);
	        } else {
                cl.onContextDeactivated(this);
	        }
	    }
	}

	//Getters
	@Override
	public boolean isDestroyed() {
		return destroyed;
	}

    @Override
    public boolean isActive() {
        return active;
    }

	@Override
	public IScreen getScreen() {
		return screen;
	}

	@Override
	public IScriptContext getScriptContext() {
		return scriptContext;
	}

	//Setters
	void setActive(boolean a) {
	    if (active != a) {
	        active = a;

	        fireActiveStateChanged(a);
	    }
	}

}
