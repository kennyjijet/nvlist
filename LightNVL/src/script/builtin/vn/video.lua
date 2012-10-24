-------------------------------------------------------------------------------
-- movie.lua
-------------------------------------------------------------------------------
-- Provides the 'built-in' VN video functions.
-------------------------------------------------------------------------------

module("vn.video", package.seeall)

-- ----------------------------------------------------------------------------
--  Variables
-- ----------------------------------------------------------------------------

-- ----------------------------------------------------------------------------
--  Functions
-- ----------------------------------------------------------------------------

---Starts playing a cutscene, pauses main thread while it plays
-- @param filename Path to a valid video file. Supported video formats are
--        platform dependent.
function movie(filename)
	Video.movie(filename)
	yield()
end
