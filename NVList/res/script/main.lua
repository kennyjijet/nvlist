--Import built-in NVList functions
require("builtin/stdlib")
require("builtin/vn")
vn.flattenModule(_G)

--Import custom GUI screens (save/load, textlog, choice, ...)
require("screens")

--Import titlescreen.lua
require("titlescreen")

--The main() function is called once at startup.
function main()
	return titlescreen()
end

--The titlescreen() function is called on every restart.
function titlescreen()
    globals:clear()
	setTextModeADV()
                
    --Script to execute when pressing the "start" button
    local startScript = "tutorial.lvn"
    
    --Script to execute when pressing the "extra" button
    local extraScript = nil
    
    --This shows the default titlescreen (the function is defined in titlescreen.lua)
    --return defaultTitlescreen(startScript, extraScript)
    
    
    TODO: How to encapsulate scripting in the GUI components?
          I could maybe use the update property to let users set a LuaFunction similar to
          the click handler. A Lua function that's called on every frame.
    
    local p = GUI.vbox{
        GUI.image{
            filename="bg/a",
            width=100,
            height=100,
        },
        GUI.image{
            filename="bg/b",
            width=100,
            height=100,
        },
    }
    
    waitClick()
    
    p:destroy()
end
