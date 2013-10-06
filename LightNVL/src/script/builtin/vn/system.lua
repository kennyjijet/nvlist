--- Contains functions related to the operating system and external environment.
-- 
module("vn.system", package.seeall)

-- ----------------------------------------------------------------------------
--  Variables
-- ----------------------------------------------------------------------------

local exitFunctions = {func=nil, cleanup=nil}

-- ----------------------------------------------------------------------------
--  Functions
-- ----------------------------------------------------------------------------

---Completely resets all state and restarts from the titlescreen.
function restart()
	return System.restart()
end

---Checks if NVList is running in an environment where it makes sense to close
-- itself. When running embedded in a webpage for example, exiting doesn't make
-- much sense.
function canExit()
	return System.canExit()
end

---Asks (or forces) the engine to exit.
-- @bool[opt=false] force If <code>true</code>, forces an exit. Otherwise, the
--      user will be presented with a confirmation popup.
function exit(force)
	if not force then
		return System.softExit()
	end
	return System.exit(true)
end

---Sets a custom function to be called when the user tries to exit the program
-- or when the <code>exit</code> function is called. This starts a new mode,
-- &quot;exit&quot; in which the supplied exit function is called.
-- @func func The function to call when the &quot;exit&quot; submode is entered.
--       If this function returns <code>false</code>, the exit process is
--       cancelled.
-- @func[opt=nil] cleanup An optional function which is called when the exit
--      mode is cancelled (this may happen while <code>func</code> is still
--      running).
function setExitFunction(func, cleanup)
	exitFunctions.func = func
	exitFunctions.cleanup = cleanup
end

-- This function is called when the user presses the window
-- close button or exit option from the window menu.
function onExit()
	local ef = exitFunctions or {}
	local func = ef.func
	local cleanup = ef.cleanup
	if func == nil and cleanup == nil then
		return System.exit(false)
	end

	return setMode("exit", function()
		local ss = screenshot(getRootLayer(), -32768, false, true)
		pushImageState()
		bg(ss)
		if func ~= nil then
			if func() ~= false then
				System.exit(true)
			end
		else
			System.exit(false)
		end
		setMode(nil)
	end, function()
		if cleanup ~= nil then
			cleanup()
		end
		popImageState()
	end)
end

---Returns a textual representation of the total play time.
function getPlayTime()
	return timer:formatTime(timer:getTotalTime())
end

---Opens the website with the given <code>url</code> in an external web browser. 
-- @string url The URL of the website.
function website(url)
	return System.openWebsite(url)
end

---Create a Lua wrapper for the specified Java class (the class file must be
-- available from the current classpath). The registered class should be
-- Serializable or Externalizable and tagged with the
-- <code>nl.weeaboo.lua2.io.LuaSerializable</code> annotation.
-- @string className Fully-qualified Java class name.
-- @tab[opt=_G] env Lua table to register the functions in. If not specified,
--              registers functions globally.
function registerJavaClass(className, env)
	env = env or _G
	return System.registerJavaClass(env, className)
end


