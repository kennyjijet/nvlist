package nl.weeaboo.vn.impl.base;

import java.io.IOException;
import java.io.ObjectInputStream;

import nl.weeaboo.vn.ILooper;
import nl.weeaboo.vn.IShader;

public class BaseShader implements IShader {

	private static final long serialVersionUID = BaseImpl.serialVersionUID;
	
	private ILooper looper;
	private double time;
	private final boolean timeNormalized;
	private transient boolean changed;	
	
	protected BaseShader(boolean timeNormalized) {
		this.timeNormalized = timeNormalized;
		
		initTransients();
	}
	
	//Functions
	private void initTransients() {
		changed = true;
	}
	
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		
		initTransients();
	}	
	
	protected void markChanged() {
		changed = true;
	}
	
	protected boolean consumeChanged() {
		boolean result = changed;
		changed = false;
		return result;
	}
	
	@Override
	public boolean update(double effectSpeed) {
		if (looper != null) {
			if (looper.update(effectSpeed)) {
				onTimeChanged();
			}
		}
		
		return consumeChanged();
	}
	
	protected void onTimeChanged() {
		markChanged();
	}
	
	//Getters	
	protected double getLooperTime(ILooper looper) {
		if (timeNormalized) {
			return looper.getNormalizedTime();
		} else {
			return looper.getTime();
		}
	}
	
	@Override
	public final double getTime() {
		return (looper != null ? getLooperTime(looper) : time);
	}
	
	public ILooper getLooper() {
		return looper;
	}
	
	//Setters
	@Override
	public final void setTime(double f) {
		if (time != f) {
			time = f;			
			if (looper != null) {
				setLooperTime(looper, f);
			}
			onTimeChanged();
		}
	}

	public void setLooper(ILooper l) {
		if (looper != l) {
			if (l != null) {
				setLooperTime(l, getTime());
			} else {
				time = getTime();
			}
			looper = l;
			markChanged();
		}
	}
	
	protected void setLooperTime(ILooper looper, double time) {
		if (timeNormalized) {
			looper.setNormalizedTime(time);
		} else {
			looper.setTime(time);
		}
	}
	
}
