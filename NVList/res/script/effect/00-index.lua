require("effect/rain")
require("effect/snow")
require("effect/fog")
require("effect/fire")

while true do
    clearText()
    bgf("bg/bg1")
    
    local selected = choice("Rain", "Snow", "Fog", "Fire", "GLSL Shaders", "Return")
    
    if selected == 1 then
        --Rain test
        startRain()
        text("Click to stop the effect.")
        stopRain()
    elseif selected == 2 then
        --Snow test
        startSnow()
        text("Click to stop the effect.")
        stopSnow()
    elseif selected == 3 then
        --Fog test
        startFog()
        text("Click to stop the effect.")
        stopFog()
        startSimpleFog()
        text("Simpler fog using a single tiled image. Click to stop the effect.")
        stopSimpleFog()
    elseif selected == 4 then
        --Fire test
        firebgf("bg/bg1")
        local sprite = fireimgf("arm01m", "r")
        text("Click to stop the effect")
        rmf(sprite)
        rmbgf()
    elseif selected == 5 then
        call("effect/glsl")
    else
        rmbgf()
        break
    end    
end

clearText()
