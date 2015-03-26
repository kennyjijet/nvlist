package nl.weeaboo.vn.awt;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.nio.FloatBuffer;

import nl.weeaboo.common.Area2D;
import nl.weeaboo.common.Rect;
import nl.weeaboo.common.Rect2D;
import nl.weeaboo.vn.AlignUtil;
import nl.weeaboo.vn.BlendMode;
import nl.weeaboo.vn.IRenderEnv;
import nl.weeaboo.vn.image.ITexture;
import nl.weeaboo.vn.image.IWritableScreenshot;
import nl.weeaboo.vn.math.Matrix;
import nl.weeaboo.vn.math.Polygon;
import nl.weeaboo.vn.render.impl.BaseRenderer;
import nl.weeaboo.vn.render.impl.BlendQuadCommand;
import nl.weeaboo.vn.render.impl.DistortQuadCommand;
import nl.weeaboo.vn.render.impl.FadeQuadCommand;
import nl.weeaboo.vn.render.impl.QuadRenderCommand;
import nl.weeaboo.vn.render.impl.RenderCommand;
import nl.weeaboo.vn.render.impl.RenderStats;
import nl.weeaboo.vn.render.impl.TriangleGrid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AwtRenderer extends BaseRenderer {

	private static final Logger LOG = LoggerFactory.getLogger(AwtRenderer.class);

    private final AwtFadeQuadRenderer fadeQuadRenderer;
	private final BufferedImage blankImage;

	private BufferedImage renderBuffer;
	private Graphics2D graphics;
	private AffineTransform screenSpaceTransform;

	private int color;
	private Rect glClipRect;
	private boolean clipEnabled;
	private BlendMode blendMode;

	private BufferedImage tempColorizedImage;

	public AwtRenderer(IRenderEnv env, RenderStats stats) {
		super(env, stats);

		fadeQuadRenderer = new AwtFadeQuadRenderer(this);

		blankImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		blankImage.setRGB(0, 0, 0xFFFFFFFF);
	}

	@Override
	protected void renderBegin() {
		int sw = renderEnv.getScreenWidth();
		int sh = renderEnv.getScreenHeight();
		if (renderBuffer == null || renderBuffer.getWidth() != sw || renderBuffer.getHeight() != sh) {
			renderBuffer = new BufferedImage(sw, sh, BufferedImage.TYPE_INT_ARGB);
			graphics = renderBuffer.createGraphics();
			screenSpaceTransform = graphics.getTransform();
		}

	    graphics.setComposite(toComposite(BlendMode.DEFAULT));
		graphics.setBackground(Color.BLACK);
		graphics.clearRect(0, 0, sw, sh);

		color = 0xFFFFFFFF;
		glClipRect = renderEnv.getGLClip();
		clipEnabled = true;
		blendMode = BlendMode.DEFAULT;
		applyClip();

		graphics.setTransform(createBaseTransform(renderEnv));
		graphics.setColor(new Color(16, 16, 16));
		graphics.fillRect(0, 0, renderEnv.getWidth(), renderEnv.getHeight());

		super.renderBegin();
	}

	@Override
	protected void renderEnd() {
		super.renderEnd();
	}

	private static AffineTransform createBaseTransform(IRenderEnv env) {
		Rect glClip = env.getGLClip();

		AffineTransform transform = new AffineTransform();
		transform.translate(glClip.x, env.getScreenHeight() - glClip.y - glClip.h);
		transform.scale(env.getScale(), env.getScale());
		return transform;
	}

	private static AffineTransform createTransform(Matrix m) {
		return new AffineTransform(m.getScaleX(), m.getShearY(), m.getShearX(), m.getScaleY(),
				m.getTranslationX(), m.getTranslationY());
	}

	private BufferedImage getImage(ITexture tex) {
		if (tex instanceof AwtTexture) {
			return ((AwtTexture)tex).getImage();
		}
		return blankImage;
	}

	@Override
	public void renderQuad(QuadRenderCommand qrc) {
	    AwtTexture tex = (AwtTexture)qrc.tex;
	    Image image = getImage(tex);
		int iw = image.getWidth(null);
		int ih = image.getHeight(null);

		int alpha = (qrc.argb >>> 24);
		if (alpha == 0) {
		    return; // Transparent, nothing to draw
		}

		Composite composite = toComposite(blendMode);
		if (qrc.argb != 0xFFFFFFFF) {
		    if ((qrc.argb & 0xFFFFFF) == 0xFFFFFF && composite instanceof AlphaComposite) {
		        // Alpha-only blend
		        composite = ((AlphaComposite)composite).derive(alpha / 255f);
		    } else {
                image = createColorizedTempImage(tex, qrc.argb);
		    }
		}
		graphics.setComposite(composite);

		AffineTransform oldTransform = graphics.getTransform();
		if (qrc.transform != Matrix.identityMatrix()) {
			graphics.transform(createTransform(qrc.transform));
		}

		int x = (int)Math.round(qrc.bounds.x);
		int y = (int)Math.round(qrc.bounds.y);
		int w = (int)Math.round(qrc.bounds.w);
		int h = (int)Math.round(qrc.bounds.h);

		int uvx = (int)Math.round(iw * qrc.uv.x);
		int uvy = (int)Math.round(ih * qrc.uv.y);
		int uvw = (int)Math.round(iw * qrc.uv.w);
		int uvh = (int)Math.round(ih * qrc.uv.h);

		graphics.drawImage(image, x, y, x+w, y+h, uvx, uvy, uvx+uvw, uvy+uvh, null);

		graphics.setTransform(oldTransform);
	}

	@Override
	public void renderBlendQuad(BlendQuadCommand bqc) {
		// TODO Support this properly instead of just rendering a regular quad
		Rect2D bounds0 = AlignUtil.getAlignedBounds(bqc.tex0, bqc.alignX0, bqc.alignY0);
		renderQuad(new QuadRenderCommand(bqc.z, bqc.clipEnabled, bqc.blendMode, bqc.argb,
				bqc.tex0, bqc.transform, bounds0.toArea2D(), bqc.uv));
	}

	@Override
	public void renderFadeQuad(FadeQuadCommand fqc) {
	    int color0 = fqc.argb;
	    int color1 = fqc.argb & 0xFFFFFF;

	    fadeQuadRenderer.renderFadeQuad(fqc.tex, fqc.transform, color0, color1, fqc.bounds, fqc.uv,
	            fqc.dir, fqc.fadeIn, fqc.span, fqc.frac);
	}

	@Override
	public void renderDistortQuad(DistortQuadCommand dqc) {
		LOG.warn("Unsupported operation: renderDistortQuad");
	}

	@Override
	public void renderTriangleGrid(TriangleGrid grid) {
	    renderTriangleGrid(grid, new ITexture[grid.getTextures()], Matrix.identityMatrix());
	}

    public void renderTriangleGrid(TriangleGrid grid, ITexture[] textures, Matrix transform) {
        final int rows = grid.getRows();
        final int cols = grid.getCols();
        final int texCount = grid.getTextures();
        final int verticesPerRow = cols * 2;

        final int vertBytes = verticesPerRow * 2 * 4;
        final int texcoordBytes = verticesPerRow * 2 * 4;

        FloatBuffer posBuffer = FloatBuffer.allocate(vertBytes);
        FloatBuffer[] texBuffers = new FloatBuffer[texCount];
        for (int n = 0; n < texBuffers.length; n++) {
            texBuffers[n] = FloatBuffer.allocate(texcoordBytes);
        }

        double[] xCoords = new double[4];
        double[] yCoords = new double[4];
        double[] uCoords = new double[4];
        double[] vCoords = new double[4];
        for (int row = 0; row < rows; row++) {
            grid.getVertices(posBuffer, row);
            posBuffer.rewind();
            for (int t = 0; t < texCount; t++) {
                grid.getTexCoords(texBuffers[t], t, row);
                texBuffers[t].rewind();
            }

            for (int col = 0; col < cols-1; col++) {
                for (int n = 0; n < 4; n++) {
                    xCoords[n] = posBuffer.get(4 * col + 2 * n);
                    yCoords[n] = posBuffer.get(4 * col + 2 * n + 1);
                }
                Area2D r = Polygon.calculateBounds(xCoords, yCoords).toArea2D();

                for (int t = 0; t < texCount; t++) {
                    for (int n = 0; n < 4; n++) {
                        uCoords[n] = texBuffers[t].get(4 * col + 2 * n);
                        vCoords[n] = texBuffers[t].get(4 * col + 2 * n + 1);
                    }
                    Area2D uv = Polygon.calculateBounds(uCoords, vCoords).toArea2D();

                    renderQuad(new QuadRenderCommand(Short.MAX_VALUE, clipEnabled, blendMode, color,
                            textures[t], transform, r, uv));
                }
            }
        }
	}

	@Override
	public void renderScreenshot(IWritableScreenshot out, Rect glScreenRect) {
		LOG.warn("Unsupported operation: renderScreenshot");
	}

	@Override
	protected boolean renderUnknownCommand(RenderCommand cmd) {
		return false;
	}

	private void applyClip() {
		if (clipEnabled) {
			AffineTransform oldTransform = graphics.getTransform();
			graphics.setTransform(screenSpaceTransform);
			graphics.setClip(glClipRect.x, renderEnv.getScreenHeight() - glClipRect.y - glClipRect.h, glClipRect.w, glClipRect.h);
			graphics.setTransform(oldTransform);
		} else {
			graphics.setClip(null);
		}
	}

	@Override
	protected void setClip(boolean c) {
		clipEnabled = c;
		applyClip();
	}

	@Override
	protected void setColor(int argb) {
		color = argb;
	}

	@Override
	protected void setBlendMode(BlendMode bm) {
	    blendMode = bm;
	}

	@Override
	protected void setClipRect(Rect glRect) {
		glClipRect = glRect;
		applyClip();
	}

	@Override
	protected void translate(double dx, double dy) {
		graphics.translate(dx, dy);
	}

	BufferedImage getRenderBuffer() {
		return renderBuffer;
	}

	private Image createColorizedTempImage(AwtTexture tex, int argb) {
	    BufferedImage image = tex.getImage();
	    final int iw = image.getWidth();
	    final int ih = image.getHeight();
	    if (tempColorizedImage == null
	            || tempColorizedImage.getWidth() != iw || tempColorizedImage.getHeight() != ih)
	    {
	        tempColorizedImage = new BufferedImage(iw, ih, BufferedImage.TYPE_INT_ARGB);
	    }

	    int[] src = tex.getARGB();
	    int[] dst = ((DataBufferInt)tempColorizedImage.getRaster().getDataBuffer()).getData();

        int alpha = ((argb>>24) & 0xFF);
	    if ((argb & 0xFFFFFF) == 0xFFFFFF) {
	        // Alpha-only blend, white
            for (int n = 0; n < dst.length; n++) {
                dst[n] = ((alpha * (src[n]>>>8)) & 0xFF000000) | (src[n] & 0xFFFFFF);
            }
	    } else {
    	    for (int n = 0; n < dst.length; n++) {
                dst[n] = mixColors(src[n], argb);
    	    }
	    }

	    return tempColorizedImage;
	}

	private static Composite toComposite(BlendMode bm) {
        switch (bm) {
        case ADD: return AdditiveComposite.INSTANCE;
        case OPAQUE: return AlphaComposite.Src;
        default: return AlphaComposite.SrcOver;
        }
	}

    private static int mixColors(int c0, int c1) {
        // Divide by 256 for speed, although it's less accurate
        int a = ((c0>>24)&0xFF) * ((c1>>24)&0xFF) >> 8;
        int r = ((c0>>16)&0xFF) * ((c1>>16)&0xFF) >> 8;
        int g = ((c0>> 8)&0xFF) * ((c1>> 8)&0xFF) >> 8;
        int b = ((c0    )&0xFF) * ((c1    )&0xFF) >> 8;
        return (a<<24)|(r<<16)|(g<<8)|(b);
    }

}
