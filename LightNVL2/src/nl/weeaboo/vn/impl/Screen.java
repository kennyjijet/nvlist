package nl.weeaboo.vn.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;

import nl.weeaboo.common.Checks;
import nl.weeaboo.common.Rect2D;
import nl.weeaboo.game.entity.DefaultEntityStreamDef;
import nl.weeaboo.game.entity.Entity;
import nl.weeaboo.game.entity.EntityStream;
import nl.weeaboo.game.entity.PartType;
import nl.weeaboo.game.entity.Scene;
import nl.weeaboo.vn.BasicPartRegistry;
import nl.weeaboo.vn.ILayer;
import nl.weeaboo.vn.INovelPart;
import nl.weeaboo.vn.IRenderEnv;
import nl.weeaboo.vn.IScreen;
import nl.weeaboo.vn.render.IDrawBuffer;

public class Screen implements IScreen, ILayerHolder {

	private static final long serialVersionUID = BaseImpl.serialVersionUID;

	protected final Scene scene;
	private final Rect2D bounds;
	private final BasicPartRegistry partRegistry;

	private ILayer rootLayer; // Lazily (re-)initialized when null or destroyed
	private ILayer activeLayer; // Could potentially point to a destroyed layer (minor memory leak)
	private IRenderEnv renderEnv;

	private transient EntityStream updateStream;

	public Screen(Scene s, Rect2D bounds, BasicPartRegistry pr, IRenderEnv env) {
		this.scene = Checks.checkNotNull(s);
		this.bounds = Checks.checkNotNull(bounds);
		this.partRegistry = Checks.checkNotNull(pr);
		this.renderEnv = Checks.checkNotNull(env);

		initTransients();
	}

	//Functions
    private void initTransients() {
        updateStream = scene.joinStream(DefaultEntityStreamDef.ALL_ENTITIES_STREAM);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();

        initTransients();
    }

    @Override
    public Entity createEntity() {
        return scene.createEntity();
    }

    @Override
    public void update() {
        // Call update on all IParts
        List<PartType<? extends INovelPart>> partTypes = partRegistry.getParts(INovelPart.class);
        for (Entity e : updateStream) {
            for (PartType<? extends INovelPart> partType : partTypes) {
                INovelPart part = e.getPart(partType);
                if (part != null) {
                    part.update();
                }
            }
        }
    }

	public void draw(IDrawBuffer buffer) {
		Layer layer = (Layer)getRootLayer();
		int layerId = buffer.reserveLayerIds(1);
		layer.draw(buffer, layerId);
	}

	@Override
	public ILayer createLayer(ILayer parentLayer) {
		if (!containsLayer(parentLayer)) {
			throw new IllegalArgumentException("Parent layer (" + parentLayer + ") isn't attached to this screen");
		}
		return doCreateLayer(parentLayer);
	}

	protected ILayer createRootLayer() {
		return doCreateLayer(null);
	}

	private ILayer doCreateLayer(ILayer parentLayer) {
		ILayer layer = newLayer(parentLayer);
		if (parentLayer != null) {
			layer.setBounds(parentLayer.getX(), parentLayer.getY(), parentLayer.getWidth(), parentLayer.getHeight());
			layer.setRenderEnv(parentLayer.getRenderEnv());
		} else {
			layer.setBounds(bounds.x, bounds.y, bounds.w, bounds.h);
			layer.setRenderEnv(renderEnv);
		}
		return layer;
	}

	/**
	 * Creates a new layer.
	 * @param parentLayer If not {@code null}, creates the new layer as a sub-layer of {@code parentLayer}.
	 */
	protected ILayer newLayer(ILayer parentLayer) {
		if (parentLayer == null) {
			return new Layer(this, scene, partRegistry);
		} else {
			// Cast is safe
			return ((Layer)parentLayer).createSubLayer();
		}
	}

	@Override
	public void onSubLayerDestroyed(ILayer layer) {
		if (layer == rootLayer) {
			rootLayer = null;
		} else {
			throw new IllegalStateException("Received a destroyed event from a non-root layer: " + layer);
		}
	}

	//Getters
	@Override
	public ILayer getRootLayer() {
		if (rootLayer == null) {
			rootLayer = createRootLayer();
		}
		return rootLayer;
	}

	@Override
	public ILayer getActiveLayer() {
		if (activeLayer == null || activeLayer.isDestroyed()) {
			activeLayer = getRootLayer();
		}
		return activeLayer;
	}

	protected boolean containsLayer(ILayer layer) {
		return rootLayer != null && (rootLayer == layer || rootLayer.containsLayer(layer));
	}

    @Override
    public IRenderEnv getRenderEnv() {
        return renderEnv;
    }

	//Setters
	public void setRenderEnv(IRenderEnv env) {
		renderEnv = env;

		getRootLayer().setRenderEnv(env);
	}

}
