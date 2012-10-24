-------------------------------------------------------------------------------
-- text.lua
-------------------------------------------------------------------------------
-- Provides the 'built-in' VN text functions.
-------------------------------------------------------------------------------

module("vn.text", package.seeall)

-- ----------------------------------------------------------------------------
--  Variables
-- ----------------------------------------------------------------------------

TextMode = {ADV=1, NVL=2}

local textMode = 0
local textAlpha = 1
local speaker = {}
local lineRead = true
local textLayerConstructors = {}
local currentStyle = nil

-- ----------------------------------------------------------------------------
--  Text Functions
-- ----------------------------------------------------------------------------

local function getTextLayerAlphas(mode)
	mode = mode or textMode

	return getImageStateAttribute("textLayerAlphas" .. mode)
end

local function getTextLayerDrawables()
	return getImageStateAttribute("textLayerDrawables" .. textMode)
end

---Returns a named drawable from the current text layer, or <code>nil</code> if
-- a drawable with that name doesn't (yet) exist.
function getTextLayerDrawable(name)
	local drawables = getTextLayerDrawables()
	if drawables == nil then
		return nil
	end
	return drawables[name]
end

local function isNameTextDrawable(name)
	return name ~= nil and string.sub(name, 1, 8) == "nameText" --Check prefix
end

local function isCursorDrawable(name)
	return name ~= nil and string.sub(name, 1, 6) == "cursor" --Check prefix
end

local function textfade(targetAlpha, speed, predicate)
	if textMode == 0 then
		return
	end

	speed = speed or 0.05
	
	local threads = {}
	local baseAlpha = getTextLayerAlphas()
	
	--Collect drawables
	local nameLookup = {}	
	for k,d in pairs(getTextLayerDrawables()) do
		nameLookup[d] = k
	end
	
	local drawables = getTextLayer():getDrawables()
	for i=1,#drawables do
		local d = drawables[i]
		local k = nameLookup[d]
		
		if predicate == nil or predicate(k, d) then		
			local a = targetAlpha
			if (isNameTextDrawable(k) and (speaker.name == nil or speaker.name == ""))
				or isCursorDrawable(k)
			then
				a = 0
			elseif baseAlpha[d] ~= nil then
				a = a * baseAlpha[d]
			end
			table.insert(threads, newThread(fadeTo, d, a, speed))
		end
	end
	update1join(threads)
end

local function updateSpeakerName()
	if getTextMode() == 0 or not speaker.nameChanged then
        return
	end

	local nameText = getTextLayerDrawable("nameText")
	
	if speaker.name ~= nil and speaker.name ~= "" then
		--Set nameText's text and make it visible
		if nameText ~= nil then
			if speaker.nameStyle ~= nil then
				nameText:setDefaultStyle(speaker.nameStyle)
			end
			nameText:setText(speaker.name)
		end
		
		--Append name to text log (and to textState if no nameText exists)
		local oldStyle = currentStyle
		if speaker.nameStyle ~= nil then
			currentStyle = extendStyle(currentStyle, speaker.nameStyle)
		end
		if nameText ~= nil then
			appendTextLog("[" .. speaker.name .. "]\n")
		else
			if isTextModeADV() then
				appendText("[" .. speaker.name .. "] ")
			else
				appendText("[" .. speaker.name .. "]\n")
			end
		end			
		currentStyle = oldStyle			

		textfade(1, 1, function(k, d)
			return isNameTextDrawable(k)
		end)
	else
		if nameText ~= nil then
			nameText:setText("")
		end
		
		textfade(0, 1, function(k, d)
			return isNameTextDrawable(k)
		end)
	end

    speaker.nameChanged = false		
end

local function getText()
	local textBox = textState:getTextDrawable()
	if textBox ~= nil then
		return textBox:getText()
	end
	return nil
end

---Sets the current text of the main textbox
-- @param str The new text (may be either a string, or a StyledText object)
function text(str)
	--clearText()
	paragraph.start()
	paragraph.append(str)
	paragraph.finish()
end

---Clears the current text of the main textbox
function clearText()
    textState:setText("")
    appendTextLog("", true)
	
	speaker.nameChanged = true
    updateSpeakerName()
end

---Appends text to the main textbox
-- @param str The text to append (may be either a string, or a StyledText
-- object)
function appendText(str)
	local styled = nil
	local logStyled = nil	
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

---Appends a new line of text to the main textbox
-- @param string The text to append
function appendTextLine(string)
	local curText = getText()
	if curText ~= nil and curText:length() > 0 then
		appendText("\n" .. string)
	else
		appendText(string)
	end
end

---Appends text to the textlog, but not the text state
-- @param string The text to append
-- @param newPage If <code>true</code>, starts a new page before appending the
-- text to the text log.
function appendTextLog(string, newPage)
	textState:appendTextLog(string, newPage)
end

---Changes the name of the person currently speaking
function say(name, textStyle, nameStyle)
	if speaker.name ~= name then
		speaker.name = name
		speaker.nameChanged = true
	end
	
	if nameStyle == nil then
		if speaker.defaultNameStyle == nil then
			speaker.defaultNameStyle = createStyle{fontStyle="bold"}
		end
		nameStyle = speaker.defaultNameStyle
	end 
	speaker.nameStyle = nameStyle
	
	speaker.textStyle = textStyle
	speaker.resetEOL = false
end

---Changes the name of the person currently speaking. Resets the speaker to
-- default at the end of the paragraph.
-- @see say
function sayLine(...)
	local result = say(...)
	speaker.resetEOL = true
	return result
end

---Registers a <code>paragraph.stringifier</code> function to convert
-- <code>$variables</code> inside text to a call to <code>sayLine</code>.
-- In addition, a global <code>say_XXX</code> function is created that calls
-- the <code>say</code> function to change the active speaker.
-- Example: <code>registerSpeaker("bal", "Balthasar")
-- $bal This line is now said with my name, Balthasar.</code>
-- @param id The value of the text after the <code>$</code>-sign that triggers
--        the say function.
function registerSpeaker(id, ...)
	local args = getTableOrVarArg(...)
	if type(args[1]) == "function" then
		paragraph.stringifiers[id] = function()
			return args[1](unpack(args, 2))
		end
		_G["say_" .. id] = function()
			return args[1](unpack(args, 2))
		end
	else
		paragraph.stringifiers[id] = function()
			return sayLine(unpack(args))
		end
		_G["say_" .. id] = function()
			return say(unpack(args))
		end
	end	
end

---Creates a new StyledText object from the given string and TextStyle
-- @param text The text part of the StyledText
-- @param style The base style for the StyledText
-- @return A new StyledText object
function createStyledText(text, style)
    return Text.createStyledText(text or "", style)
end

---Creates a new TextStyle object from the given table
-- @param s A table with attribute/value pairs for the new TextStyle
-- <br/>List of TextStyle properties: <ul>
--  <li><strong>fontName</strong> The filename (without the .ttf) of the font to use</li>
--  <li><strong>fontStyle</strong> <code>&quot;plain&quot;, &quot;bold&quot;, &quot;italic&quot; or &quot;bolditalic&quot;</code></li>
--  <li><strong>fontSize</strong> Determines the text size</li>
--  <li><strong>anchor</strong> Controls text alignment, corresponds to numpad directions (4=left, 6=right, 7=top left)</li>
--  <li><strong>color</strong> Text ARGB color packed into a single integer (red=0xFFFF0000, green=0xFF00FF00, blue=0xFF0000FF, etc)</li>
--  <li><strong>underline</strong> Set to <code>true</code> to make the text underlined</li>
--  <li><strong>outlineSize</strong> Thickness of the text outline (stroke)</li>
--  <li><strong>outlineColor</strong> Outline ARGB color packed into a single integer (white=0xFFFFFFFF, black=0xFF000000, etc)</li>
--  <li><strong>shadowColor</strong> Shadow ARGB color packed into a single integer (50% black=0x80000000, etc)</li>
--  <li><strong>shadowDx</strong> Shadow x-offset</li>
--  <li><strong>shadowDy</strong> Shadow y-offset</li>
-- </ul>
--
-- @return A newly condtructed TextStyle object, or <code>nil</code> in the
--         case of an error.
function createStyle(s)
	return Text.createStyle(s)
end

---Creates a new TextStyle object, overriding attributes in <code>s</code> with
-- those in <code>e</code>
-- @param s An existing TextStyle object
-- @param e A table with attribute/value pairs for the new TextStyle
-- @return A newly condtructed TextStyle object, or <code>nil</code> in the
--         case of an error.
function extendStyle(s, e)
	if e == nil then
		return s
	end
	if s == nil then
		return createStyle(e)
	end
	return s:extend(createStyle(e))
end

---Changes the current style for the <code>text</code> and
-- <code>appendText</code> functions.<br/>
-- Usage: <code>text text text [style{color=0xFFFF0000}] text text</code><br/>
-- For a list of all TextStyle properties (besides color), see: createStyle<br/>
--
-- @param s The style to extends the current style with. Use <code>nil</code>
-- to reset to the default style.
-- @see createStyle
function style(s)
	if s == nil then
		currentStyle = s
	else
		if type(s) ~= "userdata" then
			s = createStyle(s)
		end
		currentStyle = extendStyle(currentStyle, s)
	end
end

---Sets the text speed to the default speed multiplied by the specified factor.
-- Resets at the end of a paragraph, when changing text modes or by calling the
-- textSpeed function without any arguments.
-- @param s The number to multiply the default text speed by
function textSpeed(s)
	s = s or 1
	if s < 0 then
		s = 999999
	else
		s = math.max(0.001, s)
	end

	textState:setTextSpeed(s)
end

---Fades out the main textbox
function textoff(...)
	textfade(0, ...)	
	textAlpha = 0
end

---Fades in the main textbox
function texton(...)
	textfade(1, ...)
	textAlpha = 1
end

---Returns the current text layer
function getTextLayer(mode)
	mode = mode or textMode
	return getImageStateAttribute("textLayer" .. mode)
end

function setTextMode(m, clear)
	m = m or 0
	if textMode == m then
		return
	end
		
	--Fade out
	local oldTextLayer = getTextLayer()
	if oldTextLayer ~= nil then
		oldTextLayer:setVisible(false)
	end

	--Change text mode
	local oldTextMode = textMode
	textMode = m
	System.setTextFullscreen(textMode == TextMode.NVL)
	textState:setTextDrawable(nil)
	
	if textMode ~= 0 then
		--Create text layer
		local newLayer = getTextLayer()
		if newLayer == nil then
			newLayer = createLayer("text" .. m)
			newLayer:setZ(-2000)
			setImageStateAttribute("textLayer" .. textMode, newLayer)
			
			local oldLayer = getImageLayer()
			setImageLayer(newLayer)
			local drawables = getTextLayerConstructor(m)()
			setImageStateAttribute("textLayerDrawables" .. m, drawables)
			local alphas = setmetatable({}, {__mode="k"}) --Create a table with weak referenced keys
			for _,d in pairs(drawables) do
				alphas[d] = d:getAlpha()
			end
			setImageStateAttribute("textLayerAlphas" .. m, alphas)
			setImageLayer(oldLayer);
		end
		newLayer:setVisible(true)

		--Attach text box to text state
		local textBox = getTextLayerDrawable("text")
		if textBox ~= nil then
			textState:setTextDrawable(textBox)
		end
		
		if oldTextMode ~= 0 then
			clearText()
			speaker.nameChanged = true
		end
		
		--Fade to textAlpha
		textfade(textAlpha, 1)
	end
end

---Changes the text mode to full-screen textbox mode
function setTextModeNVL()
	setTextMode(TextMode.NVL)
end

---Changes the text mode to bottom-aligned textbox mode
function setTextModeADV()
	setTextMode(TextMode.ADV)
end

function getTextMode()
	return textMode
end

-- @return <code>true</code> if the text mode is NVL
function isTextModeNVL()
	return getTextMode() == TextMode.NVL
end

-- @return <code>true</code> if the text mode is NVL
function isTextModeADV()
	return getTextMode() == TextMode.ADV
end

-- @return <code>true</code> if the current text line has been read before.
function isLineRead()
	return lineRead
end

--- Waits until all characters in a text drawable have become visible
-- @param textDrawable An optional text drawable to wait for. This function
-- waits on the main text box if this parameter is <code>nil</code> or
-- unspecified.
function waitForTextVisible(textDrawable)
	textDrawable = textDrawable or textState:getTextDrawable()
	while textDrawable ~= nil and not textDrawable:isDestroyed()
			and not textDrawable:getFinalLineFullyVisible() do
		
		yield()
	end
end

---Runs the textLog mode
function textLog()
	local screen = nil

	return setMode("textLog", function()
		pushImageState()
		screen = Screens.textLog.new()
		screen:run()
		setMode(nil)
	end, function()
		if screen ~= nil then
			screen:destroy()
			screen = nil
			popImageState()
		end	
	end)
end

-- ----------------------------------------------------------------------------
--  Paragraph Functions
-- ----------------------------------------------------------------------------

paragraph = {
	stringifiers={}
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
	if isTextModeADV() or android then
		clearText()
	elseif isTextModeNVL() then
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

---Gets called during execution of a text line to replace words starting with
-- a dollar sign. If a handler function is registered for the word in the
-- <code>paragraph.stringifiers</code> table, that function is evaluated.
-- Otherwise, if <code>word</code> is a valid variable in the local context,
-- its value is converted to a string and appended to the current paragraph.
-- @param word The characters following the dollar sign
function paragraph.stringify(word)
	local value = paragraph.stringifiers[word]
	if value == nil then
		value = getDeepField(getLocalVars(3), word) or getDeepField(getfenv(2), word)
	end
	
	--Evaluate functions fully
	while type(value) == "function" do
		value = value()
	end
	
	--Early exit when value is nil	
	if value == nil then
		return
	end
	
	--Convert value to StyledText
	value = createStyledText(value)
	
	--Don't append when nil or empty string
	if value == nil or value:length() == 0 then
		return
	end
	
	paragraph.append(value)
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
	
	--Reset style
	style()
	
	--Reset textbox's text speed
	textSpeed()
	
	--Register line as read
	if paragraphFilename ~= nil and paragraphLineNum >= 1 then
		seenLog:setTextLineRead(paragraphFilename, paragraphLineNum)
	end
	lineRead = true
end

-- ----------------------------------------------------------------------------
--  Layer constructors
-- ----------------------------------------------------------------------------

---Returns the current constructor function for the specified text mode
-- @param mode The text mode to get the constructor for
-- @return The constructor function
-- @see setTextLayerConstructor
function getTextLayerConstructor(mode)
	return textLayerConstructors[mode] or (function() end)
end

---The text layer constructor gets called when a new text layer is created. It
-- should create the base drawables and return them in a table.
-- @param mode The text mode this constructor is for
-- @param func The constructor function to use
-- @return A table with the drawables created for the layer. Some table keys
--         have special meanings:<br/>
--         <ul>
--           <li>text: The main text box. Any key starting with &quot;text&quot; is considered to be part of the main text box (if you use &quot;textBackground&quot; as a key, that drawable will appear/disappear together with the main textbox).</li>
--           <li>nameText: An optional secondary text box to hold the speaking character's name. As with &quot;text&quot;, all keys starting with &quot;nameText&quot; are considered part of the nameTextBox.</li>
--           <li>cursor: An optional click-to-continue cursor</li>
--         </ul>
function setTextLayerConstructor(mode, func)
	mode = mode or textMode
	
	local oldLayer = getTextLayer(mode)
	if oldLayer ~= nil then
		oldLayer:destroy()
		setImageStateAttribute("textLayer" .. mode, nil)		
	end
	
	textLayerConstructors[mode] = func or (function() end)
	
	if mode == textMode then
		textMode = 0
		setTextMode(mode)
	end	
end

local function createTextLayerText(mode)
	local text = textimg()
	text:setBackgroundColorARGB(0xA0000000)
	if mode == TextMode.ADV then
		text:setBackgroundColorARGB(0xE0000000)
	end		
	text:setZ(-1000)
	return text
end

local function createTextCursor(textBox)
	local cursor = nil
	local cursorTex = tex("gui/cursor#waitClick", true) or tex("gui/cursor", true)
	if android then
		cursorTex = tex("android/cursor#waitClick", true) or tex("android/cursor", true) or cursorTex
	end
	
	if cursorTex ~= nil then
		cursor = img(cursorTex)
		local scale = 1
		if prefs.textStyle ~= nil then
			scale = 1.2 * prefs.textStyle:getFontSize() / cursor:getUnscaledHeight()
		end
		cursor:setScale(scale, scale)
		textBox:setCursor(cursor, true, true)
	end
	return cursor
end

local defaultTextLayerConstructors = {}

defaultTextLayerConstructors[TextMode.ADV] = function()
	local mode = TextMode.ADV
	
	local textBox = createTextLayerText(mode)
	local pad  = .025 * math.min(screenWidth, screenHeight);
	textBox:setSize(math.min(screenWidth, screenHeight * 1.30) - pad*2,
		screenHeight/4 - pad*2)
	textBox:setPos((screenWidth - textBox:getWidth()) / 2,
		screenHeight - textBox:getHeight() - pad)
	textBox:setPadding(pad * 0.75)
	
	local cursor = createTextCursor(textBox)

	local nameBox = nil
	if not android then
		nameBox = createTextLayerText(mode)
		nameBox:setDefaultStyle(createStyle{fontStyle="bold"})
		nameBox:setText("???")
		nameBox:setAnchor(7)
		nameBox:setPadding(.01 * math.min(screenWidth, screenHeight))
		nameBox:setSize(textBox:getWidth(), nameBox:getTextHeight() + nameBox:getPadding() * 2)
		nameBox:setPos(textBox:getX(), textBox:getY() - nameBox:getHeight())
		nameBox:setText("")
	end
	
	return {text=textBox, nameText=nameBox, cursor=cursor}
end

defaultTextLayerConstructors[TextMode.NVL] = function()
	local mode = TextMode.NVL

	local textBox = createTextLayerText(mode)
	local pad = .05 * math.min(screenWidth, screenHeight);
	textBox:setPos(pad, pad)
	textBox:setSize(screenWidth - pad*2, screenHeight - pad*2)
	textBox:setPadding(pad * .5)
	
	local cursor = createTextCursor(textBox)
	
	return {text=textBox, nameText=nil, cursor=cursor}
end

textLayerConstructors[TextMode.ADV] = defaultTextLayerConstructors[TextMode.ADV]
textLayerConstructors[TextMode.NVL] = defaultTextLayerConstructors[TextMode.NVL]

---Utility function for creating a standard text box.
function customTextBox(mode, t, baseFunc)
	if t == nil then
		return setTextLayerConstructor(mode, defaultTextLayerConstructors[mode])
	end

	local baseFunc = baseFunc or defaultTextLayerConstructors[mode]
	setTextLayerConstructor(mode, function()
		local drawables = baseFunc()
		local text = drawables.text
		local nameText = drawables.nameText
		local cursor = drawables.cursor

		local function replaceDrawable(id, newval)
			local oldval = drawables[id]
			if oldval ~= nil then oldval:destroy() end
			drawables[id] = newval
		end

		local function fixText(text, template, bounds)
			if template ~= nil then
				text:setPadding(0)
				text:setBackgroundColorARGB(0)
				if bounds == nil then
					text:setPos(template:getX(), template:getY())
					text:setSize(template:getWidth(), template:getHeight())
				end
			end
			
			if bounds ~= nil then
				text:setPos(bounds[1], bounds[2])
				text:setSize(bounds[3], bounds[4])
			end
		end

		if text ~= nil then
			local textBackground = nil
			if t.textBackground ~= nil then
				textBackground = img(t.textBackground, t.textBackgroundExtra)						
				replaceDrawable("textBackground", textBackground)
			end
			fixText(text, textBackground, t.textBounds)
		end

		if nameText ~= nil then
			local nameTextBackground = nil
			if t.nameTextBackground ~= nil then
				nameTextBackground = img(t.nameTextBackground, t.nameTextBackgroundExtra)
				replaceDrawable("nameTextBackground", nameTextBackground)
			end
			fixText(nameText, nameTextBackground, t.nameTextBounds)
		end
		
		if t.cursor ~= nil then
			cursor = img(t.cursor, t.cursorExtra)
			if t.cursorPos ~= nil then			
				cursor:setPos(t.cursorPos.x, t.cursorPos.y)
				cursor = text:setCursor(cursor, true, false)
			else
				cursor = text:setCursor(cursor, true, true)
			end
			replaceDrawable("cursor", cursor)
		end

		return drawables
	end)
end

-- ----------------------------------------------------------------------------
--  
-- ----------------------------------------------------------------------------
