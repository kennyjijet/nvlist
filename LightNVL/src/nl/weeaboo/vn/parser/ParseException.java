package nl.weeaboo.vn.parser;

@SuppressWarnings("serial")
public class ParseException extends Exception {

	public ParseException(String filename, int line, String error) {
		super(String.format("Error parsing %s:%d -> %s", filename, line, error));
	}
	
}
