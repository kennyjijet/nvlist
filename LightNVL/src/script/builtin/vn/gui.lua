-------------------------------------------------------------------------------
-- gui.lua
-------------------------------------------------------------------------------
-- User-interface related classes and functions
-------------------------------------------------------------------------------

module("vn.gui", package.seeall)

-- ----------------------------------------------------------------------------
--  Local functions
-- ----------------------------------------------------------------------------

local function setVisible(c, v)
	if c == nil then
		return
	end
	if c.setVisible ~= nil then
		return c:setVisible(v)
	end	
	if v then
		return c:setAlpha(1)
	else
		return c:setAlpha(0)
	end
end

-- ----------------------------------------------------------------------------
--  Classes
-- ----------------------------------------------------------------------------

GUIComponent = {
	x=0,
	y=0,
	w=1,
	h=1
}

function GUIComponent.new(self)
	return extend(GUIComponent, self)
end

function GUIComponent:getX()
	return self.x
end

function GUIComponent:getY()
	return self.y
end

function GUIComponent:getWidth()
	return self.w
end

function GUIComponent:getHeight()
	return self.h
end

function GUIComponent:setPos(x, y)
	return self:setBounds(x, y, self:getWidth(), self:getHeight())
end

function GUIComponent:setSize(w, h)
	return self:setBounds(self:getX(), self:getY(), w, h)
end

function GUIComponent:setBounds(x, y, w, h)
	self.x = x
	self.y = y
	self.w = w
	self.h = h
end

function GUIComponent:getVisible()
	return self.visible
end

function GUIComponent:setVisible(v)
	self.visible = v
	if self.setAlpha ~= nil then
		if v then
			self:setAlpha(1)
		else
			self:setAlpha(0)
		end
	end
end

-- ----------------------------------------------------------------------------

-- Declares a button/text drawable hybrid
TextButton = {
	button=nil,
	text=nil,
	alpha=1
}

---Creates a new TextButton; a button with a text drawable on top.
-- @param background the image path to use for the button
-- @param text The text for the button label
function TextButton.new(background, text, self)
	self = GUIComponent.new(extend(TextButton, self))
	
	self.button = self.button or button(background)
	self.text = self.text or textimg(text)
	self.text:setAnchor(5)
	
	if self.button:getTexture() == nil then
		local w = self.text:getTextWidth()
		local h = self.text:getTextHeight()
		local pad = math.max(2, math.min(w, h) / 4)
		w = w + pad * 2
		h = h + pad * 2
		self.button:setNormalTexture(colorTex(0xC0000000, w, h))
		self.button:setRolloverTexture(colorTex(0xC0808080, w, h))
		self.button:setPressedTexture(colorTex(0xC0404040, w, h))
	end

	self:fitText()
	
	return self
end

function TextButton:destroy()
	self.button:destroy()
	self.text:destroy()
end

function TextButton:setZ(z)
	self.button:setZ(z)
	self:fitText()
end

function TextButton:getAlpha(a)
	return self.alpha
end

function TextButton:setAlpha(a)
	self.alpha = a
	self.button:setAlpha(a)
	self.text:setAlpha(a)
end

function TextButton:setSelected(s)
	self.button:setSelected(s)
end

function TextButton:fitText()
	local b = self.button
	local txt = self.text
	txt:setBounds(b:getX(), b:getY(), b:getWidth(), b:getHeight())
	txt:setZ(b:getZ() - 10)
end

function TextButton:getX()
	return self.button:getX()
end

function TextButton:getY()
	return self.button:getY()
end

function TextButton:getWidth()
	return self.button:getWidth()
end

function TextButton:getHeight()
	return self.button:getHeight()
end

function TextButton:setBounds(x, y, w, h)
	self.button:setBounds(x, y, w, h)
	self:fitText()
end

function TextButton:consumePress()
	return self.button:consumePress()
end

-- ----------------------------------------------------------------------------

local ScrollBar = {
	w=10,
	h=10,
	horizontal=false,
	bar=nil,
	thumb=nil,
	pad=0,
	scrollFrac=-1,
}

function ScrollBar.new(self)
	self = GUIComponent.new(extend(ScrollBar, self))

	local sfx = ""
	if self.horizontal then
		sfx = "-h"
	end
	
	if self.thumb == nil then	
		local thumb = img("gui/components#scroll-thumb" .. sfx, {z=-1002})		
		if self.horizontal then
			local scale = self.h / thumb:getUnscaledHeight()
			thumb:setScale(scale, scale)
		else
			local scale = self.w / thumb:getUnscaledWidth()
			thumb:setScale(scale, scale)
		end
		self.thumb = thumb
	end
	
	if self.bar == nil then
	    local bar = img("gui/components#scroll-bg" .. sfx, {z=-1001})
		self.bar = bar
	end
	
	self:layout(false)
	
	return self
end

function ScrollBar:destroy()
	destroyValues(self.thumb, self.bar)
end

function ScrollBar:layout(scrollOnly)
	local bar = self.bar
	local thumb = self.thumb
	local x = self.x
	local y = self.y
	local w = self.w
	local h = self.h
	local pad = self.pad
	local canScroll = (self.scrollFrac >= 0)

	if bar ~= nil then
		setVisible(bar, canScroll and self.visible)
		if not scrollOnly then
			if self.horizontal then
				bar:setSize(w-pad*2, h)
				bar:setPos(x+pad, y)
			else
				bar:setSize(w, h-pad*2)
				bar:setPos(x, y+pad)
			end
		end
	end
	
	if thumb ~= nil then
		setVisible(thumb, canScroll and self.visible)

		if self.horizontal then		
			thumb:setPos(x + pad + self.scrollFrac * (w-pad*2-thumb:getWidth()), y + (h-thumb:getHeight())/2)
		else
			thumb:setPos(x + (w-thumb:getWidth())/2, y + pad + self.scrollFrac * (h-pad*2-thumb:getHeight()))
		end
	end
end

function ScrollBar:scrollTo(frac)
	self.scrollFrac = frac
	self:layout(true)
end

function ScrollBar:setVisible(v)
	self.visible = v
	self:layout(false)
end

function ScrollBar:setBounds(x, y, w, h)
	GUIComponent.setBounds(self, x, y, w, h)	
	self:layout(false)
end

-- ----------------------------------------------------------------------------

-- Declares a button/text drawable hybrid
Viewport = {
	w=100,
	h=100,
	vw=0,
	vh=0,
	pad=0,
	alpha=1.0,
	children=nil,
	basePositions=nil,
	layer=nil,
	scrollWheelSpeed=8,
	snapInertia=.8,
	mouseSnap=nil,
	scrollThumb=nil,
	topEdge=nil,
	bottomEdge=nil,
	fadeEdgeLength=screenHeight*.03,
	scrollBarX=nil,
	scrollBarY=nil,
	scrollBarWidth=screenHeight*.02,
	--Internal use
	lastMouseX=nil,
	lastMouseY=nil,
	scrollX=nil,
	scrollY=nil,
	snap=0,
}

---Creates a new Viewport; a scrollable section of the screen containing other
-- Drawables or GUI components.
function Viewport.new(self)
	self = GUIComponent.new(extend(Viewport, self))		
		
	self.children = self.children or {}
	self.basePositions = self.basePositions or {}
	
	self.scrollX = self.scrollX or {pos=0, min=0, max=0, spd=0}
	self.scrollY = self.scrollY or {pos=0, min=0, max=0, spd=0}
	
	if self.mouseSnap == nil then
		if System.isLowEnd() then
			self.mouseSnap = .2
		else
			self.mouseSnap = .1
		end
	end
	
	self.layer = createLayer()	
	
	return self
end

function Viewport:destroy()
	destroyValues(self.scrollBarX, self.scrollBarY, self.topEdge, self.bottomEdge)
	destroyValues(self.children)	
	self.children = {}
	
	if self.layer ~= nil then
		self.layer:destroy()
		self.layer = nil
	end
end

function Viewport:getZ()
	return self.layer:getZ()
end

function Viewport:setZ(z)
	self.layer:setZ(z)
end

function Viewport:getInnerWidth(ignoreScrollBar)
	local iw = self:getWidth() - self.pad*2
	if not ignoreScrollBar then
		if self.scrollBarY ~= nil then
			iw = iw - self.scrollBarY:getWidth()
		else
			iw = iw - self.scrollBarWidth
		end
	end
	return iw
end

function Viewport:getInnerHeight(ignoreScrollBar)
	local ih = self:getHeight() - self.pad*2
	if not ignoreScrollBar then
		if self.scrollBarX ~= nil then
			ih = ih - self.scrollBarX:getHeight()
		else
			ih = ih - self.scrollBarWidth
		end
	end
	return ih
end

function Viewport:update()
	local snap = self.snap
	local mx = self.lastMouseX
	local my = self.lastMouseY

	local scx = self.scrollX
	local oldxpos = scx.pos
	local scy = self.scrollY
	local oldypos = scy.pos
	
	local mouseX = input:getMouseX()
	local mouseY = input:getMouseY()
	local mouseContained = self.layer:contains(mouseX, mouseY)

	--Calculate mouse drag
	local dragging = input:isMouseHeld() and (mx ~= nil or my ~= nil or (mouseContained and input:consumeMouse()))
	if dragging then
	    snap = self.mouseSnap        
	    
	    if scy.min < scy.max and my ~= nil then
			scy.spd = my - mouseY
		else
			scy.spd = 0
		end
		
		if scx.min < scx.max and mx ~= nil then
			scx.spd = mx - mouseX
		else
			scx.spd = 0
		end
		
		mx = mouseX
		my = mouseY
	else
		scx.spd = scx.spd * self.snapInertia
		scy.spd = scy.spd * self.snapInertia
	    
	    if mouseContained then
		    local mscroll = self.scrollWheelSpeed * input:getMouseScroll()
		    if mscroll ~= 0 then
		        snap = 1
		        scy.spd = mscroll
		    end
		end
		
		mx = nil
		my = nil
	end	
	
	--Limit speed and update pos
	if scx.spd > -1 and scx.spd < 1 then
		scx.spd = 0
	else
		scx.pos = scx.pos + scx.spd
	end
	if scy.spd > -1 and scy.spd < 1 then
		scy.spd = 0
	else
		scy.pos = scy.pos + scy.spd
	end

	--Snapback
	for axis=1,2 do
		local s = scx
		if axis == 2 then
			s = scy
		end
		
		if not dragging then
			if s.pos < s.min then
				local dist = s.min - s.pos
				if math.abs(dist) < 1 then
					s.pos = s.min
				else
					s.pos = math.min(s.min, s.pos + dist * snap)				
				end
			elseif s.pos > s.max then
				local dist = s.max - s.pos
				if math.abs(dist) < 1 then
					s.pos = s.max
				else
					s.pos = math.max(s.max, s.pos + dist * snap)
				end
			end
		end
	end
		
	self.snap = snap
	self.lastMouseX = mx
	self.lastMouseY = my
	
	if scy.pos ~= oldypos or scx.pos ~= oldxpos then
		self:layout(true)
	end
end

function Viewport:layout(scrollOnly)
	local x = self:getX()
	local y = self:getY()
	local w = self:getWidth()
	local h = self:getHeight()
	self.layer:setBounds(x, y, w, h)

	local barXH = 0
	if self.scrollBarX ~= nil and self.scrollBarX:getVisible() then
		barXH = self.scrollBarX:getHeight()
	end
	local barYW = 0
	if self.scrollBarY ~= nil and self.scrollBarY:getVisible() then
		barYW = self.scrollBarY:getWidth()
	end
	
	local scx = self.scrollX
	scx.min = 0
	scx.max = math.max(scx.min, self.vw - w + barYW)
	
	local scy = self.scrollY
	scy.min = 0
	scy.max = math.max(scy.min, self.vh - h + barXH)
	
	if not scrollOnly then		
		self:layoutScrollBars()
		self:layoutChildren()
	end
	
	for axis=1,2 do
		local s = scx
		local bar = self.scrollBarX
		if axis == 2 then
			s = scy
			bar = self.scrollBarY
		end
		
		local scrollFrac = -1
		if s.max > s.min then
			scrollFrac = math.max(0, math.min(1, (s.pos-s.min) / math.abs(s.max-s.min)))
			bar:scrollTo(scrollFrac)
			setVisible(bar, true)
		else
			setVisible(bar, false)
		end
	end
	
	if self.topEdge ~= nil then
		local fl = math.min(self.fadeEdgeLength, scy.pos-scy.min)
		self.topEdge:setBounds(0, 0, w, fl)
	end
	if self.bottomEdge ~= nil then
		local fl = math.min(self.fadeEdgeLength, scy.max-scy.pos)
		local bottomY = h
		if self.scrollBarX ~= nil and self.scrollBarX:getVisible() then
			bottomY = self.scrollBarX:getY()
		end
		self.bottomEdge:setBounds(0, bottomY-fl+1, w, fl)
	end
	
	for i,c in ipairs(self.children) do
		local newX = self.basePositions[i].x - scx.pos
		local newY = self.basePositions[i].y - scy.pos
		local rightX = newX + c:getWidth()
		local bottomY = newY + c:getHeight()
		
		c:setPos(newX, newY)
		setVisible(c, bottomY >= 0 and newY <= h and rightX >= 0 and newX <= w)
	end
end

function Viewport:layoutChildren()
	local basepos = self.basePositions
	for i,c in ipairs(self.children) do	
		c:setPos(basepos[i].x, basepos[i].y)
	end	
end

function Viewport:layoutScrollBars()
	local barY = self.scrollBarY
	local barYW = 0
	if barY ~= nil then
		barYW = barY:getWidth()
		barY:setBounds(self.w-barYW, 0, barYW, self.h)		
	end
	
	local barX = self.scrollBarX
	if barX ~= nil then
		barX:setBounds(0, self.h-barX:getHeight(), self.w-barYW, barX:getHeight())
	end
end

function Viewport:openLayer()
	setImageLayer(self.layer)
end

function Viewport:closeLayer(components, edgeInfo, scrollInfo)
	local pathPrefix = "gui/components#"
	if android then
		pathPrefix = "android/components#"
	end

	destroyValues(self.topEdge, self.bottomEdge)
		edgeInfo = edgeInfo or {}
	if edgeInfo == false then
		self.topEdge = nil
		self.bottomEdge = nil
	else	
		self.topEdge = img(edgeInfo.top or pathPrefix .. "fade-down",
			extend({z=-999, colorRGB=0}, edgeInfo.topExtra))
		self.bottomEdge =  img(edgeInfo.bottom or pathPrefix .. "fade-up",
			extend({z=-999, colorRGB=0}, edgeInfo.bottomExtra))
	end
	
	destroyValues(self.scrollBarX, self.scrollBarY)
	scrollInfo = scrollInfo or {}
	self.scrollBarWidth = scrollInfo.scrollBarWidth or self.scrollBarWidth
	self.scrollBarX = ScrollBar.new{h=self.scrollBarWidth, pad=self.scrollBarPad, horizontal=true}
	self.scrollBarY = ScrollBar.new{w=self.scrollBarWidth, pad=self.scrollBarPad}
	
	setImageLayer(nil)
	
	for _,c in ipairs(components) do
		table.insert(self.children, c)
	end
	self:layoutVirtual()
	self:layout(false)
end

function Viewport:layoutVirtual()
	local pad = self.pad
	local vw = 0
	local vh = 0
	for i,c in ipairs(self.children) do
		self.children[i] = c
		local cx = c:getX()
		local cy = c:getY()
		self.basePositions[i] = {x=cx+pad, y=cy+pad}
		vw = math.max(vw, cx+c:getWidth()+pad*2)
		vh = math.max(vh, cy+c:getHeight()+pad*2)		
	end
	self.vw = vw
	self.vh = vh
end

function Viewport:scrollTo(xfrac, yfrac)
	if xfrac ~= nil then
		local scx = self.scrollX
		scx.pos = scx.min + xfrac * (scx.max - scx.min)
	end	
	if yfrac ~= nil then
		local scy = self.scrollY
		scy.pos = scy.min + yfrac * (scy.max - scy.min)
	end	
	self:layout(true)
end

-- ----------------------------------------------------------------------------

Layout = {
	children=nil
}

function Layout.new(self)
	self = GUIComponent.new(extend(Layout, self))
	self.children = self.children or {}
	return self
end

function Layout:add(d)
	table.insert(self.children, d)
end

function Layout:remove(d)
	removeAll(self.children, d)
end

function Layout:layout()
	for _,c in pairs(self.children) do
		c:setSize(self.w, self.h)
		c:setPos(self.x, self.y)
	end
end

-- ----------------------------------------------------------------------------

GridLayout = {
	cols=-1,
	pad=0,	
	fillW=false,
	fillH=false,
	pack=0
}

---Creates a new grid layout object, layouts out its subcomponents in
-- equal-sized cells.
function GridLayout.new(self)
	self = Layout.new(extend(GridLayout, self))
	return self
end

function GridLayout:layout()
	local cols = self.cols
	local rows = 1
	if cols > 0 then
		rows = math.ceil(#self.children / cols)
	else
		cols = #self.children
	end
	
	local pad = self.pad
	local w = self.w
	local h = self.h
	
	local colW = (w-(cols-1)*pad) / cols
	local rowH = (h-(rows-1)*pad) / rows
	local maxColW = 0
	local maxRowH = 0
	for _,d in ipairs(self.children) do
		if self.fillW or self.fillH then
			local targetW = colW
			if not self.fillW then
				targetW = d:getWidth()
			end
			local targetH = rowH
			if not self.fillH then
				targetH = d:getHeight()
			end
			d:setSize(targetW, targetH)
		end
		if self.pack then
			maxColW = math.max(maxColW, d:getWidth())
			maxRowH = math.max(maxRowH, d:getHeight())
		end
	end
		
	local startX = self.x
	local startY = self.y		
	if self.pack > 0 then
		colW = math.min(colW, maxColW)
		rowH = math.min(rowH, maxRowH)
		startX = startX + alignAnchorX(w, cols*(colW+pad)-pad, self.pack)
		startY = startY + alignAnchorY(h, rows*(rowH+pad)-pad, self.pack)
	end
	
	local x = startX
	local y = startY
	local c = 0
	for _,d in ipairs(self.children) do
		d:setPos(x + alignAnchorX(colW, d:getWidth(), 5),
				 y + alignAnchorY(rowH, d:getHeight(), 5))
	
		c = c + 1
		x = x + colW + pad
		if cols >= 0 and c >= cols then
			c = 0
			x = startX
			y = y + rowH + pad
		end
	end
end


-- ----------------------------------------------------------------------------

FlowLayout = {
	pack=7,
	pad=0,
	cols=-1,
	w=-1
}

---Creates a new flow layout object, layouts its components in a line.
function FlowLayout.new(self)
	self = Layout.new(extend(FlowLayout, self))
	return self
end

function FlowLayout:layoutLine(components, x, y, pack, pad, width)
	if components == nil or #components == 0 then
		return 0
	end
	
	--Determine line height
	local lw = math.max(0, #components - 1) * pad
	local lh = 0
	for _,c in ipairs(components) do
		lw = lw + c:getWidth()
		lh = math.max(lh, c:getHeight())
	end
	self.w = math.max(self.w, lw)
	
	--Layout components
	if width >= 0 then
		x = x + alignAnchorX(width, lw, pack)
	end
	
	for _,c in ipairs(components) do
		c:setPos(x, y + alignAnchorY(lh, c:getHeight(), pack))		
		x = x + pad + c:getWidth()
	end	
		
	return lh
end

function FlowLayout:layout()
	local pad = self.pad
	local x = self.x + pad
	local y = self.y + pad
	local cols = self.cols
	
	local lineComponents = {}
	local lineSize = 0
	local maxLineSize = self.w - pad*2
	
	for _,c in ipairs(self.children) do
		local size = c:getWidth()
		if (maxLineSize >= 0 and lineSize + pad + size > maxLineSize) or (cols > 0 and #lineComponents >= cols) then
			if #lineComponents > 0 then
				y = y + pad + self:layoutLine(lineComponents, x, y, self.pack, pad, maxLineSize)
				lineComponents = {}
			end
			lineSize = 0
		end
		table.insert(lineComponents, c)
	end
	y = y + pad + self:layoutLine(lineComponents, x, y, self.pack, pad, maxLineSize)
end

-- ----------------------------------------------------------------------------
--  Functions
-- ----------------------------------------------------------------------------

---Returns the correct X offset for a component with width <code>inner</code>
-- given the width <code>outer</code> of its desired container.
-- @param outer Width of the container
-- @param inner Width of the component inside the container
-- @param anchor Alignment of the component within the container. These
--        correspond to numpad directions (4=left, 5=center, 6=right).
-- @return The correct X offset for the inner component
function alignAnchorX(outer, inner, anchor)
	if anchor == 2 or anchor == 5 or anchor == 8 then
		return (outer-inner) / 2
	elseif anchor == 3 or anchor == 6 or anchor == 9 then
		return (outer-inner)
	end
	return 0		
end

---Returns the correct Y offset for a component with height <code>inner</code>
-- given the height <code>outer</code> of its desired container.
-- @param outer Height of the container
-- @param inner Height of the component inside the container
-- @param anchor Alignment of the component within the container. These
--        correspond to numpad directions (8=top, 5=center, 2=bottom).
-- @return The correct Y offset for the inner component
function alignAnchorY(outer, inner, anchor)
	if anchor >= 4 and anchor <= 6 then
		return (outer-inner) / 2
	elseif anchor >= 1 and anchor <= 3 then
		return (outer-inner)
	end
	return 0		
end

---Creates a new panel
function createPanel()
	return GUI.createPanel(getImageLayer())
end
