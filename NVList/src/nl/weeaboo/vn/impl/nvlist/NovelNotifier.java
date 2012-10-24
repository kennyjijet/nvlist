package nl.weeaboo.vn.impl.nvlist;

import java.io.ObjectStreamException;
import java.io.Serializable;

import nl.weeaboo.game.GameLog;
import nl.weeaboo.game.Notifier;
import nl.weeaboo.io.EnvironmentSerializable;
import nl.weeaboo.vn.ErrorLevel;
import nl.weeaboo.vn.impl.base.BaseNotifier;

public class NovelNotifier extends BaseNotifier implements Serializable {

	private final EnvironmentSerializable es;	
	private final Notifier notifier;
	
	public NovelNotifier(Notifier ntf) {
		notifier = ntf;
		
		es = new EnvironmentSerializable(this);
	}
	
	//Functions
	@Override
	public void log(ErrorLevel level, String message, Throwable t) {
		switch (level) {
		case VERBOSE: GameLog.v(message, t); break;
		case DEBUG:   GameLog.d(message, t); break;
		case WARNING: GameLog.w(message, t); break;
		case ERROR:   GameLog.e(message, t); break;
		case MESSAGE: notifier.addMessage(this, message); break;
		}
	}

	private Object writeReplace() throws ObjectStreamException {	
		return es.writeReplace();
	}
	
	//Getters
	
	//Setters
	
}
