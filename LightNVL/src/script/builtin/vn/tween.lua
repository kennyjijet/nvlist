---Defines some image transition functions (image 'tweens').
-- 
module("vn.tween", package.seeall)

-- ----------------------------------------------------------------------------
--  Variables
-- ----------------------------------------------------------------------------

-- ----------------------------------------------------------------------------
--  Functions
-- ----------------------------------------------------------------------------

---Performs an animated transition from the current texture of
-- <code>image</code> to a new texture, <code>targetTexture</code>.
-- @tparam ImageDrawable image The image to change the texture of.
-- @param targetTexture A texture object or a path to a valid image file
--        (relative to <code>res/img</code>).
function imgtween(image, targetTexture)
	targetTexture = tex(targetTexture)
	if not crossFadeTween(image, targetTexture, 30) then
		image:setTexture(targetTexture)
	end
	return image
end

---Convenience function for performing an <code>imgtween</code> on the current
-- background image. In the case of backgrounds, changing the background through
-- <code>bgf</code> has roughly the same default effect. The use of
-- <code>bgtween</code> is mainly for cases when <code>imgtween</code> is
-- overridden to do something different or to keep changes to the ImageDrawable
-- used for the background (<code>bgf</code> creates a new ImageDrawable each
-- time).
-- @param targetTexture A texture object or a path to a valid image file
--        (relative to <code>res/img</code>).
function bgtween(targetTexture)
	return imgtween(getBackground(), targetTexture)
end

---shaderTween
-------------------------------------------------------------------------------- @section

---Performs an animated transition of an ImageDrawable from
-- <code>tex1, gs1, ps1</code> to <code>tex2, gs2, ps2</code>.
-- @tparam ImageDrawable image The image to change the texture of.
-- @number duration The duration of the effect in frames (will be multiplied by
--         <code>effectSpeed</code> internally).
-- @tparam Texture tex1 The start texture.
-- @tparam GeometryShader gs1 The start geometry shader.
-- @tparam PixelShader ps1 The start pixel shader.
-- @tparam Texture tex2 The end texture.
-- @tparam GeometryShader gs2 The end geometry shader.
-- @tparam PixelShader ps2 The end pixel shader.
function shaderTween(image, duration, tex1, gs1, ps1, tex2, gs2, ps2)
	tex1 = tex(tex1)
	tex2 = tex(tex2)

	image:setTexture(tex1)
	
	duration = duration or 60
	
	local tween = ShaderImageTween.new(duration, gs1, ps1, gs2, ps2)
	tween:setEndImage(tex2)
	image:setTween(tween)
	while not image:isDestroyed() and not tween:isFinished() do
		yield()
	end
	return image
end

---Like <code>shaderTween</code>, but starts the transition from blank and ends
-- at <code>image:getTexture(), gs, ps</code>. 
-- @tparam ImageDrawable image The image to change the texture of.
-- @number duration The duration of the effect in frames (will be multiplied by
--         <code>effectSpeed</code> internally).
-- @tparam GeometryShader gs The end geometry shader.
-- @tparam PixelShader ps The end pixel shader.
-- @see shaderTween
function shaderTweenIn(image, duration, gs, ps)
	return shaderTween(image, duration, nil, nil, nil, image:getTexture(), gs, ps)
end

---Like <code>shaderTween</code>, but starts the transition from
-- <code>image:getTexture(), gs, ps</code> and ends at blank. 
-- @tparam ImageDrawable image The image to change the texture of.
-- @number duration The duration of the effect in frames (will be multiplied by
--         <code>effectSpeed</code> internally).
-- @tparam GeometryShader gs The start geometry shader.
-- @tparam PixelShader ps The start pixel shader.
-- @see shaderTween
function shaderTweenOut(image, duration, gs, ps)
	return shaderTween(image, duration, image:getTexture(), gs, ps, nil, nil, nil)
end

---Temporarily changes the shaders of <code>image</code> to <code>gs, ps</code>,
-- animates them to <code>time=1.0</code>, then changes the texture of
-- <code>image</code> to <code>targetTexture</code>. After the texture has been
-- changed, the shaders are animated back to <code>time=0.0</code> and then
-- replaced by the original shaders of <code>image</code>.
-- @tparam ImageDrawable image The image drawable to change the texture of.
-- @tparam Texture targetTexture The new texture for <code>img</code>
-- @number duration The duration of the effect in frames (will be multiplied by
--         <code>effectSpeed</code> internally).
-- @number bounceTime The shader time that should be reached halfway
--        through the effect (which is the bounce point).
-- @tparam GeometryShader gs The geometry shader
--        to use during the effect.
-- @tparam PixelShader ps The pixel shader to use
--        during the effect.
function shaderBounceTween(image, targetTexture, duration, bounceTime, gs, ps)
    targetTexture = tex(targetTexture)
    duration = duration or 60
    bounceTime = bounceTime or 1.0
    
    local oldGS = image:getGeometryShader()
    local oldPS = image:getPixelShader()
    if gs ~= nil then
        image:setGeometryShader(gs)
    end
    if ps ~= nil then
        image:setPixelShader(ps)
    end
    
    local time = 0
    local timeInc = (2 * bounceTime) / duration
    while not image:isDestroyed() do
        time = time + timeInc * effectSpeed
        if time >= bounceTime then
        	time = 2 * bounceTime - time --Wrap around
            timeInc = -timeInc
            image:setTexture(targetTexture)
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
    
    image:setTexture(targetTexture)
    image:setGeometryShader(oldGS)
    image:setPixelShader(oldPS)
    return image
end

---shutterTween
-------------------------------------------------------------------------------- @section

---Changes an ImageDrawable's texture using a shutter transition.
-- @tparam ImageDrawable image The image to shutter-tween.
-- @tparam Texture targetTexture The new texture for the image.
-- @int[opt=6] dir The direction of the shutter, 4=left, 8=up, 6=right, 2=down.
-- @int[opt=10] steps The number of shutter bars to use.
-- @number[opt=60] duration The duration of the tween in frames (will be
--        multiplied by <code>effectSpeed</code> internally).
function shutterTween(image, targetTexture, dir, steps, duration)
	dir = dir or 6
	steps = steps or 10

	local gs1 = ShutterGS.new(false, dir, steps)
	local gs2 = ShutterGS.new(true, dir, steps)
	return shaderTween(image, duration, img:getTexture(), gs1, nil, targetTexture, gs2, nil)
end

---Fades in an ImageDrawable's texture using a shutter transition.
-- @tparam ImageDrawable image The image to shutter-tween.
-- @int[opt=6] dir The direction of the shutter, 4=left, 8=up, 6=right, 2=down.
-- @int[opt=10] steps The number of shutter bars to use.
-- @number[opt=60] duration The duration of the tween in frames (will be
--        multiplied by <code>effectSpeed</code> internally).
-- @see shutterTween
function shutterTweenIn(image, dir, steps, duration)
	dir = dir or 6
	steps = steps or 10

	return shaderTweenIn(image, duration, ShutterGS.new(true, dir, steps))
end

---Fades away an ImageDrawable's texture using a shutter transition.
-- @tparam ImageDrawable image The image to shutter-tween.
-- @int[opt=6] dir The direction of the shutter, 4=left, 8=up, 6=right, 2=down.
-- @int[opt=10] steps The number of shutter bars to use.
-- @number[opt=60] duration The duration of the tween in frames (will be
--        multiplied by <code>effectSpeed</code> internally).
-- @see shutterTween
function shutterTweenOut(image, dir, steps, duration)
	dir = dir or 6
	steps = steps or 10

	return shaderTweenOut(image, duration, ShutterGS.new(false, dir, steps))
end

---wipeTween
-------------------------------------------------------------------------------- @section

---Changes an ImageDrawable's texture using a shutter transition.
-- @tparam ImageDrawable image The image to shutter-tween.
-- @tparam Texture targetTexture The new texture for the image.
-- @int[opt=6] dir The direction of the shutter, 4=left, 8=up, 6=right, 2=down.
-- @number[opt=0.1] span The range (between <code>0.0</code> and
--        <code>1.0</code>) of the partially transparent area of the wipe.
-- @number[opt=60] duration The duration of the tween in frames (will be
--        multiplied by <code>effectSpeed</code> internally).
function wipeTween(image, targetTexture, dir, span, duration)
	dir = dir or 6
	span = span or 0.2

	local gs1 = WipeGS.new(false, dir, span)
	local gs2 = WipeGS.new(true, dir, span)
	return shaderTween(image, duration, image:getTexture(), gs1, nil, targetTexture, gs2, nil)
end

---Fades in an ImageDrawable's texture using a wipe transition.
-- @tparam ImageDrawable image The image to shutter-tween.
-- @int[opt=6] dir The direction of the shutter, 4=left, 8=up, 6=right, 2=down.
-- @number[opt=0.1] span The range (between <code>0.0</code> and
--        <code>1.0</code>) of the partially transparent area of the wipe.
-- @number[opt=60] duration The duration of the tween in frames (will be
--        multiplied by <code>effectSpeed</code> internally).
-- @see wipeTween
function wipeTweenIn(image, dir, span, duration)
	dir = dir or 6
	span = span or 0.2

	return shaderTweenIn(image, duration, WipeGS.new(true, dir, span))
end

---Fades away an ImageDrawable's texture using a wipe transition.
-- @tparam ImageDrawable image The image to shutter-tween.
-- @int[opt=6] dir The direction of the shutter, 4=left, 8=up, 6=right, 2=down.
-- @number[opt=0.1] span The range (between <code>0.0</code> and
--        <code>1.0</code>) of the partially transparent area of the wipe.
-- @number[opt=60] duration The duration of the tween in frames (will be
--        multiplied by <code>effectSpeed</code> internally).
-- @see wipeTween
function wipeTweenOut(image, dir, span, duration)
	dir = dir or 6
	span = span or 0.2

	return shaderTweenOut(image, duration, WipeGS.new(false, dir, span))
end

---crossFadeTween
-------------------------------------------------------------------------------- @section

---Changes an ImageDrawable's texture using a cross fade (dissolve) transition.
-- @tparam ImageDrawable image The image to tween.
-- @tparam Texture targetTexture The new texture for the image.
-- @param duration The duration of the fade in frames (will be multiplied by
--        <code>effectSpeed</code> internally).
-- @param interpolator A function or Interpolator object mapping an input
--        in the range <code>(0, 1)</code> to an output in the range
--        <code>(0, 1)</code>.
-- @treturn bool <code>false</code> if cross fade tweens aren't supported on the
--          current hardware. Useful to provide a fallback tween in case the
--          cross fade tween doesn't work.
function crossFadeTween(image, targetTexture, duration, interpolator)
	if CrossFadeTween == nil or not CrossFadeTween.isAvailable() then
		return false
	end
	
	duration = duration or 30
	targetTexture = tex(targetTexture)

	local tween = CrossFadeTween.new(duration, interpolator)
	tween:setEndImage(targetTexture)
	image:setTween(tween)
	while not image:isDestroyed() and not tween:isFinished() do
		yield()
	end
	return true
end

---bitmapTween
-------------------------------------------------------------------------------- @section

---Changes an ImageDrawable's texture through a dissolve effect shaped by a
-- grayscale bitmap.
-- @tparam ImageDrawable image The image to tween.
-- @tparam Texture targetTexture The new texture for the image.
-- @param bitmap The filename (relative to <code>res/img</code>) of the bitmap
--        that controls the shape of the tween.
-- @number duration The duration of the tween in frames (will be
--        multiplied by <code>effectSpeed</code> internally).
-- @number[opt=0.5] range Determines the relative width of the fading region
--        between <code>0.0</code> and <code>1.0</code>.
-- @param[opt=nil] interpolator A function or interpolator object mapping an
--       input in the range <code>(0, 1)</code> to an output in the range
--       <code>(0, 1)</code>.
-- @treturn bool <code>false</code> if bitmap tweens aren't supported on the
--          current hardware. Useful to provide a fallback tween in case the
--          bitmap tween doesn't work.
function bitmapTween(image, targetTexture, bitmap, duration, range, interpolator)
	if BitmapTween == nil or not BitmapTween.isAvailable() then
		return false
	end

	duration = duration or 60
	range = range or 0.5
	targetTexture = tex(targetTexture)

	local tween = BitmapTween.new(bitmap, duration, range, interpolator)
	tween:setEndImage(targetTexture) --, 7)
	image:setTween(tween)
	while not image:isDestroyed() and not tween:isFinished() do
		yield()
	end
	return image
end

---Fades in an ImageDrawable's texture using a bitmap transition.
-- @tparam ImageDrawable image The image to tween.
-- @param bitmap The filename (relative to <code>res/img</code>) of the bitmap
--        that controls the shape of the tween.
-- @number duration The duration of the tween in frames (will be
--        multiplied by <code>effectSpeed</code> internally).
-- @number[opt=0.5] range Determines the relative width of the fading region
--        between <code>0.0</code> and <code>1.0</code>.
-- @param[opt=nil] interpolator A function or interpolator object mapping an
--       input in the range <code>(0, 1)</code> to an output in the range
--       <code>(0, 1)</code>.
-- @treturn bool <code>false</code> if bitmap tweens aren't supported on the
--          current hardware. Useful to provide a fallback tween in case the
--          bitmap tween doesn't work.
-- @see bitmapTween
function bitmapTweenIn(image, bitmap, duration, range, interpolator)
	if BitmapTween == nil or not BitmapTween.isAvailable() then
		return false
	end

	local tex = image:getTexture()
	image:setTexture(nil)
	return bitmapTween(image, tex, bitmap, duration, range, interpolator)
end

---Fades away an ImageDrawable's texture using a bitmap transition.
-- @tparam ImageDrawable image The image to tween.
-- @param bitmap The filename (relative to <code>res/img</code>) of the bitmap
--        that controls the shape of the tween.
-- @number duration The duration of the tween in frames (will be
--        multiplied by <code>effectSpeed</code> internally).
-- @number[opt=0.5] range Determines the relative width of the fading region
--        between <code>0.0</code> and <code>1.0</code>.
-- @param[opt=nil] interpolator A function or interpolator object mapping an
--       input in the range <code>(0, 1)</code> to an output in the range
--       <code>(0, 1)</code>.
-- @treturn bool <code>false</code> if bitmap tweens aren't supported on the
--          current hardware. Useful to provide a fallback tween in case the
--          bitmap tween doesn't work.
-- @see bitmapTween
function bitmapTweenOut(image, bitmap, duration, range, interpolator)
	if BitmapTween == nil or not BitmapTween.isAvailable() then
		return false
	end

	return bitmapTween(image, nil, bitmap, duration, range, interpolator)
end

-------------------------------------------------------------------------------- @section end
