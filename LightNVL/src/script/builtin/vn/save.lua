-------------------------------------------------------------------------------
-- savescreen.lua
-------------------------------------------------------------------------------
-- Provides the 'built-in' VN save functions.
-------------------------------------------------------------------------------

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

---This function creates a snapshot of the most critical save data and stores
-- this data in <code>Save.getSavepointStorage()</code>. This function is
-- automatically called before each new .lvn script.
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

---Performs an auto save (automatically cycles through available auto save slots)
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

---Returns the number of auto save slots
-- @return The number of auto save slots, or <code>0</code> if auto saving is
-- turned off. 
function getAutoSaveSlots(slots)
	return autoSaveSlots
end

---Sets the number of auto save slots, use <code>0</code> to turn off autosaving
-- @param slots The number of auto save slots to cycle through 
function setAutoSaveSlots(slots)
	autoSaveSlots = slots
end

---Returns an empty (regular) save slot
function getFreeSaveSlot()
	return Save.getFreeSaveSlot()
end

---Performs a save in the specified quick save slot.
-- @param slot The quick save slot (between 1 and 99).
function quickSave(slot)
	slot = Save.getQuickSaveSlot(slot or 1)
	if slot == nil then return end
	return Save.save(slot)
end

---Loads a quick save.
-- @param slot The quick save slot (between 1 and 99).
function quickLoad(slot)
	slot = Save.getQuickSaveSlot(slot or 1)
	if slot == nil then return end
	return Save.load(slot)
end

---Gets called right before saving
function onSave(slot)
	setSharedGlobal("vn.save.lastSaved", slot)
end

---Gets called when a regular load failed and the script must restore its
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

-- ----------------------------------------------------------------------------
--  Save screen
-- ----------------------------------------------------------------------------

local function saveLoadScreen(isSave)
	local screen = nil
	local selected = 0
	local metaData = nil

	return setMode("saveLoadScreen", function()
		local guiScreen = nil
		if isSave then
			guiScreen = System.createSaveScreen()
		else
			guiScreen = System.createLoadScreen()
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
				local ss = nil
				if selected < math.min(Save.getQuickSaveSlot(1), Save.getAutoSaveSlot(1)) then
					ss = screenshot(getImageLayer())
					ss:makeTransient()
				end
				Save.save(selected, ss, metaData)
			else
				Save.load(selected)
			end
		end	
	end)
end

---Starts the save screen in save mode
function saveScreen()
	return saveLoadScreen(true)
end

---Starts the save screen in load mode
function loadScreen()
	return saveLoadScreen(false)
end
