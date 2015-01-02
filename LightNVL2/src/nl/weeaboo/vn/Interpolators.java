package nl.weeaboo.vn;

import nl.weeaboo.lua2.io.LuaSerializable;
import nl.weeaboo.vn.IInterpolator;

public final class Interpolators {

	public static final IInterpolator LINEAR = new LinearInterpolator();
	public static final IInterpolator BUTTERWORTH = new ButterworthInterpolator();
	public static final IInterpolator HERMITE = new HermiteInterpolator();
	
	public static final IInterpolator SMOOTH = HERMITE;
	
	private Interpolators() {		
	}
	
	//Inner Classes
	@LuaSerializable
	private static class LinearInterpolator implements IInterpolator {

		private static final long serialVersionUID = 1L;

		@Override
		public float remap(float x) {
			return x;
		}			
	}

	@LuaSerializable
	private static class ButterworthInterpolator implements IInterpolator {

		private static final long serialVersionUID = 1L;

		@Override
		public float remap(float x) {
			if (x >= .5f) {
				x = 1f - x;
				return -1.5f + 2.5f / (1f + x * x);			
			} else {
				return  2.5f - 2.5f / (1f + x * x);
			}
		}			
	}
	
	@LuaSerializable
	private static class HermiteInterpolator implements IInterpolator {

		private static final long serialVersionUID = 1L;
		
		@Override
		public float remap(float x) {
			return x * x * (3 - 2 * x);			
		}
		
	}
	
	
}
