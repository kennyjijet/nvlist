-------------------------------------------------------------------------------
-- tween.lua
-------------------------------------------------------------------------------
-- Provides the 'built-in' VN tween functions.
-------------------------------------------------------------------------------

module("vn.tween", package.seeall)

-- ----------------------------------------------------------------------------
--  Variables
-- ----------------------------------------------------------------------------

-- ----------------------------------------------------------------------------
--  Functions
-- ----------------------------------------------------------------------------

---Tweens <code>i</code> to the texture specified by <code>t</code>
-- @param i The image to change the texture of.
-- @param t A texture object or a path to a valid image file
function imgtween(i, t)
	t = tex(t)
	if not crossFadeTween(i, t, 30) then
		i:setTexture(t)
	end
	return i
end

---Tweens background image to the texture specified by <code>tex</code>
-- @param tex A texture object or a path to a valid image file
function bgtween(tex, ...)
	return imgtween(getBackground(), tex, ...)
end







---Fades an image using from <code>tex1, gs1, ps1</code> to
-- <code>tex2, gs2, ps2</code>.
-- @param img The image to change the texture of.
-- @param duration The duration of the tween in frames (will be multiplied by
--        <code>effectSpeed</code> internally).
-- @param tex1 The start texture for the tween
-- @param gs1 The start geometry shader for the tween
-- @param ps1 The start pixel shader for the tween
-- @param tex2 The end texture for the tween
-- @param gs2 The end geometry shader for the tween
-- @param ps2 The end pixel shader for the tween
function shaderTween(img, duration, tex1, gs1, ps1, tex2, gs2, ps2)
	tex1 = tex(tex1)
	tex2 = tex(tex2)

	img:setTexture(tex1)
	
	duration = duration or 60
	
	local tween = ShaderImageTween.new(duration, gs1, ps1, gs2, ps2)
	tween:setEndImage(tex2)
	img:setTween(tween)
	while not img:isDestroyed() and not tween:isFinished() do
		yield()
	end
	return img
end

---Fades in an image using shaders.
-- @param img The image to tween
-- @param duration The duration of the tween in frames (will be multiplied by
--        <code>effectSpeed</code> internally).
-- @param gs The end geometry shader for the tween
-- @param ps The end pixel shader for the tween
-- @see shaderTween
function shaderTweenIn(img, duration, gs, ps)
	return shaderTween(img, duration, nil, nil, nil, img:getTexture(), gs, ps)
end

---Fades out an image using shaders.
-- @param img The image to tween
-- @param duration The duration of the tween in frames (will be multiplied by
--        <code>effectSpeed</code> internally).
-- @param gs The start geometry shader for the tween
-- @param ps The start pixel shader for the tween
-- @see shaderTween
function shaderTweenOut(img, duration, gs, ps)
	return shaderTween(img, duration, img:getTexture(), gs, ps, nil, nil, nil)
end




---Temporarily changes the shaders of <code>img</code> to <code>gs, ps</code>,
-- animates them to <code>time=1.0</code>, then changes the texture of
-- <code>img</code> to <code>dstTex</code>. After the texture has been changed,
-- the shaders are animated back to <code>time=0.0</code>, and then replaced
-- by the original shaders of <code>img</code>.
-- @param img The image to tween
-- @param dstTex The new texture for <code>img</code>
-- @param duration The duration of the tween in frames
-- @param bounceTime The shader time that should be reached halfway through the
--        tween (at the bounce point). Defaults to <code>1.0</code>
-- @param gs The geometry shader to use during the tween (or <code>nil</code>
-- 	      to use the current geometry shader).
-- @param ps The pixel shader to use during the tween (or <code>nil</code>
--        to use the current pixel shader).
function shaderBounceTween(img, dstTex, duration, bounceTime, gs, ps)
    dstTex = tex(dstTex)
    duration = duration or 60
    bounceTime = bounceTime or 1.0
    
    local oldGS = img:getGeometryShader()
    local oldPS = img:getPixelShader()
    if gs ~= nil then
        img:setGeometryShader(gs)
    end
    if ps ~= nil then
        img:setPixelShader(ps)
    end
    
    local time = 0
    local timeInc = (2 * bounceTime) / duration
    while not img:isDestroyed() do
        time = time + timeInc * effectSpeed
        if time >= bounceTime then
        	time = 2 * bounceTime - time --Wrap around
            timeInc = -timeInc
            img:setTexture(dstTex)
        elseif time < 0 then
            break
        end
        
        if gs ~= nil then
            gs:setTime(time)
        end
        if ps ~= nil then
            ps:setTime(time)
        end
        
        yield()
    end
    
    img:setGeometryShader(oldGS)
    img:setPixelShader(oldPS)
    return img
end






---Tweens an image to a new texture using a shutter transition
-- @param img The image to shutter-tween
-- @param tex The new texture for the image
-- @param dir The direction of the shutter, 4=left, 8=up, 6=right, 2=down
-- @param steps The number of bars for the shutter tween
-- @param duration The duration of the tween in frames (will be multiplied by
--        <code>effectSpeed</code> internally).
function shutterTween(img, tex, dir, steps, duration)
	dir = dir or 6
	steps = steps or 10

	local gs1 = ShutterGS.new(false, dir, steps)
	local gs2 = ShutterGS.new(true, dir, steps)
	return shaderTween(img, duration, img:getTexture(), gs1, nil, tex, gs2, nil)
end

---Tweens in an image using a shutter transition
-- @param img The image to shutter-tween
-- @param dir The direction of the shutter, 4=left, 8=up, 6=right, 2=down
-- @param steps The number of bars for the shutter tween
-- @param duration The duration of the tween in frames (will be multiplied by
--        <code>effectSpeed</code> internally).
-- @see shutterTween
function shutterTweenIn(img, dir, steps, duration)
	dir = dir or 6
	steps = steps or 10

	return shaderTweenIn(img, duration, ShutterGS.new(true, dir, steps))
end

---Tweens out an image using a shutter transition
-- @param img The image to shutter-tween
-- @param dir The direction of the shutter, 4=left, 8=up, 6=right, 2=down
-- @param steps The number of bars for the shutter tween
-- @param duration The duration of the tween in frames (will be multiplied by
--        <code>effectSpeed</code> internally).
-- @see shutterTween
function shutterTweenOut(img, dir, steps, duration)
	dir = dir or 6
	steps = steps or 10

	return shaderTweenOut(img, duration, ShutterGS.new(false, dir, steps))
end







---Tweens an image to a new texture using a wipe transition
-- @param img The image to wipe-tween
-- @param tex The new texture for the image
-- @param dir The direction of the wipe, 4=left, 8=up, 6=right, 2=down
-- @param span The range (between <code>0.0</code> and <code>1.0</code>) of
--        the partially transparent area of the wipe tween.
-- @param duration The duration of the tween in frames (will be multiplied by
--        <code>effectSpeed</code> internally).
function wipeTween(img, tex, dir, span, duration)
	dir = dir or 6
	span = span or 0.2

	local gs1 = WipeGS.new(false, dir, span)
	local gs2 = WipeGS.new(true, dir, span)
	return shaderTween(img, duration, img:getTexture(), gs1, nil, tex, gs2, nil)
end

---Tweens in an image using a wipe transition
-- @param img The image to wipe-tween
-- @param dir The direction of the wipe, 4=left, 8=up, 6=right, 2=down
-- @param span The range (between <code>0.0</code> and <code>1.0</code>) of
--        the partially transparent area of the wipe tween.
-- @param duration The duration of the tween in frames (will be multiplied by
--        <code>effectSpeed</code> internally).
-- @see wipeTween
function wipeTweenIn(img, dir, span, duration)
	dir = dir or 6
	span = span or 0.2

	return shaderTweenIn(img, duration, WipeGS.new(true, dir, span))
end

---Tweens out an image using a wipe transition
-- @param img The image to wipe-tween
-- @param dir The direction of the wipe, 4=left, 8=up, 6=right, 2=down
-- @param span The range (between <code>0.0</code> and <code>1.0</code>) of
--        the partially transparent area of the wipe tween.
-- @param duration The duration of the tween in frames (will be multiplied by
--        <code>effectSpeed</code> internally).
-- @see wipeTween
function wipeTweenOut(img, dir, span, duration)
	dir = dir or 6
	span = span or 0.2

	return shaderTweenOut(img, duration, WipeGS.new(false, dir, span))
end








---Tweens an image following the shape of the specified grayscale bitmap
-- @param img The image to bitmap-tween
-- @param dstTex The new texture for the image
-- @param fadeFilename The bitmap that determines the shape of the tween
-- @param duration The duration of the tween in frames (will be multiplied by
--        <code>effectSpeed</code> internally).
-- @param range Determines the relative width of the fading region between
--        <code>0.0</code> and <code>1.0</code>
-- @param interpolator A function or interpolator object mapping an input
--        in the range <code>(0, 1)</code> to an output in the range
--        <code>(0, 1)</code>.
-- @return <code>false</code> if the bitmap tween isn't supported on the
--         current hardware. Useful to provide a fallback tween in case the
--         bitmap tween doesn't work.
function bitmapTween(img, dstTex, fadeFilename, duration, range, interpolator)
	if BitmapTween == nil or not BitmapTween.isAvailable() then
		return false
	end

	duration = duration or 60
	range = range or 0.5
	dstTex = tex(dstTex)

	local tween = BitmapTween.new(fadeFilename, duration, range, interpolator)
	tween:setEndImage(dstTex) --, 7)
	img:setTween(tween)
	while not img:isDestroyed() and not tween:isFinished() do
		yield()
	end
	return img
end

---Tweens in an image following the shape of the specified grayscale bitmap
-- @param img The image to bitmap-tween
-- @param fadeFilename The bitmap that determines the shape of the tween
-- @param duration The duration of the tween in frames (will be multiplied by
--        <code>effectSpeed</code> internally).
-- @param range Determines the relative width of the fading region between
--        <code>0.0</code> and <code>1.0</code>
-- @param interpolator A function or interpolator object mapping an input
--        in the range <code>(0, 1)</code> to an output in the range
--        <code>(0, 1)</code>.
-- @return <code>false</code> if the bitmap tween isn't supported on the
--         current hardware. Useful to provide a fallback tween in case the
--         bitmap tween doesn't work.
-- @see bitmapTween
function bitmapTweenIn(img, fadeFilename, duration, range, interpolator)
	if BitmapTween == nil or not BitmapTween.isAvailable() then
		return false
	end

	local tex = img:getTexture()
	img:setTexture(nil)
	return bitmapTween(img, tex, fadeFilename, duration, range, interpolator)
end

---Tweens out an image following the shape of the specified grayscale bitmap
-- @param img The image to bitmap-tween
-- @param fadeFilename The bitmap that determines the shape of the tween
-- @param duration The duration of the tween in frames (will be multiplied by
--        <code>effectSpeed</code> internally).
-- @param range Determines the relative width of the fading region between
--        <code>0.0</code> and <code>1.0</code>
-- @param interpolator A function or interpolator object mapping an input
--        in the range <code>(0, 1)</code> to an output in the range
--        <code>(0, 1)</code>.
-- @return <code>false</code> if the bitmap tween isn't supported on the
--         current hardware. Useful to provide a fallback tween in case the
--         bitmap tween doesn't work.
-- @see bitmapTween
function bitmapTweenOut(img, fadeFilename, duration, range, interpolator)
	if BitmapTween == nil or not BitmapTween.isAvailable() then
		return false
	end

	return bitmapTween(img, nil, fadeFilename, duration, range, interpolator)
end







---Tweens an image to a new texture using a cross fade transition
-- @param img The image to crossfade
-- @param dstTex The new texture for the image
-- @param duration The duration of the fade in frames (will be multiplied by
--        <code>effectSpeed</code> internally).
-- @param interpolator A function or interpolator object mapping an input
--        in the range <code>(0, 1)</code> to an output in the range
--        <code>(0, 1)</code>.
-- @return <code>false</code> if the crossfade tween isn't supported on the
--         current hardware.
function crossFadeTween(img, dstTex, duration, interpolator)
	if CrossFadeTween == nil or not CrossFadeTween.isAvailable() then
		return false
	end
	
	duration = duration or 30
	dstTex = tex(dstTex)

	local tween = CrossFadeTween.new(duration, interpolator)
	tween:setEndImage(dstTex)
	img:setTween(tween)
	while not img:isDestroyed() and not tween:isFinished() do
		yield()
	end
	return true
end
