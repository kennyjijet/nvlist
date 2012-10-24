-------------------------------------------------------------------------------
-- edt.lua - Event Dispatch Thread
-------------------------------------------------------------------------------
-- Provides utilities for scheduling operations on the main thread.
-------------------------------------------------------------------------------

require("builtin/stdlib")

local ipairs = ipairs
local table = table
local removeAll = removeAll

module("edt")

-- ----------------------------------------------------------------------------
--  Variables
-- ----------------------------------------------------------------------------

---Tasks are functions that are called each frame on the dispatch thread.
local tasks = {}

---Events are functions are called once during the next frame and then removed.
local events = {}

-- ----------------------------------------------------------------------------
--  Functions
-- ----------------------------------------------------------------------------

---Adds a function to the active task list. Tasks get called once each frame.
-- @param task The task function to add
function addTask(task)
	table.insert(tasks, task)
end

---Removes all instances of task from the tasks list.
-- @param task The task function to remove
function removeTasks(task)
	tasks = removeAll(tasks, task)
end

---Adds a one-time event to the event queue
-- @param event The event function to enqueue
function addEvent(event)
	if events == nil then
		events = {event}
	else
		table.insert(events, event)
	end
end

---Removes all copies of event from the event queue
-- @param event The event function to remove
function removeEvents(event)
	if events ~= nil then
		events = removeAll(events, event)
	end
end

---Returns the thread events must be called on
function getEventThread(mainThread)
	return mainThread
end

---Should be called by the system once every frame. Runs tasks and events on
-- the main thread.
function update()
	--Run tasks
	for _,task in ipairs(tasks) do
		task()
	end
	
	--Return events to be scheduled
	local result = nil
	if events ~= nil and #events > 0 then
		result = events
		events = nil
	end
	return result
end

