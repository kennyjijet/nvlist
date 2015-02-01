package nl.weeaboo.vn.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

import nl.weeaboo.common.Rect2D;
import nl.weeaboo.game.entity.Entity;
import nl.weeaboo.game.entity.EntityStream;
import nl.weeaboo.game.entity.PartType;
import nl.weeaboo.game.entity.Scene;
import nl.weeaboo.vn.ILayer;
import nl.weeaboo.vn.IRenderEnv;
import nl.weeaboo.vn.IScreenshotBuffer;
import nl.weeaboo.vn.entity.IDrawablePart;
import nl.weeaboo.vn.render.IDrawBuffer;
import nl.weeaboo.vn.render.impl.ScreenshotBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Layer implements ILayer, ILayerHolder {

	private static final long serialVersionUID = BaseImpl.serialVersionUID;
	private static final Logger LOG = LoggerFactory.getLogger(Layer.class);

	private final ILayerHolder parent;
	private final Scene scene;
	private final PartType<? extends DrawablePart> drawablePart;

	private final BoundsHelper boundsHelper = new BoundsHelper();
	private final ScreenshotBuffer screenshotBuffer = new ScreenshotBuffer();
	private final EntityStream renderStream;
	private final List<Layer> subLayers = new ArrayList<Layer>();

	private IRenderEnv renderEnv;
	private short z;
	private boolean visible = true;
	private boolean destroyed;

	private transient boolean changed;

	public Layer(ILayerHolder parent, Scene s, PartType<? extends DrawablePart> drawablePart) {
		if (parent == null) throw new IllegalArgumentException("Parent may not be null");

		this.parent = parent;
		this.scene = s;
		this.drawablePart = drawablePart;

		renderStream = scene.registerStream(new DrawableZStreamDef(this, drawablePart, -1));

		initTransients();
	}

	//Functions
	private void initTransients() {
		changed = true;
		boundsHelper.setChangeListener(new IChangeListener() {
			@Override
			public void onChanged() {
				markChanged();
			}
		});
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();

		initTransients();
	}

	protected final void markChanged() {
		changed = true;
	}

	@Override
	public final boolean consumeChanged() {
		boolean result = changed;
		changed = false;
		return result;
	}

	@Override
	public final void destroy() {
		if (!destroyed) {
			LOG.debug("Layer destroyed: {}", this);

			destroyed = true;
			parent.onSubLayerDestroyed(this);
		}
	}

	public Layer createSubLayer() {
		Layer result = new Layer(this, scene, drawablePart);
		subLayers.add(result);
		LOG.debug("Sub-layer created: {}, parent={}", result, this);
		return result;
	}

	@Override
	public void add(Entity e) {
		e.moveToScene(scene);
		DrawablePart.moveToLayer(e.getPart(drawablePart), this);
	}

	@Override
	public boolean contains(Entity e) {
		return renderStream.contains(e);
	}

	protected void invalidateStreams() {
		renderStream.invalidate();
	}

	@Override
	public void onSubLayerDestroyed(ILayer layer) {
		subLayers.remove(layer);
	}

	public void draw(IDrawBuffer buffer, int layerId) {
		if (!isVisible()) {
			return;
		}

		buffer.startLayer(layerId, this);

		final Rect2D r = getBounds();
		final double lw = r.w;
		final double lh = r.h;

		//Render drawables (insert layer render commands for the layers)
		for (Entity e : renderStream) {
			if (e.isDestroyed()) {
				continue;
			}

			IDrawablePart dp = e.getPart(drawablePart);
			if (dp.isVisible(.001)) {
				if (!dp.isClipEnabled() || dp.getBounds().intersects(0, 0, lw, lh)) {
					buffer.draw(e);
				}
			}
		}

		//Add render commands for our sub-layers
		int baseSubLayerId = buffer.reserveLayerIds(subLayers.size());
		for (int n = 0; n < subLayers.size(); n++) {
			Layer subLayer = subLayers.get(n);
			if (!subLayer.isDestroyed() && subLayer.isVisible()) {
				buffer.drawLayer(baseSubLayerId+n, subLayer);
			}
		}

		//Add screenshot render commands to the end of the list
		screenshotBuffer.flush(buffer);

		//Recursively render the contents of our sub-layers
		for (int n = 0; n < subLayers.size(); n++) {
			Layer subLayer = subLayers.get(n);
			if (!subLayer.isDestroyed() && subLayer.isVisible()) {
				subLayer.draw(buffer, baseSubLayerId+n);
			}
		}
	}

	@Override
	public String toString() {
	    return String.format("Layer(%08x)", hashCode());
	}

	//Getters
	@Override
	public final boolean isDestroyed() {
		return destroyed;
	}

	@Override
	public final double getX() { return boundsHelper.getX(); }

	@Override
	public final double getY() { return boundsHelper.getY(); }

	@Override
	public final short getZ() { return z; }

	@Override
	public final double getWidth() { return boundsHelper.getWidth(); }

	@Override
	public final double getHeight() { return boundsHelper.getHeight(); }

	@Override
	public Rect2D getBounds() {
		return boundsHelper.getBounds();
	}

	@Override
	public boolean isVisible() {
		return visible;
	}

	@Override
	public boolean containsLayer(ILayer layer) {
		if (layer == null) {
			return false;
		}

		for (ILayer sub : subLayers) {
			if (sub == layer || sub.containsLayer(layer)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public IScreenshotBuffer getScreenshotBuffer() {
		return screenshotBuffer;
	}

	@Override
	public IRenderEnv getRenderEnv() {
		return renderEnv;
	}

	//Setters
	@Override
	public final void setX(double x) { setPos(x, getY()); }

	@Override
	public final void setY(double y) { setPos(getX(), y); }

	@Override
	public void setZ(short z) {
		if (this.z != z) {
			this.z = z;
			markChanged();
		}
	}

	@Override
	public final void setWidth(double w) { setSize(w, getHeight()); }

	@Override
	public final void setHeight(double h) { setSize(getWidth(), h); }

	@Override
	public void setPos(double x, double y) { boundsHelper.setPos(x, y); }

	@Override
	public void setSize(double w, double h) { boundsHelper.setSize(w, h); }

	@Override
	public void setBounds(double x, double y, double w, double h) {
		setPos(x, y);
		setSize(w, h);
	}

	@Override
	public void setRenderEnv(IRenderEnv env) {
		for (ILayer layer : subLayers) {
			layer.setRenderEnv(env);
		}

		for (Entity e : renderStream) {
			IDrawablePart d = e.getPart(drawablePart);
			d.setRenderEnv(env);
		}
	}

	@Override
	public void setVisible(boolean v) {
		if (visible != v) {
			visible = v;
			markChanged();
		}
	}

}
