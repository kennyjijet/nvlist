package nl.weeaboo.vn;

import nl.weeaboo.game.entity.Entity;
import nl.weeaboo.vn.impl.TransformablePart;

import org.junit.Test;

public class BaseEntityTest extends AbstractEntityTest {

	@Test
	public void transformablePartTest() {
		Entity e = TestUtil.newImage(pr, scene);
		TransformablePart transformable = e.getPart(pr.transformable);

		double x = -50;
		double y = -50;
		double w = 100;
		double h = 100;

		// Bounds
		transformable.setBounds(x, y, w, h);
		TestUtil.assertEquals(x, y, w, h, transformable.getBounds());

		// Rotated bounds
		transformable.setRotation(64); // Rotate 1/8th circle clockwise around top-left
		final double diagonal = Math.sqrt(w*w + h*h);
		TestUtil.assertEquals(x-diagonal/2, y, diagonal, diagonal, transformable.getBounds());

		// Scaled
		transformable.setRotation(0);
		transformable.setScale(0.5, 2);
		TestUtil.assertEquals(x, y, w*.5, h*2, transformable.getBounds());

		// Align
		transformable.setPos(0, 0);
		transformable.setScale(1, 1);
		transformable.setAlign(0.5, 0.5);
		TestUtil.assertEquals(x, y, w, h, transformable.getBounds());
	}

}
