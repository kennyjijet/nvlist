---Functions related to text display.
-- 
module("vn.text", package.seeall)

-- ----------------------------------------------------------------------------
--  Variables
-- ----------------------------------------------------------------------------

---Text modes. These determine the way the textbox looks.
TextMode = {
	ADV=1, --Adventure game style bottom-aligned text.
	NVL=2, --Novel style full screen text.
}

local textMode = 0
local textAlpha = 1
speaker = {}
lineRead = true
local textLayerConstructors = {}
local currentStyle = nil

-- ----------------------------------------------------------------------------
--  Local Functions
-- ----------------------------------------------------------------------------

local function getTextLayerAlphas(mode)
	mode = mode or textMode

	return getImageStateAttribute("textLayerAlphas" .. mode)
end

local function getTextLayerDrawables()
	return getImageStateAttribute("textLayerDrawables" .. textMode)
end

local function isNameTextDrawable(name)
	return name ~= nil and string.sub(name, 1, 8) == "nameText" --Check prefix
end

local function isCursorDrawable(name)
	return name ~= nil and string.sub(name, 1, 6) == "cursor" --Check prefix
end

local function textfade(targetAlpha, fadeDurationFrames, predicate)
	if textMode == 0 then
		return
	end

	fadeDurationFrames = fadeDurationFrames or 20
	
	local threads = {}
	local baseAlpha = getTextLayerAlphas()
	
	--Collect drawables
	local nameLookup = {}	
	for k,d in pairs(getTextLayerDrawables()) do
		nameLookup[d] = k
	end
	
	local drawables = getTextLayer():getContents()
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
			table.insert(threads, newThread(fadeTo, d, a, fadeDurationFrames))
		end
	end
	update1join(threads)
end

local function hideSpeakerName()
	local nameText = getTextLayerDrawable("nameText")
	if nameText ~= nil then
		nameText:setText("")
		textfade(0, 0, function(k, d)
			return isNameTextDrawable(k)
		end)
	end
end

function updateSpeakerName()
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
		local oldStyle = currentStyle;
		currentStyle = speaker.nameStyle
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

		textfade(textAlpha, 0, function(k, d)
			return isNameTextDrawable(k)
		end)
	else
		hideSpeakerName()
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

-- ----------------------------------------------------------------------------
--  Functions
-- ----------------------------------------------------------------------------

---Sets the current text of the main textbox.
-- @param str The new text (may be either a string or a StyledText object). Any
--        embedded stringifiers or text tags are evaluated unless
--        <code>meta.parse == false</code>.
-- @tab[opt=nil] triggers An optional table containing functions that should be
--               called at specific text positions.
-- @tab[opt=nil] meta A table with metadata for this line of text (filename,
--      line, etc.)
function text(str, triggers, meta)
	meta = meta or {}

	local textBox = textState:getTextDrawable()

	--Update lineRead for this line
	lineRead = seenLog:isTextLineRead(meta.filename, meta.line)
	
	--Handle paragraph start differently based on text mode
	if isTextModeADV() then
		clearText()
	elseif isTextModeNVL() then
		if android then
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
	
	--Parse str and execute stringifiers, text tag handlers, etc.
	if meta.parse ~= false then --Explicit ~= false check is important to distinguish from nil.
		str, triggers = Text.parseText(str, triggers)
	end
	
	--Update speaker and append text to the textbox
    updateSpeakerName()
	if textBox ~= nil then
		triggers = Text.rebaseTriggers(triggers, textBox)
	end
		    
	currentStyle = speaker.textStyle
	appendText(str)
	
	--Now wait until all text has faded in and execute triggers at appropriate times
	waitForTextVisible(textBox, triggers)

	--Turn off skip mode if applicable
	if getSkipMode() == SkipMode.PARAGRAPH then
		setSkipMode(0)
	end
	
	--Wait for click
	waitClick()	
	
	--Reset speaker
	currentStyle = nil
	if speaker.resetEOL then
		say()
	end
	
	--Register line as read
	if meta.filename ~= nil and meta.line >= 1 then
		seenLog:setTextLineRead(meta.filename, meta.line)
	end
	lineRead = true	
end

---Clears the text of the main textbox (effectively sets it to <code>""</code>).
-- In ADV mode, the text is cleared between each line of text. In NVL mode, you
-- need to call <code>clearText</code> manually.
function clearText()
    textState:setText("")
    appendTextLog("", true)
    hideSpeakerName()
end

---Appends text to the main textbox.
-- @param str The text to append (may be either a string or a StyledText
--        object).
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

---Appends text to the main textbox. Puts the added text on a new line.
-- @param str The text to append (may be either a string or a StyledText
--        object).
-- @deprecated 4.0
function appendTextLine(str)
	deprecated("4.0")

	local curText = getText()
	if curText ~= nil and curText:length() > 0 then
		appendText("\n" .. str)
	else
		appendText(str)
	end
end

---Appends text to the textlog, but not the main textbox. Allows you to manually
-- add lines to the textlog, which can be useful if your VN has text that's not
-- displayed in the main textbox.
-- @param str The text to append (may be either a string or a StyledText
--        object).
-- @bool[opt=false] newPage If <code>true</code>, starts a new page in the
--      textlog before appending the text.
function appendTextLog(str, newPage)
	textState:appendTextLog(str, newPage)
end

---Changes the name of currently speaking character.
-- @string name The character's display name.
-- @tparam[opt=nil] TextStyle textStyle Default text style to use for text added
--        to the main textbox while this character is speaking.
-- @tparam[opt=nil] TextStyle nameStyle Default text style to use for the
--        display of this character's name.
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

---Like <code>say</code>, but resets the speaking character at the end of the
-- current paragraph.
-- @see say
function sayLine(...)
	local result = say(...)
	speaker.resetEOL = true
	return result
end

---Registers a stringifier function to replace occurrences of $<code>id</code>
-- with a call to <code>sayLine</code>.<br/>
-- In addition, this function creates a global <code>say_XXX</code> function,
-- replacing XXX with <code>id</code>, to change the active speaker for multiple
-- lines at once.<br/>
-- Example use: <code>registerSpeaker("bal", "Balthasar")
-- $bal This line is now said with my name, Balthasar.</code>
-- @string id Unique identifier string for the speaker.
-- @param ... All other parameters are passed to <code>sayLine</code>.
function registerSpeaker(id, ...)
	local args = getTableOrVarArg(...)
	if type(args[1]) == "function" then
		registerStringifier(id, function()
			return args[1](unpack(args, 2))
		end)
		_G["say_" .. id] = function()
			return args[1](unpack(args, 2))
		end
	else
		registerStringifier(id, function()
			return sayLine(unpack(args))
		end)
		_G["say_" .. id] = function()
			return say(unpack(args))
		end
	end	
end

---Parses the given string, turning it into a StyledText object by evaluating
-- any embedded stringifiers and/or text tags.
-- @string text The string to parse.
-- @treturn StyledText A StyledText object.
-- @since 4.0
function parseText(text)
	return Text.parseText(text)
end

---Creates a new StyledText object from the given string and TextStyle.
-- @string text The text part of the StyledText.
-- @tparam TextStyle style The base style for the StyledText.
-- @treturn StyledText A new StyledText object.
function createStyledText(text, style)
    return Text.createStyledText(text or "", style)
end

---Creates a new TextStyle object from the given table.
-- @tab styleProperties A table with attribute/value pairs for the new TextStyle.
-- <br/>List of TextStyle properties: <ul>
--  <li><strong>fontName</strong> The filename (without the .ttf) of the font to use.</li>
--  <li><strong>fontStyle</strong> <code>&quot;plain&quot;, &quot;bold&quot;, &quot;italic&quot; or &quot;bolditalic&quot;</code></li>
--  <li><strong>fontSize</strong> Determines the text size</li>
--  <li><strong>anchor</strong> Controls text alignment, corresponds to numpad directions (4=left, 6=right, 7=top left)</li>
--  <li><strong>color</strong> Text ARGB color packed into a single integer (red=0xFFFF0000, green=0xFF00FF00, blue=0xFF0000FF, etc.)</li>
--  <li><strong>underline</strong> Set to <code>true</code> to make the text underlined</li>
--  <li><strong>outlineSize</strong> Thickness of the text outline (stroke)</li>
--  <li><strong>outlineColor</strong> Outline ARGB color packed into a single integer (white=0xFFFFFFFF, black=0xFF000000, etc.)</li>
--  <li><strong>shadowColor</strong> Shadow ARGB color packed into a single integer (50% black=0x80000000, etc.)</li>
--  <li><strong>shadowDx</strong> Shadow x-offset</li>
--  <li><strong>shadowDy</strong> Shadow y-offset</li>
--  <li><strong>speed</strong> Relative text speed (2.0 for double speed, 0.5 for half speed, etc.)</li>
-- </ul>
--
-- @treturn TextStyle A newly created TextStyle object, or <code>nil</code> if
--          an error occurs.
function createStyle(styleProperties)
	return Text.createStyle(styleProperties)
end

---Creates a new TextStyle object from <code>style</code>, but overriding it
-- with the attributes in <code>extendProperties</code>.
-- @tparam TextStyle style The TextStyle object to use as a base.
-- @tab extendProperties A table with attribute/value pairs for the new
--      TextStyle. See <code>createStyle</code> for the list of possible
--      attributes.
-- @treturn TextStyle A newly created TextStyle object, or <code>nil</code> if
--          an error occurs.
-- @see createStyle
function extendStyle(style, extendProperties)
	if extendProperties == nil then
		return style
	end
	if style == nil then
		return createStyle(extendProperties)
	end
	return style:extend(createStyle(extendProperties))
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

-- @treturn bool <code>true</code> if the currently displayed text has been read
--          before.
function isLineRead()
	return lineRead
end

---Waits until the text in the main textbox (or other TextDrawable) has finished
-- appearing.
-- @tparam[opt=nil] TextDrawable textDrawable An optional TextDrawable to wait
--        for. If not specified, the text drawable of the main textbox is used.
-- @tab[opt=nil] triggers An optional table containing functions that should be
--               called at specific text positions.
function waitForTextVisible(textDrawable, triggers)
	textDrawable = textDrawable or textState:getTextDrawable()
	
	local i0 = 0
	while textDrawable ~= nil and not textDrawable:isDestroyed() do
		if triggers ~= nil then
			local i1 = math.min(textDrawable:getMaxVisibleChars(), math.floor(textDrawable:getVisibleChars()))
			for i=i0,i1 do
				if triggers[i] ~= nil then
					triggers[i]()
				end
			end
			i0 = i1 + 1
		end
		
		if textDrawable:getFinalLineFullyVisible() then
			break
		end
		
		yield()
	end
end

---Shows the textlog screen.
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

---Stringifiers
-------------------------------------------------------------------------------- @section stringifiers

local stringifiers = {}

---Registers the specified function to be used whenever <code>id</code> needs to
-- be stringified.
-- @string id The word to register a custom stringifier function for.
-- @func func A function that returns a string or StyledText object.
-- @since 4.0
function registerStringifier(id, func)
	stringifiers[id] = func
end

---Gets called during execution of a text line to replace words starting with
-- a dollar sign. If a stringify handler function is registered for the word,
-- that function is evaluated.
-- Otherwise, if <code>word</code> is a valid variable in the local context,
-- its value is converted to a string representation.
-- @param word The characters following the dollar sign
-- @param level The relative level to search for local variables, depends on
--        the depth of the call tree before stringify is called.
-- @since 4.0
function stringify(word, level)
	level = level or 3

	local value = stringifiers[word]
	if value == nil and paragraph ~= nil and paragraph.stringifiers ~= nil then
		--Backwards compatibility with NVList 3.x
		value = paragraph.stringifiers[word]
	end
	if value == nil then
		value = getDeepField(getLocalVars(level + 1), word) or getDeepField(getfenv(level), word)
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
	
	return value
end

---Text tags
-------------------------------------------------------------------------------- @section textTags

local tagHandlers = {}

if not prefs.vnds and not nvlist3 then
	Text.registerBasicTagHandlers(tagHandlers)
end

---Registers text tag handler functions (open/close) for a specific text tag.
-- @since 4.0
function registerTextTagHandler(tag, openFunc, closeFunc)
	tagHandlers[tag] = openFunc
	tagHandlers["/" .. tag] = closeFunc
end

-- Gets called when an open tag is encountered within text.
-- @since 4.0
function textTagOpen(tag, values, level)
	values = values or {}
	level = level or 3

	local func = tagHandlers[tag]
	if func == nil then
		return
	end
	
	--Resolve argument strings to their proper types/values
	local newValues = {}
	for k,v in pairs(values) do
		v = getDeepField(getLocalVars(level + 1), v)
		  or getDeepField(getfenv(level), v)
		  or Text.resolveConstant(v)
		newValues[k] = v
	end

	return func(tag, newValues)
end

-- Gets called whenever a close tag is encountered within text.
-- @since 4.0
function textTagClose(tag)
	local func = tagHandlers["/" .. (tag or "")]
	if func == nil then
		return
	end
	return func(tag, values)
end

---Text box manipulation
-------------------------------------------------------------------------------- @section textBox

---Fades away the main textbox.
function textoff(...)
	textAlpha = 0
	textfade(0, ...)	
end

---Fades in the main textbox.
function texton(...)
	textAlpha = 1
	textfade(textAlpha, ...)
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
			newLayer = createLayer(getRootLayer())
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
		textfade(textAlpha, 0)
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

---Returns the current text mode.
function getTextMode()
	return textMode
end

--- @return <code>true</code> if the text mode is NVL
function isTextModeNVL()
	return getTextMode() == TextMode.NVL
end

--- @return <code>true</code> if the text mode is NVL
function isTextModeADV()
	return getTextMode() == TextMode.ADV
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
    
	local cursorTex = nil
    if android then
        cursorTex = tex("gui/cursor-android#waitClick", true)
                 or tex("gui/cursor-android", true)
                 or tex("builtin/gui/cursor-android#waitClick", true)
                 or tex("builtin/gui/cursor-android", true)
    end
    if cursorTex == nil then
        cursorTex = tex("gui/cursor#waitClick", true)
                 or tex("gui/cursor", true)
                 or tex("builtin/gui/cursor#waitClick", true)
                 or tex("builtin/gui/cursor", true)
    end
	
	if cursorTex ~= nil then
		cursor = img(cursorTex)
		local scale = 1
		if prefs.textStyle ~= nil then
			scale = 1.2 * prefs.textStyle:getFontSize() / cursor:getUnscaledHeight()
		end
		cursor:setScale(scale)
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

-------------------------------------------------------------------------------- section end 
