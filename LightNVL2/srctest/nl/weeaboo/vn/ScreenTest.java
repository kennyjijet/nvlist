package nl.weeaboo.vn;

import nl.weeaboo.vn.ILayer;
import nl.weeaboo.vn.core.impl.Screen;

import org.junit.Assert;
import org.junit.Test;

public class ScreenTest extends AbstractEntityTest {

	@Test
	public void layers() {
		Screen screen = TestUtil.newScreen(pr, scene);
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
