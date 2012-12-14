-------------------------------------------------------------------------------
-- screens.lua
-------------------------------------------------------------------------------
-- Defines the standard user interface screens.
-------------------------------------------------------------------------------

---Adds a visible scroll bar to an existing viewport
-- @param viewport The viewport to change the scrollbar of.
-- @param horizontal A boolean <code>true/false</code> whether to change the
--        horizontal or vertical scrollbar.
-- @param pad A table containing <code>top, right, bottom, left</code> fields 
--        to determine the amount of empty space that should be reserved around
--        the scrollbar.
-- @param images A table containing image paths or textures to use for the
--        scrollbar and/or fading edges:
--        <code>(scrollBG, scrollThumb, fadeUp, fadeDown)</code>.
function setViewportScrollBar(viewport, horizontal, pad, images)
	local sz = screenHeight * .015
	pad = extend({top=0, right=0, bottom=0, left=0}, pad or {})
	images = images or {}

	local func = viewport.setScrollBarY
	local sfx = ""
	if horizontal then
		func = viewport.setScrollBarX
		sfx = "-h"
	end
	
	local pfx = "gui/components#"
	if android then
		pfx = "android/components#"
	end
	
	local scrollBgTex = tex(images.scrollBG or pfx .. "scroll-bg" .. sfx, true)
	local scrollThumbTex = tex(images.scrollThumb or pfx .. "scroll-thumb" .. sfx, true)
		
	func(viewport, sz, scrollBgTex, scrollThumbTex, pad.top, pad.right, pad.bottom, pad.left)
		
	if not horizontal then
		local fadeUpTex = tex(images.fadeDown or pfx .. "fade-down", true)
		local fadeDownTex = tex(images.fadeUp or pfx .. "fade-up", true)
		viewport:setFadingEdges(screenHeight * .02, 0x000000, fadeUpTex, fadeDownTex)		
	end
end

-- ----------------------------------------------------------------------------
--  Save/Load Screen
-- ----------------------------------------------------------------------------

local KEY_SAVE_LAST = "vn.save.lastSaved" --Property value gets set in onSave()
local KEY_SAVE_PAGE = "vn.save.lastPage"

local SaveSlot = {
	slot=1,
	button=nil,
	image=nil,
	label=nil,
	isSave=false,
	empty=true,
	new=false,
	newImage=nil,
	compact=false,
	backgroundImagePath="gui/savescreen#slotButton-"
	}

function SaveSlot.new(self)
	self = extend(SaveSlot, self)
	
	local buttonImagePath = "gui/savescreen#slotButton-"
	if self.compact then
		buttonImagePath = "gui/savescreen#quicksave-"
	end
	
	local b = button(buttonImagePath)
	b:setToggle(true)
	b:setEnabled(self.isSave or not self.empty)
	
	local l = textimg(self.label)
	--l:setBackgroundColorARGB(0xA0000000)
	l:setZ(b:getZ() - 10)
	l:setPadding(8)
	l:extendDefaultStyle(createStyle{anchor=2})
	l:setVerticalAlign(1)
	
	local i = nil	
	local newI = nil
	if not self.compact then
		if self.screenshot ~= nil then
			i = img(self.screenshot)
		elseif not self.empty then
			i = img("gui/savescreen#noImage")
		end
		if i ~= nil then
			i:setZ(b:getZ() - b:getWidth()/2)	
		end
		
		if self.new and not self.empty then
			newI = img("gui/savescreen#newSave")
			newI:setZ(i:getZ() - 1)
		end
	end

	self.button = b
	self.image = i
	self.label = l
	self.newImage = newI
	
	return self
end

function SaveSlot:destroy()
	destroyValues{self.button, self.image, self.newImage, self.label}
end

function SaveSlot:getBounds()
	return self.button:getBounds()
end

function SaveSlot:setBounds(x, y, w, h)
	local b = self.button
	local l = self.label
	local i = self.image
	
	local scale = math.min(w / b:getUnscaledWidth(), h / b:getUnscaledHeight())
	b:setScale(scale)

	l:extendDefaultStyle(createStyle{fontName="SansSerif", anchor=2, fontSize=scale * 16, shadowDx=1, shadowDy=1})

	if i ~= nil then
		local maxW = 224
		local maxH = 126
		local iw = i:getUnscaledWidth()
		local ih = i:getUnscaledHeight()
		if maxW <= b:getWidth() and maxH <= .8*b:getHeight() and iw >= 1 and ih >= 1 then
			i:setScale(scale * math.min(maxW / iw, maxH / ih))
			i:setVisible(true)
			l:setSize(b:getWidth(), b:getHeight()-i:getHeight())
		else
			i:setVisible(false)
			l:setSize(b:getWidth(), b:getHeight())
		end		
	else	
		l:setSize(b:getWidth(), b:getHeight())
	end	
	
	b:setPos(x, y)
	l:setPos(x, y + b:getHeight() - l:getHeight())

	if i ~= nil then	
		local pad = (b:getWidth() - i:getWidth())/2
		i:setPos(x + pad, y + pad)
	end		
	
	local newI = self.newImage
	if newI ~= nil then
		if i ~= nil then	
			--Align with top-right of screenshot
			newI:setPos(i:getX() + i:getWidth() - newI:getWidth(), i:getY())
		else
			--Align with top-right of button
			newI:setPos(b:getX() + b:getWidth() - newI:getWidth(), b:getY())
		end
	end	
end

function SaveSlot:update()
	--[[
	if self.button:isEnabled() and self.button:isRollover() and not self.button:isPressed() then
		if self.image ~= nil then
			self.image:setBlendMode(BlendMode.ADD)
		end
	else
		if self.image ~= nil then
			self.image:setBlendMode(BlendMode.DEFAULT)
		end
	end
	--]]
end

-- ----------------------------------------------------------------------------

local SaveLoadScreen = {
	isSave=false,
	page=nil,
	pages=10,
	selected=0,
	metaData=nil, --Meta data Lua table added to the save data.
	rows=2,
	cols=5,
	newSaveSlot=SaveSlot.new,
	x=0,
	y=0,
	w=screenWidth,
	h=screenHeight,
	pack=5,
	qcols=-1,
	qh=screenHeight/14,
	qpack=6,
	pad=nil,
	screenshotWidth=nil,
	screenshotHeight=nil,
	--GUI Components
	pageButtons=nil,
	saves=nil,
	qsaves=nil,
	okButton=nil,
	cancelButton=nil,
	topFade=nil,
	bottomFade=nil
	}

function SaveLoadScreen.new(self)
	self = extend(SaveLoadScreen, self)
				
	self.page = self.page or getSharedGlobal(KEY_SAVE_PAGE) or 1
	self.pad = self.pad or math.min(self.w, self.h) / 100
	self.screenshotWidth = self.screenshotWidth or prefs.saveScreenshotWidth				
	self.screenshotHeight = self.screenshotHeight or prefs.saveScreenshotHeight				
				
	self.saves = {}
	self.qsaves = {}		
	
	self.pageButtons = {}		
	for p=1,self.pages do
		local tb = button("gui/savescreen#pageButton-")
		tb:setText(p)
		tb:setToggle(true)
		self.pageButtons[p] = tb
	end
	
	local okText = "Load"
	if self.isSave then
		okText = "Save"
	end
	
	local cancelText = "Cancel"
	
	self.okButton = button("gui/savescreen#button-")
	self.okButton:setText(okText)
	
	self.cancelButton = button("gui/savescreen#button-")
	self.cancelButton:setText(cancelText)
	
	local topFadeTex = tex("gui/savescreen#fade-top", true)
	if topFadeTex ~= nil then
		self.topFade = img(topFadeTex, {z=10})
	end

	local bottomFadeTex = tex("gui/savescreen#fade-bottom", true)
	if bottomFadeTex ~= nil then
		self.bottomFade = img("gui/savescreen#fade-bottom", {z=10})
	end
	
	local sz = self.okButton:getHeight() / 2.5	
	local buttonStyle = createStyle{fontName="SansSerif", fontStyle="bold", fontSize=sz, shadowColor=0}
	self.okButton:extendDefaultStyle(buttonStyle)
	self.cancelButton:extendDefaultStyle(buttonStyle)
	for _,tb in pairs(self.pageButtons) do
		tb:extendDefaultStyle(buttonStyle)
	end
		
	self:setPage(self.page, true)
	self:initQSaves()
		
	return self
end

function SaveLoadScreen:destroy()
	destroyValues(self.pageButtons)
	destroyValues(self.saves)
	destroyValues(self.qsaves)
	destroyValues{self.okButton, self.cancelButton}
	destroyValues{self.topFade, self.bottomFade}
end

function SaveLoadScreen:layout()
	local x = self.x
	local y = self.y
	local w = self.w
	local h = self.h
	local qh = self.qh
	
	local ipad = self.pad
	local vpad = h / 7
	local mainW = w - ipad*2
	local mainH = h - vpad*2 - qh - ipad*3

	doLayout(GridLayout, x, y, w, vpad,
		{padding=ipad, pack=5, shrink={true, false}},
		self.pageButtons)
	
	for i=1,2 do
		--Because of the peculiar implementation of SaveSlot, we need to layout twice;
		--once for the size and then once again for the position.
		doLayout(GridLayout, x+ipad, y+vpad+ipad, mainW, mainH,
			{cols=self.cols, padding=ipad, pack=self.pack, stretch=true},
			self.saves)
		
		doLayout(GridLayout, x+ipad, y+h-vpad-qh-ipad, mainW, qh,
			{cols=self.qcols, padding=ipad, pack=self.qpack, stretch=true},
			self.qsaves)
	end
	
	doLayout(GridLayout, x, y+h-vpad, w, vpad,
		{padding=ipad, pack=5},
		{self.okButton, self.cancelButton})
		
	if self.topFade ~= nil then
		self.topFade:setBounds(x, y, w, vpad)
	end
	if self.bottomFade ~= nil then
		self.bottomFade:setBounds(x, y+math.ceil(h-vpad), w, vpad)
	end
end

function SaveLoadScreen:initQSaves()
	destroyValues(self.qsaves)
	self.qsaves = {}
	for pass=1,2 do
		local defaultLabel = "autosave"
		local startSlot = Save.getAutoSaveSlot(1)
		local endSlot = startSlot + getAutoSaveSlots()
		if pass == 2 then
			startSlot = Save.getQuickSaveSlot(1)
			endSlot = startSlot + 1
			defaultLabel = "quicksave"
		end

		local saved = Save.getSaves(startSlot, endSlot)
		local sorted = {}
		for _,si in pairs(saved) do
			table.insert(sorted, si)
		end		
        table.sort(sorted, function(x, y)
        	--print(x:getTimestamp(), y:getTimestamp())
        	return x:getTimestamp() > y:getTimestamp()
        end)
        
		for i=1,endSlot-startSlot do
			local slot = startSlot + i
			local label = "Empty\n" .. defaultLabel .. " " .. i
			local empty = true
			
			local si = sorted[i]
			if si ~= nil then
				slot = si:getSlot()
				label = defaultLabel .. " " .. i .. "\n" .. si:getDateString()
				empty = false
			end
			
			local ss = self.newSaveSlot{slot=slot, label=label, empty=empty,
				isSave=self.isSave, new=false, compact=true}
			table.insert(self.qsaves, ss)
		end
	end
end

function SaveLoadScreen:getPage()
	return self.page
end

function SaveLoadScreen:setPage(p, force)
	for i,pb in ipairs(self.pageButtons) do
		pb:setSelected(i == p) 
	end

	if self.page ~= p or force then
		self.page = p				
		
		--Destroy old slots
		destroyValues(self.saves);
		self.saves = {}				
		
		--Create new slots
		local slotsPerPage = self.rows * self.cols
		local pageStart = 1 + (p - 1) * slotsPerPage
		local pageEnd   = 1 + (p    ) * slotsPerPage
		local saved = Save.getSaves(pageStart, pageEnd)	
		local lastSaved = getSharedGlobal(KEY_SAVE_LAST)
		
		for i=pageStart,pageEnd-1 do
			local slot = i
			local screenshot = nil
			local label = "Empty " .. slot
			local empty = true
			local new = false
			
			local si = saved[i]
			if si ~= nil then
				slot = si:getSlot()
				screenshot = si:getScreenshot(self.screenshotWidth, self.screenshotHeight)
				label = si:getLabel()
				empty = false
				new = (lastSaved == i)
			end
			
			local ss = self.newSaveSlot{slot=slot, label=label, empty=empty, screenshot=screenshot,
				isSave=self.isSave, new=new}
			table.insert(self.saves, ss)
		end
		
		if self.selected < pageStart or self.selected >= pageEnd then
			self.selected = 0
		end
		self:setSelected(self.selected)
		
		self:layout()
	end
end

function SaveLoadScreen:setSelected(s)
	self.selected = s
	for _,save in ipairs(self.saves) do
		save.button:setSelected(save.slot == s)
	end
	for _,save in ipairs(self.qsaves) do
		save.button:setSelected(save.slot == s)
	end
end

function SaveLoadScreen:run()
	self:layout()

	while not input:consumeCancel() do
		for i,pb in ipairs(self.pageButtons) do
			if pb:consumePress() then
				self:setPage(i)
			end
		end
		for _,save in ipairs(self.saves) do
			save:update()
			if save.button:consumePress() then
				self:setSelected(save.slot)
				break
			end
		end
		for _,save in ipairs(self.qsaves) do
			save:update()
			if save.button:consumePress() then
				self:setSelected(save.slot)
				break
			end
		end
		
		self.okButton:setEnabled(self.selected ~= 0)
		if self.okButton:consumePress() then
			break
		elseif self.cancelButton:consumePress() then
			self.selected = 0
			break
		end
		
		yield()
	end
	
	setSharedGlobal(KEY_SAVE_PAGE, self:getPage())
	
	return self.selected, self.metaData
end

-- ----------------------------------------------------------------------------
--  Text Log
-- ----------------------------------------------------------------------------

local TextLogScreen = {
	viewport=nil,
}

function TextLogScreen.new(self)
	self = extend(TextLogScreen, self)
	
	return self
end

function TextLogScreen:destroy()
	if self.viewport ~= nil then
		self.viewport:destroy()
		self.viewport = nil
	end
end

function TextLogScreen:run()	
    local x = 0
    local y = 0
    local w = screenWidth
    local h = screenHeight
    local pathPrefix = "gui/textlog#"
    local clipEnabled = true
    
    if android then
        pathPrefix = "android/textlog#"
        clipEnabled = false; --Android textlog draws outside its allotted screen bounds
    end
            
	local sz = math.min(w, h)
	local vpad  = 0.03 * sz
	local bh    = 0.15 * sz
	local tl = textState:getTextLog()
	local pages = tl:getPageCount()
	if System.isLowEnd() then
		pages = math.min(pages, 25) --Limit number of pages
	end
	local page = pages-1
    
    --Create edge images
    local topEdge = nil
    if not android then
    	local topTex = tex(pathPrefix .. "edge-top", true)
    	if topTex ~= nil then
			topEdge = img(topTex, {z=10, clipEnabled=clipEnabled})
		end
	end	
	local bottomEdge = nil
	local bottomTex = tex(pathPrefix .. "edge-bottom", true)
	if bottomTex ~= nil then
		bottomEdge = img(bottomTex, {z=10, clipEnabled=clipEnabled})
	end
    
	--Create controls
	local returnButton = button(pathPrefix .. "return-")
    returnButton:setClipEnabled(clipEnabled)
    local scale = math.min(2, (.90 * bh) / returnButton:getHeight())
    if not android then
    	scale = math.min(1, scale)
    end
    returnButton:setScale(scale)
	if System.isTouchScreen() then
		returnButton:setTouchMargin(bh/4)
	end
	returnButton:addActivationKeys(Keys.RIGHT, Keys.DOWN)
    
	--Create viewport and fill with text pages
	local viewport = createViewport(w, h)
	viewport:setClipEnabled(clipEnabled)
	viewport:setZ(1000)
	viewport:setPadding(vpad)
    viewport:setLayout(createFlowLayout{padding=vpad, cols=1})
	local si = {top=vpad, right=vpad, bottom=vpad, left=vpad}
	if android then
		si.top = si.top + vpad * 3
	end
	setViewportScrollBar(viewport, false, si)
	if android then
		viewport:setFadingEdges(0)
	end
	self.viewport = viewport
	        
    local ts = {}
    local defaultStyle = prefs.textLogStyle or createStyle{color=0xFFFFFF80}
	for p=pages,1,-1 do
		local t = textimg()
		t:setClipEnabled(clipEnabled)
		t:extendDefaultStyle(defaultStyle)
		t:setText(tl:getPage(-p))
		--t:setSize(iw, t:getTextHeight()) --Shrink text bounds to what's required (causes problems when text size changes)
		t:setBlendMode(BlendMode.OPAQUE)
		viewport:add(t)
		table.insert(ts, t)
	end

	local oldBounds = {x=0, y=0, w=0, h=0}
	local function layout()
		if not clipEnabled then
		    local renderEnv = imageState:getRenderEnv()
		    local windowBounds = renderEnv:getGLScreenVirtualBounds()
		    x = windowBounds.x
		    y = windowBounds.y
		    w = windowBounds.w
		    h = windowBounds.h
		end		
	
		if x ~= oldBounds.x or y ~= oldBounds.y or w ~= oldBounds.w or h ~= oldBounds.h then
			local top = y		
			if topEdge ~= nil then
				topEdge:setBounds(x, y, w, vpad)
				top = topEdge:getY() + topEdge:getHeight()
			end
			
			local bottomY = y+h-bh-vpad+1
			local bottomH = bh+vpad
			if bottomEdge ~= nil then
				bottomEdge:setBounds(x, bottomY, w, bottomH)
			end
		
			returnButton:setPos(x+(w-returnButton:getWidth())/2, bottomY + (bottomH-returnButton:getHeight())/2)
			
			viewport:setBounds(x, top, w, math.ceil(h-bh-vpad-top))
    		local iw = viewport:getInnerWidth() - vpad*2 - 2
			for _,t in ipairs(ts) do
				t:setSize(iw, h*2) --Initial width required to get text to linewrap properly
			end
			viewport:layout()
			viewport:setScrollFrac(0, 1)
			
			oldBounds = {x=x, y=y, w=w, h=h}
		end
	end

	--User interaction loop
	while not input:consumeCancel() do
		if returnButton:consumePress() then
			break
		end
		layout() --Necessary because the text size could change at any time
		yield()
	end	
end

-- ----------------------------------------------------------------------------
--  Choice
-- ----------------------------------------------------------------------------

local ChoiceScreen = {
	cancelled=false,
	selected=-1,
	options=nil,
	buttons=nil,
	viewport=nil,
	components=nil,
	choiceStyle=nil,
	selectedChoiceStyle=nil,
	pad=screenHeight*.03,
	ipad=screenHeight*.015,
	w=screenWidth,
	h=screenHeight*.75
}

function ChoiceScreen.new(choiceId, ...)
	local self = extend(ChoiceScreen, {options=getTableOrVarArg(...) or {}})
	
	self.choiceStyle = self.choiceStyle or prefs.choiceStyle or createStyle()
	self.selectedChoiceStyle = self.selectedChoiceStyle
		or extendStyle(self.choiceStyle, prefs.selectedChoiceStyle or {color=0xFF808080})
	
	local viewport = createViewport(self.w-self.pad*2, self.h-self.pad*2)
	viewport:setPos(self.pad, self.pad)
	viewport:setZ(-2000)
	viewport:setPadding(self.pad)
	viewport:setLayout(createFlowLayout{pack=5, anchor=2, padding=self.ipad, cols=1})
	self.viewport = viewport
	
	setImageLayer(viewport:getLayer())
	self.buttons = {}
	self.components = {}
	for i,opt in ipairs(self.options) do
		local b = button("gui/choice-button")
		b:setText(opt or "???")		
		b:setAlpha(0)
		
		local buttonScale = 1
		if b:getUnscaledWidth() > screenWidth * .8 then
			buttonScale = (screenWidth * .8) / b:getUnscaledWidth()
		end
		b:setScale(buttonScale)
		
		if seenLog:isChoiceSelected(choiceId, i) then
			b:extendDefaultStyle(self.selectedChoiceStyle)
		else
			b:extendDefaultStyle(self.choiceStyle)
		end
		table.insert(self.buttons, b)
		
		local c = toLayoutComponent(b)
		viewport:add(c)
		table.insert(self.components, c)
	end
	setImageLayer(nil)
		
	return self
end

function ChoiceScreen:destroy()
	self:cancel()
end

function ChoiceScreen:layout()
	for pass=1,3 do
		self.viewport:validateLayout()
		for i,b in ipairs(self.buttons) do
			transferBounds(self.components[i], b)
		end
	
		if not self.viewport:canScrollY() then
			break
		end
		
		if pass == 1 then
			self.viewport:getLayout():setPadding(self.ipad / 4)
		elseif pass == 2 then
			if not self.viewport:hasScrollBarY() then
				setViewportScrollBar(self.viewport)
				self.viewport:setFadingEdges(0)
			end
		end
	end
end

function ChoiceScreen:fadeButtons(visible, speed)
	local targetAlpha = 1
	if not visible then
		targetAlpha = 0
	end
	
	local threads = {}
	for _,b in ipairs(self.buttons) do
		table.insert(threads, newThread(fadeTo, b, targetAlpha, speed))
	end
	update1join(threads)
end

function ChoiceScreen:run()
	self.selected = -1
	self:layout()

	self:fadeButtons(false, 1)
	self:fadeButtons(true)

	local focusIndex = 0
	local selected = -1
	local len = #self.options
	while selected < 0 and len > 0 do
		local oldb = self.buttons[focusIndex]
		
		if input:consumeUp() then
			focusIndex = math.max(1, focusIndex - 1)
			local c = self.components[focusIndex]
			if c ~= nil then
				self.viewport:scrollToVisible(c)
			end
		end
		if input:consumeDown() then
			focusIndex = math.min(#self.buttons, focusIndex + 1)
			local c = self.components[focusIndex]
			if c ~= nil then
				self.viewport:scrollToVisible(c)
			end
		end

		local newb = self.buttons[focusIndex]
		if oldb ~= newb then
			if oldb ~= nil then oldb:setKeyboardFocus(false) end
			if newb ~= nil then newb:setKeyboardFocus(true) end
		end

		if input:consumeCancel() then
			self:fadeButtons(false)
			self:cancel()
			return --We must return immediately after calling cancel
		end

		for i,b in ipairs(self.buttons) do
			if focusIndex == 0 or i == focusIndex then
				b:setColor(1, 1, 1)
			else
				b:setColor(.5, .5, .5)
			end
			
			if b:consumePress() then
				self:onButtonPressed(i)
				selected = i - 1
				break
			end
			
			transferBounds(self.components[i], b)
		end

		yield()
	end
	
	self.selected = selected
end

function ChoiceScreen:cancel()
	self.cancelled = true
	destroyValues(self.buttons)
	self.buttons = {}
	self.components = {}
	destroyValues{self.viewport}
end

function ChoiceScreen:getOptions()
	return self.options
end

--Zero-based index
function ChoiceScreen:getSelected()
	return self.selected
end

function ChoiceScreen:isCancelled()
	return self.cancelled
end

--Gets called when a button is pressed, changing self.selected happens elsewhere
function ChoiceScreen:onButtonPressed(index)	
	self:fadeButtons(false)
end

--Zero-based index
function ChoiceScreen:setSelected(s)
	if s < 0 or s >= #self.options then
		s = -1
	end
	self.selected = s
end

-- ----------------------------------------------------------------------------
-- ----------------------------------------------------------------------------
-- ----------------------------------------------------------------------------

Screens.save = SaveLoadScreen
Screens.load = SaveLoadScreen
Screens.textLog = TextLogScreen
Screens.choice = ChoiceScreen
