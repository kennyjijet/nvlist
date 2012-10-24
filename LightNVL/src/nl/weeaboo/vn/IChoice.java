package nl.weeaboo.vn;

import java.io.Serializable;

public interface IChoice extends Serializable {

	// === Functions ===========================================================
	
	/**
	 * Disposes all resources related to the choice.
	 */
	public void destroy();
	
	/**
	 * Marks the choice as cancelled (usually hiding it in the process), cannot be undone.
	 */
	public void cancel();
	
	// === Getters =============================================================
	
	/**
	 * @return The index of the selected option (<code>[0..n]</code>), returns
	 *         <code>-1</code> if no selection has been made yet.
	 */
	public int getSelected();
	
	/**
	 * @return <code>true</code> if the choice popup was cancelled for whatever
	 *         reason.
	 */
	public boolean isCancelled();

	/**
	 * @return The list of options
	 */
	public String[] getOptions();
	
	// === Setters =============================================================
	
	/**
	 * Sets the selected attribute
	 * 
	 * @throws IllegalArgumentException If
	 *         <code>s &lt; 0 || s &gt;= options.length</code>
	 */
	public void setSelected(int s);
	
}
