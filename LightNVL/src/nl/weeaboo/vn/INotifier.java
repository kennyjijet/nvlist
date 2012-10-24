package nl.weeaboo.vn;

public interface INotifier {

	// === Functions ===========================================================
	
	/**
	 * @see #v(String, Throwable)
	 */
	public void v(String message);
	
	/**
	 * Verbose 
	 */
	public void v(String message, Throwable t);
	
	/**
	 * @see #d(String, Throwable)
	 */
	public void d(String message);

	/**
	 * Debug, for warnings that are helpful to the developer but useless for the
	 * end user.
	 */
	public void d(String message, Throwable t);
	
	/**
	 * @see #w(String, Throwable)
	 */
	public void w(String message);

	/**
	 * Warning 
	 */
	public void w(String message, Throwable t);

	/**
	 * @see #message(String, Throwable)
	 */
	public void message(String message);

	/**
	 * Message to the user
	 */
	public void message(String message, Throwable t);
	
	/**
	 * @see #e(String, Throwable)
	 */
	public void e(String message);

	/**
	 * Error 
	 */
	public void e(String message, Throwable t);
	
	/**
	 * General error function that all other methods forward to
	 */
	public void log(ErrorLevel el, String message, Throwable t);
	
	// === Getters =============================================================
	
	// === Setters =============================================================
	public void setMinimumLevel(ErrorLevel el);
	
}
