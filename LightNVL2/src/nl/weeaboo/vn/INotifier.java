package nl.weeaboo.vn;

public interface INotifier {

	// === Functions ===========================================================

    /**
     * @see #message(String, Throwable)
     */
    public void message(String message);

    /**
     * Message to the user
     */
    public void message(String message, Throwable t);

	/**
	 * @see #verbose(String, Throwable)
	 */
	public void verbose(String message);

	/**
	 * Verbose
	 */
	public void verbose(String message, Throwable t);

	/**
	 * @see #debug(String, Throwable)
	 */
	public void debug(String message);

	/**
	 * Debug, for warnings that are helpful to the developer but useless for the
	 * end user.
	 */
	public void debug(String message, Throwable t);

	/**
	 * @see #warn(String, Throwable)
	 */
	public void warn(String message);

	/**
	 * Warning
	 */
	public void warn(String message, Throwable t);

	/**
	 * @see #error(String, Throwable)
	 */
	public void error(String message);

	/**
	 * Error
	 */
	public void error(String message, Throwable t);

	/**
	 * General error function that all other methods forward to
	 */
	public void log(ErrorLevel el, String message, Throwable t);

	// === Getters =============================================================

	// === Setters =============================================================
	public void setMinimumLevel(ErrorLevel el);

}
