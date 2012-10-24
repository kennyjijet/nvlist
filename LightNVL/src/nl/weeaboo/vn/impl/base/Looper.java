package nl.weeaboo.vn.impl.base;

import nl.weeaboo.lua2.FastMath;
import nl.weeaboo.lua2.io.LuaSerializable;
import nl.weeaboo.vn.ILooper;

@LuaSerializable
public class Looper implements ILooper {

	private static final long serialVersionUID = BaseImpl.serialVersionUID;
	
	private final LoopMode loopMode;
	private double duration;

	private double time;
	private double inc;
	
	public Looper(double inc) {
		this.loopMode = LoopMode.NOWRAP;
		this.duration = 1;
		this.inc = inc;
	}
	public Looper(LoopMode mode, double duration) {
		if (mode == null) throw new IllegalArgumentException("Loop mode must not be null");
		
		this.loopMode = mode;
		this.duration = duration;				
		this.inc = 1;
	}
	
	//Functions
	@Override
	public boolean update(double effectSpeed) {
		if (inc == 0 || effectSpeed == 0) {
			return false;
		}
		
		setTime(time + inc * effectSpeed);
		
		return true;
	}
	
	protected void onTimeChanged() {
		switch (loopMode) {
		case NOWRAP: updateNoWrap(); break;
		case WRAP:   updateWrap();   break;
		case BOUNCE: updateBounce(); break;
		case SINE:   updateSine();   break;
		default: throw new RuntimeException("Unsupported loop mode: " + loopMode);
		}		
	}
	
	protected void updateNoWrap() {
		//Nothing to do
	}
	
	protected void updateWrap() {
		if (time > duration) {
			time %= duration;
		}
		while (time < 0) {
			time += duration;
		}			
	}
	protected void updateBounce() {		
		if (time > duration) {
			if (inc > 0) inc = -inc;

			double over = (time - duration) % (2 * duration);
			if (over < duration) {
				time = duration - over;
			} else {
				time = over % duration;
			}
		} else if (time < 0) {
			if (inc < 0) inc = -inc;

			double under = -time % (2 * duration);
			if (under < duration) {
				time = under;
			} else {
				time = duration - (under % duration);
			}
		}

		
	}
	protected void updateSine() {
	}
	
	//Getters
	@Override
	public double getTime() {
		if (loopMode == LoopMode.SINE) {
			return duration * getNormalizedTime();
		}
		return time;
	}
	
	@Override
	public double getNormalizedTime() {
		if (loopMode == LoopMode.SINE) {
			return .5f + .5f * FastMath.fastSin(384 + 512 * (float)(time / duration));
		}
		return time / duration;
	}

	/**
	 * @return The duration of a single loop
	 */
	@Override
	public double getDuration() {
		return duration;
	}
	
	//Setters
	@Override
	public void setNormalizedTime(double t) {
		switch (loopMode) {
		case NOWRAP:
		case WRAP:
		case BOUNCE:
			setTime(t * getDuration());
			break;
		case SINE:
			setTime((FastMath.fastArcSin((float)t) / 512f) * duration);
			break;
		default: throw new RuntimeException("Unsupported loop mode: " + loopMode);
		}
	}
	
	@Override
	public void setTime(double t) {
		if (time != t) {
			time = t;
			onTimeChanged();
		}
	}
	
	public void setDuration(double d) {
		duration = d;
		time = 0;
	}
	
}
