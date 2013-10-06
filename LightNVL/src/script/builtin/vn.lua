--- Provides core functions for scripting visual novels.
--  @module vn

require("builtin/stdlib")
require("builtin/edt")

autoRead = false
autoReadWait = -1

quickRead = false

Screens = {}

-- ----------------------------------------------------------------------------
--  Local variables
-- ----------------------------------------------------------------------------

local waitClickTime = 0
local autoReadTime = 0
local hardWaitClick = false

--- A table containing the different kinds of skip modes.
SkipMode = {
	STOP=0,      --Stops skipping.
	PARAGRAPH=1, --Skips until the end of the current paragraph (displays fading-in text instantly).	
	SCENE=2,     --Skips until the end of the end of the current script file or until a choice appears.
}
local skipMode = 0

local threadGroups = {
    main = Thread.newGroup()
}

local mode = "main"
local modeExitCallback = nil

-- ----------------------------------------------------------------------------
--  Functions
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

---Executes the script with the given filename. When the called script
-- completes, resumes executing the current script. Use
-- <code>jump("some-script.lvn")</code> when you don't want/need to
-- come back to the current script.
-- @string filename Path to the script, relative to the <code>res/script</code>
--         folder.
function call(filename)
	savepoint(filename)
	return _dofile(filename)
end


---Jumps execution to the specified script file. If you want to resume from the
-- current position after the new script ends, use <code>call</code> instead.
-- @string filename Path to the script, relative to the <code>res/script</code>
--         folder.
function jump(filename)
	savepoint(filename)
	return Thread.jump(filename)
end

---Creates a new Lua thread.
-- @func func The function to run in the new thread. When the function finishes
--       executing, the thread is destroyed.
-- @param ... Any number of parameters to pass to <code>func</code>.
-- @treturn Thread A new thread object.
function newThread(func, ...)
    return threadGroups[mode]:newThread(func, ...)
end

function deprecated(deprecatedSince)
	local targetVersion = prefs.engineTargetVersion
	if deprecatedSince ~= nil and targetVersion ~= nil
			and System.compareVersion(deprecatedSince, targetVersion) <= 0
	then
		local info = debug.getinfo(3, 'n')
		notifier:d("Warning: Deprecated function used (" .. info.name .. ")")
	end
end

---Asks the user to select an option.
-- @string ... Any number of strings to use as options. Example use:
--         <code>choice("First option", "Second option")</code>
-- @treturn number The index of the selected option (starting at <code>1</code>).
function choice(...)
	return choice2(getScriptPos(1), ...)
end

function choice2(uniqueChoiceId, ...)
	local options = getTableOrVarArg(...)
	if options == nil or #options == 0 then
		options = {"Genuflect"}
	end
    
	local selected = -1
	while selected < 0 do
		local thread = nil
		
		local c = GUI.createChoice(options)
		if c == nil then
			c = Screens.choice.new(uniqueChoiceId, options)
			
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

    selected = selected + 1
    
	seenLog:setChoiceSelected(uniqueChoiceId, selected)	
	return selected
end

---Waits for the specified time to pass. Time progression is influenced by the
-- current <code>effectSpeed</code> and the wait may be cancelled by holding the
-- skip key or pressing the text continue key.
-- @number durationFrames The wait time in frames (default is 60 frames per second).
-- @bool[opt=false] ignoreEffectSpeed If <code>true</code>, ignores
--                  the <code>effectSpeed</code>.
function wait(durationFrames, ignoreEffectSpeed)
	while durationFrames > 0 do
		if quickRead or input:consumeTextContinue() then
			break
		end
		if ignoreEffectSpeed then
			durationFrames = durationFrames - 1
		else
			durationFrames = durationFrames - effectSpeed
		end
		yield()
	end    
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

function isWaitClick()
	return waitClickTime ~= 0
end

function getWaitClickTime()
	return waitClickTime, hardWaitClick
end

---Waits until the text continue key is pressed. Skipping ignores the wait
-- unless <code>unskippable</code> is set to <code>true</code>.
-- @bool[opt=false] unskippable If <code>true</code>, the skip mode is reset
--                  to <code>SkipMode.STOP</code>.
function waitClick(unskippable)
    setWaitClick(true, unskippable)
    yield()
end

---Waits indefinitely.
function waitForever()
	while true do
		yield(60) --A high yield value will prevent the thread from waking up.
	end
end

---Turns skip mode on for the remainder of the paragraph.
-- @see setSkipMode
function skipParagraph()
	return setSkipMode(math.max(skipMode, SkipMode.PARAGRAPH))
end

---Turns skip mode on for the remainder of the scene (the end of the file, or
-- when a choice appears).
-- @see setSkipMode
function skipScene()
	return setSkipMode(math.max(skipMode, SkipMode.SCENE))
end

---Changes the current skip mode, starts skipping when <code>mode ~= 0</code>.
-- @tparam SkipMode mode The kind of skip mode to change to.
-- @see skipParagraph
-- @see skipScene 
function setSkipMode(mode)	
	skipMode = mode or 0
	if skipMode == 0 then
		quickRead = false
	end
end

---Returns the current skip mode.
-- @treturn SkipMode The current skip mode, <code>SkipMode.STOP</code> when not
--                   skipping.
function getSkipMode()
	return skipMode
end

---Toggles whether of not to skip past unread text lines
-- @deprecated 4.0
function setSkipUnread(u)
	deprecated("4.0")

	prefs.skipUnread = not not u
end

---Returns <code>true</code> if skipping should stop at unread text
function getSkipUnread()
	if input:isQuickReadAlt() then
		return not prefs.skipUnread
	end
	return prefs.skipUnread 
end

---Globals
-------------------------------------------------------------------------------- @section globals

---Sets the value of a global variable in the <code>globals</code> object.
-- Data stored in this way is better protected against data loss.
-- @string name The name of the variable. Names starting with <code>vn.</code>
--         are reserved for use by NVList.
-- @param value The new value to store for <code>name</code>.
function setGlobal(name, value)
	globals:set(name, value)
end

---Returns a value previously stored using <code>setGlobal</code>.
-- @string name The name of the variable.
-- @return The stored value, or <code>nil</code> if none exists.
-- @see setGlobal
function getGlobal(name)
	return globals:get(name)
end

---Increases/decreases the value of the stored global by the specified amount.
-- @string name The name of the variable.
-- @number inc The value to add to the global. This value may be negative.
-- @return The new value of the variable.
-- @see setGlobal
function incrGlobal(name, inc)
	local val = getGlobal(name)
	val = (val or 0) + (inc or 0)
	setGlobal(name, val)
	return val
end

---Clears all variables previously stored using <code>setGlobal</code>.
-- @see setGlobal
function clearGlobals()
	return globals:clear()
end

---Sets a shared global variable. Similar to <code>setGlobal</code>, except all
-- save slots have access to the same set of <em>shared</em> globals. Shared
-- globals are often used to mark routes as cleared or unlocked.
-- @string name The name of the shared global. Names starting with
--        <code>vn.</code> are reserved for use by NVList.
-- @param value The new value to store for <code>name</code>.
-- @see setGlobal
function setSharedGlobal(name, value)
	sharedGlobals:set(name, value)
end

---Returns a value previously stored using <code>setSharedGlobal</code>.
-- @param name The name of the shared global.
-- @return The stored value, or <code>nil</code> if none exists.
-- @see setSharedGlobal
function getSharedGlobal(name)
	return sharedGlobals:get(name)
end

-------------------------------------------------------------------------------- @section end

---Changes the current mode. Modes are mainly used to more conveniently
-- implement sub-screens like the text log or save screen. Entering a new mode
-- causes threads created in the main mode (<code>"main"</code>) to pause.
-- Threads created in modes other than <code>"main"</code> are destroyed when
-- that mode ends. If you create any images or user interface components, make
-- sure to clean them up in <code>onModeExit</code> -- the mode may revert to
-- <code>"main"</code> at any time.
-- @string m The name of the mode to enter, use <code>"main"</code> or
--           <code>nil</code> to return to the default mode. You can use any
--           name you want to enter your own custom modes.
-- @func[opt=nil] onModeEnter An optional function to call immediately after
--                entering the new mode. Mostly used for creating the user
--                interface components for the new mode.
-- @func[opt=nil] onModeExit An optional function to call when the new mode
--                exits. Useful for cleaning up the changes made during
--                <code>onModeEnter</code>.
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

---Returns the current mode.
-- @treturn string The current mode, <code>"main"</code> when in the default
--                 mode.
-- @see setMode
function getMode()
	return mode
end

---Calls the default setter function corresponding to a property called
-- <code>name</code>.
-- @param obj The object to set the property value on.
-- @param name The name of the property to change.
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

---Calls the <code>setProperty</code> function for each key/value pair in
-- <code>props</code>.
-- @param obj The object to set the property value on.
-- @param props The table of properties to set.
function setProperties(obj, props)
	if props ~= nil then
		for k,v in pairs(props) do
			setProperty(obj, k, v)
		end
	end
end

---Calls the default getter function corresponging to a property called
-- <code>name</code>.
-- @param obj The object to get the property value of.
-- @param name The name of the property to get the value of.
-- @return The value of the property (result of calling the property getter
--         function).
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
	
	local val = getter(obj)
	if type(val) == "userdata" then
		local clazz = val:getClass():getSimpleName()
		if clazz == "Rect2D" or clazz == "Rect" then
			return {val.x, val.y, val.w, val.h}
		elseif clazz == "Dim2D" or clazz == "Dim" then
			return {val.w, val.h}
		elseif clazz == "Insets2D" or clazz == "Insets" then
			return {val.top, val.right, val.bottom, val.left}
		end
	end
	return val
end

---This function is executed whenever a property in <code>prefs</code> changes.
function onPrefsChanged()
end

-- ----------------------------------------------------------------------------
--  Task
-- ----------------------------------------------------------------------------

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
	
	--Update the value of quickRead
	quickRead = input:isQuickRead() or skipMode ~= 0

	--Let users disable an ongoing skipMode	
	if skipMode ~= 0 and not input:isQuickRead() and input:consumeTextContinue() then
		setSkipMode(0)
	end

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
			else
				if not suspended then
					if input:consumeTextLog() then
						return edt.addEvent(textLog)
					elseif input:consumeViewCG() then
						return edt.addEvent(viewCG)
					end
				end			
			end
		end
		
		--Waiting for a click
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

-- ----------------------------------------------------------------------------
--  Include submodules
-- ----------------------------------------------------------------------------

module("vn", package.seeall)

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
	"video",
	"nvlist3bc"
}
for _,module in ipairs(submodules) do
	require("builtin/vn/" .. module)
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

-- Internal function
-- Flattens this module and its submodules into <code>env</code>
-- @param env The table (often <code>_G</code>) to flatten the module into.
function flattenModule(env)	
	flattenSingle(env, package.loaded.vn)
	for _,module in ipairs(submodules) do
		flattenSingle(env, package.loaded.vn[module])		
	end
	_G.vn = nil --Delete the now flattened table
end
