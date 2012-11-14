package nl.weeaboo.vn.impl.base;

import nl.weeaboo.common.StringUtil;

final class BaseImpl {

	private BaseImpl() {		
	}
	
	static final long serialVersionUID = 50L;
		
	public static String replaceExt(String filename, String ext) {
		int index = filename.indexOf('#');
		if (index < 0) {
			return StringUtil.replaceExt(filename, ext);
		} else {
			return StringUtil.replaceExt(filename.substring(0, index), ext)
				+ filename.substring(index);
		}
	}
	
	public static int packRGBAtoARGB(double r, double g, double b, double a) {
		int ri = Math.max(0, Math.min(255, (int)Math.round(r * 255f)));
		int gi = Math.max(0, Math.min(255, (int)Math.round(g * 255f)));
		int bi = Math.max(0, Math.min(255, (int)Math.round(b * 255f)));
		int ai = Math.max(0, Math.min(255, (int)Math.round(a * 255f)));
		return (ai<<24)|(ri<<16)|(gi<<8)|(bi);
	}
	
	public static void checkRange(double val, String name) {
		checkRange(val, name, Double.MIN_VALUE, Double.MAX_VALUE);
	}
	public static void checkRange(double val, String name, double min) {
		checkRange(val, name, min, Double.MAX_VALUE);
	}
	public static void checkRange(double val, String name, double min, double max) {
		if (Double.isNaN(val) || Double.isInfinite(val) || val < min || val > max) {
			throw new IllegalArgumentException("Invalid value for " + name + ": " + val);
		}
	}
	
}
