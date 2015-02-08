--- Provides core functions for scripting visual novels.
--  @module vn

require("builtin/stdlib")

-- ----------------------------------------------------------------------------
--  Functions
-- ----------------------------------------------------------------------------

-- Prints a warning when called from a script targetting an engine version more recent than the time of
-- deprecation.
function deprecated(deprecatedSince)
    local targetVersion = prefs.engineTargetVersion
    if deprecatedSince ~= nil and targetVersion ~= nil
            and System.compareVersion(deprecatedSince, targetVersion) <= 0
    then
        local info = debug.getinfo(3, 'n')
        notifier:d("Warning: Deprecated function used (" .. info.name .. ")")
    end
end

-- Forward NVList 2.x style thread creation to the new interface
Thread.new = function(...)
    notifier:w("Don't use Thread.new(), use newThread() instead")
    return newThread(...)
end

local _dofile = dofile
dofile = function(...)
    notifier:w("Don't use dofile(), use call instead")
    return call(...)
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
