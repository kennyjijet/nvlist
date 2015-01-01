package nl.weeaboo.vn.script;

import java.io.IOException;
import java.io.InputStream;

public interface IScriptLoader {

	/**
	 * @param pattern The filename, or filename pattern.
	 * @return The canonical filename of a script file, or {@code null} if the given pattern did not match any
	 *         existing script file.
	 */
	public String findScriptFile(String pattern);

	/**
	 * Opens the given script file as an inputstream.
	 *
	 * @param normalizedFilename The canonical filename of the script file to open (i.e. the result of a call
	 *        to {@link #findScriptFile(String)}).
	 * @throws IOException If an exception occurs while trying to open the specified script.
	 * @see #findScriptFile(String)
	 */
	public InputStream openScript(String normalizedFilename) throws IOException;

}
