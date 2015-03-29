package nl.weeaboo.vn.render.impl;

import nl.weeaboo.common.Area2D;
import nl.weeaboo.common.Checks;
import nl.weeaboo.common.Rect2D;
import nl.weeaboo.gl.GLBlendMode;
import nl.weeaboo.gl.GLDraw;
import nl.weeaboo.gl.GLManager;
import nl.weeaboo.gl.SpriteBatch;
import nl.weeaboo.gl.tex.GLTexture;
import nl.weeaboo.vn.AlignUtil;
import nl.weeaboo.vn.BlendMode;
import nl.weeaboo.vn.IRenderEnv;
import nl.weeaboo.vn.image.ITexture;
import nl.weeaboo.vn.image.impl.TextureAdapter;

public abstract class GLRenderer extends BaseRenderer {

    protected final GLManager glm;

    //--- Properties only valid between renderBegin() and renderEnd() beneath this line ---
    protected GLDraw glDraw;
    private int buffered;
    private TextureAdapter quadTexture;
    private final SpriteBatch quadBatch = new SpriteBatch(1024);
    private final float[] tempFloat = new float[8]; //Temporary var
    //-------------------------------------------------------------------------------------

    public GLRenderer(GLManager glm, IRenderEnv env, RenderStats stats) {
        super(env, stats);

        this.glm = Checks.checkNotNull(glm);
    }

    @Override
    public void renderBegin() {
        glDraw = glm.getGLDraw();

        glDraw.pushMatrix();
        glDraw.setTexture(null);
        quadBatch.init(glm);

        buffered = 0;
        quadTexture = null;

        super.renderBegin();
    }

    @Override
    public void renderEnd() {
        quadTexture = null;

        glDraw.setTexture(null, true);
        glDraw.popMatrix();

        super.renderEnd();
    }

    @Override
    public void renderQuad(QuadRenderCommand qrc) {
        setTexture(qrc.tex);

        double u = qrc.uv.x;
        double v = qrc.uv.y;
        double uw = qrc.uv.w;
        double vh = qrc.uv.h;
        if (qrc.tex != null) {
            TextureAdapter ta = (TextureAdapter)qrc.tex;
            if (ta.glId() != 0) {
                Area2D texUV = ta.getUV();
                u  = texUV.x + u * texUV.w;
                v  = texUV.y + v * texUV.h;
                uw = texUV.w * uw;
                vh = texUV.h * vh;
            }
        }

        double x = qrc.bounds.x;
        double y = qrc.bounds.y;
        double w = qrc.bounds.w;
        double h = qrc.bounds.h;

        quadBatch.setColor(glDraw.getColor());
        if (qrc.transform.hasShear()) {
            // Slow path
            tempFloat[0] = tempFloat[6] = (float)(x  );
            tempFloat[2] = tempFloat[4] = (float)(x+w);
            tempFloat[1] = tempFloat[3] = (float)(y  );
            tempFloat[5] = tempFloat[7] = (float)(y+h);
            qrc.transform.transform(tempFloat, 0, 8);

            quadBatch.draw(tempFloat, (float)u, (float)v, (float)uw, (float)vh);
        } else {
            // Optimized path for scale+translate transforms
            double sx = qrc.transform.getScaleX();
            double sy = qrc.transform.getScaleY();
            x = x * sx + qrc.transform.getTranslationX();
            y = y * sy + qrc.transform.getTranslationY();
            w = w * sx;
            h = h * sy;

            quadBatch.draw((float)x, (float)y, (float)w, (float)h, (float)u, (float)v, (float)uw, (float)vh);
        }

        buffered++;
        if (quadBatch.getRemaining() <= 0) {
            flushQuadBatch();
        }
    }

    @Override
    public void renderBlendQuad(BlendQuadCommand bqc) {
        // TODO LVN-019 Support this properly
        Rect2D bounds0 = AlignUtil.getAlignedBounds(bqc.tex0, bqc.alignX0, bqc.alignY0);
        renderQuad(new QuadRenderCommand(bqc.z, bqc.clipEnabled, bqc.blendMode, bqc.argb,
                bqc.tex0, bqc.transform, bounds0.toArea2D(), bqc.uv));
    }

    @Override
    public void renderFadeQuad(FadeQuadCommand fqc) {
        // TODO LVN-019 Support this properly
        renderQuad(new QuadRenderCommand(fqc.z, fqc.clipEnabled, fqc.blendMode, fqc.argb,
                fqc.tex, fqc.transform, fqc.bounds, fqc.uv));
    }

    @Override
    public void renderDistortQuad(DistortQuadCommand dqc) {
        // TODO LVN-019 Support this properly
        renderQuad(new QuadRenderCommand(dqc.z, dqc.clipEnabled, dqc.blendMode, dqc.argb,
                dqc.tex, dqc.transform, dqc.bounds, dqc.uv));
    }

    @Override
    protected boolean renderUnknownCommand(RenderCommand cmd) {
        return false;
    }

    @Override
    protected void flushQuadBatch() {
        if (buffered == 0) {
            return;
        }

        GLTexture qtex = (quadTexture != null ? quadTexture.getTexture() : null);
        GLTexture cur = glDraw.getTexture();
        if (qtex != cur) {
            qtex = qtex.glTryLoad(glm);
            glDraw.setTexture(qtex);
            quadBatch.flush(glm);
            glDraw.setTexture(cur);
        } else {
            quadBatch.flush(glm);
        }

        renderStats.onRenderQuadBatch(buffered);
        buffered = 0;
        quadTexture = null;
    }

    protected void setTexture(ITexture tex) {
        TextureAdapter ta = (TextureAdapter)tex;
        if (quadTexture != tex && (quadTexture == null || ta == null || quadTexture.glId() != ta.glId())) {
            flushQuadBatch();
        }

        quadTexture = ta;
        if (ta != null) {
            ta.glTryLoad(glm);
            glDraw.setTexture(ta.getTexture());
        } else {
            glDraw.setTexture(null);
        }
    }

    @Override
    protected void setColor(int argb) {
        glDraw.setColor(argb);
    }

    @Override
    protected void setBlendMode(BlendMode bm) {
        switch (bm) {
        case DEFAULT:
            glDraw.setBlendMode(GLBlendMode.DEFAULT);
            break;
        case ADD:
            glDraw.setBlendMode(GLBlendMode.ADD);
            break;
        default:
            glDraw.setBlendMode(null);
            break;
        }
    }

    @Override
    protected void translate(double dx, double dy) {
        glDraw.translate(dx, dy);
    }

}
