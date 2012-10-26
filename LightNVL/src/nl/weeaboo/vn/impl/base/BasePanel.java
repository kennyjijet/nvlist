package nl.weeaboo.vn.impl.base;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import nl.weeaboo.common.Insets2D;
import nl.weeaboo.common.Rect2D;
import nl.weeaboo.lua2.io.LuaSerializable;
import nl.weeaboo.vn.IDrawBuffer;
import nl.weeaboo.vn.IDrawable;
import nl.weeaboo.vn.IInput;
import nl.weeaboo.vn.ILayer;
import nl.weeaboo.vn.IPanel;
import nl.weeaboo.vn.layout.ILayout;
import nl.weeaboo.vn.layout.ILayoutConstraints;
import nl.weeaboo.vn.layout.LayoutComponent;
import nl.weeaboo.vn.layout.NullLayout;

@LuaSerializable
public abstract class BasePanel extends BaseDrawable implements IPanel {

	private static final long serialVersionUID = BaseImpl.serialVersionUID;
	
	private double width, height;
	private Insets2D padding, margin;
	private List<LayoutComponent> components;
	private ILayout layout;
	private boolean layoutDirty;
	
	public BasePanel() {
		padding = new Insets2D(0, 0, 0, 0);
		margin = new Insets2D(0, 0, 0, 0);
		components = new ArrayList<LayoutComponent>();
		layout = NullLayout.INSTANCE;
	}
	
	//Functions
	@Override
	public boolean update(ILayer layer, IInput input, double effectSpeed) {
		if (super.update(layer, input, effectSpeed)) {
			markChanged();
		}
		
		if (layoutDirty) {			
			layout();
		}
		
		return consumeChanged();
	}
	
	@Override
	public void draw(IDrawBuffer d) {
	}

	@Override
	public void invalidateLayout() {
		layoutDirty = true;
	}
	
	@Override
	public void layout() {
		layoutDirty = false;
		
		Rect2D innerBounds = getInnerBounds();
		
		layout.layout(innerBounds, Collections.unmodifiableCollection(components));		
	}
	
	@Override
	public void add(IDrawable d) {
		add(d, null);
	}
	
	@Override
	public void add(IDrawable d, ILayoutConstraints c) {
		components.add(new LayoutComponent(d, c));
		invalidateLayout();
	}

	@Override
	public void remove(IDrawable d) {
		for (Iterator<LayoutComponent> itr = components.iterator(); itr.hasNext(); ) {
			LayoutComponent c = itr.next();
			if (c.component.equals(d)) {
				itr.remove();
				break;
			}
		}
		invalidateLayout();
	}
	
	//Getters
	@Override
	public ILayout getLayout() {
		return layout;
	}
	
	@Override
	public boolean contains(IDrawable d) {
		return components.contains(d);
	}
	
	@Override
	public double getWidth() {
		return width;
	}
	
	@Override
	public double getInnerWidth() {
		return Math.max(0, width - padding.left - padding.right - margin.left - margin.right);
	}

	@Override
	public double getHeight() {
		return height;
	}
	
	@Override
	public double getInnerHeight() {
		return Math.max(0, height - padding.top - padding.bottom - margin.top - margin.bottom);
	}

	@Override
	public Rect2D getInnerBounds() {
		return new Rect2D(getX()+margin.left+padding.left, getY()+margin.top+padding.top, getInnerWidth(), getInnerHeight());
	}
	
	@Override
	public Insets2D getPadding() {
		return padding;
	}

	@Override
	public Insets2D getMargin() {
		return margin;
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
			padding = new Insets2D(top, right, bottom, left);
			
			markChanged();
			invalidateLayout();
		}
	}

	@Override
	public void setMargin(double pad) {
		setMargin(pad, pad, pad, pad);
	}

	@Override
	public void setMargin(double vertical, double horizontal) {
		setMargin(vertical, horizontal, vertical, horizontal);
	}

	@Override
	public void setMargin(double top, double right, double bottom, double left) {
		if (margin.top != top || margin.right != right || margin.bottom != bottom || margin.left != left) {
			margin = new Insets2D(top, right, bottom, left);
			
			markChanged();
			invalidateLayout();
		}
	}
	
}
