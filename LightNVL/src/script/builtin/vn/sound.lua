-------------------------------------------------------------------------------
-- sound.lua
-------------------------------------------------------------------------------
-- Provides the 'built-in' VN sound functions.
-------------------------------------------------------------------------------

module("vn.sound", package.seeall)

-- ----------------------------------------------------------------------------
--  Variables
-- ----------------------------------------------------------------------------

-- ----------------------------------------------------------------------------
--  Functions
-- ----------------------------------------------------------------------------

---Returns a free sound channel between <code>from</code> and <code>to</code>
-- (inclusive), or <code>0</code> if no such channel can be found.
-- @param from Must be a positive number
-- @param to Must be >= <code>from</code>
function getFreeChannel(from, to)
	from = math.max(1, from or 1)
	to = to or from + 999
	
	for ch=from,to do
		if soundState:get(ch) == nil then
			return ch
		end
	end
	return 0
end

---Starts playback of sound/voice/music
-- @param filename Path to a valid audio file
-- @param loops The number of times the sound should repeat. Default is
--        <code>1</code>, use <code>-1</code> for infinite looping.
-- @param vol Loudness of the sound between <code>0.0</code> and
--        <code>1.0</code>, default is <code>1.0</code>
-- @param ch The sound channel to use for the sound, defaults to <code>1</code>
-- @param type The sound type: <code>SoundType.SOUND</code>,
--        <code>SoundType.MUSIC</code> or <code>SoundType.VOICE</code>.
-- @return The sound channel of the new playing sound, or <code>nil</code> if
--         for whatever reason no sound has been started.
function sound(filename, loops, vol, ch, type)
    loops = loops or 1
    if loops == 0 then
    	--Playing a sound zero times is easy, just don't do anything
    	return
    end
	--if quickRead and loops == 1 then
	--	return
	--end
    vol = vol or 1.0
    ch = ch or 1
    type = type or SoundType.SOUND

	if type == SoundType.MUSIC then
		local name = Sound.getName(filename)
		if name ~= nil and name ~= "" then
			notifier:message("Now playing: " .. name)
		end
	end

   	Sound.start(ch, filename, type, loops, vol)
   	
    return ch
end

---Stops the sound playing in the specified channel
-- @param ch The channel of the sound that should be stopped.
-- @see sound
function soundStop(ch)
    Sound.stop(ch)
end

---Plays a sound effect of type <code>SoundType.VOICE</code
-- @see sound
function voice(filename, loops, vol, ch)
    return sound(filename, loops, vol, ch, SoundType.VOICE)
end

---Starts playback of looping background music. This function calls
-- <code>sound</code> internally with <code>ch=0</code>.
-- @param filename Path to a valid audio file
-- @param vol Loudness of the music between <code>0.0</code> and
--        <code>1.0</code>, default is <code>1.0</code>
-- @see sound
function music(filename, vol)
    return sound(filename, -1, 1.0, 0, SoundType.MUSIC)
end

---Stops music started with the <code>music</code> function.
-- @see music
function musicStop()
    soundStop(0)
end

---Changes the volume of a currently playing sound/music/voice, possibly
-- gradually over a period of time.
-- @param sound The sound/music/voice channel (or a Sound object) to change the
--        volume of.
-- @param newvol The target volume for the Sound
-- @param duration The duration (in frames) it should take to gradually change
--        the volume.
function changeVolume(sound, newvol, duration)
	if type(sound) == "number" then
		sound = soundState:get(sound)
	end
	newvol = newvol or 1
	duration = duration or 0
	
	local vol = sound:getPrivateVolume()
	if duration > 0 then
		local delta = (newvol - vol) / duration
		while math.abs(newvol - vol) > math.abs(delta * effectSpeed) do
			vol = vol + delta * effectSpeed
			sound:setPrivateVolume(vol)
			--print(vol)
			yield()
		end 
	end
	sound:setPrivateVolume(newvol)
end

