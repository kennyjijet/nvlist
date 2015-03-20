package nl.weeaboo.vn.awt;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import nl.weeaboo.common.Area2D;
import nl.weeaboo.common.Rect2D;
import nl.weeaboo.vn.BlendMode;
import nl.weeaboo.vn.image.ITexture;
import nl.weeaboo.vn.math.Matrix;
import nl.weeaboo.vn.math.Polygon;
import nl.weeaboo.vn.render.RenderUtil;
import nl.weeaboo.vn.render.impl.AbstractFadeQuadRenderer;
import nl.weeaboo.vn.render.impl.QuadRenderCommand;

class AwtFadeQuadRenderer extends AbstractFadeQuadRenderer {

    private final AwtRenderer renderer;

    private final double[] xCoords = new double[4];
    private final double[] yCoords = new double[4];
    private final double[] uCoords = new double[4];
    private final double[] vCoords = new double[4];

    public AwtFadeQuadRenderer(AwtRenderer r) {
        renderer = r;
    }

    @Override
    protected void renderTriangleStrip(ITexture tex, Matrix transform, FloatBuffer vertices,
            FloatBuffer texcoords, IntBuffer colors, int count) {

        int cols = count / 2;

        for (int col = 0; col < cols-1; col++) {
            for (int n = 0; n < 4; n++) {
                xCoords[n] = vertices.get(4 * col + 2 * n);
                yCoords[n] = vertices.get(4 * col + 2 * n + 1);
            }
            Rect2D r = Polygon.calculateBounds(xCoords, yCoords);
            Area2D bounds = new Area2D(Math.floor(r.x), Math.floor(r.y), Math.max(1, r.w), Math.max(1, r.h));

            for (int n = 0; n < 4; n++) {
                uCoords[n] = texcoords.get(4 * col + 2 * n);
                vCoords[n] = texcoords.get(4 * col + 2 * n + 1);
            }
            Area2D uv = Polygon.calculateBounds(uCoords, vCoords).toArea2D();

            int colorARGB = RenderUtil.unPremultiplyAlpha(RenderUtil.toARGB(colors.get(4 * col)));

            renderer.renderQuad(new QuadRenderCommand(Short.MAX_VALUE, true, BlendMode.DEFAULT, colorARGB,
                    tex, transform, bounds, uv));
        }
    }

}
