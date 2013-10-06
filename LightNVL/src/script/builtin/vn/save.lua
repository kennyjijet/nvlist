--- Save support.
--  
module("vn.save", package.seeall)

-- ----------------------------------------------------------------------------
--  Variables
-- ----------------------------------------------------------------------------

local autoSaveSlots = 3
if android then
	autoSaveSlots = 0 --Android can't load autosaves yet
end

-- ----------------------------------------------------------------------------
--  Functions
-- ----------------------------------------------------------------------------


local function saveLoadScreen(isSave)
	local screen = nil
	local selected = 0
	local metaData = nil

	return setMode("saveLoadScreen", function()
		local guiScreen = nil
		if isSave then
			guiScreen = GUI.createSaveScreen()
		else
			guiScreen = GUI.createLoadScreen()
		end

		if guiScreen ~= nil then
			yield() --Wait one frame to let the GUI thread create the save/load screen
			while not guiScreen:isFinished() do
				yield()
			end
		else
			--textoff(1)
			pushImageState()

			if isSave then
				screen = Screens.save.new{isSave=isSave, page=page}
			else
				screen = Screens.load.new{isSave=isSave, page=page}
			end
			selected, metaData = screen:run()
		end
		setMode(nil)
	end, function()
		if screen ~= nil then
			screen:destroy()
			screen = nil
			
			popImageState()
			--texton(1)
		end
		
		if selected ~= 0 then
			if isSave then
				local ssTable = nil
				if selected < math.min(Save.getQuickSaveSlot(1), Save.getAutoSaveSlot(1)) then
					local ss = screenshot(imageState:getDefaultLayer(), -32768)
					ss:markTransient()
					ssTable = {screenshot=ss, width=prefs.saveScreenshotWidth, height=prefs.saveScreenshotHeight}
				end
				Save.save(selected, ssTable, metaData)
			else
				Save.load(selected)
			end
		end	
	end)
end

---Shows the save screen.
function saveScreen()
	return saveLoadScreen(true)
end

---Shows the load screen.
function loadScreen()
	return saveLoadScreen(false)
end

---Performs a quick save.
-- @int[opt=1] slot The quick save slot (between 1 and 99).
function quickSave(slot)
	slot = Save.getQuickSaveSlot(slot or 1)
	if slot == nil then return end
	return Save.save(slot)
end

---Loads a quick save.
-- @int[opt=1] slot The quick save slot (between 1 and 99).
function quickLoad(slot)
	slot = Save.getQuickSaveSlot(slot or 1)
	if slot == nil then return end
	return Save.load(slot)
end

---This function is called right before saving.
-- @int slot The save slot that's being saved in.
function onSave(slot)
	if slot < math.min(Save.getQuickSaveSlot(1), Save.getAutoSaveSlot(1)) then
		setSharedGlobal("vn.save.lastSaved", slot)
	end
end

---Gets called when a regular load fails and the script must restore its
-- previous state using only the information stored in
-- <code>Save.getSavepointStorage()</code>.
function onLoad()
	local storage = Save.getSavepointStorage()	
	local filename = storage:get("filename")
	local textMode = storage:get("textMode")
	
	globals:clear()
	globals:set("", storage:get("globals"))
	
	setTextMode(textMode)
	return call(filename)
end

-- This function gets called whenever a new script is entered through a
-- <code>call</code>. It allows NVList to set some data in the savepoint storage
-- for use as a really shitty backup save when the regular save won't load for
-- whatever reason. After updating the savepoint storage, an autosave is
-- performed.
--@string scriptpos Script position in the format "filename:line".
function savepoint(scriptpos)
	scriptpos = scriptpos or getScriptPos(1)
	
	local m = scriptpos:gmatch("([^:]+)")
	local filename = m()
	local line = m()

	local storage = Save.getSavepointStorage()
	storage:set("filename", filename)
	storage:set("line", line)
	storage:set("textMode", getTextMode())
	
	--Also store a copy of the globals
	storage:set("globals", globals)
	
	return autoSave()
end

---Performs an auto save (automatically cycles through available auto save slots).
function autoSave()
	if autoSaveSlots <= 0 then
		return nil
	end

	local key = "vn.save.autoSaveIndex"
	local autoSaveIndex = getSharedGlobal(key) or 1
	--print("Autosave: ", autoSaveIndex)
	slot = Save.getAutoSaveSlot(autoSaveIndex)
	
	--Increase auto save index
	autoSaveIndex = autoSaveIndex + 1
	if autoSaveIndex > autoSaveSlots then
		autoSaveIndex = 1
	end
	setSharedGlobal(key, autoSaveIndex)
	
	if slot == nil then return end	
	return Save.save(slot)
end

---Returns the number of auto save slots.
-- @treturn int The number of auto save slots, or <code>0</code> if auto saving
-- is turned off. 
function getAutoSaveSlots()
	return autoSaveSlots
end

---Changes the number of auto save slots used.
-- @int slots The number of auto save slots to cycle through. Use <code>0</code>
--      to effectively turn off auto saving. 
function setAutoSaveSlots(slots)
	autoSaveSlots = slots
end

---Returns a free save slot.
-- @treturn int The number of an unused save slot. 
function getFreeSaveSlot()
	return Save.getFreeSaveSlot()
end
