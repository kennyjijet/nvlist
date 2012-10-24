package nl.weeaboo.nvlist.build.android;

import java.io.File;
import java.io.IOException;

public interface FileHandler {

	public void process(String relpath, File srcF, File dstF) throws IOException;
	
}
