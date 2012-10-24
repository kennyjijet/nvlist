
-------------------------------------------------------------------------------
-- fog.lua
-------------------------------------------------------------------------------
-- Fog effect
-------------------------------------------------------------------------------

-- ----------------------------------------------------------------------------
-- Variables
-- ----------------------------------------------------------------------------

local fog = nil

-- ----------------------------------------------------------------------------
-- Classes
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

function stopFog(fadeSpeed)
    fadeSpeed = fadeSpeed or 0.01

    if fog ~= nil then
        fadeTo(fog, 0, fadeSpeed)
        fog:destroy()
        fog = nil
    end
end

function startFog(fadeSpeed, overrides)
    stopFog(fadeSpeed)

    fadeSpeed = fadeSpeed or 0.01
    local alpha = .4
    if overrides ~= nil then
        alpha = overrides.alpha or alpha
    end
    
    fog = Fog.new(overrides)
    fog:setAlpha(0)
    fog:start()
    fadeTo(fog, alpha, fadeSpeed)
    return fog
end

