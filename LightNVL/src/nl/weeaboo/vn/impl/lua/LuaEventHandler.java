package nl.weeaboo.vn.impl.lua;

import static org.luaj.vm2.LuaValue.NONE;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import nl.weeaboo.lua2.LuaException;
import nl.weeaboo.lua2.io.LuaSerializable;
import nl.weeaboo.lua2.link.LuaLink;

import org.luaj.vm2.LuaClosure;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.Varargs;

@LuaSerializable
public class LuaEventHandler implements Serializable {

	private static final long serialVersionUID = LuaImpl.serialVersionUID;

	private List<Event> events;
	
	public LuaEventHandler() {
		events = new LinkedList<Event>();
	}
	
	public void addEvent(LuaFunction func) {
		addEvent(func, NONE);
	}
	public void addEvent(LuaFunction func, Varargs args) {
		if (func == null || func.isnil()) {
			return; //No function to call
		}
		
		events.add(new Event(func, args));
	}
	
	public void clear() {
		events.clear();
	}
	
	public boolean isEmpty() {
		return events.isEmpty();
	}
	
	public void flushEvents(LuaLink mainThread) throws LuaException {
		while (!events.isEmpty()) {
			flushEvent(mainThread, events.remove(0));
		}
	}
	
	private void flushEvent(LuaLink mainThread, Event e) throws LuaException {
		System.out.println("ABC");
		
		if (e.func instanceof LuaClosure) {
			mainThread.pushCall((LuaClosure)e.func, e.args);
		} else {
			e.func.invoke(e.args);
		}
	}
	
	//Inner Classes
	private static class Event implements Serializable {

		private static final long serialVersionUID = 1L;
		
		public final LuaFunction func;
		public final Varargs args;
		
		public Event(LuaFunction f, Varargs a) {
			this.func = f;
			this.args = a;
		}
		
	}
	
}
