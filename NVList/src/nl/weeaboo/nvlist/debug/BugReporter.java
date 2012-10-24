package nl.weeaboo.nvlist.debug;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.Map.Entry;

import nl.weeaboo.common.StringUtil;
import nl.weeaboo.filemanager.FileManager;

public class BugReporter {

	public static String F_FILENAME     = "filename";
	public static String F_VERSION_CODE = "version";
	public static String F_DATE         = "date";
	public static String F_STACK_TRACE  = "stackTrace";
	public static String F_VISIBLE_TEXT = "visibleText";
	public static String F_MESSAGE      = "message";
	
	public static final String OUTPUT_FILENAME = "bugreport.log";
	
	private final OutputStream out;
	
	public BugReporter(FileManager fm) throws IOException {
		this(fm.getOutputStream(OUTPUT_FILENAME, true));
	}
	public BugReporter(OutputStream out) {
		this.out = out;
	}
	
	//Functions
	public void dispose() {
		try {
			out.close();
		} catch (IOException e) {
			//Ignore
		}
	}
	
	public void write(Map<String, String> record) throws IOException {
		StringBuilder sb = new StringBuilder();
		for (Entry<String, String> entry : record.entrySet()) {
			String key = entry.getKey();
			String val = entry.getValue();			
			if (key != null && val != null) {
				sb.append('[').append(key).append("]\n");
				sb.append(val.replace("\n[", "\n ["));
				sb.append('\n');
			}
		}
		sb.append("[***]\n\n");
		out.write(StringUtil.toUTF8(sb.toString()));
		out.flush();
	}
	
	//Getters
	
	//Setters
	
}
