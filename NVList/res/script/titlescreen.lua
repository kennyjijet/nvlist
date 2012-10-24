
function defaultTitlescreen(startScript, extraScript)
    textoff(1) --Turns off the text box with speed 1 (instantly)
    
    bgf("gui/titlescreen-background") --Fade in the title screen background
    
    --Create a few buttons
    local startB = button("gui/titlescreen-buttons#start-")
    local loadB  = button("gui/titlescreen-buttons#load-")
    local extraB = button("gui/titlescreen-buttons#extra-")
    local quitB  = button("gui/titlescreen-buttons#quit-")
    
    --Positions the buttons at the right side of the screen, centered vertically
    local buttons = {startB, loadB, extraB, quitB}    
    local y = (screenHeight - 100 * #buttons) / 2
    for i,b in pairs(buttons) do --For each button:b at index:i
        local startY = y + 100 * (i-1)
        local endY = startY + 100
        b:setPos(screenWidth-300, startY+(endY-startY-b:getHeight())/2)
    end

    --This function is called in two places to clean up the buttons/background
    local function cleanup()
        destroyValues(buttons) --Removes the buttons
        rmbgf() --Fades out and removes the background
        texton(1) --Turns on the text box with speed 1 (instantly)
    end
    
    while true do
        if startB:consumePress() then
            if startScript ~= nil then
                --Clean up the title screen, jump to the start of the VN
                cleanup()
                return call(startScript)
            else
                notifier:message("Start button pressed (startScript not set)")
            end
        elseif loadB:consumePress() then            
            loadScreen() --Shows the load screen, then returns here
        elseif extraB:consumePress() then
            if extraScript ~= nil then
                --Clean up the title screen, jump to the extras script
                cleanup()
                return call(extraScript)
            else
                notifier:message("Extra button pressed (extraScript not set)")
            end
        elseif quitB:consumePress() then            
            exit(false) --Asks the user if they want to exit the program
        end
        
        yield() --Wait for one frame to allow the screen to update (without the yield, we'd have an infinite loop)
    end

    exit(true)
end
