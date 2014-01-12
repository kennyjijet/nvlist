package nl.weeaboo.vn;

import java.util.EmptyStackException;

public interface ILayer extends IDrawable {

	//Functions
	/**
	 * Adds <code>d</code> to this layer and removes it from its old layer. When
	 * this layer is destroyed or the drawable ever becomes unassociated with
	 * any layer, the drawable is destroyed.
	 * 
	 * @param d The new drawable to add
	 * @throws IllegalArgumentException If <code>d</code> is <code>null</code>
	 */
	public void add(IDrawable d);
	
	/**
	 * Removes all drawables from this layer.
	 */
	public void clearContents();

	/**
	 * @see #pushContents(short)
	 */
	public void pushContents();
	
	/**
	 * Pushes a copy of the layer contents on the stack. Call {@link #popContents()}
	 * later to restore.
	 */
	public void pushContents(short z);

	/**
	 * Restores a layer's contents as previously saved with {@link #pushContents()}.
	 * 
	 * @throws EmptyStackException
	 */
	public void popContents() throws EmptyStackException;
		
	//Getters	
	/**
	 * @return All active drawables contained in the layer.
	 */
	public IDrawable[] getContents();
	
	/**
	 * @return <code>out</code>, or if it's <code>null</code> or too small, a
	 *         new array containing all active drawables in this layer.
	 * @see #getContents()
	 */
	public IDrawable[] getContents(IDrawable[] out);
	
	/**
	 * @return All active drawables contained in this layer and all of its
	 *         sub-layers.
	 */
	public IDrawable[] getContentsRecursive();
	
	/**
	 * Checks whether the specified drawable is contained in the top stack
	 * element of this imagestate.
	 * @param d The drawable to search for.
	 * @return <code>true</code> if the drawable is found
	 */
	public boolean contains(IDrawable d);
	
	/**
	 * Similar to {@link #contains(double, double)}, but in coordinates relative
	 * to the layer origin.
	 * 
	 * @see #contains(double, double)
	 */
	public boolean containsRel(double x, double y);
	
	/**
	 * @return A buffer for pending screenshots. The screenshots will be
	 *         scheduled during the next call to {@link #draw(IDrawBuffer)}.
	 */
	public IScreenshotBuffer getScreenshotBuffer();
	
	//Setters
	
}
