-------------------------------------------------------------------------------
-- image.lua
-------------------------------------------------------------------------------
-- Provides the 'built-in' VN image functions.
-------------------------------------------------------------------------------

module("vn.image", package.seeall)

-- ----------------------------------------------------------------------------
--  Variables
-- ----------------------------------------------------------------------------

local imageStateMeta = {
	{background=nil, layer=nil}
}

-- ----------------------------------------------------------------------------
--  Functions
-- ----------------------------------------------------------------------------

---Returns the <code>x, y, z</code> of the specified sprite slot
-- <code>(lc, l, c, rc, r)</code>
-- @param i The image to position
-- @param slot The sprite slot (a string)
-- @param y The baseline y (sprite bottom y-coordinate)
-- @return Three values, the natural <code>x, y, z</code> for the image in the
--         specified sprite slot.
local function getSpriteSlotPosition(i, slot, y)
	local x = 0
	local y = (y or screenHeight) - i:getHeight()
	local z = 0
	
	local w2 = i:getWidth()/2
	if slot == "l" then
		x = screenWidth*1/5 - w2
		z = 1
	elseif slot == "lc" then
		x = screenWidth*1/3 - w2
		z = 2		
	elseif slot == "c" then
		x = screenWidth*1/2 - w2
	elseif slot == "rc" then
		x = screenWidth*2/3 - w2
		z = 2		
	elseif slot == "r" then
		x = screenWidth*4/5 - w2
		z = 1
	end
	
	return x, y, z
end

---Creates an image
-- @param tex A texture object or a path to a valid image file
-- @param x Can be either a string or a number. If it's a number, it specifies
--        the leftmost x-coordinate of the image. If it's a string, it can be
--        one of:<br/>
--        <ul>
--          <li>l</li>
--          <li>lc</li>
--          <li>c</li>
--          <li>rc</li>
--          <li>r</li>
--        </ul>
--        These refer to predefined sprite positions from left to right.
-- @param y If x is given as a string, the desired y-coordinate of the bottom
--        of the sprite. If x is a number, the topmost y-coordinate. 
-- @param props Optional argument containing a table containing initial values
--        for the new image's properties. 
-- @return The newly created image
function img(tex, x, y, props)
	if type(x) == "table" then
		props = x
	elseif type(y) == "table" then
		props = y
	end

	local i = Image.createImage(getImageLayer(), tex)
		
	if type(x) == "string" then
		local z = 0
		x, y, z = getSpriteSlotPosition(i, x, y)
		i:setZ(z)
	end	
	if type(x) == "number" and type(y) == "number" then
		i:setPos(x, y)
	end
	
	--Handle properties given in a table
	if type(props) == "table" then
		for k,v in pairs(props) do
			setProperty(i, k, v)	
		end
	end	
		
	return i
end

---Creates an image and fades it in
-- @see img
function imgf(...)
	local i = img(...)
	i:setAlpha(0)
	fadeTo(i, 1)
	return i	
end

---Removes an image after first fading it out
-- @param i The image to remove
function rmf(i)
	fadeTo(i, 0)
	rm(i)
end

---Removes and image immediately
-- @param i The image to remove
function rm(i)
	if i ~= nil and not i:isDestroyed() then
		i:destroy()
	end
end

---Removes the background image previously created with <code>bg</code>
function rmbg(...)
	local bg = getBackground()
	setImageStateAttribute("background", nil)
	return rm(bg, ...)
end

---Removes the background image after first fading it out
function rmbgf(...)
	local bg = getBackground()
	setImageStateAttribute("background", nil)
	return rmf(bg, ...)
end

---Changed the current background
-- @param tex A texture object or a path to a valid image file
-- @param ... Any further arguments will be passed to <code>img</code>
--        internally.
-- @return The newly created background
function bg(tex, ...)
	local background = getBackground()
    if background ~= nil and not background:isDestroyed() then
        background:destroy()
    end
    
    local props = {z=30000}
    if ... ~= nil then
    	addAll(props, ...)
    end
	background = img(tex, props)
	
	setImageStateAttribute("background", background)	
	return background
end

---Crossfades to a new background
-- @param tex A texture object or a path to a valid image file
-- @param fadeTime The fade time (in frames) of the crossfade. Defaults to 30
--        (0.5 seconds).
-- @param ... Any further arguments will be passed to <code>bg</code>
--        internally.
-- @return The newly created background
function bgf(tex, fadeTime, ...)
	fadeTime = fadeTime or 30

	local background = getBackground()
	if background == nil or background:isDestroyed() then	
		background = bg(tex, ...)
		if fadeTime > 0 then
			background:setAlpha(0)
			fadeTo(background, 1, 1.0 / fadeTime)
		end
	else
		local newbg = img(tex, ...)
		if fadeTime > 0 then		
			newbg:setAlpha(0)
			newbg:setZ(background:getZ() - 1)
			fadeTo(newbg, 1, 1.0 / fadeTime)
		end
		newbg:setZ(background:getZ())		
	    background:destroy()
	    background = newbg
	end	
		
	setImageStateAttribute("background", background)
	return background
end

---Returns the current background
-- @return The current background image, or <code>nil</code> if there's no
--         background currently set.
function getBackground()
	local imageLayer = getImageLayer()
	local background = getImageStateAttribute("background")
	if imageLayer == nil or not imageLayer:contains(background) then
		setImageStateAttribute("background", nil)
		background = nil
	end
	return background
end

---Converts the input argument to a texture object if possible
-- @param arg The value to convert to a texture object
-- @param suppressErrors If <code>true</code> suppress any errors
function tex(arg, suppressErrors)
	if type(arg) == "string" then
		return Image.getTexture(arg, suppressErrors)
	end
	return arg
end

---Creates a new texture with the specified width/height and color.
-- @param argb The ARGB color packed into a single int (<code>0xFFFF0000</code>
--        is red, <code>0xFF0000FF</code> is blue, etc.)
-- @param w The width for the generated texture
-- @param w The height for the generated texture
-- @return A new texture (w,h) with all pixels colored <code>argb</code>
function colorTex(argb, w, h)
	return Image.createColorTexture(argb, w, h)
end

---Creates a new on-screen button
-- @param filename Path to a valid image
-- @return The newly created button
function button(filename)
	return Image.createButton(getImageLayer(), filename)
end

---Creates a new textbox
-- @param text The initial text to display (optional)
-- @return The newly created textbox
function textimg(text)
	return Image.createText(getImageLayer(), text)
end

---Takes a screenshot to be used later to create an image drawable
-- @param layer The layer in which to take the screenshot. Any layers
--        underneath it will be visible in the screenshot. Passing
--        <code>nil</code> for the layer param takes a screenshot of everything.
-- @param z The z-index in the selected layer to take the screenshot at.
-- @return A screenshot object to be used as an argument to the <code>img</code>
--         function later.
function screenshot(layer, z)
	local ss = nil
	while ss == nil do
		ss = Image.screenshot(layer, z)
		while not ss:isAvailable() and not ss:isCancelled() do
			--print("looping", ss:isCancelled())
			yield()
		end
        
		if not ss:isAvailable() then
			ss = nil 
		end
	end
	return ss	
end

---Takes a screenshot and makes an image out of it. Very useful for creating
-- complex fade effects by making it possible to fade out the entire screen
-- as a single image.
-- @param layer The layer in which to take the screenshot
-- @param z The z-index to take the screenshot at.
-- @return The newly created image
function screen2image(layer, z)
	layer = layer or getImageLayer()
	z = z or -999
	
	i = Image.createImage(layer, screenshot(layer, z))
	i:setZ(z + 1)
	return i
end

---Creates a new camera object
-- @param layer The layer to create the camera on.
-- @return The newly created camera object.
function createCamera(layer)
	layer = layer or getImageLayer()
	return Image.createCamera(layer)
end

---Creates a new layer
-- @param id A string uniquely identifying the layer
-- @return The newly created layer, or <code>null</code> if a layer with the
-- given id already exists.
function createLayer(id)
	return imageState:createLayer(id)
end

---Returns the current image layer
function getImageLayer()
	return getImageStateAttribute("layer") or imageState:getLayer()
end

---Changes the current image layer
function setImageLayer(l)
	setImageStateAttribute("layer", l)
end

---Returns the <code>overlay</code> layer which is usually the topmost layer.
function getOverlayLayer()
	return imageState:getLayer("overlay")
end

---Saves the current images onto a stack to be restored later with
-- <code>popLayerState</code>. Completely clears the <code>layer</code>
-- of all images.
-- @param layer The layer to push the state of.
-- @param z Optional parameter, if specified only pushes images with
--          <code>image.z &gt;= z</code>. 
function pushLayerState(layer, z)
	layer = layer or getImageLayer()
	if z ~= nil then
		layer:push(z)
	else
		layer:push()
	end
end

---Pops the top of the layer's state stack, overwriting the current set of
-- active images.
function popLayerState(layer)
	layer = layer or getImageLayer()
	layer:pop()
end

---Stores a copy of the current image state on a stack then clears the current
-- image state.
function pushImageState()
	imageState:push() --pushLayerState()
	
	table.insert(imageStateMeta, {textMode = getTextMode()})
	setTextMode(0)
end

---The inverse of pushImageState, restores the image state to the stored copy.
-- @see pushImageState
function popImageState()
	setTextMode(0)
	imageState:pop() --popLayerState()
	
	local t = table.remove(imageStateMeta)
	setTextMode(t.textMode)
end

---Suggests the filenames givens as arguments should be preloaded
function preload(...)
	return Image.preload(...)
end

---Runs the view CG mode
function viewCG()
	if getTextMode() == 0 then
		return
	end

	local textLayer = getTextLayer()
	return setMode("viewCG", function()
		textLayer:setVisible(false)
	
		while not input:consumeCancel() and not input:consumeConfirm()
			and not input:consumeTextContinue() and not input:consumeViewCG() do
			
			yield() 
		end
		
		setMode(nil)
	end, function()
		textLayer:setVisible(true)
	end)
end

---Sets an attribute that gets pushed/popped upon calls to
-- <code>pushImageState, popImageState</code>
-- @param key The name for the attribute to set
-- @param val The new value for the attribute 
-- @see getImageStateAttribute
function setImageStateAttribute(key, val)
	local meta = imageStateMeta[#imageStateMeta]
	meta[key] = val
end

---Returns the value of an attribute that gets pushed/popped together with the
-- image state.
-- @param key The name of the attribute to get the value of
-- @return The current value of the attribute specified by <code>key</code>
-- @see setImageStateAttribute
function getImageStateAttribute(key)
	local meta = imageStateMeta[#imageStateMeta]
	return meta[key]
end

-- ----------------------------------------------------------------------------
--  Transitions
-- ----------------------------------------------------------------------------

---Gradually changes the alpha of <code>i</code> to <code>targetAlpha</code>.
-- @param i The image to change the alpha of.
-- @param targetAlpha The end alpha for <code>i</code>
-- @param speed The change in alpha each frame (will be multiplied with
--        <code>effectSpeed</code> internally), should always be positive.
function fadeTo(i, targetAlpha, speed)
	speed = math.abs(speed or 0.05)
	
	local alpha = i:getAlpha()		
	if alpha > targetAlpha then
		while alpha - speed * effectSpeed > targetAlpha do
			alpha = alpha - speed * effectSpeed
			i:setAlpha(alpha)
			yield()
		end
	elseif alpha < targetAlpha then
		alpha = i:getAlpha()
		while alpha + speed * effectSpeed < targetAlpha do
			alpha = alpha + speed * effectSpeed
			i:setAlpha(alpha)
			yield()
		end
	end
	
    i:setAlpha(targetAlpha)
	--yield()
end

---Gradually changes the position of <code>i</code> to <code>(x, y)</code>.
-- @param i The image to change the position of.
-- @param x The end x-pos for <code>i</code>
-- @param y The end y-pos for <code>i</code>
-- @param frames The number of frames the transition should last (gets
--        multiplied with effectSpeed internally)
-- @param interpolator A function or interpolator object mapping an input
--        in the range <code>(0, 1)</code> to an output in the range
--        <code>(0, 1)</code>.
function translateTo(i, x, y, frames, interpolator)
	x = x or i:getX()
	y = y or i:getY()
	frames = frames or 60
	interpolator = Interpolators.get(interpolator, Interpolators.SMOOTH)
		
	local startX = i:getX()
	local startY = i:getY()
	
	local frame = 1
	while not i:isDestroyed() and frame + effectSpeed <= frames do
		local f = interpolator:remap(frame / frames)
		i:setPos(startX + (x-startX) * f, startY + (y-startY) * f)
		frame = frame + effectSpeed
		yield()
	end
	i:setPos(x, y)
end

---Gradually offsets the position(s) of <code>i</code> by <code>(dx, dy)</code>.
-- @param i A drawable or table of drawables to move
-- @param dx The amount to move in the x-direction
-- @param dy The amount to move in the y-direction
-- @param frames The number of frames the transition should last (gets
--        multiplied with effectSpeed internally)
function translateRelative(i, dx, dy, frames)
	if i == nil then
		i = {}
	elseif type(i) ~= "table" then
		i = {i}
	end
	dx = dx or 0
	dy = dy or 0
	frames = frames or 60

	local threads = {}
	for _,d in pairs(i) do
		table.insert(threads, newThread(translateTo, d, d:getX()+dx, d:getY()+dy, frames))
	end		
	join(threads)
end
