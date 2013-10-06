--- Audio-related functions.
--  
module("vn.sound", package.seeall)

-- ----------------------------------------------------------------------------
--  Variables
-- ----------------------------------------------------------------------------

-- ----------------------------------------------------------------------------
--  Functions
-- ----------------------------------------------------------------------------

---Changes the background music. This function calls the <code>sound</code>
-- function internally with <code>channel=0</code>.
-- @string filename Path to a valid audio file, relative to
--         <code>res/snd</code>.
-- @number[opt=1.0] volume Loudness of the music between <code>0.0</code> and
--        <code>1.0</code>.
-- @see sound
function music(filename, volume)
    return sound(filename, -1, volume, 0, SoundType.MUSIC)
end

---Stops background music started with the <code>music</code> function.
-- @number fadeTimeFrames Optional argument specifying the duration (in frames)
--         of a slow fade-out instead of stopping playback immediately.
-- @see music
function musicStop(fadeTimeFrames)
    soundStop(0, fadeTimeFrames)
end

---Starts playing a sound effect, voice clip or background music.
-- @string filename Path to a valid audio file, relative to
--         <code>res/snd</code>.
-- @int[opt=1] loops The number of times the sound should repeat. Default is
--             <code>1</code> (play it only once). Use <code>-1</code> for
--             infinite looping.
-- @number[opt=1.0] volume Loudness of the sound between <code>0.0</code> and
--                  <code>1.0</code>, default is <code>1.0</code>
-- @int[opt=1] channel The audio channel to use. Each channel can only play one
--             sound at a time.
-- @tparam[opt=SoundType.SOUND] SoundType type The sound type:
--         <code>SoundType.SOUND</code>, <code>SoundType.MUSIC</code> or
--         <code>SoundType.VOICE</code>.
-- @treturn int The sound channel of the new playing sound, or <code>nil</code>
--          if for whatever reason no sound could be started.
function sound(filename, loops, volume, channel, type)
    loops = loops or 1
    if loops == 0 then
    	--Playing a sound zero times is easy, just don't do anything
    	return
    end
	--if quickRead and loops == 1 then
	--	return
	--end
    volume = volume or 1.0
    channel = channel or 1
    type = type or SoundType.SOUND

	if type == SoundType.MUSIC then
		local name = Sound.getName(filename)
		if name ~= nil and name ~= "" then
			notifier:message("Now playing: " .. name)
		end
	end

   	Sound.start(channel, filename, type, loops, volume)
   	
    return channel
end

---Stops the sound playing in the specified audio channel.
-- @int channel The audio channel.
-- @number fadeTimeFrames Optional argument specifying the duration (in frames)
--         of a slow fade-out instead of stopping playback immediately.
-- @see sound
function soundStop(channel, fadeTimeFrames)
    Sound.stop(channel, fadeTimeFrames)
end

---Plays a voice clip. This function calls the <code>sound</code> function
-- internally with type <code>SoundType.VOICE</code>.
-- @string filename Path to a valid audio file, relative to
--         <code>res/snd</code>.
-- @int[opt=101] channel The audio channel to use. Each channel can only play
--               one sound at a time.
-- @see sound
function voice(filename, channel)
    return sound(filename, 1, 1.0, channel or 101, SoundType.VOICE)
end

---Changes the volume of playing sound/music/voice.
-- @param sound The Sound object or audio channel to change the volume of.
-- @number[opt=1.0] targetVolume The target volume for the Sound.
-- @number[opt=0] durationFrames If specified, the number of frames over which
--                to gradually change the Sound's volume to the
--                <code>targetVolume</code>.
function changeVolume(sound, targetVolume, durationFrames)
	if type(sound) == "number" then
		sound = soundState:get(sound)
	end
	targetVolume = targetVolume or 1
	durationFrames = durationFrames or 0
	
	local vol = sound:getPrivateVolume()
	if durationFrames > 0 then
		local delta = (targetVolume - vol) / durationFrames
		while math.abs(targetVolume - vol) > math.abs(delta * effectSpeed) do
			vol = vol + delta * effectSpeed
			sound:setPrivateVolume(vol)
			--print(vol)
			yield()
		end 
	end
	sound:setPrivateVolume(targetVolume)
end

---Returns a free sound channel between <code>from</code> and <code>to</code>
-- (inclusive), or <code>0</code> if no such channel is found.
-- @int[opt=1] from Must be a number greater than zero.
-- @int[opt=from+999] to Must be a number greater than or equal to
--                    <code>from</code>.
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
