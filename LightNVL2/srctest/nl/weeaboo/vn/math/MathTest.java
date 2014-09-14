package nl.weeaboo.vn.math;

import static nl.weeaboo.vn.TestUtil.assertEquals;

import java.io.IOException;

import nl.weeaboo.vn.TestUtil;

import org.junit.Assert;
import org.junit.Test;

public class MathTest {

	private static final double E = 0.0001;

	@Test
	public void vectorTest() {
		Vec2 a = new Vec2(1, 2);
		Vec2 b = new Vec2(4, 3);
		Assert.assertEquals(10, a.dot(b), E);

		a.add(b);
		assertEquals(5, 5, a, E);
		a.sub(b);
		assertEquals(1, 2, a, E);
		a.scale(-.5);
		assertEquals(-.5, -1, a, E);
	}

	@Test
	public void vectorSerializeTest() throws IOException, ClassNotFoundException {
		Vec2 a = new Vec2(1, 2);
		Assert.assertEquals(a, TestUtil.deserialize(TestUtil.serialize(a), Vec2.class));
		a = new Vec2(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY);
		Assert.assertEquals(a, TestUtil.deserialize(TestUtil.serialize(a), Vec2.class));
		a = new Vec2(Double.MIN_VALUE, Double.MIN_NORMAL);
		Assert.assertEquals(a, TestUtil.deserialize(TestUtil.serialize(a), Vec2.class));
		a = new Vec2(Double.NaN, 0.0);
		Assert.assertTrue(a.equals(TestUtil.deserialize(TestUtil.serialize(a), Vec2.class), E));
	}

}
