package nl.weeaboo.vn.impl.base;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import nl.weeaboo.common.Insets2D;
import nl.weeaboo.common.Rect2D;
import nl.weeaboo.lua2.io.LuaSerializable;
import nl.weeaboo.vn.IContainer;
import nl.weeaboo.vn.IDrawBuffer;
import nl.weeaboo.vn.IDrawable;
import nl.weeaboo.vn.IInput;
import nl.weeaboo.vn.ILayer;
import nl.weeaboo.vn.layout.DrawableLayoutComponent;
import nl.weeaboo.vn.layout.ILayout;
import nl.weeaboo.vn.layout.ILayoutComponent;
import nl.weeaboo.vn.layout.ILayoutConstraints;
import nl.weeaboo.vn.layout.NullLayout;

@LuaSerializable
public abstract class BaseContainer extends BaseDrawable implements IContainer {

	private static final long serialVersionUID = BaseImpl.serialVersionUID;
	
	private double width, height;
	private Insets2D padding;
	private LinkedHashMap<IDrawable, ILayoutComponent> components;
	private ILayout layout;
	private boolean layoutDirty;
	
	public BaseContainer() {
		padding = new Insets2D(0, 0, 0, 0);
		components = new LinkedHashMap<IDrawable, ILayoutComponent>();
		layout = NullLayout.INSTANCE;
	}
	
	//Functions
	@Override
	public void destroy() {
		if (!isDestroyed()) {
			for (IDrawable d : components.keySet()) {
				d.destroy();
			}
			components.clear();
			super.destroy();
		}
	}
	
	protected void removeDestroyedComponents() {
		for (Iterator<IDrawable> itr = components.keySet().iterator(); itr.hasNext(); ) {
			IDrawable d = itr.next();
			if (d.isDestroyed()) {
				itr.remove();
			}
		}
	}
	
	@Override
	public boolean update(ILayer layer, IInput input, double effectSpeed) {
		removeDestroyedComponents();
		
		if (super.update(layer, input, effectSpeed)) {
			markChanged();
		}
		
		validateLayout();
		
		return consumeChanged();
	}
	
	@Override
	public void draw(IDrawBuffer d) {
		//d.drawQuad(getZ(), isClipEnabled(), getBlendMode(), 0xFFFF00FF, null, getTransform(), 0, 0, getWidth(), getHeight(), getPixelShader());
	}

	@Override
	public void invalidateLayout() {
		layoutDirty = true;
	}
	
	@Override
	public void validateLayout() {
		if (layoutDirty) {
			layoutDirty = false;
			layout();
		}
	}
	
	@Override
	public void layout() {
		layoutDirty = false;
		
		layout.layout(getLayoutBounds(), getLayoutComponents());		
	}
	
	@Override
	public void add(IDrawable d) {
		add(d, null);
	}
	
	@Override
	public void add(IDrawable d, ILayoutConstraints c) {
		components.put(d, createLayoutComponent(d, c));
		if (d.getZ() >= getZ()) {
			d.setZ((short)(getZ() - 1));
		}
		invalidateLayout();
	}

	protected ILayoutComponent createLayoutComponent(IDrawable d, ILayoutConstraints c) {
		return new DrawableLayoutComponent(d, c);
	}
	
	@Override
	public void remove(IDrawable d) {
		for (Iterator<Entry<IDrawable, ILayoutComponent>> itr = components.entrySet().iterator(); itr.hasNext(); ) {
			Entry<IDrawable, ILayoutComponent> entry = itr.next();
			if (entry.getKey().equals(d)) {
				itr.remove();
				break;
			}
		}
		invalidateLayout();
	}
	
	protected void translateComponents(double dx, double dy) {
		if (Math.abs(dx) <= .0001 && Math.abs(dy) <= .0001) {
			return; //We don't have to do anything
		}
		
		for (ILayoutComponent lc : components.values()) {
			lc.setPos(lc.getX()+dx, lc.getY()+dy);
		}
	}
	
	//Getters
	protected Collection<ILayoutComponent> getLayoutComponents() {
		return Collections.unmodifiableCollection(components.values());		
	}
	
	@Override
	public ILayout getLayout() {
		return layout;
	}
	
	protected Rect2D getLayoutBounds() {
		return getInnerBounds();
	}
	
	@Override
	public boolean contains(IDrawable d) {
		return components.containsKey(d);
	}
	
	@Override
	public double getWidth() {
		return width;
	}
	
	@Override
	public double getInnerWidth() {
		return Math.max(0, width - padding.left - padding.right);
	}

	@Override
	public double getHeight() {
		return height;
	}
	
	@Override
	public double getInnerHeight() {
		return Math.max(0, height - padding.top - padding.bottom);
	}

	@Override
	public Rect2D getInnerBounds() {
		return new Rect2D(getX()+padding.left, getY()+padding.top, getInnerWidth(), getInnerHeight());
	}
	
	@Override
	public Insets2D getPadding() {
		return padding;
	}
	
	//Setters
	@Override
	public void setLayout(ILayout l) {
		if (l == null) l = NullLayout.INSTANCE;
		
		if (layout != l) {
			layout = l;
			invalidateLayout();
		}
	}
	
	@Override
	public void setPos(double x, double y) {
		double oldX = getX();
		double oldY = getY();
		if (oldX != x || oldY != y) {
			super.setPos(x, y);
			
			translateComponents(x-oldX, y-oldY);
		}
	}
	
	@Override
	public void setSize(double w, double h) {
		if (width != w || height != h) {
			width = w;
			height = h;
			
			markChanged();
			invalidateLayout();
		}
	}

	@Override
	public void setBounds(double x, double y, double w, double h) {
		setPos(x, y);
		setSize(w, h);
	}
	
	@Override
	public void setPadding(double pad) {
		setPadding(pad, pad, pad, pad);
	}

	@Override
	public void setPadding(double vertical, double horizontal) {
		setPadding(vertical, horizontal, vertical, horizontal);
	}

	@Override
	public void setPadding(double top, double right, double bottom, double left) {
		if (padding.top != top || padding.right != right || padding.bottom != bottom || padding.left != left) {
			double oldLeft = padding.left;
			double oldTop = padding.top;
			
			padding = new Insets2D(top, right, bottom, left);
			
			translateComponents(left-oldLeft, top-oldTop);
			
			markChanged();
			invalidateLayout();
		}
	}
	
}
