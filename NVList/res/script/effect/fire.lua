
-------------------------------------------------------------------------------
-- fire.lua
-------------------------------------------------------------------------------
-- Heat haze and glow effects
-------------------------------------------------------------------------------

local FireImage = {
    sprite=nil,
    overlay=nil,
    thread=nil,
    alpha=1,
    glow=true,
    brightness=0.15
}

function FireImage.new(image, self)
	self = extend(FireImage, self)

    self.sprite = image
    
    if self.glow then
        self.overlay = img(image:getTexture(), {z=image:getZ()-1, blendMode=BlendMode.ADD})
        self.overlay:setPos(image:getX(), image:getY())
        self.overlay:setTexture(self:generateOverlayTexture(image:getTexture()), 5)
    else
        local ps = Shader.createGLSLShader("fire-shadow")
        ps:setLooper(Looper.new(0.01))
        self.sprite:setPixelShader(ps)
    end
    
    self.thread = newThread(function()
        local t = 0
        while self.overlay ~= nil and not self.overlay:isDestroyed() do
            local a = 0
            if self.glow then
                a = .9 + .1 * math.fastSin(t * 0.7) * math.fastCos(t * 0.3)
            end
            self.overlay:setAlpha(self.alpha * a)
            t = t + 8 * effectSpeed
            yield()
        end
    end)

    return self
end

function FireImage:destroy()
    self.sprite:destroy()
    if self.overlay ~= nil then
        self.overlay:destroy()
    end
    self.thread:destroy()
end

function FireImage:isDestroyed()
    return self.sprite:isDestroyed()
end

function FireImage:generateOverlayTexture(t)
    t = tex(t)    
    t = ImageFx.mipmap(t, 2)
    t = brighten(t, self.brightness)
    t = blur(t, 64, 2)
    return t
end

function FireImage:getAlpha()
    return self.alpha
end

function FireImage:setAlpha(a)
    self.alpha = a or 0
    self.sprite:setAlpha(a)
end

function FireImage:setTexture(t)
    t = tex(t)

	if CrossFadeTween == nil or not CrossFadeTween.isAvailable() then
		self.sprite:setTexture(t)		
	    if self.overlay ~= nil then
        	self.overlay:setTexture(self:generateOverlayTexture(t), 5)
       	end
		return
	end

	local tween1 = CrossFadeTween.new(30)
	tween1:setEndImage(t, 5)

    local tween2 = nil
    if self.overlay ~= nil then
        tween2 = CrossFadeTween.new(30)
        tween2:setEndImage(self:generateOverlayTexture(t), 5)
    end
        
    yield()

    --Install tweens
	self.sprite:setTween(tween1)
    if tween2 ~= nil then
        self.overlay:setTween(tween2)
    end

    --Wait for tween to finish
	while tween1 ~= nil and not self.sprite:isDestroyed() and not tween1:isFinished() do
        yield()
    end
    while tween2 ~= nil and not self.overlay:isDestroyed() and not tween2:isFinished() do
		yield()
	end
end

function fireimg(...)
    return FireImage.new(img(...))
end

function fireimgf(...)
    local i = fireimg(...)
    i:setAlpha(0)
    fadeTo(i, 1)
    return i
end

function firebg(...)
    local i = bg(...)
    
    if Shader.isGLSLVersionSupported("1.1") then
        local ps = Shader.createGLSLShader("fire-background")
        ps:setLooper(Looper.new(0.01))
        i:setPixelShader(ps)
    else
        i:setColorRGB(0xC04020)
    end
    
    return i
end

function firebgf(...)
    local i = firebg(...)
    i:setAlpha(0)
    fadeTo(i, 1)
    return i
end

