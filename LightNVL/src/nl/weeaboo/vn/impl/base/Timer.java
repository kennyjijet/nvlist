package nl.weeaboo.vn.impl.base;

import static nl.weeaboo.vn.NovelPrefs.TIMER_IDLE_TIMEOUT;

import java.io.IOException;
import java.io.Serializable;

import nl.weeaboo.io.EnvironmentSerializable;
import nl.weeaboo.lua2.io.LuaSerializable;
import nl.weeaboo.vn.IInput;
import nl.weeaboo.vn.IStorage;
import nl.weeaboo.vn.ITimer;

@LuaSerializable
public class Timer extends EnvironmentSerializable implements ITimer, Serializable {

	private static final long serialVersionUID = BaseImpl.serialVersionUID;

	private static final String KEY_TOTAL_OLD = "timer.total";    //NVList <= 2.6 key
	private static final String KEY_TOTAL     = "vn.timer.total";
	
	private static final int  MAX_TOTAL_TIME_SECONDS = 359999; //99:59:59
	
	private long lastTimestamp;
	private long totalTimeNanos;
	private long maxFrameTimeNanos;
	private long idleTimeNanos;
	private long maxIdleTimeNanos;
	
	public Timer() {
		reset0();
	}
	
	//Functions
	/**
	 * @return The time in nanoseconds 
	 */
	protected static long secondsToNanos(int seconds) {
		return seconds * 1000000000L;
	}
	
	/**
	 * @return The time in seconds, rounded down
	 */
	protected static int nanosToSeconds(long nanos) {
		return (int)Math.min(Integer.MAX_VALUE, nanos / 1000000000L);
	}
	
	private void reset0() {
		lastTimestamp = 0;
		totalTimeNanos = 0;
		maxFrameTimeNanos = secondsToNanos(10);
		idleTimeNanos = 0;
		maxIdleTimeNanos = secondsToNanos(TIMER_IDLE_TIMEOUT.getDefaultValue());
	}
	
	@Override
	public void update(IInput input) {
		long timestamp = System.nanoTime();
		if (lastTimestamp != 0) { //We use timestamp==0 to indicate invalid
			long diff = Math.max(0, timestamp-lastTimestamp);
			
			if (input.isIdle()) {
				idleTimeNanos += diff;
			} else {
				idleTimeNanos = 0;
			}
			
			//Don't increase time when idle for a long time
			if (idleTimeNanos <= maxIdleTimeNanos) {
				//Don't increase time by too much to protect against freezes/hibernate/glitches
				totalTimeNanos += Math.min(maxFrameTimeNanos, diff);
			}
		}
		lastTimestamp = timestamp;
		
		//System.out.println(formatTime(getTotalTime()) + " [" + formatTime(getIdleTime()) + "]");
	}
	
	@Override
	public void load(IStorage storage) throws IOException {
		reset0();
		
		int seconds = storage.getInt(KEY_TOTAL, storage.getInt(KEY_TOTAL_OLD, 0));
		totalTimeNanos = secondsToNanos(seconds);
	}

	@Override
	public void save(IStorage storage) throws IOException {
		storage.setInt(KEY_TOTAL, getTotalTime());
	}
	
	@Override
	public String formatTime(int seconds) {
		int hours = seconds / 3600;
		seconds -= hours * 3600;
		int minutes = seconds / 60;
		seconds -= minutes * 60;
		return String.format("%02d:%02d:%02d", hours, minutes, seconds);
	}
	
	//Getters
	@Override
	public int getTotalTime() {
		return (int)Math.min(MAX_TOTAL_TIME_SECONDS, nanosToSeconds(totalTimeNanos));
	}
	
	@Override
	public int getIdleTime() {
		return nanosToSeconds(idleTimeNanos);
	}
	
	//Setters	
	@Override
	public void setIdleTimeout(int seconds) {
		if (seconds < 0) throw new IllegalArgumentException("timeout must be >= 0, given: " + seconds);
		
		maxIdleTimeNanos = secondsToNanos(seconds);
	}
	
}
