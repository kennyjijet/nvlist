
--choice() --Choice without arguments test (shouldn't crash, may show one or more placeholder options)

--[[ Word wrapping tests
setTextModeNVL()
text("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Duis tincidunt, eros nec consectetur feugiat, neque nulla condimentum sapien, sed sagittis felis metus non nisi. Nullam id lobortis tortor. Duis consequat orci erat, ac pharetra lacus. Nulla auctor orci sed magna posuere eget laoreet massa posuere. In hac habitasse platea dictumst. Mauris eu velit tortor. In et urna lorem.")

text("Quisque et ipsum ut sem hendrerit eleifend. Sed pulvinar commodo ante, malesuada scelerisque lectus rhoncus at. Nulla facilisi. Cras et eros in tellus congue pulvinar. Donec tincidunt, elit ut ultrices auctor, neque ligula laoreet odio, eu sollicitudin nibh arcu at ante. Duis sodales, nisl id dapibus aliquam, magna est vehicula ipsum, ut scelerisque neque purus ut nibh. Sed commodo pretium metus, vel dictum nibh sagittis ac. Sed scelerisque bibendum ipsum, sed ultricies felis bibendum in. Ut sodales, orci vitae faucibus ultrices, metus lorem varius urna, vitae dignissim velit arcu at ante. Integer nulla sapien, iaculis facilisis consequat quis, viverra sit amet dui. Fusce tortor nunc, faucibus adipiscing dictum ut, varius vel lacus. Curabitur in justo quam, id congue quam.")

text("Vestibulum arcu justo, gravida rhoncus feugiat ut, sollicitudin quis sapien. Integer tempor placerat justo, non mattis magna luctus ac. Proin id ipsum odio, at suscipit turpis. Donec ultricies, neque eu mollis feugiat, tellus erat ultrices risus, id tincidunt nibh ligula sed lectus. Integer gravida augue sit amet mauris posuere sed dignissim\n\n arcu varius. Sed dolor lorem, ullamcorper sed tempor sit amet, accumsan a sem. Aenean laoreet elementum rhoncus. Donec sollicitudin lectus nec erat dapibus convallis. Quisque porttitor, est at tempus suscipit, ante diam pulvinar nisl, ac auctor magna nulla a quam. Cras enim lacus, dignissim sed tempor a, tincidunt ut est. Sed aliquet risus nec sapien placerat ultrices vitae eget tortor. Nulla vestibulum viverra viverra.")

text("Aliquam non tortor justo. Sed pulvinar sodales ligula sit amet ornare. Sed id sapien risus, ut consequat sapien. Curabitur dui felis, euismod non consequat non, ullamcorper eget arcu. Phasellus ullamcorper ullamcorper ante, eu convallis orci ornare quis. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Cras posuere accumsan vulputate. Etiam iaculis nunc nec magna lobortis ultrices. Aenean nisi nunc, adipiscing vel suscipit sed, congue in arcu.")

text("Fusce auctor massa iaculis erat dignissim vel imperdiet velit interdum. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Etiam vehicula fringilla turpis. Nulla facilisi. Duis consequat accumsan placerat. Suspendisse consequat venenatis semper. Nulla fermentum fermentum nunc sit amet molestie. Aenean et dolor est. Donec fringilla ipsum a mi vestibulum nec laoreet massa dictum. Praesent ultricies accumsan nisl, eleifend consequat metus porttitor eu. Ut consectetur dignissim porttitor. Nulla porttitor pretium augue id auctor. Integer eleifend sapien at purus porttitor quis tincidunt sem elementum. Maecenas vulputate augue sit amet justo interdum aliquam. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec sed quam sem, vel ultrices dolor.")
]]

--[[ Text rendering tests ]]
bgf("bg/bg1")
while true do
    text(createStyledText("111111 111111", {anchor=5, tags={1}}):concat(" filler filler filler  filler filler filler  filler filler filler  filler filler filler "):concat(createStyledText("222222 222222", {anchor=5, tags={2}})))

    --RTL and BiDirectional text
    text(createStyledText("שהר שהר שהר aaa שהר שהר שהרשהרשהרשהר", {fontName="SansSerif", anchor=5}))
    text(createStyledText("abc Test שהרשהרשהרשהרשהר (Test2)", {fontName="SansSerif", anchor=7, underline=true}))
    text(createStyledText("של 1234 ום WORD של!@#$%^&*()ום", {fontName="SansSerif", anchor=9}))

    --Can't break inside かぁ combination
    text(createStyledText("中中中中中中中中中中中中中中中中中中中中中中中中中中中中中中中中中中中中かかかかかかかかかかかかかかかかかかかかかかかかかかかかかかかかかかぁななななな", {fontName="sazanami-gothic"}))
    
    --This line has embedded zero-width spaces
    text("embedded zero-width spaces: vrachtautoband​ventieldopjes​fabrieks​directeurs​assistentes​uniformen​knopen​maker")

    --Non-breaking spaces
    text("This sentence uses a long word with non-breaking spaces: \"start               end\"")
    
    --Soft hyphens (should be hidden when not the last printable char in a line. Supported in NVList 3.1+, may not work on Android)
    --To support would require deleting all SHY characters not at the end of the line.
    text("embedded soft-hyphens: vrachtautoband­​ventieldopjes­fabrieks­directeurs­assistentes­uniformen­knopen­maker")

    --Font/exotic characters test
    text(createStyledText([=[Ā Á Ǎ À ā á ǎ à
Ǖ Ǘ Ǚ Ǜ ǖ ǘ ǚ ǜ
Ĉ ĉ Ĝ ĝ Ĥ ĥ Ĵ ĵ Ŝ ŝ Ŭ ŭ
Я не говорю по-русски.
Αυτου οι θανατον μητσομαι.
Ἰοὺ ἰού· τὰ πάντʼ ἂν ἐξήκοι σαφῆ.
אני לא לומד
中 華 民 族 中 华 民 族
𦮙]=], createStyle{fontName="SansSerif"}))

end
rmbgf()

--[[
--Glow effect test
textoff()

firebgf("Main_Square")
local fatty = fireimgf("NPC_Fatty", "c")

while true do
    if input:consumeKey(Keys.A) then
        fatty:setTexture("NPC_Fatty_Angry")
    elseif input:consumeKey(Keys.B) then
        fatty:setTexture("NPC_Fatty")
    end
    
    if input:consumeKey(Keys.C) then
        firebgf("Main_Square")
    elseif input:consumeKey(Keys.D) then
        firebgf("First_Street")
    end
    yield()
end

texton()

local fatty = imgf("NPC_Fatty", "c")

local ps = GLSL.new("fire")
ps:setLooper(Looper.new(0.01))
getBackground():setPixelShader(ps)

local fatty2 = img("NPC_Fatty", "c", {z=fatty:getZ()-1, blendMode=BlendMode.ADD})
fatty2:setTexture(brighten(blur(fatty2:getTexture(), 64), 0.25), 5)
local thread = newThread(function()
    local t = 0
    while not fatty2:isDestroyed() do
        local s = math.fastSin(t * 0.7) * math.fastCos(t * 0.3)
        fatty2:setAlpha(0.95 + 0.05 * s)
        t = t + 4 * effectSpeed
        yield()
    end
end)
]]

--[[
--Multiplane camera test

local fatty = imgf("NPC_Fatty", "c")

local images = {}
for i=1,3 do
    images[i] = img("white", 300 * i, (300 * i) % screenHeight, {scale=5, color={.33*i, 0, 1-.33*i}, z=-i})
end

local camera = Image.createCamera()
for _,i in ipairs(images) do
    camera:add(i, 4 + 1 * i:getZ())
end

camera:addZoom(600, 320, 180, 640, 360)
camera:addZoom(600, 640, 360, 360, 360)
camera:addZoom(600, 0, 0, 1280, 720)

local looper = Looper.new(LoopMode.WRAP, camera:getDuration())
while true do
    looper:update(effectSpeed)
    camera:setTime(looper:getTime())
    yield()
end

camera:destroy()
]]

--[[ screenmask shader test
bgf("Dark_Alleyway")               
local maskShader = GLSL.new("screenmask")
maskShader:setParam("mask", tex("fade/matrix"))
getBackground():setPixelShader(maskShader)    
]]

--[[ distort shader test
local i = img("arm01m", "c")
local shader = DistortGS.new(1, 128)
shader:setLooper(Looper.new(.01))
shader:setClampBounds(0, 0, i:getWidth(), i:getHeight())
i:setGeometryShader(shader)

while not input:consumeTextContinue() do
    shader:set(function(x, y, t)
        return 5 * math.fastCos(5000 * y - 250 * t), 0
    end)
    yield()
end

rm(i)
]]

--[[ image gallery test, make sure test images are located in the img/cg folder.
require("gui/gallery")
imageGallery("cg")
]]

--[[ Viewports demo
local viewports = {}
for i=1,4 do
    local viewport = Viewport.new{x=50 + 200 * i, y=50, w=150, h=225}
    viewport:openLayer()

    local cs = {
        img("white", {y=0, size={100, 100}}),
        img("white", {y=125, size={100, 100}, colorRGB=0xFF0000}),
        img("white", {x=75, y=250, size={100, 100}, colorRGB=0x00FF00}),
        img("white", {y=375, size={100, 100}, colorRGB=0xFFFF00})
    }
    for j=i+1,4 do
        rm(cs[j])
        cs[j] = nil
    end
    
    viewport:closeLayer(cs)
    table.insert(viewports, viewport)
end

while true do
    for _,viewport in ipairs(viewports) do
        viewport:update()
    end
    yield()
end
destroyValues(viewports)
]]

--[[ ParallelAnimator/FunctorAnimator loop test
local i = img("white")

local anim1 = Anim.createTween(i, "pos", {0, 0}, {screenWidth, 0}, 120)
local anim2 = Anim.createTween(i, "color", {1, 1, 1}, {1, 0, 0}, 60)
local anim3 = Anim.fromFunction(function(f) print("F: " .. f) end, 60)
local anim = Anim.par(anim1, anim2, anim3):run(3)

text("Done")
]]

--[[ NVList 3.0 GUI panel/layout test

textoff()

local panel = createPanel(300, 300)

local layouts = {}

local layout = FlowLayout.new()
layout:setPadding(10)
table.insert(layouts, layout)

local layout = GridLayout.new()
layout:setCols(5)
layout:setPack(2)
layout:setAnchor(5)
layout:setPadding(10)
table.insert(layouts, layout)

local layoutIndex = 1
layout = layouts[layoutIndex]
panel:setLayout(layout)

local cs = {}
for n=1,20 do
    cs[n] = img("white", {size={100, 100}})
    panel:add(cs[n])
end

local frame = 0
while not input:consumeConfirm() do
    frame = (frame + 1) % 512

    if input:consumeKey(Keys.Q) then
        layoutIndex = layoutIndex + 1
        if layoutIndex > #layouts then
            layoutIndex = 1
        end
        layout = layouts[layoutIndex]
        panel:setLayout(layout)
    elseif input:consumeKey(Keys.R) then
        for i,d in ipairs(cs) do
            d:setColor(1.0 - (.1 * i) % 1, 1.0, 1.0 - (.03 * i) % 1)
            d:setSize(math.random(50, 200), math.random(50, 200))
        end
        panel:invalidateLayout()
    elseif input:consumeKey(Keys.W) then
        layout:setLeftToRight(true)
        panel:invalidateLayout()
    elseif input:consumeKey(Keys.E) then
        layout:setLeftToRight(false)
        panel:invalidateLayout()
    elseif input:consumeKey(Keys.B) then
        panel:setBackground(tex("bg/bg" .. math.random(1, 3)))
    elseif input:consumeKey(Keys.N) then        
        local corners = {}
        for i=1,4 do corners[i] = tex("bg/bg" .. math.random(1, 3)) end
        
        local sides = {}
        for i=1,4 do sides[i] = tex("bg/bg" .. math.random(1, 3)) end
        
        panel:setBorderInsets(20)
        panel:setBorder(sides, corners)
    else
        --Allow changing the layout anchor by pressing the numpad
        for i=1,9 do
            if input:consumeKey(Keys["NUMPAD" .. i]) then
                layout:setAnchor(i)
            end
        end
    end
    
    local pad = (screenHeight/3) * (.5 - .5*math.fastCos(.5 * frame))
    panel:setBounds(pad, pad, screenWidth-2*pad, screenHeight-2*pad)
    panel:layout()
    
    yield()
end

panel:destroy()
]]

--[[ NVList 3.0 viewport test

viewports = {}
for i=1,4 do
    local viewport = createViewport(150, 225)
    viewport:setPos(50 + 200 * i, 50)
        
    --local layout = GridLayout.new()
    --layout:setCols(1)
    --layout:setPack(7)
    --layout:setAnchor(5)
    --layout:setPadding(10)
    --viewport:setLayout(layout)
    
    setViewportScrollBar(viewport)
    setViewportScrollBar(viewport, true)
    
    local cs = {
        img("white", {y=0, size={100, 100}}),
        img("white", {y=125, size={100, 100}, colorRGB=0xFF0000}),
        img("white", {x=75, y=250, size={100, 100}, colorRGB=0x00FF00}),
        img("white", {y=375, size={100, 100}, colorRGB=0xFFFF00})
    }
    for j=i+1,4 do
        rm(cs[j])
        cs[j] = nil
    end
    for _,i in pairs(cs) do
        viewport:add(i)
    end
    table.insert(viewports, viewport)
end

while not input:consumeConfirm() do
    yield()
end
destroyValues(viewports)
]]

--[[ NVList 3.0 scrolling choice test

choice("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z")
 
]]

--[[ NVList 3.0 text on a button
local tb = button("gui/components#button-")
tb:setPos(alignAnchorX(screenWidth, tb:getWidth(), 5), alignAnchorY(screenHeight, tb:getHeight(), 5))
tb:setText("Text on a button")

while not input:consumeConfirm() and not tb:consumePress() do    
    yield()
end
]]

--[[ Volatile screenshots
bgf("bg/bg1")
wait(60)
local ss = screen2image(nil, -32768, false, true)
ss:setColor(1, 0, 0)
ss:setAlpha(0)
fadeTo(ss, 1)
while not input:consumeConfirm() do
    yield()
end
rmf(ss)
wait(60)
rmbgf()
]]

yield()

return titlescreen()
