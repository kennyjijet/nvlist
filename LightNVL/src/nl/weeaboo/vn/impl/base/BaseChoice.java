package nl.weeaboo.vn.impl.base;

import nl.weeaboo.vn.IChoice;

public class BaseChoice implements IChoice {

	private static final long serialVersionUID = BaseImpl.serialVersionUID;
	
	private boolean cancelled;
	private String[] options;
	private int selected;
	
	public BaseChoice(String... opts) {
		options = opts.clone();
		selected = -1;
	}
	
	//Functions
	@Override
	public void destroy() {
		cancel();
	}
	
	@Override
	public void cancel() {
		cancelled = true;
	}

	//Getters
	@Override
	public int getSelected() {
		return selected;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}
	
	@Override
	public String[] getOptions() {
		return options.clone();
	}
	
	//Setters
	@Override
	public void setSelected(int s) {
		if (s < 0 || s >= options.length) throw new IllegalArgumentException("Selection index outside valid range: " + s);
		
		selected = s;
	}
	
}
