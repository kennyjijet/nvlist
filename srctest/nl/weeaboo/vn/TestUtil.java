package nl.weeaboo.vn;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.logging.LogManager;

import nl.weeaboo.common.Dim;
import nl.weeaboo.common.Rect;
import nl.weeaboo.common.Rect2D;
import nl.weeaboo.game.entity.Entity;
import nl.weeaboo.game.entity.Scene;
import nl.weeaboo.vn.impl.ImagePart;
import nl.weeaboo.vn.impl.RenderEnv;
import nl.weeaboo.vn.impl.TransformablePart;
import nl.weeaboo.vn.math.Vec2;

import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TestUtil {

	public static final IRenderEnv BASIC_ENV = new RenderEnv(new Dim(1280, 720), new Rect(0, 75, 800, 450), new Dim(800, 600), false);

	private static final Logger LOG = LoggerFactory.getLogger(TestUtil.class);
	private static final double EPSILON = 0.001;

	private TestUtil() {
	}

	public static Entity newImage(TestPartRegistry pr, Scene scene) {
		TransformablePart transformable = new TransformablePart();
		ImagePart image = new ImagePart(transformable);

		Entity e = scene.createEntity();
		e.setPart(pr.drawable, transformable);
		e.setPart(pr.transformable, transformable);
		e.setPart(pr.image, image);
		return e;
	}

	public static void configureLogger() {
		try {
			InputStream in = TestUtil.class.getResourceAsStream("logging.debug.properties");
			if (in == null) {
				throw new FileNotFoundException();
			}
			try {
				LogManager.getLogManager().readConfiguration(in);
			} finally {
				in.close();
			}
		} catch (IOException e) {
			LOG.warn("Unable to read logging config", e);
		}
	}

	public static void assertEquals(Rect2D expected, Rect2D r) {
		assertEquals(expected.x, expected.y, expected.w, expected.h, r);
	}
	public static void assertEquals(double expectedX, double expectedY, double expectedW, double expectedH, Rect2D r) {
		Assert.assertEquals(expectedX, r.x, EPSILON);
		Assert.assertEquals(expectedY, r.y, EPSILON);
		Assert.assertEquals(expectedW, r.w, EPSILON);
		Assert.assertEquals(expectedH, r.h, EPSILON);
	}

	public static void assertEquals(double x, double y, Vec2 vec, double epsilon) {
		Assert.assertEquals(x, vec.x, epsilon);
		Assert.assertEquals(y, vec.y, epsilon);
	}

	public static <T> byte[] serialize(T obj) throws IOException {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		ObjectOutputStream out = new ObjectOutputStream(bout);
		try {
			out.writeObject(obj);
		} finally {
			out.close();
		}
		return bout.toByteArray();
	}

	public static <T> T deserialize(byte[] data, Class<T> clazz) throws IOException, ClassNotFoundException {
		return deserialize(new ByteArrayInputStream(data), clazz);
	}
	public static <T> T deserialize(InputStream in, Class<T> clazz) throws IOException, ClassNotFoundException {
		ObjectInputStream oin = new ObjectInputStream(in);
		return clazz.cast(oin.readObject());
	}

}
