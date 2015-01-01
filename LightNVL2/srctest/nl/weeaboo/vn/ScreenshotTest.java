package nl.weeaboo.vn;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import javax.imageio.ImageIO;

import nl.weeaboo.game.entity.Scene;
import nl.weeaboo.game.entity.World;
import nl.weeaboo.vn.awt.AwtDecodingScreenshot;
import nl.weeaboo.vn.awt.AwtTexture;
import nl.weeaboo.vn.impl.Screen;
import nl.weeaboo.vn.render.impl.DrawBuffer;
import nl.weeaboo.vn.render.impl.LayerRenderCommand;
import nl.weeaboo.vn.render.impl.RenderCommand;
import nl.weeaboo.vn.render.impl.ScreenshotRenderCommand;
import nl.weeaboo.vn.render.impl.WritableScreenshot;

import org.junit.Assert;
import org.junit.Test;

public class ScreenshotTest {

	@Test
	public void stateTransitions() {
		WritableScreenshot s = new WritableScreenshot((short)0, false);
		Assert.assertFalse(s.isAvailable());
		Assert.assertFalse(s.isCancelled());
		Assert.assertFalse(s.isTransient());
		Assert.assertFalse(s.isVolatile());

		s.markTransient();
		Assert.assertTrue(s.isTransient());
		Assert.assertFalse(s.isVolatile());

		s = new WritableScreenshot((short)0, true);
		Assert.assertTrue(s.isTransient());
		Assert.assertTrue(s.isVolatile());
	}

	@Test
	public void writeScreenshot() {
		int w = 10;
		int h = 10;
		int[] pixels = createPixels(w, h);

		AwtTexture tex = new AwtTexture(w, h);

		WritableScreenshot s = new WritableScreenshot((short)0, false);
		try {
			s.setVolatilePixels(tex, w, h);
			Assert.fail("Expected an exception: Shouldn't be able to set volatile pixels on a non-volatile screenshot");
		} catch (RuntimeException re) {
			// This is good
		}
		s.setPixels(pixels, w, h, w, h);
		Assert.assertArrayEquals(pixels, s.getPixels());

		s = new WritableScreenshot((short)0, true);
		try {
			s.setPixels(pixels, w, h, w, h);
			Assert.fail("Expected an exception: Shouldn't be able to set non-volatile pixels on a volatile screenshot");
		} catch (RuntimeException re) {
			// This is good
		}
		s.setVolatilePixels(tex, w, h);
		Assert.assertEquals(tex, s.getVolatilePixels());
	}

	@Test
	public void decodingScreenshot() throws IOException {
		int w = 10;
		int h = 10;
		int[] pixels = createPixels(w, h);

		BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		img.setRGB(0, 0, w, h, pixels, 0, w);
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		ImageIO.write(img, "png", bout);

		AwtDecodingScreenshot ds = new AwtDecodingScreenshot(ByteBuffer.wrap(bout.toByteArray()));
		Assert.assertEquals(w, ds.getPixelsWidth());
		Assert.assertEquals(h, ds.getPixelsHeight());
		Assert.assertArrayEquals(pixels, ds.getPixels());
	}

	@Test
	public void screenshotBuffer() {
		TestPartRegistry pr = new TestPartRegistry();
		World world = new World(pr);
		Scene scene = world.createScene();

		Screen screen = TestUtil.newScreen(pr, scene);
		ILayer root = screen.getRootLayer();

		WritableScreenshot s = new WritableScreenshot((short)0, false);

		IScreenshotBuffer ssb = root.getScreenshotBuffer();
		ssb.add(s, false);

		DrawBuffer buf = new DrawBuffer(pr.transformable, pr.image);
		screen.draw(buf);
		Assert.assertTrue(ssb.isEmpty()); // Screenshot buffer empties into the draw buffer

		LayerRenderCommand lrc = buf.getRootLayerCommand();
		int cstart = buf.getLayerStart(lrc.layerId);
		int cend = buf.getLayerEnd(lrc.layerId);
		Assert.assertEquals(cend-cstart, 1);

		RenderCommand[] cmds = buf.sortCommands(cstart, cend);
		ScreenshotRenderCommand src = (ScreenshotRenderCommand)cmds[cstart];

		// Assert that the correct ScreenshotRenderCommand has been added to the render commands
		Assert.assertEquals(s.getZ(), src.z);
		Assert.assertSame(s, src.ss);
	}

	private static int[] createPixels(int w, int h) {
		int[] argb = new int[w * h];
		Arrays.fill(argb, 0xAA996633);
		return argb;
	}

}
