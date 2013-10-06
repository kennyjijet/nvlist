package nl.weeaboo.vn.parser;

import java.io.IOException;
import java.io.InputStream;

public interface LVNParser {

	public LVNFile parseFile(String filename, InputStream in) throws ParseException, IOException;
	
}
