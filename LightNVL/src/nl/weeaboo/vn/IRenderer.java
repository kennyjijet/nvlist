package nl.weeaboo.vn;


public interface IRenderer {
	
	// === Functions ===========================================================
	/**
	 * Resets all properties.
	 */
	public void reset();
		
	/**
	 * Renders all buffered draw commands.
	 * @param layer The layer to render (also renders all sub-layers).
	 * @param buffer A draw buffer containing the draw commands to render.
	 */
	public void render(ILayer layer, IDrawBuffer buffer);

	// === Getters =============================================================
	
	public RenderEnv getEnv();
	public IDrawBuffer getDrawBuffer();
	
	// === Setters =============================================================
	
}
