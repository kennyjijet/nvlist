package nl.weeaboo.vn.impl.lua;

import java.util.ArrayList;
import java.util.List;

import org.luaj.vm2.LuaThread;
import org.luaj.vm2.lib.DebugLib;

public final class LuaNovelUtil {

	private static final int DEFAULT_STACK_LIMIT = 8;
	private static final String LVN_PATTERN = ".lvn:";
	
	private LuaNovelUtil() {		
	}
	
	//Functions
	public static String[] getLuaStack() {
		return getLuaStack(LuaThread.getRunning());
	}
	public static String[] getLuaStack(LuaThread thread) {
		if (thread == null) {
			return null;
		}		
		
		List<String> result = new ArrayList<String>();
		for (int level = 0; level < DEFAULT_STACK_LIMIT; level++) {
			String line = DebugLib.fileline(thread, level);
			if (line == null) break;

			result.add(line);
		}
		return result.toArray(new String[result.size()]);
	}
	
	public static String getNearestLVNSrcloc(String[] stack) {
		if (stack == null) {
			return null;
		}
		
		for (String frame : stack) {
			if (frame.contains(LVN_PATTERN)) {
				return frame;
			}
		}
		return null;
	}
	
	public static String getNearestLVNSrcloc(LuaThread thread) {
		if (thread == null) {
			return null;
		}		
		for (int level = 0; level < DEFAULT_STACK_LIMIT; level++) {
			String line = DebugLib.fileline(thread, level);
			if (line == null) break;

			if (line.contains(LVN_PATTERN)) {
				return line;
			}
		}
		return null;
	}
	
}
