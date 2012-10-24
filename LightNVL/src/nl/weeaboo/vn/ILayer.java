package nl.weeaboo.vn;

import java.io.Serializable;
import java.util.EmptyStackException;

import nl.weeaboo.common.Rect2D;
import nl.weeaboo.vn.impl.base.ScreenshotBuffer;

public interface ILayer extends Serializable {

	//Functions
	/**
	 * @param d The new drawable to add
	 * @throws IllegalArgumentException If <code>d</code> is <code>null</code>
	 */
	public void add(IDrawable d);
	
	/**
	 * Removes all objects from this layer.
	 */
	public void clear();
	
	/**
	 * Destroys the layer and all drawables it contains.
	 */
	public void destroy();

	/**
	 * @see #push(short)
	 */
	public void push();
	
	/**
	 * Saves a copy of the layer's current state. Call {@link #pop()} later to
	 * restore.
	 */
	public void push(short z);

	/**
	 * Restores a layer's state previously saved with {@link #push()}.
	 * @throws EmptyStackException
	 */
	public void pop() throws EmptyStackException;
	
	/**
	 * Updates the image state and all drawables it contains
	 * 
	 * @param input The current user input
	 * @param effectSpeed The suggested relative animation speed
	 * @return <code>true</code> if the state changed since the last call to
	 *         this function.
	 */
	public boolean update(IInput input, double effectSpeed);
	
	/**
	 * Draws the image state and all drawables it contains
	 * 
	 * @param renderer The renderer to use for drawing
	 */
	public void draw(IRenderer renderer);
	
	//Getters
	/**
	 * @return An array containing all active images
	 */
	public IDrawable[] getDrawables();
	
	/**
	 * @return <code>out</code>, or if it's <code>null</code> or too small, a
	 *         new array containing all active images.
	 */
	public IDrawable[] getDrawables(IDrawable[] out);
	
	/**
	 * Checks whether the specified drawable is contained in the top stack
	 * element of this imagestate.
	 * @param d The drawable to search for.
	 * @return <code>true</code> if the drawable is found
	 */
	public boolean contains(IDrawable d);

	/**
	 * Checks if the specified coordinates lie within the layer's bounds.
	 */
	public boolean contains(double x, double y);
	
	/**
	 * @return A buffer for pending screenshots. The screenshots will be
	 *         scheduled during the next call to {@link #draw(IRenderer)}.
	 */
	public ScreenshotBuffer getScreenshotBuffer();
	
	/**
	 * @return <code>true</code> if {@link #destroy()} has been called.
	 */
	public boolean isDestroyed();
	
	public double getX();
	public double getY();
	public double getWidth();
	public double getHeight();
	public Rect2D getBounds();
	public short getZ();
	public boolean isVisible();
	
	//Setters
	public void setZ(short z);
	public void setPos(double x, double y);
	public void setSize(double w, double h);
	public void setBounds(double x, double y, double w, double h);
	public void setVisible(boolean v);
	
}
