
-------------------------------------------------------------------------------
-- rain.lua
-------------------------------------------------------------------------------
-- Rain effect
-------------------------------------------------------------------------------

-- ----------------------------------------------------------------------------
-- Variables
-- ----------------------------------------------------------------------------

local rainCloud = nil

-- ----------------------------------------------------------------------------
-- Classes
-- ----------------------------------------------------------------------------

local Raindrop = {
    cloud=nil,
    sprite=nil,
    textures=nil,
    dir=256,
    speed=0,
    z=1,
    dx=0,
    dy=1,
    minX=0,
    maxX=screenWidth,
    maxY=screenHeight
}

function Raindrop.new(cloud, self)
	self = extend(Raindrop, self)

    self.cloud = cloud
    self.textures = self.textures or cloud.textures
    
    self.sprite = img(nil)
    self.sprite:setColorRGB(0xc0c0c0)
    
    self:setSpeed(40 + 20 * math.random())
    self:init()
    self:setPos(self:getX(), self:getY() - math.random() * screenHeight)

    return self
end

function Raindrop:init()
    self.z = math.random(5, 150)

    local scale = math.max(0.1, 1 - (self.z - 25) / 100)
    self.sprite:setScale(scale, scale)
    
    self:setPos(math.random(self.minX, self.maxX), -self.sprite:getHeight())
    
    local baseDir = self.cloud.windDir
    local deltaDir = 2 + math.abs(baseDir-256) * .25
    self:setDir(math.random(baseDir-deltaDir, baseDir+deltaDir))    
end

function Raindrop:destroy()
    self.sprite:destroy()
end

function Raindrop:isDestroyed()
    return self.sprite:isDestroyed()
end

function Raindrop:update()
    local s = self.sprite
    local x = s:getX()
    local y = s:getY()
    
    if x < self.minX or x > self.maxX or y > self.maxY then
        self:init()
    else
        s:setPos(x + self.dx, y + self.dy)    
    end
end

function Raindrop:getX()
    return self.sprite:getX()
end

function Raindrop:getY()
    return self.sprite:getY()
end

function Raindrop:setPos(x, y)
    self.sprite:setPos(x, y)
end

function Raindrop:onChange()
    self.sprite:setRotation(self.dir)
    
    local sinDir = math.fastSin(self.dir)
    local cosDir = math.fastCos(self.dir)
    self.dx = self.speed * sinDir
    self.dy = self.speed * -cosDir
    
    local blurIndex = math.floor(1.0 / self.sprite:getScaleY())
    blurIndex = math.max(1, math.min(#self.textures, blurIndex))
    local texIndex = math.ceil(self.speed / 15)
    texIndex = math.max(1, math.min(#self.textures[blurIndex], texIndex))
    local t = self.textures[blurIndex][texIndex]
    self.sprite:setTexture(t)
    
    local w2 = self.sprite:getWidth()/2
    local h2 = self.sprite:getHeight()/2
    local sizeX = math.abs(cosDir) * w2 + math.abs(sinDir) * h2 
    local sizeY = math.abs(sinDir) * w2 + math.abs(cosDir) * h2
    local deltaX = -self.dx * (screenHeight + sizeX * 2) / self.dy
    self.minX = -sizeX + math.min(0, deltaX)
    self.maxX = screenWidth + sizeX + math.max(0, deltaX)
    
    self.maxY = screenHeight + sizeY
end

function Raindrop:setSpeed(s)
    self.speed = s

    self:onChange()
end

function Raindrop:setDir(d)
    self.dir = d

    self:onChange()
end

-- ----------------------------------------------------------------------------

local Raincloud = {
    windDir=256,
    textures=nil,
    thread=nil,
    drops=nil    
}

function Raincloud.new(self)
    self = extend(Raincloud, self)
    
    if self.textures == nil then
        self.textures = {}
        for b=1,2 do
            self.textures[b] = {}
            for i=1,4 do
                self.textures[b][i] = tex("effect/rain/rain#b" .. b .. "i" .. i)
            end
        end
    end

    return self
end

function Raincloud:destroy()
    if self.drops ~= nil then
        for _,d in pairs(self.drops) do
            d:destroy()
        end
        self.drops = nil
    end
    if self.thread ~= nil then
        self.thread:destroy()
        self.thread = nil
    end
end

function Raincloud:start(numRaindrops)
    local drops = {}
    local bstep = 100
    for b=1,numRaindrops,bstep do
        for i=b,math.min(numRaindrops, b+bstep-1) do
            drops[i] = Raindrop.new(self)
        end
        yield()
    end    
    self.drops = drops

    local targetWind = 256
    local targetWindChange = 16
    self.thread = newThread(function()
        while true do
            if scriptDebug and input:consumeKey(Keys.R) then
                targetWind = 192 + math.floor(math.random(0, 1)) * 128
                --print(drops[1].minX, drops[1].maxX)
            end
                
            if self.windDir > 288 then
                self.windDir = math.max(288, self.windDir - 2 * targetWindChange)
            elseif self.windDir < 192 then
                self.windDir = math.min(192, self.windDir + 2 * targetWindChange)
            else
                self.windDir = self.windDir + targetWindChange * signum(targetWind - self.windDir)
            end

            for i,v in ipairs(drops) do
                drops[i]:update()
            end
            yield()
        end
    end)
end

-- ----------------------------------------------------------------------------

function stopRain()
    if rainCloud ~= nil then
        rainCloud:destroy()
        rainCloud = nil
    end
end

function startRain(numRaindrops)
    stopRain()

    numRaindrops = numRaindrops or 200
    if System.isLowEnd() then
        numRaindrops = numRaindrops / 2
    end

    rainCloud = Raincloud.new()
    rainCloud:start(numRaindrops)
    return rainCloud
end

