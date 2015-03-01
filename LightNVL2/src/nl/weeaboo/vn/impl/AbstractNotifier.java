package nl.weeaboo.vn.impl;

import nl.weeaboo.vn.ErrorLevel;
import nl.weeaboo.vn.INotifier;

public abstract class AbstractNotifier implements INotifier {

	protected volatile ErrorLevel minimumLevel;

	public AbstractNotifier() {
		minimumLevel = ErrorLevel.VERBOSE;
	}

	//Functions
	@Override
	public void verbose(String message) {
		verbose(message, null);
	}

	@Override
	public void verbose(String message, Throwable t) {
		log(ErrorLevel.VERBOSE, message, t);
	}

	@Override
	public void debug(String message) {
		debug(message, null);
	}

	@Override
	public void debug(String message, Throwable t) {
		log(ErrorLevel.DEBUG, message, t);
	}

	@Override
	public void warn(String message) {
		warn(message, null);
	}

	@Override
	public void warn(String message, Throwable t) {
		log(ErrorLevel.WARNING, message, t);
	}

	@Override
	public void message(String message) {
		message(message, null);
	}

	@Override
	public void message(String message, Throwable t) {
		log(ErrorLevel.MESSAGE, message, t);
	}

	@Override
	public void error(String message) {
		error(message, null);
	}

	@Override
	public void error(String message, Throwable t) {
		log(ErrorLevel.ERROR, message, t);
	}

	//Getters

	//Setters
	@Override
	public void setMinimumLevel(ErrorLevel el) {
		minimumLevel = el;
	}

}
