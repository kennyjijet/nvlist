package nl.weeaboo.vn;

public interface IShaderFactory {

	// === Functions ===========================================================
	
	/**
	 * @return A newly created IPixelShader which runs on the GPU.
	 */
	public IPixelShader createGLSLShader(String filename);
	
	// === Getters =============================================================

	/**
	 * @return The maximum supported OpenGL Shading Language version.
	 */
	public String getGLSLVersion();
	
	/**
	 * @return <code>true</code> if the specified GLSL version is supported.
	 */
	public boolean isGLSLVersionSupported(String version);
	
	// === Setters =============================================================
	
}
