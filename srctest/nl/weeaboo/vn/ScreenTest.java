package nl.weeaboo.vn;

import nl.weeaboo.common.Rect2D;
import nl.weeaboo.vn.impl.Screen;

import org.junit.Assert;
import org.junit.Test;

public class ScreenTest extends AbstractEntityTest {

	private static final Rect2D SCREEN_BOUNDS = new Rect2D(13, 17, 800, 600);

	@Test
	public void layers() {
		Screen screen = new Screen(scene, SCREEN_BOUNDS, pr.drawable, TestUtil.BASIC_ENV);
		ILayer active = screen.getActiveLayer();
		Assert.assertNotNull(active); // Active layer should never be null
		ILayer root = screen.getRootLayer();
		Assert.assertSame(active, root); // Root layer should never be null

		// Sub-layer creation and containsLayer() test
		ILayer subLayer = screen.createLayer(root);
		ILayer subSubLayer = screen.createLayer(subLayer);
		Assert.assertTrue(root.containsLayer(subLayer));
		Assert.assertTrue(root.containsLayer(subSubLayer));
		Assert.assertTrue(subLayer.containsLayer(subSubLayer));
	}

}
