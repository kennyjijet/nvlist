package nl.weeaboo.vn.impl.base;

import java.util.Arrays;

import nl.weeaboo.collections.MergeSort;
import nl.weeaboo.common.Rect2D;
import nl.weeaboo.vn.BlendMode;
import nl.weeaboo.vn.IDistortGrid;
import nl.weeaboo.vn.IDrawBuffer;
import nl.weeaboo.vn.IGeometryShader;
import nl.weeaboo.vn.IImageDrawable;
import nl.weeaboo.vn.ILayer;
import nl.weeaboo.vn.IPixelShader;
import nl.weeaboo.vn.IScreenshot;
import nl.weeaboo.vn.ITexture;
import nl.weeaboo.vn.RenderEnv;
import nl.weeaboo.vn.layout.LayoutUtil;
import nl.weeaboo.vn.math.Matrix;
import nl.weeaboo.vn.math.Vec2;

public class BaseDrawBuffer implements IDrawBuffer {

	private final RenderEnv env;
	
	private ILayer[] layers;
	private int[] layerStarts;
	private int layersL;
	
	private BaseRenderCommand[] commands;
	private int commandsL;
	
	public BaseDrawBuffer(RenderEnv env) {
		this.env = env;
		
		reserveLayers(8);
		reserveCommands(64);
	}
	
	// === Functions ===========================================================
	public static BaseDrawBuffer cast(IDrawBuffer buf) {
		if (buf == null) return null;
		if (buf instanceof BaseDrawBuffer) return (BaseDrawBuffer)buf;
		throw new ClassCastException("Supplied draw buffer is of an invalid class: " + buf.getClass() + ", expected: " + BaseDrawBuffer.class);
	}
	
	private void reserveLayers(int minLength) {
		if (layersL >= minLength) {
			return;
		}
		
		ILayer[] newLayers = new ILayer[Math.max(minLength, layersL*2)];
		int[] newLayerStarts = new int[newLayers.length];
		if (layers != null) {
			System.arraycopy(layers, 0, newLayers, 0, layersL);
		}
		if (layerStarts != null) {
			System.arraycopy(layerStarts, 0, newLayerStarts, 0, layersL);
		}
		layers = newLayers;
		layerStarts = newLayerStarts;
	}
	
	private void reserveCommands(int minLength) {
		if (commandsL >= minLength) {
			return;
		}
		
		BaseRenderCommand[] newCommands = new BaseRenderCommand[Math.max(minLength, commandsL*2)];
		if (commands != null) {
			System.arraycopy(commands, 0, newCommands, 0, commandsL);
		}
		commands = newCommands;
	}
	
	@Override
	public void reset() {
		Arrays.fill(layers, 0, layersL, null);
		layersL = 0;
		
		Arrays.fill(commands, 0, commandsL, null);
		commandsL = 0;
	}
	
	@Override
	public void screenshot(IScreenshot ss, boolean clip) {
		draw(new ScreenshotRenderCommand(ss, clip));
	}
	
	@Override
	public void draw(IImageDrawable id) {
		drawWithTexture(id, id.getTexture(), id.getAlignX(), id.getAlignY(),
				id.getGeometryShader(), id.getPixelShader());
	}
	
	@Override
	public void drawWithTexture(IImageDrawable id, ITexture tex, double alignX, double alignY,
			IGeometryShader gs, IPixelShader ps)
	{		
		if (gs == null) {
			Vec2 offset = LayoutUtil.getImageOffset(tex, alignX, alignY);
			drawQuad(id.getZ(), id.isClipEnabled(), id.getBlendMode(), id.getColorARGB(),
					tex, id.getTransform(), offset.x, offset.y,
					id.getUnscaledWidth(), id.getUnscaledHeight(), ps);
		} else {
			gs.draw(this, id, tex, alignX, alignY, ps);
		}
	}
	
	@Override
	public void drawQuad(short z, boolean clipEnabled, BlendMode blendMode, int argb,
			ITexture tex, Matrix trans, double x, double y, double w, double h, IPixelShader ps)
	{
		drawQuad(z, clipEnabled, blendMode, argb, tex, trans, x, y, w, h, 0, 0, 1, 1, ps);
	}
	
	@Override
	public void drawQuad(short z, boolean clipEnabled, BlendMode blendMode, int argb,
			ITexture tex, Matrix trans, double x, double y, double w, double h,
			double u, double v, double uw, double vh, IPixelShader ps)
	{	
		draw(new QuadRenderCommand(z, clipEnabled, blendMode, argb, tex,
				trans, x, y, w, h, u, v, uw, vh, ps));
	}

	@Override
	public void drawFadeQuad(short z, boolean clipEnabled, BlendMode blendMode, int argb,
			ITexture tex, Matrix trans, double x, double y, double w, double h, IPixelShader ps,
			int dir, boolean fadeIn, double span, double time)
	{
		draw(new FadeQuadCommand(z, clipEnabled, blendMode, argb, tex,
				trans, x, y, w, h, ps, dir, fadeIn, span, time));
	}
	
	@Override
	public void drawBlendQuad(short z, boolean clipEnabled, BlendMode blendMode, int argb,
			ITexture tex0, double alignX0, double alignY0,
			ITexture tex1, double alignX1, double alignY1,
			Matrix trans, IPixelShader ps,
			double frac)
	{
		draw(new BlendQuadCommand(z, clipEnabled, blendMode, argb,
				tex0, alignX0, alignY0,
				tex1, alignX1, alignY1,
				trans, ps,
				frac));
	}
	
	@Override
	public void drawDistortQuad(short z, boolean clipEnabled, BlendMode blendMode, int argb,
			ITexture tex, Matrix trans, double x, double y, double w, double h, IPixelShader ps,
			IDistortGrid distortGrid, Rect2D clampBounds)
	{
		draw(new DistortQuadCommand(z, clipEnabled, blendMode, argb,
				tex, trans, x, y, w, h, ps,
				distortGrid, clampBounds));
	}
	
	public void draw(BaseRenderCommand cmd) {
		reserveCommands(commandsL+1);
		commands[commandsL++] = cmd;
	}
	
	public BaseRenderCommand[] sortCommands(int start, int end) {
		int diff = end - start;
		if (diff > 100) {
			Arrays.sort(commands, start, end);
		} else {
			MergeSort.sort(commands, start, end);
		}
		return commands;
	}
	
	/**
	 * Switches the active layer to <code>layer</code>. All draw commands
	 * submitted from this point onward will be associated with that layer.
	 */
	public void startLayer(ILayer layer) {
		if (layersL > 0 && layers[layersL-1] == layer) {
			return; //Layer already active
		}
		
		if (getLayerStart(layer) >= 0) {
			throw new IllegalArgumentException("Layer already added to draw buffer");
		}
		
		reserveLayers(layersL+1);
		layers[layersL] = layer;
		layerStarts[layersL] = commandsL;
		layersL++;
	}
	
	// === Getters =============================================================
		
	@Override
	public RenderEnv getEnv() {
		return env;
	}
	
	public int getLayerStart(ILayer layer) {
		for (int n = 0; n < layersL; n++) {
			if (layers[n] == layer) {
				return layerStarts[n];
			}
		}
		return -1;
	}
	
	public int getLayerEnd(ILayer layer) {
		for (int n = 0; n < layersL; n++) {
			if (layers[n] == layer) {
				return (n+1 < layersL ? layerStarts[n+1] : commandsL);
			}
		}
		return -1;
	}
	
	// === Setters =============================================================
		
}
