--- User interface related functionality.
module("vn.gui", package.seeall)

-- ----------------------------------------------------------------------------
--  Classes
-- ----------------------------------------------------------------------------

-- ----------------------------------------------------------------------------
--  Functions
-- ----------------------------------------------------------------------------

GUI.definitions={
}    

setmetatable(GUI, {
    __index = function(table, key)
        return table.definitions[key]
    end
})

---Registers a component with name <code>type</code> and builder function
-- <code>func</code>.
function GUI.register(type, func)
    GUI.definitions[type] = func
end

function GUI.initComponent(component, props)
    if props ~= nil then
        for k,v in pairs(props) do
            if type(k) ~= "number" then
                local setterName = "set" .. string.upper(string.sub(k, 1, 1)) .. string.sub(k, 2)
                local setter = component[setterName]
                if setter ~= nil then
                    if type(v) == "table" then
                        setter(component, unpack(v))
                    else
                        setter(component, v)
                    end
                end
            end
        end
    end
end

function GUI.initContainer(container, props)
    GUI.initComponent(props)
    if props ~= nil then
        for _,c in ipairs(props) do
            container:add(c)
        end
    end
end

GUI.register("panel", function(props)
    local c = createPanel(props.width, props.height)
    GUI.initContainer(c, props)
    return c
end)

GUI.register("vbox", function(props)
    return GUI.definitions.panel(extend(props, {
        layout=Layout.vertical{stretch=false},
    }))
end)

GUI.register("hbox", function(props)
    return GUI.definitions.panel(extend(props, {
        layout=Layout.horizontal{stretch=false},
    }))
end)

GUI.register("vfill", function(props)
    return GUI.definitions.panel(extend(props, {
        layout=Layout.vertical{stretch=true},
    }))
end)

GUI.register("hfill", function(props)
    return GUI.definitions.panel(extend(props, {
        layout=Layout.horizontal{stretch=true},
    }))
end)

GUI.register("grid", function(props)
    return GUI.definitions.panel(extend(props, {
        layout=Layout.grid{cols=props.cols},
    }))
end)

GUI.register("viewport", function(props)
    local c = createViewport(props.width, props.height)
    GUI.initContainer(c, props)
    return c
end)

GUI.register("button", function(props)
    local c = button(props.image)
    GUI.initComponent(c, props)
    return c
end)

GUI.register("image", function(props)
    local c = img(props.filename or props.texture)
    GUI.initComponent(c, props)
    return c
end)

GUI.register("text", function(props)
    local c = textimg(props.text)
    GUI.initComponent(c, props)
    return c
end)

-- ----------------------------------------------------------------------------

Layout = {}

function Layout.vertical(props)
	if props.stretch then
		return createGridLayout(extend(props, {
			cols=1,
		}))
	else
		return createFlowLayout(extend(props, {
			cols=1,
		}))
	end
end

function Layout.horizontal(props)
	if props.stretch then
		return createGridLayout(extend(props, {
			cols=-1,
		}))
	else
		return createFlowLayout(extend(props, {
			cols=-1,
		}))
	end
end

function Layout.grid(props)
	return createGridLayout(props)
end

-- ----------------------------------------------------------------------------

