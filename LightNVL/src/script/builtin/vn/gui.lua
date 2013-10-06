--- User interface related functionality.
module("vn.gui", package.seeall)

-- ----------------------------------------------------------------------------
--  Classes
-- ----------------------------------------------------------------------------


-- ----------------------------------------------------------------------------
--  Functions
-- ----------------------------------------------------------------------------

---Creates a new Panel. Other user interface components can be added to the
-- panel, which allows the panel to handle their layout based on its Layout.
-- Giving the panel a border is the easiest way to draw a bordered or rounded
-- rectangle.
-- @number w Initial width of the panel.
-- @number h Initial height of the panel.
-- @treturn Panel The newly created Panel.
function createPanel(w, h)
	w = w or 0
	h = h or 0

	local panel = GUI.createPanel(getImageLayer())	
	panel:setSize(w, h)
	return panel
end

---Creates a new Viewport. Viewports create a fixed-size scrollable area for
-- their sub-components. Optional horizontal and vertical scrollbars can be
-- added to provide a visual indication that the contents of the viewport can
-- be scrolled.
-- @number w Initial width of the viewport.
-- @number h Initial height of the viewport.
-- @treturn Viewport The newly created Viewport.
function createViewport(w, h)
	w = w or 0
	h = h or 0

	local viewport = GUI.createViewport(getImageLayer())	
	viewport:setSize(w, h)	
	return viewport
end

local function createLayout(classDef, overrides)
	local ly = classDef.new()
	setProperties(ly, overrides)
	return ly
end

---Creates a new FlowLayout object. FlowLayouts arrange their components in
-- rows, placing as many components one a row as possible before moving on to
-- the next row. 
-- @tab[opt={}] layoutProperties A table containing property overrides for the
--              newly created layout object.
function createFlowLayout(layoutProperties)
	return createLayout(FlowLayout, layoutProperties)
end

---Creates a new GridLayout object. GridLayouts arrange their components in a
-- fixed-column uniformly sized grid. The components can optionally be made to
-- scale to fit the grid cells if the <code>stretch</code> property is set to
-- <code>true</code>.
-- @tab[opt={}] layoutProperties A table containing property overrides for the
--              newly created layout object.
function createGridLayout(layoutProperties)
	return createLayout(GridLayout, layoutProperties)
end

---Uses the given layout to arrange the given components. This function can
-- layout any object which implements getBounds/setBounds methods, not just
-- Drawables.
-- @tparam Layout layout The layout object to use, or a table containing a
--        <code>new</code> function which can be used to create a new layout
--        object (<code>FlowLayout</code>, <code>GridLayout</code>, etc.)
-- @number x The top-left x coordinate of the layout bounds.
-- @number y The top-left y coordinate of the layout bounds.
-- @number w The width of the layout bounds.
-- @number h The height of the layout bounds.
-- @tab[opt={}] layoutProperties A table containing property overrides for the
--              layout object.
-- @tab components A table of things to layout, must implement
--      getBounds/setBounds methods.
function doLayout(layout, x, y, w, h, layoutProperties, components)
	if type(layout) == "table" then
		layout = createLayout(layout)
	end
	setProperties(layout, layoutProperties)
	
	local lcs = {}
	local i = 1
	for _,c in pairs(components) do		
		lcs[i] = toLayoutComponent(c)		
		i = i + 1		
	end
	
	layout:layout(x or 0, y or 0, w or 0, h or 0, lcs)
	
	local i = 1
	for _,c in pairs(components) do
		local lc = lcs[i]
		transferBounds(lc, c)
		i = i + 1
	end
end

---Creates an dummy of component to use as a spacer in layouts or containers
-- (Panels, Viewports, etc.)
-- @number x The top-left x coordinate for the component.
-- @number y The top-left y coordinate for the component.
-- @number w The width of the component.
-- @number h The height of the component.
-- @treturn LayoutComponent A new (invisible) component.
function createLayoutComponent(x, y, w, h)
	return GUI.createLayoutComponent(x, y, w, h)
end

---Creates a layout component from the given userdata or table. The initial
-- bounds for the component are the values returned from a call
-- <code>c:getBounds()</code>.
-- @param c The Lua userdata or table to initialize the layout component with.
-- @treturn LayoutComponent A new layout component based on the bounds of
--          <code>c</code>.
function toLayoutComponent(c)
	return createLayoutComponent(unpack(getProperty(c, "bounds")))
end

---Applies the bounds of one component to another.
-- @param from The object containing the source bounds.
-- @param to The destination object to update the bounds of.
function transferBounds(from, to)
	return to:setBounds(unpack(getProperty(from, "bounds")))
end

---Returns the correct X offset for a component with width <code>inner</code>,
-- inside a parent container of width <code>outer</code>, horizontally aligned
-- based on the given anchor.
-- @number outer Width of the container.
-- @number inner Width of the component inside the container.
-- @int anchor Alignment of the component within the container. These
--      correspond to numpad directions (4=left, 5=center, 6=right).
-- @treturn number The correct X offset for the component.
function alignAnchorX(outer, inner, anchor)
	if anchor == 2 or anchor == 5 or anchor == 8 then
		return (outer-inner) / 2
	elseif anchor == 3 or anchor == 6 or anchor == 9 then
		return (outer-inner)
	end
	return 0		
end

---Returns the correct Y offset for a component with height <code>inner</code>,
-- inside a parent container of height <code>outer</code>, vertically aligned
-- based on the given anchor.
-- @number outer Width of the container.
-- @number inner Width of the component inside the container.
-- @param anchor Alignment of the component within the container. These
--        correspond to numpad directions (8=top, 5=center, 2=bottom).
-- @treturn number The correct Y offset for the component.
function alignAnchorY(outer, inner, anchor)
	if anchor >= 4 and anchor <= 6 then
		return (outer-inner) / 2
	elseif anchor >= 1 and anchor <= 3 then
		return (outer-inner)
	end
	return 0		
end
