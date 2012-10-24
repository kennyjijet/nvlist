package nl.weeaboo.vn.impl.base;

import java.io.Serializable;

import nl.weeaboo.vn.ErrorLevel;
import nl.weeaboo.vn.INotifier;

public abstract class BaseNotifier implements INotifier, Serializable {

	private static final long serialVersionUID = BaseImpl.serialVersionUID;
	
	protected volatile ErrorLevel minimumLevel;
	
	public BaseNotifier() {
		minimumLevel = ErrorLevel.VERBOSE;
	}
	
	//Functions
	@Override
	public void v(String message) {
		v(message, null);
	}

	@Override
	public void v(String message, Throwable t) {
		log(ErrorLevel.VERBOSE, message, t);
	}
	
	@Override
	public void d(String message) {
		d(message, null);
	}

	@Override
	public void d(String message, Throwable t) {
		log(ErrorLevel.DEBUG, message, t);
	}
	
	@Override
	public void w(String message) {
		w(message, null);
	}

	@Override
	public void w(String message, Throwable t) {
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
	public void e(String message) {
		e(message, null);
	}
	
	@Override
	public void e(String message, Throwable t) {
		log(ErrorLevel.ERROR, message, t);
	}
	
	//Getters
	
	//Setters
	@Override
	public void setMinimumLevel(ErrorLevel el) {
		minimumLevel = el;
	}
	
}
