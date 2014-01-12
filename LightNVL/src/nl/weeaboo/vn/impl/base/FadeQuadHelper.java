package nl.weeaboo.vn.impl.base;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import nl.weeaboo.common.Area2D;
import nl.weeaboo.vn.IInterpolator;
import nl.weeaboo.vn.IPixelShader;
import nl.weeaboo.vn.ITexture;
import nl.weeaboo.vn.math.Matrix;

public abstract class FadeQuadHelper {

	private static final boolean RENDER_TEST = false;
	private static final int MAX_SEGMENTS = 128;

	protected final BaseRenderer renderer;
	private final IInterpolator interpolator;	
	
    private FloatBuffer vs;
    private FloatBuffer ts;
    private IntBuffer cs;
    private int segments;
	
	public FadeQuadHelper(BaseRenderer r) {
		renderer = r;
		interpolator = LUTInterpolator.fromInterpolator(Interpolators.BUTTERWORTH, 256);

    	ByteBuffer vertsB = ByteBuffer.allocateDirect((8+MAX_SEGMENTS*2) * 2 * 4);
    	vertsB.order(ByteOrder.nativeOrder());
        vs = vertsB.asFloatBuffer();

        ByteBuffer texCoordsB = ByteBuffer.allocateDirect((8+MAX_SEGMENTS*2) * 2 * 4);
    	texCoordsB.order(ByteOrder.nativeOrder());
        ts = texCoordsB.asFloatBuffer();

    	ByteBuffer colorsB = ByteBuffer.allocateDirect((8+MAX_SEGMENTS*2) * 1 * 4);
    	colorsB.order(ByteOrder.LITTLE_ENDIAN); //Colors are R G B A bytes
        cs = colorsB.asIntBuffer();
	}
	
	//Functions
	public void renderFadeQuad(ITexture tex, Matrix transform, int color0, int color1,
			Area2D bounds, Area2D uv, IPixelShader ps,
			int dir, boolean fadeIn, double span, double frac)
	{
		double a, b;
		frac = frac * (1.0 + span) - span; //Stretch frac to (-span, 1)
		if (dir == 2 || dir == 6) {
			a = frac;
		} else {
			a = 1.0 - frac - span;
		}
		b = a + span;
	
		if (!fadeIn ^ (dir == 8 || dir == 4)) {
			int temp = color0;
			color0 = color1;
			color1 = temp;
		}
		
		if (RENDER_TEST) {
			tex = null;
			uv = new Area2D(0, 0, 1, 1);
		} else {
			uv = BaseRenderer.combineUV(uv, tex.getUV());
		}

		boolean horizontal = (dir == 4 || dir == 6);
		setupTriangleStrip(bounds, uv, horizontal,
				(float)a, (float)b,
				premultiplyAlpha(color0), premultiplyAlpha(color1),
				interpolator);
		
		renderTriangleStrip(tex, transform, ps, vs, ts, cs, 8+2*segments);	
	}
	
	protected abstract void renderTriangleStrip(ITexture tex, Matrix transform, IPixelShader ps,
			FloatBuffer vertices, FloatBuffer texcoords, IntBuffer colors, int count);
	
    protected void setupTriangleStrip(Area2D bounds, Area2D uv, boolean horizontal, float start, float end,
    		int premultColor0, int premultColor1, IInterpolator interpolator)
    {
    	if (RENDER_TEST) {
    		premultColor0 = 0xFFFFFF00;
    		premultColor1 = 0xFF00FF00;	    		
    	}
    		    	
    	if (start < 0) {
    		premultColor0 = interpolateColor(premultColor0, premultColor1, (end-0)/(end-start));
    	}
    	if (end > 1) {
    		premultColor1 = interpolateColor(premultColor0, premultColor1, (end-1)/(end-start));
    	}
    	
    	start = Math.max(0f, Math.min(1f, start));
    	end = Math.max(0f, Math.min(1f, end));
    	
		float x0 = (float)(bounds.x);
		float x1 = (float)(bounds.x+bounds.w);	    		
		float y0 = (float)(bounds.y);
		float y1 = (float)(bounds.y+bounds.h);	    		

    	float u0 = (float)(uv.x);
    	float v0 = (float)(uv.y);
    	float u1 = (float)(uv.x+uv.w);
    	float v1 = (float)(uv.y+uv.h);
		
    	float uva, uvb, posa, posb;
    	if (horizontal) {
    		uva = (float)(uv.x + start * uv.w);
    		uvb = (float)(uv.x + end * uv.w);
    		posa = (float)(bounds.x + start * bounds.w);
    		posb = (float)(bounds.x + end * bounds.w);
    	} else {
    		uva = (float)(uv.y + start * uv.h);
    		uvb = (float)(uv.y + end * uv.h);
    		posa = (float)(bounds.y + start * bounds.h);
    		posb = (float)(bounds.y + end * bounds.h);
    	}
    		    	
    	//Colors must be in ABGR for OpenGL
    	int rgba0 = toRGBA(premultColor0);
    	int rgba1 = toRGBA(premultColor1);
    	
    	if (RENDER_TEST) {
    		rgba0 = 0xFFFF0000;
    		rgba1 = 0xFF0000FF;
    	}
    	
		segments = Math.min(MAX_SEGMENTS, (int)(posb-posa));
		
    	{
    		//Generate vertices	    		
    		if (horizontal) {
        		//System.out.printf("(%.0f, %.0f, %.0f, %.0f) (%.1f, %.1f, %.1f, %.1f)\n", x0, posa, posb, x1, u0, uva, uvb, u1);	    			
	    		vs.put(x0);   vs.put(y0);   ts.put(u0);  ts.put(v0);  cs.put(rgba0);
	    		vs.put(x0);   vs.put(y1);   ts.put(u0);  ts.put(v1);  cs.put(rgba0);
	    		vs.put(posa); vs.put(y0);   ts.put(uva); ts.put(v0);  cs.put(rgba0);
	    		vs.put(posa); vs.put(y1);   ts.put(uva); ts.put(v1);  cs.put(rgba0);
    		} else {
        		//System.out.printf("(%.0f, %.0f, %.0f, %.0f) (%.1f, %.1f, %.1f, %.1f)\n", y0, posa, posb, y1, v0, uva, uvb, v1);	    			
	    		vs.put(x0);   vs.put(y0);   ts.put(u0);  ts.put(v0);  cs.put(rgba0);
	    		vs.put(x1);   vs.put(y0);   ts.put(u1);  ts.put(v0);  cs.put(rgba0);
	    		vs.put(x0);   vs.put(posa); ts.put(u0);  ts.put(uva); cs.put(rgba0);
	    		vs.put(x1);   vs.put(posa); ts.put(u1);  ts.put(uva); cs.put(rgba0);
    		}
    		
    		for (int n = 0; n < segments; n++) {
    			float f = (n+.5f) / segments;
    			float pos = posa + (posb-posa) * f;
    			float uvpos = uva + (uvb-uva) * f;
    			int c = toRGBA(interpolateColor(premultColor0, premultColor1, interpolator.remap(1-f)));
    			
	    		if (horizontal) {
		    		vs.put(pos); vs.put(y0); ts.put(uvpos); ts.put(v0); cs.put(c);		    		
		    		vs.put(pos); vs.put(y1); ts.put(uvpos); ts.put(v1); cs.put(c);
	    		} else {
		    		vs.put(x0); vs.put(pos); ts.put(u0); ts.put(uvpos); cs.put(c);
		    		vs.put(x1); vs.put(pos); ts.put(u1); ts.put(uvpos); cs.put(c);
	    		}
    		}
    		
    		if (horizontal) {
	    		vs.put(posb); vs.put(y0);   ts.put(uvb);  ts.put(v0);   cs.put(rgba1);
	    		vs.put(posb); vs.put(y1);   ts.put(uvb);  ts.put(v1);   cs.put(rgba1);
	    		vs.put(x1);   vs.put(y0);   ts.put(u1);   ts.put(v0);   cs.put(rgba1);
	    		vs.put(x1);   vs.put(y1);   ts.put(u1);   ts.put(v1);   cs.put(rgba1);
    		} else {
	    		vs.put(x0);   vs.put(posb); ts.put(u0);   ts.put(uvb);  cs.put(rgba1);
	    		vs.put(x1);   vs.put(posb); ts.put(u1);   ts.put(uvb);  cs.put(rgba1);
	    		vs.put(x0);   vs.put(y1);   ts.put(u0);   ts.put(v1);   cs.put(rgba1);
	    		vs.put(x1);   vs.put(y1);   ts.put(u1);   ts.put(v1);   cs.put(rgba1);
    		}	    		
    	}
    	
    	vs.rewind();
    	ts.rewind();
    	cs.rewind();
    }	    
	
    protected static int interpolateColor(int c0, int c1, float w) {
    	if (w >= 1) return c0;
    	if (w <= 0) return c1;
    	
    	int a = mix((c0>>24) & 0xFF, (c1>>24) & 0xFF, w);
    	int r = mix((c0>>16) & 0xFF, (c1>>16) & 0xFF, w);
    	int g = mix((c0>>8 ) & 0xFF, (c1>>8 ) & 0xFF, w);
    	int b = mix((c0    ) & 0xFF, (c1    ) & 0xFF, w);
    	return (a<<24)|(r<<16)|(g<<8)|(b);
    }
    
    private static int mix(int a, int b, float w) {
    	return Math.max(0, Math.min(255, Math.round(b - (b-a) * w)));
    }
    
    protected static int premultiplyAlpha(int argb) {
		int a = (argb >> 24) & 0xFF;
		int r = Math.max(0, Math.min(255, a * ((argb>>16)&0xFF) / 255));
		int g = Math.max(0, Math.min(255, a * ((argb>> 8)&0xFF) / 255));
		int b = Math.max(0, Math.min(255, a * ((argb    )&0xFF) / 255));
		return (a<<24)|(r<<16)|(g<<8)|(b);
	}
    
    protected static int toRGBA(int argb) {
    	return (argb&0xFF000000) | ((argb<<16)&0xFF0000) | (argb&0xFF00) | ((argb>>16)&0xFF);
    }
	    
	//Getters
	
	//Setters
    
}
