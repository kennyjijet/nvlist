package nl.weeaboo.vn.core.impl;

import nl.weeaboo.game.Notifier;
import nl.weeaboo.vn.ErrorLevel;

public class GameNotifier extends DefaultNotifier {

    public Notifier gameNotifier;

    public GameNotifier(Notifier gameNotifier) {
        this.gameNotifier = gameNotifier;
    }

    @Override
    public void log(ErrorLevel level, String message, Throwable t) {
        super.log(level, message, t);

        if (level == ErrorLevel.MESSAGE) {
            gameNotifier.addMessage(this, message);
        }
    }
}
