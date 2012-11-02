-------------------------------------------------------------------------------
-- gallery2.lua
-------------------------------------------------------------------------------
-- Default image gallery v2.0
-------------------------------------------------------------------------------


-- ----------------------------------------------------------------------------
--  Classes
-- ----------------------------------------------------------------------------

local ImageSlot = {
	gallery=nil,
	index=-1,
    thumbnail=nil,
    fullpaths=nil,
	button=nil,
	label=nil,
    x=0,
    y=0,
    w=nil,
    h=nil
	}

function ImageSlot.new(self)
	self = extend(ImageSlot, self)
	
	self.button = button()
	if self.thumbnail then
		self.button:setNormalTexture(tex(self.thumbnail))
	else
		self.button:setNormalTexture(colorTex(0xFF808080, 32, 32))
	end
	self.button:setToggle(true)

    self.w = self.w or self.button:getWidth()
    self.h = self.h or self.button:getHeight()
    
	if #self.fullpaths > 1 then
		self.label = textimg("(" .. #self.fullpaths .. ")")
		self.label:setAnchor(3)
	end
	
	self.overlay = img("gui/imagegallery#white")
	self.overlay:setBlendMode(BlendMode.ADD)
	
	return self
end

function ImageSlot:destroy()
    destroyValues{self.button, self.label}
end

function ImageSlot:getBounds()
    return {self.x, self.y, self.w, self.h}
end

function ImageSlot:setBounds(x, y, w, h)
    self.x = x
    self.y = y
    self.w = w
    self.h = h
    
    self:layout()
end

function ImageSlot:layout()
    local x = self.x
    local y = self.y
    local w = self.w
    local h = self.h    

	local b = self.button
	local o = self.overlay
	local scale = math.min(w / b:getUnscaledWidth(), h / b:getUnscaledHeight())
	
	b:setScale(scale, scale)		
	b:setPos(x + (w-b:getWidth())/2, y + (h-b:getHeight())/2)
		
	o:setBounds(b:getX(), b:getY(), b:getWidth(), b:getHeight())
	o:setZ(b:getZ() - 10)
	
	self:layoutLabel()
end

function ImageSlot:layoutLabel()
	local b = self.button
	local lbl = self.label
	if lbl ~= nil then
		lbl:setZ(b:getZ()-5)
		lbl:setPadding(b:getHeight()/32)
		lbl:setSize(b:getWidth(), b:getHeight() / 4)
		lbl:setPos(b:getX(), b:getY()+b:getHeight()-lbl:getHeight())
	end
end

function ImageSlot:tweenImage(i, texture)
	return imgtween(i, texture)
end

function ImageSlot:update()
	if self.button:isRollover() and self.button:isEnabled() then
		self.overlay:setAlpha(0.5)
	else
		self.overlay:setAlpha(0)
	end
end

function ImageSlot:setText(txt)
	local lbl = self.label
	if lbl ~= nil then
		lbl:setText(txt)
	end
	--self:layoutLabel()
end

function ImageSlot:show()
	local index = 0
	local length = #self.fullpaths
	local locked = 0
	
    local paths = {}
	for i,fullpath in ipairs(self.fullpaths) do
		fullpath = fullpath or self.thumbnail
		if self.gallery:isLocked(fullpath) then
			locked = locked + 1
		else
			paths[i] = fullpath
			if index == 0 then
				index = i
			end
		end
	end
    preload(unpack(paths)) --Gives us a 2 frame head start for loading the full image(s)

    self.button:setEnabled(false)
    self.overlay:setAlpha(0)
    yield()
    
	local ss = screenshot()
	pushImageState()
	
	ss = img(ss)
	ss:setZ(1000)

	local lbl = nil
	if length > 1 then
		lbl = textimg("")
		lbl:setDefaultStyle(createStyle{anchor=9})
		lbl:setZ(-5)
		lbl:setPadding(screenHeight / 64)
		--lbl:setAnchor(9)
		lbl:setSize(screenWidth/4, screenHeight/8)
		lbl:setPos(screenWidth-lbl:getWidth(), 0)
	end
	
	local function updatelbl()
		if lbl ~= nil then
			local lockedText = ""
			if locked == 1 then			
				lockedText = "\n" .. locked .. " image locked"
			elseif locked > 1 then
				lockedText = "\n" .. locked .. " images locked"
			end
			lbl:setText(index .. "/" .. length .. lockedText)
		end
	end
	
	local i = img(paths[index])
    local b = self.button
    local small = {b:getX(), b:getY(), b:getWidth(), b:getHeight()}
    local dur = 20
    local ip = Interpolators.SMOOTH
	
	local function getLargeBounds()
		local iw = math.max(1, i:getWidth())
		local ih = math.max(1, i:getHeight())
		local scale = math.min(screenWidth/iw, screenHeight/ih) --Find scale factor to fill screen, maintaining aspect ratio
		return {(screenWidth-scale*iw)/2, (screenHeight-scale*ih)/2, scale*iw, scale*ih} --Scaled image bounds, centered on the screen
	end
	
    Anim.par({
        Anim.createTween(i, "bounds", small, getLargeBounds(), dur, ip),
        Anim.createTween(ss, "alpha", nil, 0, dur)
    }):run()

	updatelbl()
    while true do
        if input:consumeCancel() then
            break
		elseif input:consumeConfirm() or input:consumeTextContinue() then
			repeat --Skip to next unlocked image, quit if no more images
				index = index + 1
			until index > length or paths[index] ~= nil
			
			if paths[index] ~= nil then
				updatelbl()
				self:tweenImage(i, tex(paths[index]))
			else
				break
			end
        end
        yield()
    end
	if lbl ~= nil then	
		lbl:destroy()
	end
	
    Anim.par({
        Anim.createTween(i, "bounds", getLargeBounds(), small, dur, ip),
        Anim.createTween(ss, "alpha", nil, 1, dur)
    }):run()
	

	i:destroy()
	ss:destroy()
	
	popImageState()
    self.button:setEnabled(true)
end

-- ----------------------------------------------------------------------------

local ImageGallery = {
	files=nil,
	slots=nil,
	pageButtons=nil,
	returnButton=nil,
	topFade=nil,
	bottomFade=nil,
	page=0,
	selected=0,
	rows=2,
	cols=3,
	}

function ImageGallery.new(folder, self)
	self = extend(ImageGallery, self or {})
		
	self.buttonStyle = self.buttonStyle or createStyle{fontName="sans serif", fontStyle="bold", shadowColor=0}
	
	self.slots = self.slots or {}
	self.returnButton = button("gui/imagegallery#button-")
	self.returnButton:setText("Return")
	self.returnButton:setDefaultStyle(self.buttonStyle)
	
	self.topFade = img("gui/imagegallery#fade-top")
	self.topFade:setZ(10)

	self.bottomFade = img("gui/imagegallery#fade-bottom")
	self.bottomFade:setZ(10)
		
	return self
end

function ImageGallery:destroy()
    destroyValues(self.pageButtons)
    destroyValues(self.slots)
	destroyValues{self.returnButton}
end

function ImageGallery:layout()
	local w = screenWidth
	local h = screenHeight
	local ipad = w / 32
	local vpad = h / 7
	local quicksaveH = vpad / 2
	local mainW = w - ipad*2
    local mainPadV = h / 5.33333333333
	local mainH = h - mainPadV*2

	doLayout(GridLayout, 0, 0, w, vpad,
        {padding=ipad, pack=5},
        self.pageButtons)
        
	for i=1,2 do
        doLayout(GridLayout, ipad, mainPadV, mainW, mainH,
            {cols=self.cols, pack=5, stretch=true},
            self.slots)
    end
    
    doLayout(GridLayout, 0, h-vpad, w, vpad,
        {padding=ipad, pack=5},
        {self.returnButton})
		
	self.topFade:setBounds(0, 0, w, vpad)
	self.bottomFade:setBounds(0, math.ceil(h-vpad), w, vpad)
end

---Returns <code>true</code> if <code>fullpath</code> shouldn't be included in the image gallery
function ImageGallery:isExcludePath(fullpath)
	local lastsix = string.sub(fullpath, -6)
    if lastsix == "/thumb" or lastsix == "-thumb" then
        --Exclude files ending in "/thumb" "-thumb" (these are thumbnail versions of other images)
        return true
    end
    return false
end

---Returns the thumbnail Texture (or <code>nil</code> if no path exists) for the specified full image path.
function ImageGallery:getThumbnail(fullpath)
    local path = string.gsub(fullpath, "^(.*)%..-$", "%1") .. "-thumb" --Strip file-ext, append "-thumb"
    --print(path)
    return tex(path, true) --Try to retrieve the texture, suppressing any error encountered
end

function ImageGallery:isLocked(path)
	return not seenLog:hasImage(path)
end

function ImageGallery:setFolder(folder)
	self.files = {}
	
	local folderPrefixLen = #folder
	if folderPrefixLen > 0 and string.sub(folder, -1) ~= "/" then
		folderPrefixLen = folderPrefixLen + 1
	end
	
	local groups = {} --Mapping from group name to group index in self.files
	for k,rawpath in pairs(Image.getImageFiles(folder)) do
		local path = string.gsub(rawpath, "^(.*)%..-$", "%1") --Strips file-ext		
		if not self:isExcludePath(path) then
			local relpath = string.sub(path, folderPrefixLen + 1)
			
			local group = nil
			local lastSlashIndex = string.find(string.reverse(relpath), "/")
			if lastSlashIndex ~= nil then
				group = string.sub(relpath, 1, #relpath-lastSlashIndex)
			end
		
			local index = groups[group]
			if not index then
				table.insert(self.files, {path})
				index = #self.files
			else
				table.insert(self.files[index], path)
			end			
						
			if group ~= nil then
				groups[group] = index
			end
			
			--print(k, group, path, index)
		end
	end
	groups = nil -- Allow groups table to be garbage collected, we don't need it anymore

	local numPages = math.ceil(#self.files / (self.rows * self.cols))
	
	if self.pageButtons ~= nil then
		destroyValues(self.pageButtons)
	end
	
	self.pageButtons = {}
	for p=1,numPages do
		local tb = button("gui/imagegallery#pageButton-")
		tb:setText(p)
		tb:setDefaultStyle(self.buttonStyle)
		tb:setToggle(true)
		table.insert(self.pageButtons, tb)
	end
	
	self:setPage(1)
end

function ImageGallery:setPage(p)
	for i,pb in ipairs(self.pageButtons) do
		pb:setSelected(i == p) 
	end

	if self.page ~= p then
		self.page = p
		
		--Destroy old slots
        destroyValues(self.slots)
		self.slots = {}				
		
		--Create new slots
		local slotsPerPage = self.rows * self.cols
		local pageStart = 1 + (p - 1) * slotsPerPage
		local pageEnd = math.min(#self.files, p * slotsPerPage)
		
		for i=pageStart,pageEnd do
			local index = i
			local fullpath = "gui/imagegallery#locked"
            local thumbnail = "gui/imagegallery#locked"
			local empty = true
            
            local group = self.files[i]
			for _,file in ipairs(group) do
				if not self:isLocked(file) then
					empty = false
					thumbnail = self:getThumbnail(file)
					if thumbnail == nil then
						thumbnail = file --Just use the full-size version if no real thumbnail is available
					end
					break
				end
			end
			
			local is = ImageSlot.new{gallery=self, index=index, thumbnail=thumbnail, fullpaths=group}
			is.button:setEnabled(not empty)
			table.insert(self.slots, is)
		end
		
		if self.selected < pageStart or self.selected >= pageEnd then
			self.selected = 0
		end
		self:setSelected(self.selected)
		
		self:layout()
	end
end

function ImageGallery:setSelected(i)
	self.selected = i
	
	local selectedSlot = nil
	for _,slot in ipairs(self.slots) do
		if slot.index == i then
			selectedSlot = slot			
			slot.button:setSelected(true)
		else
			slot.button:setSelected(false)		
		end
	end
	
	if selectedSlot ~= nil then
		selectedSlot:show()
	end
end

function ImageGallery:run()
	self:layout()

	while not input:consumeCancel() do
		for i,pb in ipairs(self.pageButtons) do
			if pb:consumePress() then
				self:setPage(i)
			end
		end
		for _,slot in ipairs(self.slots) do
			slot:update()
			if slot.button:consumePress() then
				self:setSelected(slot.index)
				break
			end
		end
		
		if self.returnButton:consumePress() then
			break
		end
		
		yield()
	end
end

-- ----------------------------------------------------------------------------
--  Functions
-- ----------------------------------------------------------------------------

function preloadImageGallery()
	preload("gui/imagegallery")
end

---Shows an image gallery for the specified folder. Images placed in subfolders
-- are displayed as an image sequence, grouped together in a single button. This
-- is useful for grouping together multiple variants of an image. <br/>
-- You can provide thumbnail versions for images to improve performance and
-- image quality. Thumbnail images must be named the same as the non-thumbnail
-- version, but with <code>-thumb</code> appended (<code>abc.jpg</code> -&gt;
-- <code>abc-thumb.jpg</code>).
-- @param folder The image folder to display the images from.
-- @param overrides A Lua table containing overrides to pass to the ImageGallery
--        constructor.
function imageGallery(folder, overrides)
	folder = folder or ""

    --Create a screenshot and place it over the screen, change images, then fade away screenshot
	local ss = screenshot()
	pushImageState()
	ss = img(ss)
	ss:setZ(-32000)
	local thread = newThread(rmf, ss)
	
	local gallery = ImageGallery.new(overrides)
	gallery:setFolder(folder)
	gallery:run()	

    join(thread)

    --Create a screenshot and place it over the screen, change images, then fade away screenshot
	local ss = screenshot()
	gallery:destroy()	
	popImageState()
	ss = img(ss)
	ss:setZ(-32000)
	fadeTo(ss, 0)
	ss:destroy()	
end
