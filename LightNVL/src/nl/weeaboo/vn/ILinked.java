package nl.weeaboo.vn;

public interface ILinked {

	/**
	 * Clears all tag -&gt; link associations.
	 */
	public void clearLinks();
	
	/**
	 * @return An array (possibly with length <code>0</code>) containing all
	 *         tags that have been pressed since the last call to this function.
	 */
	public int[] consumePressedLinks();

	/**
	 * @return The link associated with the specified tag.
	 */
	public String getLink(int tag);
	
	/**
	 * Registers a link with the specified tag. Only one link can be associated
	 * with a specific tag at one time.
	 * 
	 * @return The link previously associated with <code>tag</code>, or
	 *         <code>null</code> if none exists.
	 */
	public String setLink(int tag, String link);
	
}
