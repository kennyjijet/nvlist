package nl.weeaboo.vn.impl.nvlist;


/*
public class RotatedQuadCommand extends CustomRenderCommand {

	private final ITexture itex;
	private final Matrix transform;
	private final double x, y, w, h;
	private final double rotX, rotY, rotZ;
	
	public RotatedQuadCommand(short z, boolean clipEnabled, BlendMode blendMode,
			int argb, ITexture tex, Matrix trans, double x, double y, double w, double h,
			IPixelShader ps, double rotX, double rotY, double rotZ)
	{
		super(z, clipEnabled, blendMode, argb, ps, (byte)0);
		
		this.itex = tex;
		this.transform = trans;
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
		this.rotX = rotX;
		this.rotY = rotY;
		this.rotZ = rotZ;
	}

	//Functions
	@Override
	protected void renderGeometry(IRenderer r) {
		Renderer rr = (Renderer)r;		
		GLManager glm = rr.getGLManager();
		GL2ES1 gl = glm.getGL();
		
		boolean lightingWasEnabled = gl.glIsEnabled(GL2ES1.GL_LIGHTING);
		//boolean depthWasEnabled = gl.glIsEnabled(GL2ES1.GL_DEPTH_TEST);
		
		gl.glEnable(GL2ES1.GL_LIGHTING);
		//gl.glEnable(GL2ES1.GL_DEPTH_TEST);		
		
		gl.glPushMatrix();

		glm.translate(transform.getTranslationX(), transform.getTranslationY());		
		glm.translate((x+w/2) * transform.getScaleX(), (y+h/2) * transform.getScaleY());
		gl.glRotatef((float)rotZ, 0, 0, 1);
		gl.glRotatef((float)rotY, 0, 1, 0);
		gl.glRotatef((float)rotX, 1, 0, 0);
		
		MutableMatrix m = transform.mutableCopy();
		m.setTranslation(0, 0);
		gl.glMultMatrixf(m.toGLMatrix(), 0);
		
		rr.renderQuad(itex, Matrix.identityMatrix(),
				-w/2, -h/2, w, h, null, 0, 0, 1, 1);
		
		gl.glPopMatrix();
				
		if (!lightingWasEnabled) gl.glDisable(GL2ES1.GL_LIGHTING);
		//if (!depthWasEnabled) gl.glDisable(GL2ES1.GL_DEPTH_TEST);
	}
	
	//Getters
	
	//Setters
	
}
*/