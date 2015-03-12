package nl.weeaboo.vn;

import nl.weeaboo.vn.impl.AbstractNotifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestNotifier extends AbstractNotifier {

    private final Logger LOG = LoggerFactory.getLogger(TestNotifier.class);

    @Override
    public void log(ErrorLevel el, String message, Throwable t) {
        switch (el) {
        case VERBOSE:
            LOG.trace(message, t);
            break;
        case DEBUG:
            LOG.debug(message, t);
            break;
        case WARNING:
            LOG.warn(message, t);
            break;
        case ERROR:
            LOG.error(message, t);
            break;
        case MESSAGE:
            LOG.info(message, t);
            break;
        }
    }

}
