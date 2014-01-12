
nvlist3 = System.compareVersion(prefs.engineTargetVersion, "4.0") < 0

if not nvlist3 then
	return
end

local styleStack = {} 

local function getText()
	local textBox = textState:getTextDrawable()
	if textBox ~= nil then
		return textBox:getText()
	end
	return nil
end

function text(str)
	paragraph.start()
	paragraph.append(str)
	paragraph.finish()
end

function appendText(str)
	local styled = nil
	local logStyled = nil
	local currentStyle = getCurrentStyle()
	if lineRead and prefs.textReadStyle ~= nil then
		styled = createStyledText(str, extendStyle(prefs.textReadStyle, currentStyle))
		logStyled = createStyledText(str, currentStyle)
	else
		styled = createStyledText(str, currentStyle)
		logStyled = styled
	end

	textState:appendText(styled)
    appendTextLog(logStyled)

	local textBox = textState:getTextDrawable()
	if quickRead and textBox ~= nil then
		textBox:setVisibleChars(999999)
	end
end

function fadeTo(i, targetAlpha, speed)
	speed = math.abs(speed or 0.05)
	
	local alpha = i:getAlpha()
	if alpha > targetAlpha then
		while alpha - speed * effectSpeed > targetAlpha do
			alpha = alpha - speed * effectSpeed
			i:setAlpha(alpha)
			yield()
		end
	elseif alpha < targetAlpha then
		while alpha + speed * effectSpeed < targetAlpha do
			alpha = alpha + speed * effectSpeed
			i:setAlpha(alpha)
			yield()
		end
	end
	i:setAlpha(targetAlpha)
end

local _texton = texton
function texton(speed, ...)
	return _texton(1.0 / (speed or 0.05), ...)
end

local _textoff = textoff
function textoff(speed, ...)
	return _textoff(1.0 / (speed or 0.05), ...)
end

---Changes the text speed modifier. Resets at the end of a paragraph, when
-- changing text modes or by calling the <code>textSpeed</code> function
-- without any arguments.
-- @number[opt=1.0] speed The text speed multiplication factor.
function textSpeed(speed)
	speed = speed or 1
	if speed < 0 then
		speed = 999999
	else
		speed = math.max(0.001, speed)
	end

	textState:setTextSpeed(speed)
end

---Plays a voice clip. This function calls the <code>sound</code> function
-- internally with type <code>SoundType.VOICE</code>.
-- @string filename Path to a valid audio file, relative to
--         <code>res/snd</code>.
-- @int[opt=1] loops The number of times the sound should repeat. Default is
--             <code>1</code> (play it only once). Use <code>-1</code> for
--             infinite looping.
-- @number[opt=1.0] volume Loudness of the sound between <code>0.0</code> and
--                  <code>1.0</code>, default is <code>1.0</code>
-- @int[opt=101] channel The audio channel to use. Each channel can only play
--               one sound at a time.
-- @see sound
function voice(filename, loops, volume, channel)
    return sound(filename, loops, volume, channel or 101, SoundType.VOICE)
end

---Changes the current style used for the <code>text</code> and
-- <code>appendText</code> functions.<br/>
-- Usage: <code>text text text [style{color=0xFFFF0000}] text text</code><br/>
-- For a list of all TextStyle properties, see: <code>createStyle</code><br/>
-- @tparam TextStyle textStyle The TextStyle object to use.
-- @see createStyle
function style(textStyle)
	local i = #styleStack	
	if textStyle == nil then
		styleStack[i] = nil
        return nil
	end
	
	if type(textStyle) ~= "userdata" then
		textStyle = createStyle(textStyle)
	end
	styleStack[i] = extendStyle(styleStack[i], textStyle)
	return styleStack[i]
end

---Returns the top of the current style state. The style stack can be
-- manipulated with <code>pushStyle</code>, <code>popStyle</code>,
-- <code>style</code>.
-- treturn TextStyle The current text style.
function getCurrentStyle()
	return styleStack[#styleStack]
end

---Adds a new entry to the top of the style stack that's a copy of
-- <code>getCurrentStyle</code>. 
function pushStyle()
	local st = getCurrentStyle()
	table.insert(styleStack, st)
	return st
end

---Pops the top entry from the style stack.
function popStyle()
	return table.remove(styleStack)
end

---Paragraph
-------------------------------------------------------------------------------- @section paragraph

paragraph = {
	stringifiers={},
}

local paragraphFilename = nil
local paragraphLineNum = 0
local paragraphAppends = 0

---Gets called at the beginning of each text line.
-- @param filename The filename in which the paragraph resides
-- @param lineNum The zero-based text line index of the paragraph
function paragraph.start(filename, lineNum)
	paragraphFilename = filename
	paragraphLineNum = lineNum or 0
	
	--Update lineRead for this line
	lineRead = seenLog:isTextLineRead(paragraphFilename, paragraphLineNum)
	
	--Handle paragraph start differently based on text mode
	if isTextModeADV() then
		clearText()
	elseif isTextModeNVL() then
		if android then
			local textBox = textState:getTextDrawable()
			if textBox ~= nil and textBox:getTextHeight() >= .5 * textBox:getHeight() then
				clearText() --Auto-page to compensate for much larger text size on Android
			end
		end
		
		local curText = getText()
		if curText ~= nil and curText:length() > 0 then
			local lastChar = curText:getChar(curText:length()-1)
			if lastChar ~= 0x20 and lastChar ~= 0x0A then			
				appendText("\n\n")
			end
		end	
	end

	paragraphAppends = 0	
end

---Gets called during execution of a text line to add pieces of text between
-- commands.
-- @param string The text to append
function paragraph.append(string)
	if paragraphAppends == 0 then
	    updateSpeakerName()
		if speaker.textStyle ~= nil then
			style(speaker.textStyle)
		end
	end	
	paragraphAppends = paragraphAppends + 1

	appendText(string)
	
	--Now wait until all text has faded in, otherwise commands following this
	--one will start while the text is still fading in.
	waitForTextVisible()
end

---Gets called at the end of each text line
function paragraph.finish()	
	--Now wait until all text has faded in, otherwise the waitClick can be called
	--prematurely
	waitForTextVisible()

	--Turn off skip mode if applicable
	if getSkipMode() == SkipMode.PARAGRAPH then
		setSkipMode(0)
	end
	
	--Wait for click
	waitClick()	
	
	--Reset speaker
	if speaker.resetEOL then
		say()
	end
	
	--Clear style stack
	styleStack = {}
	
	--Reset textbox's text speed
	textSpeed()
	
	--Register line as read
	if paragraphFilename ~= nil and paragraphLineNum >= 1 then
		seenLog:setTextLineRead(paragraphFilename, paragraphLineNum)
	end
	lineRead = true
end

function paragraph.stringify(word)	
	word = stringify(word, 2)
	if word ~= nil then
		paragraph.append(word)
	end
end

---End
-------------------------------------------------------------------------------- @section end

function table.pack(...)
	return { n = select("#", ...), ... }
end

---Returns a Lua closure wrapping the Java implementation of the yield function.
function yieldClosure(...)
	return yield(...)
end

