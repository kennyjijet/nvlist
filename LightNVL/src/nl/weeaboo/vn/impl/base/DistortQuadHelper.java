package nl.weeaboo.vn.impl.base;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import nl.weeaboo.common.Area2D;
import nl.weeaboo.common.Rect2D;
import nl.weeaboo.vn.IDistortGrid;
import nl.weeaboo.vn.IPixelShader;
import nl.weeaboo.vn.ITexture;
import nl.weeaboo.vn.math.Matrix;

public abstract class DistortQuadHelper {

	private static boolean RENDER_TEST = false;
	
	protected final BaseRenderer renderer;
	
    private FloatBuffer vs;
    private FloatBuffer ts;
    private IntBuffer cs;
	
	public DistortQuadHelper(BaseRenderer r) {
		renderer = r;

    	ByteBuffer vertsB = ByteBuffer.allocateDirect((DistortGS.MAX_SEGMENTS*2) * 2 * 4);
    	vertsB.order(ByteOrder.nativeOrder());
        vs = vertsB.asFloatBuffer();

        ByteBuffer texCoordsB = ByteBuffer.allocateDirect((DistortGS.MAX_SEGMENTS*2) * 2 * 4);
    	texCoordsB.order(ByteOrder.nativeOrder());
        ts = texCoordsB.asFloatBuffer();

    	ByteBuffer colorsB = ByteBuffer.allocateDirect((DistortGS.MAX_SEGMENTS*2) * 1 * 4);
    	colorsB.order(ByteOrder.LITTLE_ENDIAN); //Colors are R G B A bytes
        cs = colorsB.asIntBuffer();
	}
	
	//Functions
	public void renderDistortQuad(ITexture tex, Matrix transform, int argb,
			double x, double y, double w, double h, IPixelShader ps,
			IDistortGrid grid, Rect2D clampBounds)
	{
		if (RENDER_TEST) {
			tex = null;
		}

		preRender(tex, transform);
		try {
			Area2D uv = (tex != null ? tex.getUV() : new Area2D(0, 0, 1, 1));
			for (int row = 0; row < grid.getHeight(); row++) {
				int count = setupTriangleStrip(grid, row, premultiplyAlpha(argb),
						(float)x, (float)y, (float)w, (float)h, uv, clampBounds);
				renderStrip(vs, ts, cs, count);
			}
		} finally {
			postRender();
		}
	}
	
	protected abstract void preRender(ITexture tex, Matrix transform);
	protected abstract void renderStrip(FloatBuffer vertices, FloatBuffer texcoords, IntBuffer colors, int count);
	protected abstract void postRender();

    protected int setupTriangleStrip(IDistortGrid grid, int row, int argb,
    		float x, float y, float w, float h, Area2D uv, Rect2D clampBounds)
    {
		float clx0, clx1, cly0, cly1;
		if (clampBounds != null) {
			clx0 = (float)(x + clampBounds.x);
			clx1 = (float)(x + clampBounds.x + clampBounds.w);
			cly0 = (float)(y + clampBounds.y);
			cly1 = (float)(y + clampBounds.y + clampBounds.h);
		} else {
			clx0 = x;
			clx1 = x + w;
			cly0 = y;
			cly1 = y + h;
		}
	
    	float g0 = 1f * (row    ) / grid.getHeight();
    	float g1 = 1f * (row + 1) / grid.getHeight();
    	
		float x0 = x;
		float x1 = x + w;	    		
		float y0 = mix(y, y+h, g0);
		float y1 = mix(y, y+h, g1);
		
    	float u0 = (float)(uv.x);
    	float u1 = (float)(uv.x+uv.w);
    	float v0 = mix((float)(uv.y), (float)(uv.y+uv.h), g0);
    	float v1 = mix((float)(uv.y), (float)(uv.y+uv.h), g1);
    	
    	int segments = Math.min(DistortGS.MAX_SEGMENTS, grid.getWidth()+1);

		for (int n = 0; n < segments; n++) {
			float f = 1f * n / (segments - 1);			
			float px = mix(x0, x1, f);
			float pu = mix(u0, u1, f);
			
			int rgba = toRGBA(argb);
			if (RENDER_TEST && ((n&1)^(row&1)) == 0) {
				rgba = 0xFF808080;
			}
			
    		vs.put(clamp(px + grid.getDistortX(n, row), clx0, clx1));
    		vs.put(clamp(y0 + grid.getDistortY(n, row), cly0, cly1));
    		ts.put(pu);
    		ts.put(v0);
    		cs.put(rgba);
    		
    		//System.out.println(n + "," + row + " :: " + grid.getDistortY(n, row) + " ~ " + grid.getDistortY(n, row+1));
    		
    		vs.put(clamp(px + grid.getDistortX(n, row+1), clx0, clx1));
    		vs.put(clamp(y1 + grid.getDistortY(n, row+1), cly0, cly1));
    		ts.put(pu);
    		ts.put(v1);
    		cs.put(rgba);
    	}
    	
    	vs.rewind();
    	ts.rewind();
    	cs.rewind();
    	
    	return segments * 2;
    }	    
	
    private static float clamp(float val, float min, float max) {
    	if (val < min) return min;
    	if (val > max) return max;
    	return val;
    }
    
    private static float mix(float a, float b, float w) {
    	float min, max;
    	if (a <= b) {
    		min = a;
    		max = b;
    	} else {
    		min = b;
    		max = a;
    	}
    	return Math.max(min, Math.min(max, a + (b-a) * w));
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
