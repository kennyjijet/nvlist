package nl.weeaboo.vn.awt;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;

import nl.weeaboo.common.Rect;
import nl.weeaboo.common.Rect2D;
import nl.weeaboo.vn.AlignUtil;
import nl.weeaboo.vn.BlendMode;
import nl.weeaboo.vn.IRenderEnv;
import nl.weeaboo.vn.IScreenshot;
import nl.weeaboo.vn.ITexture;
import nl.weeaboo.vn.math.Matrix;
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

	private final BufferedImage blankImage;

	private BufferedImage image;
	private Graphics2D graphics;
	private AffineTransform screenSpaceTransform;

	private int color;
	private Rect glClipRect;
	private boolean clipEnabled;

	public AwtRenderer(IRenderEnv env, RenderStats stats) {
		super(env, stats);

		blankImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		blankImage.setRGB(0, 0, 0xFFFFFFFF);
	}

	@Override
	protected void renderBegin() {
		int sw = env.getScreenWidth();
		int sh = env.getScreenHeight();
		if (image == null || image.getWidth() != sw || image.getHeight() != sh) {
			image = new BufferedImage(sw, sh, BufferedImage.TYPE_INT_ARGB);
			graphics = image.createGraphics();
			screenSpaceTransform = graphics.getTransform();
		}

		graphics.setBackground(Color.BLACK);
		graphics.clearRect(0, 0, sw, sh);

		color = 0xFFFFFFFF;
		glClipRect = env.getGLClip();
		clipEnabled = true;
		applyClip();

		graphics.setTransform(createBaseTransform(env));
		graphics.setColor(new Color(16, 16, 16));
		graphics.fillRect(0, 0, env.getWidth(), env.getHeight());

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
		BufferedImage image = getImage(qrc.tex);

		if (color != 0xFFFFFFFF) {
			RescaleOp op = new RescaleOp(getRGBA(), new float[] {0, 0, 0, 0}, null);
			image = op.filter(image, null);
		}

		AffineTransform oldTransform = graphics.getTransform();
		if (qrc.transform != Matrix.identityMatrix()) {
			graphics.transform(createTransform(qrc.transform));
		}

		int x = (int)Math.round(qrc.bounds.x);
		int y = (int)Math.round(qrc.bounds.y);
		int w = (int)Math.round(qrc.bounds.w);
		int h = (int)Math.round(qrc.bounds.h);

		int uvx = (int)Math.round(image.getWidth() * qrc.uv.x);
		int uvy = (int)Math.round(image.getHeight() * qrc.uv.y);
		int uvw = (int)Math.round(image.getWidth() * qrc.uv.w);
		int uvh = (int)Math.round(image.getHeight() * qrc.uv.h);

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
		// TODO Support this properly instead of just rendering a regular quad
		renderQuad(new QuadRenderCommand(fqc.z, fqc.clipEnabled, fqc.blendMode, fqc.argb,
				fqc.tex, fqc.transform, fqc.bounds, fqc.uv));
	}

	@Override
	public void renderDistortQuad(DistortQuadCommand dqc) {
		LOG.warn("Unsupported operation: renderDistortQuad");
	}

	@Override
	public void renderTriangleGrid(TriangleGrid grid) {
		LOG.warn("Unsupported operation: renderTriangleGrid");
	}

	@Override
	public void renderScreenshot(IScreenshot out, Rect glScreenRect) {
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
			graphics.setClip(glClipRect.x, env.getScreenHeight() - glClipRect.y - glClipRect.h, glClipRect.w, glClipRect.h);
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
		switch (bm) {
		case ADD: graphics.setComposite(AdditiveComposite.INSTANCE); break;
		case OPAQUE: graphics.setComposite(AlphaComposite.Src); break;
		default: graphics.setComposite(AlphaComposite.SrcOver); break;
		}
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

	private float[] getRGBA() {
		return new float[] {
			((color>>16)&0xFF) / 255f,
			((color>>8 )&0xFF) / 255f,
			((color    )&0xFF) / 255f,
			((color>>24)&0xFF) / 255f
		};
	}

	BufferedImage getRenderBuffer() {
		return image;
	}

}
