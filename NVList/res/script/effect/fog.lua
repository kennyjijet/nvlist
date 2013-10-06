
-------------------------------------------------------------------------------
-- fog.lua
-------------------------------------------------------------------------------
-- Fog effect
-------------------------------------------------------------------------------

-- ----------------------------------------------------------------------------
-- Variables
-- ----------------------------------------------------------------------------

local fog = nil
local simpleFog = nil

-- ----------------------------------------------------------------------------
-- Functions
-- ----------------------------------------------------------------------------

local Fog = {
    thread=nil,
    images=nil,
    tw=1,
    th=1,
    alpha=1,
    dx=.50,
    dy=.15
}

function Fog.new(self)
	self = extend(Fog, self)

    local tile = tex(self.tex or "effect/fog/fog")
    local tw = tile:getWidth()
    local th = tile:getHeight()
    self.tw = tw
    self.th = th
    
    if self.images == nil then
        self.images = {}                
        local rows = math.ceil((tw+screenHeight)/tw)
        local cols = math.ceil((th+screenWidth)/th)
        for y=0,rows-1 do
            for x=0,cols-1 do
                local i = img(tile, x*tw, y*th)
                i:setZ(self.z or 0)
                table.insert(self.images, i)
            end
        end
    end
    
    return self
end

function Fog:destroy()
    for _,i in ipairs(self.images) do
        i:destroy()
    end
    self.images = {}

    if self.thread ~= nil then
        self.thread:destroy()
        self.thread = nil
    end
end

function Fog:start()
    if self.thread ~= nil then
        self.thread:destroy()
    end
    
    self:setAlpha(self.alpha)
    
    local dx = self.dx
    local dy = self.dy
    local minX = -self.tw
    local maxX = self.tw * math.ceil(screenWidth / self.tw)
    local minY = -self.th
    local maxY = self.th * math.ceil(screenHeight / self.th)

    self.thread = newThread(function()    
        while true do
            for _,i in ipairs(self.images) do
                local x = i:getX() + dx
                local y = i:getY() + dy

                if x < minX then x = x + (maxX - minX) end
                if y < minY then y = y + (maxY - minY) end
                if x > maxX then x = x - (maxX - minX) end
                if y > maxY then y = y - (maxY - minY) end
                
                i:setPos(x, y)
            end
            yield()
        end
    end)
end

function Fog:getAlpha()
    return self.alpha
end

function Fog:setAlpha(a)
    self.alpha = a
    for _,i in pairs(self.images) do
        i:setAlpha(a)
    end
end

-- ----------------------------------------------------------------------------

function stopFog(fadeDuration)
    fadeDuration = fadeDuration or 90

    if fog ~= nil then
        fadeTo(fog, 0, fadeDuration)
        fog:destroy()
        fog = nil
    end
end

function startFog(fadeDuration, overrides)
    stopFog(fadeDuration)

    fadeDuration = fadeDuration or 90

    local alpha = .4
    if overrides ~= nil then
        alpha = overrides.alpha or alpha
    end
    
    fog = Fog.new(overrides)
    fog:setAlpha(0)
    fog:start()
    fadeTo(fog, alpha, fadeDuration)
    return fog
end

-- ----------------------------------------------------------------------------

local SimpleFog = {
    thread=nil,
    image=nil,
    tw=1,
    th=1,
    alpha=1,
    dx=.50,
    dy=.15,
    scaleX=1,
    scaleY=1,
    bounds=nil
}

function SimpleFog.new(self)
	self = extend(SimpleFog, self)

    local tile = tex(self.tex or "effect/fog/fog")
    local tw = tile:getWidth()
    local th = tile:getHeight()
    self.tw = tw
    self.th = th
    
    local bounds = self.bounds or {0, 0, screenWidth, screenHeight}

    if self.image == nil then
        local i = img(tile)
        i:setBounds(bounds[1], bounds[2], bounds[3], bounds[4])
        i:setZ(self.z or 0)
        self.image = i
    end
    
    return self
end

function SimpleFog:destroy()
    self.image:destroy()

    if self.thread ~= nil then
        self.thread:destroy()
        self.thread = nil
    end
end

function SimpleFog:start()
    if self.thread ~= nil then
        self.thread:destroy()
    end
    
    self:setAlpha(self.alpha)
    
    local dx = self.dx
    local dy = self.dy    
    local tw = self.tw
    local th = self.th
    
    local u = 0
    local v = 0
    local tilesW = self.image:getWidth() / (self.scaleX * tw)
    local tilesH = self.image:getHeight() / (self.scaleY * th)
    
    self.thread = newThread(function()    
        while true do
            u = u - dx / tw
            v = v - dy / th
            self.image:setUV(u, v, tilesW, tilesH)
            yield()
        end
    end)
end

function SimpleFog:getAlpha()
    return self.alpha
end

function SimpleFog:setAlpha(a)
    self.alpha = a
    self.image:setAlpha(a)
end

-- ----------------------------------------------------------------------------

function stopFog(fadeDuration)
    fadeDuration = fadeDuration or 90


    if fog ~= nil then
        fadeTo(fog, 0, fadeDuration)
        fog:destroy()
        fog = nil
    end
end

function startFog(fadeDuration, overrides)
    stopFog(fadeDuration)

    fadeDuration = fadeDuration or 90

    local alpha = .4
    if overrides ~= nil then
        alpha = overrides.alpha or alpha
    end
    
    fog = Fog.new(overrides)
    fog:setAlpha(0)
    fog:start()
    fadeTo(fog, alpha, fadeDuration)
    return fog
end

function stopSimpleFog(fadeDuration)
    fadeDuration = fadeDuration or 90

    if simpleFog ~= nil then
        fadeTo(simpleFog, 0, fadeDuration)
        simpleFog:destroy()
        simpleFog = nil
    end
end

function startSimpleFog(fadeDuration, overrides)
    stopSimpleFog(fadeDuration)

    fadeDuration = fadeDuration or 90

    local alpha = .4
    if overrides ~= nil then
        alpha = overrides.alpha or alpha
    end
    
    simpleFog = SimpleFog.new(overrides)
    simpleFog:setAlpha(0)
    simpleFog:start()
    fadeTo(simpleFog, alpha, fadeDuration)
    return simpleFog
end

