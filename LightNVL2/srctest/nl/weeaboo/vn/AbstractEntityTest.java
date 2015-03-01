package nl.weeaboo.vn;

import nl.weeaboo.game.entity.Scene;
import nl.weeaboo.game.entity.World;

import org.junit.Before;

public class AbstractEntityTest {

	protected BasicPartRegistry pr;
	protected World world;
	protected Scene scene;

	@Before
	public void init() {
		pr = new BasicPartRegistry();
		world = new World(pr);
		scene = world.createScene();
	}

}
