-------------------------------------------------------------------------------
-- anim.lua
-------------------------------------------------------------------------------
-- Provides the 'built-in' VN animation functions.
-------------------------------------------------------------------------------

module("vn.anim", package.seeall)

-- ----------------------------------------------------------------------------
--  Variables
-- ----------------------------------------------------------------------------

-- ----------------------------------------------------------------------------
--  Classes
-- ----------------------------------------------------------------------------

local function destroyAnimatorThread(anim)
    local t = anim.thread
    anim.thread = nil
    if t ~= nil then t:destroy() end
end

local Animator = {
    time=0,
    duration=0,
    loops=1,
    thread=nil
}

---Starts a background animation thread.
function Animator:start(loops)
	self.loops = loops or self.loops or 1
	destroyAnimatorThread(self)	
	self.time = 0
	
    self.thread = newThread(function()
        while not self.destroyed do
            self.time = self.time + effectSpeed            
            if self.time >= self.duration then
            	self:onLoopEnd()
            	if self.loops == 0 then
	                self:finish()
	                return --Thread gets killed inside finish() anyway...
	            end
            end
            self:update()
            yield()
        end
    end)
    self:update()
end

---Called when the current time exceeds the duration
function Animator:onLoopEnd()
	local spillover = math.max(0, self.duration - self.time)
	
	local completed = 1
	if spillover > self.duration and self.duration > 0 then
		completed = spillover / self.duration
	end
	
	self.time = math.max(0, spillover - self.duration * completed)
	if self.loops < 0 then
		return self.loops
	end
	self.loops = math.max(0, self.loops - completed)
end

---Calls <code>start</code>, and waits for the background thread to finish.
-- @see Animator:start
function Animator:run(...)
    self:start(...)
    Anim.waitFor(self)
end

---Gets called every frame once started
function Animator:update()
end

---Returns <code>true</code> if the animation is currently running.
function Animator:isRunning()
	return self.thread ~= nil and not self.thread:isFinished()
end

---Called after the animation ends, either by finishing or by being destroyed.
function Animator:onEnd()
	destroyAnimatorThread(self)
end

---Immediately kills any background threads, not bothering to cleanly finish
-- the animation. Use this method if you want to cancel the animation, use
-- <code>finish</code> if you want to end it cleanly by skipping to the end.
-- @see Animator:finish
function Animator:destroy()
	self:onEnd()
end

---Instantly skips to the end of the animation.
function Animator:finish()
    self.time = self.duration
	self.loops = 0
    self:update()

	self:onEnd()
end

-- ----------------------------------------------------------------------------

local WaitAnimator = {
}

function WaitAnimator.new(self)
	return extend(Animator, WaitAnimator, self)
end

-- ----------------------------------------------------------------------------

local ParallelAnimator = {
	anims=nil
}

function ParallelAnimator.new(self)
	self = extend(Animator, ParallelAnimator, self)
	self.anims = values(self.anims or {})
	return self
end

function ParallelAnimator:multicall(func, ...)
	for _,anim in pairs(self.anims) do
		anim[func](anim, ...)
	end
end

function ParallelAnimator:start(loops)
	self.loops = loops or self.loops or 1
	destroyAnimatorThread(self)
		
	self:multicall("start", 1)
	
    self.thread = newThread(function()
        while not self.destroyed do
        	local running = false
			for _,anim in pairs(self.anims) do
				if anim:isRunning() then
					running = true
					break
				end
			end
        
        	if not running then
            	self:onLoopEnd()
            	if self.loops == 0 then
	                self:finish()
	                return --Thread gets killed inside finish() anyway...
	            else
	            	self:multicall("start", 1)
	            end
	        end
	        
        	yield()
        end
    end)
	
	self:update()
end

function ParallelAnimator:update()
	return self:multicall("update")
end

function ParallelAnimator:destroy()
	self:multicall("destroy")
	return Animator.destroy(self)
end

function ParallelAnimator:finish()
	self:multicall("finish")
	return Animator.finish(self)
end

-- ----------------------------------------------------------------------------

local SequentialAnimator = {
	anims=nil,
	active=0
}

function SequentialAnimator.new(self)
	self = extend(Animator, SequentialAnimator, self)	
	
	self.anims = values(self.anims or {})
		
	return self
end

function SequentialAnimator:start(loops)
	self.loops = loops or self.loops or 1
	destroyAnimatorThread(self)
	self.time = 0
	self.active = 1
	
    self.thread = newThread(function()
        while not self.destroyed and self.active <= #self.anims do
        	self:update()
        	yield()
        end
    end)
    
	local anim = self.anims[self.active]
	if anim ~= nil then
    	anim:start()
    end    
    self:update()
end

function SequentialAnimator:update()
    if self.anims[self.active] == nil then
    	self:onLoopEnd()
    	if self.loops == 0 then
    		return
    	end
    	self.active = 1
    	self.activeAnim = self.anims[self.active]
    end

	while not self.destroyed and self.active <= #self.anims do
		local anim = self.anims[self.active]
		
		--If current animation finished
   		if not anim:isRunning() then
   			self.active = self.active + 1
   			if self.active > #self.anims then
   				--Reached end of current loop
		    	self:onLoopEnd()
		    	if self.loops == 0 then
		    		break
		    	end
		    	self.active = 1
   			end
   			
			anim = self.anims[self.active]
			anim:start()
   		end
		
		--Call update on the current anim
   		anim:update()
		break
	end    
end

function SequentialAnimator:destroy()
	local anim = self.anims[self.active]
	if anim ~= nil then
		anim:destroy()
	end
	return Animator.destroy(self)
end

function SequentialAnimator:finish()
	self.loops = 0
	while not self.destroyed and self.active <= #self.anims do
		local anim = self.anims[self.active]
		if anim ~= nil then
			anim:finish()
		end
		self.active = self.active + 1
	end
	return Animator.finish(self)
end

-- ----------------------------------------------------------------------------

local PropertyInterpolator = {
    obj=nil,
    property=nil,
    interpolator=nil,
    startval=nil,
    endval=nil
}

function PropertyInterpolator.new(self)
    return extend(Animator, PropertyInterpolator, self)
end

function PropertyInterpolator:start(loops)
    self.startval = self.startval or getProperty(self.obj, self.property)
    return Animator.start(self, loops)
end

function PropertyInterpolator:update()
    local f = 0
    if self.duration > 0 and self.time >= 0 and self.time <= self.duration then
    	f = self.time / self.duration
    	if self.interpolator ~= nil then
    		f = self.interpolator:remap(f)
    	end
    end

    local v = self:interpolateValue(self.startval, self.endval, f)
    setProperty(self.obj, self.property, v)
end

function PropertyInterpolator:interpolateValue(a, b, frac)
    local result = Anim.interpolateValue(a, b, frac)
    --print(a, b, frac, result)
    return result
end

-- ----------------------------------------------------------------------------

local FilmstripAnimator = {
    obj=nil,
    oldtex=nil,
    frames=nil --Each frame in the filmstrip is a table: {texture, duration}
}

function FilmstripAnimator.new(self)
    self = extend(Animator, FilmstripAnimator, self)
    
    local d = 0
    for _,f in ipairs(self.frames) do
    	d = d + f.duration
    end
    self.duration = d
    
    return self
end

function FilmstripAnimator:start(loops)
	self.oldtex = self.oldtex or self.obj:getTexture()
    return Animator.start(self, loops)
end

function FilmstripAnimator:update()
	if self.obj:isDestroyed() then
		self:destroy()
		return
	end

	local frame0 = nil
	local frame1 = nil
	local frac = 0
	
	--Find which frame should be displayed
	local time = self.time
	for i,f in ipairs(self.frames) do
		frame0 = f
		if time <= f.duration then
			frame1 = self.frames[i+1]
			if frame1 == nil and (self.loops < 1 or self.loops > 1) then
				frame1 = self.frames[1]
			end
			frac = time / f.duration
			break
		else
			time = time - f.duration
		end
	end
	
	--Something weird happened (1 frame or less | self.time past last frame)
	if frame1 == nil then
		if frame0 == nil then
			self.obj:setTexture(self.oldtex)
			return
		else
			frame1 = frame0
		end
	end
	
    self.setTexture(self.obj, tex(frame0.texture), tex(frame1.texture), frac)
end

function FilmstripAnimator:onEnd()
	self.obj:setTexture(self.oldtex)
	Animator.onEnd(self)
end

---Preloads all textures that would be used in the animation.
function FilmstripAnimator:preload()
	for _,f in ipairs(self.frames) do
		preload(f.texture)
	end
end

function FilmstripAnimator.setTexture(i, tex0, tex1, frac)
	i:setTexture(tex0) --Don't fade or anything, just show the current tex until it's done
end

-- ----------------------------------------------------------------------------

local FunctorAnimator = {
	func=nil
}

function FunctorAnimator.new(self)
	return extend(Animator, FunctorAnimator, self)
end

function FunctorAnimator:update()
	local frac = 0
	if self.duration > 0 then
		frac = self.time / self.duration
	end
	self.func(frac)
end

-- ----------------------------------------------------------------------------
--  Functions
-- ----------------------------------------------------------------------------

Anim = {
}

---Interpolates between two values <code>a, b</code> based on a weight factor
-- <code>frac</code>.
-- @param a The first value
-- @param b The second value
-- @param frac The weight factor between <code>0.0</code> and <code>1.0</code>,
--        where <code>0.0</code> returns <code>a</code> and <code>1.0</code>
--        returns <code>b</code>.
-- @return A value in-between <code>a</code> and <code>b</code>.
function Anim.interpolateValue(a, b, frac)
    local typeA = type(a)
    local typeB = type(b)

    if typeA == "table" and typeB == "table" then
        local result = {}
        for k,va in pairs(a) do
            local vb = b[k]
            result[k] = Anim.interpolateValue(va, vb, frac)
        end
        return result
    elseif typeA ~= "number" or typeB ~= "number" then
        if frac >= 0.5 then
            return b
        else
            return a
        end
    else
        return a + (b-a) * frac
    end
end

---Blocks until all Animators passed as arguments are not (or are no longer) running.
function Anim.waitFor(...)
	for _,anim in ipairs(arg) do
	    while anim:isRunning() do
	    	yield()
	    end
	end
end

---Calls <code>Anim.tweenFromTo</code> using the current value of the property
-- as its <code>startval</code>.
-- @see Anim.tweenFromTo
function Anim.tweenTo(obj, property, endval, duration, interpolator)
    return Anim.tweenFromTo(obj, property, nil, endval, duration, interpolator)
end

---Gradually changes the value of <code>property</code> from
-- <code>startval</code> to <code>endval</code> over the course of
-- <code>duration</code> frames.
-- @param obj The object to change the property of
-- @param property The property to change
-- @param startval The initial value to set the property to
-- @param endval The end value for the property
-- @param duration The number of frames to take
-- @param interpolator An optional Interpolator object, can be used to create
--        an ease-in, ease-out effect.
-- @see Anim.createTween
function Anim.tweenFromTo(obj, property, startval, endval, duration, interpolator)
    local tween = Anim.createTween(obj, property, startval, endval, duration, interpolator)
    tween:run()
end

---Returns an Animator providing more control than <code>Anim.tweenFromTo</code>.
-- @see Anim.tweenFromTo
function Anim.createTween(obj, property, startval, endval, duration, interpolator)
    duration = duration or 60

    return PropertyInterpolator.new{
        obj=obj,
        property=property,
        duration=duration,
        startval=startval,
        endval=endval,
        interpolator=interpolator
        }
end

---Returns an Animator that does nothing but wait for the specified duration.
-- @param duration The wait duration in frames
function Anim.createWait(duration)
	return WaitAnimator.new{
		duration=duration
		}
end

---Returns an Animator wrapping the given function. The Animator (once started)
-- will call the function every frame with one argument:
-- <code>time / duration</code>.
-- @param func The function to call every frame
-- @param duration The duration of the animation in frames (holding the skip key
--        can cause the animation to advance multiple frames at once).
function Anim.fromFunction(func, duration)
	return FunctorAnimator.new{
		func=func,
		duration=duration
		}
end

---Returns a new Animator that changes an ImageDrawable's texture based on a
-- list of images and durations.
-- @param obj The ImageDrawable that the filmstrip animation should change the
--        texture of.
-- @param frames A table of tables, each containing a <code>texture</code> and
--        a <code>duration</code>. Example:
--        <code>{{"image1", 30}, {"image2", 10}}</code>
function Anim.createFilmstrip(obj, frames)
	return FilmstripAnimator.new{
		obj=obj,
		frames=frames
		}	
end

---Creates a new Animator that runs all the given Animator arguments in
-- parallel.
function Anim.par(...)
	local anims = getTableOrVarArg(...)
	return ParallelAnimator.new{anims=anims}
end

---Creates a new Animator that runs all the given Animator arguments in
-- sequence.
function Anim.seq(...)
	local anims = getTableOrVarArg(...)
	return SequentialAnimator.new{anims=anims}
end
