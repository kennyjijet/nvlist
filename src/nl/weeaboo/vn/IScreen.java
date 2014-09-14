package nl.weeaboo.vn;

import java.io.Serializable;

public interface IScreen extends Serializable {

	/**
	 * Creates a new layer and adds it to {@code parentLayer}.
	 *
	 * @throws IllegalArgumentException If {@code parentLayer} isn't attached to this screen.
	 */
	public ILayer createLayer(ILayer parentLayer);

	/**
	 * @return The root layer of this screen.
	 */
	public ILayer getRootLayer();

	/**
	 * @return The current default layer for new drawables that are added to this screen.
	 */
	public ILayer getActiveLayer();

}
