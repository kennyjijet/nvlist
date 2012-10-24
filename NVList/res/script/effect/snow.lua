
-------------------------------------------------------------------------------
-- snow.lua
-------------------------------------------------------------------------------
-- Snow effect
-------------------------------------------------------------------------------

-- ----------------------------------------------------------------------------
-- Variables
-- ----------------------------------------------------------------------------

local snowCloud = nil

-- ----------------------------------------------------------------------------
-- Classes
-- ----------------------------------------------------------------------------

local Snowflake = {
    cloud=nil,
    sprite=nil,
    textures=nil,
    dir=256,
    speed=0,
    minX=0,
    maxX=screenWidth,
    maxY=screenHeight,
    depth=0,
    frame=1
}

function Snowflake.new(cloud, self)
	self = extend(Snowflake, self)

    self.cloud = cloud
    self.textures = self.textures or cloud.textures
    
    self.sprite = img(self.textures[math.random(1, #self.textures)])

    local z = math.random(7, 20)
    self.depth = z * 0.1
    cloud.camera:add(self.sprite, self.depth, true, true)
    
    self.speed = 8 + 4 * math.random()
    self:init()
    self:setPos(self:getX(), self:getY() - math.random() * screenHeight * 2)
    
    self.sprite:setZ(z)

    return self
end

function Snowflake:init()
    local zoom = self.depth
    local sw = 64 / zoom
    local sh = 64 / zoom
    self.minX = (-0.5 * screenWidth ) * zoom - sw
    self.maxX = ( 1.5 * screenWidth ) * zoom + sw
    local minY= (-0.5 * screenHeight) * zoom - sh
    self.maxY = ( 1.0 * screenHeight) * zoom + sh
    
    self:setPos(self.minX+math.random()*(self.maxX-self.minX), minY)
    self.frame = math.random(1, 512)    
end

function Snowflake:destroy()
    self.cloud.camera:remove(self.sprite)
    self.sprite:destroy()
end

function Snowflake:isDestroyed()
    return self.sprite:isDestroyed()
end

function Snowflake:update()
    local s = self.sprite
    local x = s:getX()
    local y = s:getY()
    
    if x < self.minX or x > self.maxX or y > self.maxY then
        self:init()
        
        --self.sprite:setColor(0, 1, 0)
    else
        self.dir = self.cloud.windDir + 16 * math.fastSin(self.frame * 2)
    
        local sinDir = math.fastSin(self.dir)
        local cosDir = math.fastCos(self.dir)
        local dx = self.speed * sinDir
        local dy = self.speed * -cosDir    
        s:setPos(x + dx, y + dy)        

        --self.sprite:setColor(s:getY() / self.maxY, 0, 0)
    end
    
    self.frame = self.frame + 1
end

function Snowflake:getX()
    return self.sprite:getX()
end

function Snowflake:getY()
    return self.sprite:getY()
end

function Snowflake:setPos(x, y)
    self.sprite:setPos(x, y)
end

-- ----------------------------------------------------------------------------

local Snowcloud = {
    windDir=256,
    textures=nil,
    camera=nil,
    thread=nil,
    flakes=nil
}

function Snowcloud.new(self)
    self = extend(Snowcloud, self)
    
    if self.textures == nil then
        self.textures = {}
        for i=3,5 do
            table.insert(self.textures, tex("effect/snow/snow#i" .. i))
        end
    end
    
    local c = createCamera()
    c:setBlurLevels(2)
    c:setSubjectDistance(2)
    self.camera = c
    
    return self
end

function Snowcloud:destroy()
    if self.flakes ~= nil then
        for _,f in pairs(self.flakes) do
            f:destroy()
        end
        self.flakes = nil
    end
    self.camera:destroy()
    if self.thread ~= nil then
        self.thread:destroy()
        self.thread = nil
    end
end

function Snowcloud:start(numSnowflakes)
    local flakes = {}
    local bstep = 100
    for b=1,numSnowflakes,bstep do
        for i=b,math.min(numSnowflakes, b+bstep-1) do
            flakes[i] = Snowflake.new(self)
        end
        yield()
    end
    self.flakes = flakes
    
    if self.thread ~= nil then
        self.thread:destroy()
    end
    
    self.thread = newThread(function()
        while true do
            for i,v in ipairs(flakes) do
                v:update()
            end
            self.camera:applyTransform()
            yield()
        end
    end)
end

-- ----------------------------------------------------------------------------

function stopSnow()
    if snowCloud ~= nil then
        snowCloud:destroy()
        snowCloud = nil
    end
end

function startSnow(numSnowflakes)
    stopSnow()

    numSnowflakes = numSnowflakes or 150
    if System.isLowEnd() then
        numSnowflakes = numSnowflakes / 2
    end

    snowCloud = Snowcloud.new()
    snowCloud:start(numSnowflakes)
    return snowCloud
end

