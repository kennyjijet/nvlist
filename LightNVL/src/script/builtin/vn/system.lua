-------------------------------------------------------------------------------
-- system.lua
-------------------------------------------------------------------------------
-- Contains functions related to the operating system and external environment
-------------------------------------------------------------------------------

module("vn.system", package.seeall)

-- ----------------------------------------------------------------------------
--  Classes
-- ----------------------------------------------------------------------------

-- ----------------------------------------------------------------------------
--  Functions
-- ----------------------------------------------------------------------------

---Returns a string representation of the total playtime
function getPlayTime()
	return timer:formatTime(timer:getTotalTime())
end

---Opens the website specified by <code>url</code> in an external web browser. 
-- @param url The URL of the website
function website(url)
	return System.openWebsite(url)
end

---Completely restarts the visual novel from the title screen.
function restart()
	return System.restart()
end

---Returns <code>true</code> if the engine runs in an environment where it can
-- close itself. When running embedded in a webpage for example, exiting
-- doesn't make much sense.
function canExit()
	return System.canExit()
end

---Asks (or forces) the engine to exit. The behavior of this function is
-- undefined if <code>canExit</code> returns <code>false</code>.
-- @param force If <code>true</code>, forces an exit. Otherwise, the user will
-- be presented with a confirmation popup.
function exit(force)
	return System.exit(force)
end

---Create a Lua wrapper for the specified Java class (the class file must of
-- course be available from the current classpath). The registered class should
-- be Serializable or Externalizable and tagged with the
-- <code>nl.weeaboo.lua2.io.LuaSerializable</code> annotation.
-- @param className Fully-qualified Java class name
-- @param env Lua table to register the functions in (default=_G).
function registerJavaClass(className, env)
	env = env or _G
	return System.registerJavaClass(env, className)
end


