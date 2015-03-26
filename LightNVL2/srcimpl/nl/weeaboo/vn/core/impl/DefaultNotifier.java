package nl.weeaboo.vn.core.impl;

import nl.weeaboo.vn.ErrorLevel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultNotifier extends AbstractNotifier {

    private final Logger LOG = LoggerFactory.getLogger(DefaultNotifier.class);

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
        default:
            throw new IllegalArgumentException("Unsupported error level: " + el);
        }
    }

}
