-------------------------------------------------------------------------------
-- vn.lua - Visual novel base functions
-------------------------------------------------------------------------------
-- Provides the 'built-in' VN functions. The Java side only exposes the
-- absolute minimal API, as much as possible is implemented in Lua for maximum
-- scriptability.
-------------------------------------------------------------------------------

require("builtin/stdlib")
require("builtin/edt")

autoRead = false
autoReadWait = -1

quickRead = false
skipUnread = true

Screens = {}

-- ----------------------------------------------------------------------------
--  Local variables
-- ----------------------------------------------------------------------------

local waitClickTime = 0
local autoReadTime = 0
local hardWaitClick = false

SkipMode = {PARAGRAPH=1, SCENE=2}
local skipMode = 0

---The current mode
local threadGroups = {
    main = Thread.newGroup()
}

local mode = "main"
local modeExitCallback = nil

-- ----------------------------------------------------------------------------
--  Mode
-- ----------------------------------------------------------------------------

---Returns the current mode
-- @see setMode
function getMode()
	return mode
end
    
---Changes the current mode
-- @param m The new mode
-- @param onModeEnter An optional function argument that will be called when
--        the new mode starts.
-- @param onModeExit An optional function argument that will be called when the
--        new mode ends.
function setMode(m, onModeEnter, onModeExit)
    m = m or "main"
    if mode == m then
        return
    end

    --Call mode exit callback
    if modeExitCallback ~= nil then
        modeExitCallback()
    end
    
   	--End old threads
    local oldMode = mode
    local oldThreadGroup = threadGroups[oldMode]
    
    --Change mode
    mode = m
    
    --Start/resume new threads
    local tg = threadGroups[mode]
    if tg == nil or tg:isDestroyed() then
        tg = Thread.newGroup()
        threadGroups[mode] = tg
    end
    tg:resume()

    --Store exit callback and call enter callback
    modeExitCallback = onModeExit
    if onModeEnter == nil then
    	modeThread = nil
    else
        modeThread = newThread(onModeEnter)
    end

	--End old thread group
    if oldThreadGroup ~= nil and not oldThreadGroup:isDestroyed() then
        --print("Suspend", oldMode)
        if oldMode == "main" then
            oldThreadGroup:suspend()
        else
            oldThreadGroup:destroy()
        end
    end
    Thread.endCall()
end

---Returns the current skip mode
function getSkipMode()
	return skipMode
end

---Changes the current SkipMode to <code>s</code>
-- @param s The new SkipMode, or <code>0</code> to stop skipping.
function setSkipMode(s)	
	skipMode = s or 0
	if skipMode == 0 then
		quickRead = false
	end
end

---Returns <code>true</code> if skipping should stop at unread text
function getSkipUnread()
	if input:isQuickReadAlt() then
		return not skipUnread
	end
	return skipUnread 
end

---Toggles whether of not to skip past unread text lines
function setSkipUnread(u)
	if u then
		skipUnread = true
	else
		skipUnread = false
	end
end

---Turns skip mode on for the remainder of the paragraph
function skipParagraph()
	return setSkipMode(math.max(skipMode, SkipMode.PARAGRAPH))
end

---Turns skip mode on for the remainder of the scene (the end of the file, or
-- when a choice appears)
function skipScene()
	return setSkipMode(math.max(skipMode, SkipMode.SCENE))
end

-- ----------------------------------------------------------------------------
--  Tasks and global function overrides
-- ----------------------------------------------------------------------------

Thread.new = function(...)
	notifier:w("Don't use Thread.new(), use newThread() instead")
	return newThread(...)
end

_dofile = dofile
dofile = function(...)
	notifier:w("Don't use dofile(), use call instead")
	return _dofile(...)
end

local function setMinimumWait(w)
	if modeThread ~= nil then
		modeThread:setMinimumWait(w)
	end
	_mainThread:setMinimumWait(w)
end

local function vnTask()
	local suspended = threadGroups.main:isSuspended()
	if videoState:isBlocking() then
		return setMinimumWait(1)
	elseif suspended then
		_mainThread:setMinimumWait(1) --Block main thread together with main group even though it's not in it.
	end
	
	--Regular (text) mode is active
	quickRead = input:isQuickRead() or skipMode ~= 0

	--Check for hotkeys
	if input:consumeSaveScreen() then
		return edt.addEvent(saveScreen)
	elseif input:consumeLoadScreen() then
		return edt.addEvent(loadScreen)
	end
	
	--Handle textContinue desire
	local textBox = textState:getTextDrawable()
	local charsVisible = true
	local linesVisible = true
	if textBox ~= nil then
		charsVisible = textBox:getCurrentLinesFullyVisible()
		linesVisible = textBox:getFinalLineFullyVisible()
	end
	
	--print(charsVisible, linesVisible, waitClickTime, autoReadTime)
	
	if not charsVisible then
		autoReadTime = 0
		if quickRead or input:consumeTextContinue() then
			textBox:setVisibleChars(999999)
			skipParagraph()
		end
	else
		local autoContinue = false
		if autoRead then
			autoReadTime = autoReadTime + 1
			if autoReadTime >= autoReadWait then
				autoReadTime = 0
				autoContinue = true
			end
		end
			
		if not linesVisible then
			--Disable to avoid skipping past never-displayed lines
			if skipMode == SkipMode.PARAGRAPH then
				setSkipMode(0)
			end
		
			if quickRead or autoContinue or input:consumeTextContinue() then
				local startLine = textBox:getStartLine()
				local newStart = math.max(startLine+1, math.min(textBox:getLineCount(), textBox:getEndLine()))			
				textBox:setStartLine(newStart)
				autoReadTime = 0

				if scriptDebug and not android and not isVNDS then
					notifier:d("Page break automatically inserted")
				end
			end
		end
	
		if waitClickTime ~= 0 then
			if not suspended then
				if input:consumeTextLog() then
					return edt.addEvent(textLog)
				elseif input:consumeViewCG() then
					return edt.addEvent(viewCG)
				end
			end
			
			if quickRead and not hardWaitClick and (getSkipUnread() or isLineRead()) then
				setWaitClick(false)
			elseif autoContinue or input:consumeTextContinue() then
				setWaitClick(false)
			end
		end
	end
		
	if waitClickTime ~= 0 then
		if waitClickTime > 0 then
			waitClickTime = waitClickTime - 1
		end
		return setMinimumWait(1)
	end
end

edt.addTask(vnTask)

--Override function from edt
function edt.prePushEvents(mainThread)
	local w = mainThread:getWait()
	local wcTime, hardWc = getWaitClickTime()

	--Prevents events from running while inside a submode
	--if videoState:isBlocking() or threadGroups.main:isSuspended() then
	--	mainThread:setWait(1)
	--else
		mainThread:setWait(0)
	--end
	setWaitClick(false)

	--print("pre")

	return w, wcTime, hardWc
end

--Override function from edt
function edt.postPushEvents(mainThread, w, wcTime, hardWc)
	waitClickTime = wcTime
	hardWaitClick = hardWc
	mainThread:setWait(w)

	--print("post")
	
	Thread.endCall() --Immediately returns from the function and yields without leaving a stack frame
end


















module("vn", package.seeall)

-- ----------------------------------------------------------------------------
--  Include submodules
-- ----------------------------------------------------------------------------

--Require submodules
local submodules = {
	"anim",
	"gui",
	"image",
	"imagefx",
	"save",
	"screens",
	"sound",
	"system",
	"text",
	"tween",
	"video"
}
for _,module in ipairs(submodules) do
	require("builtin/vn/" .. module)
end

-- ----------------------------------------------------------------------------
--  Functions
-- ----------------------------------------------------------------------------

function deprecated(engineVersion)
	local targetVersion = prefs.engineTargetVersion
	if engineVersion == nil or targetVersion == nil
			or System.compareVersion(engineVersion, targetVersion) <= 0
	then
		local info = debug.getinfo(3, 'n')
		notifier:d("Warning: Deprecated function used (" .. info.name .. ")")
	end
end

---Pauses the current thread for the specified time. The pause time is
-- influenced by the <code>effectSpeed</code>.
-- @param duration The time to wait in frames
function wait(duration)
	while duration > 0 do
		if quickRead or input:consumeTextContinue() then
			break
		end
		duration = duration - effectSpeed
		yield()
	end    
end

function isWaitClick()
	return waitClickTime ~= 0
end

function getWaitClickTime()
	return waitClickTime, hardWaitClick
end

function setWaitClick(w, hard)
	if w then
		waitClickTime = -1
	else
		waitClickTime = 0
	end
	autoReadTime = 0
	
	if hard then
		hardWaitClick = true
		setSkipMode(0)
	else
		hardWaitClick = false
	end
end

---Pauses the main thread and yields
function waitClick(unskippable)
    setWaitClick(true, unskippable)
    yield()
end

---Deprecated, use <code>setSharedGlobal</code> instead.
function setSystemVar(name, value)
	deprecated("2.7")
	setSharedGlobal(name, value)
end

---Deprecated, use <code>getSharedGlobal</code> instead.
function getSystemVar(name)
	deprecated("2.7")
	return getSharedGlobal(name)
end

---Sets a shared global. All save slots have access to the same set of shared
-- globals. Commonly used to mark routes as cleared or unlocked.
-- @param name The name of the shared global. Names starting with
--        <code>vn.</code> are reserved for use by NVList.
-- @param value The new value for the shared global
function setSharedGlobal(name, value)
	sharedGlobals:set(name, value)
end

---Returns the value of the specified shared global.
-- @param name The name of the shared global
-- @see setSharedGlobal
function getSharedGlobal(name)
	return sharedGlobals:get(name)
end

---Starts executing the specified script
function call(filename)
	savepoint(filename)
	return _dofile(filename)
end

---Creates a new Lua thread
-- @param func The function to run in the new thread
-- @param ... The parameters to pass to the function
-- @return A new thread object
function newThread(func, ...)
    return threadGroups[mode]:newThread(func, ...)
end

---Calls the default setter function corresponding to a property called
-- <code>name</code>.
-- @param obj The object to set the property value on
-- @param name The name of the property to change
-- @param ... The parameters to pass to the setter function (the new value for
--        the property).
function setProperty(obj, name, ...)
	local vals = getTableOrVarArg(...)
	
	local setterName = "set" .. string.upper(string.sub(name, 1, 1)) .. string.sub(name, 2)
	local setter = obj[setterName]
	if setter == nil then
		error("Invalid property: " .. setterName)
	end
	return setter(obj, unpack(vals))
end

---Calls the default getter function corresponging to a property called
-- <code>name</code>.
-- @param obj The object to get the property value of
-- @param name The name of the property to get the value of
-- @return The value of the property (result of calling the property getter
--         function)
function getProperty(obj, name)
	local getterName = "get" .. string.upper(string.sub(name, 1, 1)) .. string.sub(name, 2)
	
	local getter = obj[getterName]
	if getter == nil then
		--Hardcoded getters for composite properties
		if name == "backgroundColor" then
			return {obj:getBackgroundRed(), obj:getBackgroundGreen(), obj:getBackgroundBlue(), obj:getBackgroundAlpha()}
		elseif name == "bounds" then
			return {obj:getX(), obj:getY(), obj:getWidth(), obj:getHeight()}
		elseif name == "color" then
			return {obj:getRed(), obj:getGreen(), obj:getBlue(), obj:getAlpha()}
		elseif name == "pos" then
			return {obj:getX(), obj:getY()}
		elseif name == "size" then
			return {obj:getWidth(), obj:getHeight()}
		elseif name == "scale" then
			return {obj:getScaleX(), obj:getScaleY()}
		elseif name == "zoom2D" then
			local rect = obj:getZoom()
			return {rect.x, rect.y, rect.w, rect.h}
		elseif name == "zoom3D" then
			local rect = obj:getZoom()
			return {obj:getZ(), rect.x, rect.y}
		end
		
		error("Invalid property: " .. getterName)
	end
	return getter(obj)
end

---Asks the user to select an option
-- @param ... A vararg with all selectable options
-- @return The index of the selected option, starting at <code>1</code>
function choice(...)
	return choice2(getScriptPos(1), ...)
end

function choice2(uniqueChoiceId, ...)
	local selected = -1
	while selected < 0 do
		local thread = nil
		
		local c = GUI.createChoice(...)
		if c == nil then
			c = Screens.choice.new(uniqueChoiceId, ...)
			
			if thread ~= nil then thread:destroy() end
			thread = newThread(c.run, c) --Start background thread to execute ChoiceScreen.run()			
		end
		
		while not c:isCancelled() and c:getSelected() < 0 do
			yield(2)
		end
		selected = c:getSelected()
		
		if thread ~= nil then
			thread:destroy()
			thread = nil
		end
		
		if not c:isCancelled() then
			c:cancel()
		end
		c:destroy()
		
		if selected < 0 then
			waitClick()
		end
	end

	seenLog:setChoiceSelected(uniqueChoiceId, selected + 1)
	
	return selected + 1
end

-- ----------------------------------------------------------------------------
--  Flatten functions
-- ----------------------------------------------------------------------------

local function flattenSingle(env, pkg)
	if pkg == nil then
		return
	end

	for k,v in pairs(pkg) do
		if k[0] ~= '_' then
			env[k] = v
		end
	end
end

---Flattens this module and its submodules into <code>env</code>
-- @param env The table (often <code>_G</code>) to flatten the module into.
function flattenModule(env)	
	flattenSingle(env, package.loaded.vn)
	for _,module in ipairs(submodules) do
		flattenSingle(env, package.loaded.vn[module])		
	end
	_G.vn = nil --Delete the now flattened table
end
