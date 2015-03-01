package nl.weeaboo.vn;

import java.util.logging.Level;
import java.util.logging.Logger;

import nl.weeaboo.vn.impl.AbstractNotifier;

public class TestNotifier extends AbstractNotifier {

    private final Logger LOG = Logger.getLogger(TestNotifier.class.getName());

    @Override
    public void log(ErrorLevel el, String message, Throwable t) {
        switch (el) {
        case VERBOSE:
            LOG.log(Level.CONFIG, message, t);
            break;
        case DEBUG:
            LOG.log(Level.INFO, message, t);
            break;
        case WARNING:
            LOG.log(Level.WARNING, message, t);
            break;
        case ERROR:
            LOG.log(Level.SEVERE, message, t);
            break;
        case MESSAGE:
            LOG.log(Level.INFO, message, t);
            break;
        }
    }

}
