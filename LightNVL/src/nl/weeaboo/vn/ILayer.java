package nl.weeaboo.vn;

import java.util.Collection;
import java.util.EmptyStackException;

import nl.weeaboo.vn.impl.base.ScreenshotBuffer;

public interface ILayer extends IDrawable {

	//Functions
	/**
	 * @param d The new drawable to add
	 * @throws IllegalArgumentException If <code>d</code> is <code>null</code>
	 */
	public void add(IDrawable d);
	
	/**
	 * Removes all drawables from this layer.
	 */
	public void clearContents();

	/**
	 * @see #push(short)
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
	 * @return All layers contained inside this layer. If
	 *         <code>recursive == true</code>, also returns all layers contained
	 *         in sub-layers.
	 */
	public Collection<ILayer> getSubLayers(boolean recursive);
	
	/**
	 * @return All active drawables contained in the layer.
	 */
	public IDrawable[] getContents();
	
	/**
	 * @return <code>out</code>, or if it's <code>null</code> or too small, a
	 *         new array containing all active images.
	 * @see #getContents()
	 */
	public IDrawable[] getContents(IDrawable[] out);
	
	/**
	 * Checks whether the specified drawable is contained in the top stack
	 * element of this imagestate.
	 * @param d The drawable to search for.
	 * @return <code>true</code> if the drawable is found
	 */
	public boolean contains(IDrawable d);
	
	/**
	 * @return A buffer for pending screenshots. The screenshots will be
	 *         scheduled during the next call to {@link #draw(IRenderer)}.
	 */
	public ScreenshotBuffer getScreenshotBuffer();
	
	//Setters
	
}
