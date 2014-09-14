package nl.weeaboo.vn.awt;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

import nl.weeaboo.common.Rect2D;
import nl.weeaboo.game.entity.Entity;
import nl.weeaboo.game.entity.Scene;
import nl.weeaboo.game.entity.World;
import nl.weeaboo.vn.ILayer;
import nl.weeaboo.vn.IRenderEnv;
import nl.weeaboo.vn.TestPartRegistry;
import nl.weeaboo.vn.TestUtil;
import nl.weeaboo.vn.entity.IImagePart;
import nl.weeaboo.vn.entity.ITransformablePart;
import nl.weeaboo.vn.impl.Screen;
import nl.weeaboo.vn.render.impl.DrawBuffer;

public class AwtRendererTest {

	public static void main(String[] args) {
		final IRenderEnv env = TestUtil.BASIC_ENV;
		final TestPartRegistry pr = new TestPartRegistry();
		World world = new World(pr);
		Scene scene = world.createScene();
		Rect2D screenBounds = new Rect2D(0, 0, env.getWidth(), env.getHeight());
		final Screen screen = new Screen(scene, screenBounds, pr.drawable, env);

		Random random = new Random();

		ILayer layer = screen.getActiveLayer();
		final List<Entity> entities = new ArrayList<Entity>();
		for (int n = 0; n < 100; n++) {
			Entity e = TestUtil.newImage(pr, scene);
			ITransformablePart tp = e.getPart(pr.transformable);
			IImagePart ip = e.getPart(pr.image);
			ip.setTexture(new AwtTexture(100, 100));

			tp.setAlign(.5, .5);
			tp.setPos(random.nextInt(env.getWidth()), random.nextInt(env.getHeight()));
			tp.setRotation(random.nextInt(512));
			tp.setSize(100, 100);
			//tp.setBlendMode(BlendMode.ADD);

			entities.add(e);
			layer.add(e);
		}

		final AwtRenderer r = new AwtRenderer(env, null);
		final RenderPanel renderPanel = new RenderPanel(r, env);

		Timer timer = new Timer(10, new ActionListener() {

			final DrawBuffer buf = new DrawBuffer(pr.transformable, pr.image);

			@Override
			public void actionPerformed(ActionEvent event) {
				for (Entity e : entities) {
					ITransformablePart tp = e.getPart(pr.transformable);
					Rect2D r = tp.getBounds();

					double x = tp.getX() + 4;
					if (x >= env.getWidth() + r.w/2) {
						x = -r.w / 2;
					}
					double y = tp.getY() + 3;
					if (y >= env.getHeight() + r.h/2) {
						y = -r.h / 2;
					}
					tp.setPos(x, y);
					tp.setRotation(tp.getRotation() + 1 + .01 * e.getId());
				}

				buf.reset();
				screen.draw(buf);
				r.render(buf);

				renderPanel.repaint();
			}
		});
		timer.start();

		JFrame frame = new JFrame("AWT render test");
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(renderPanel);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	@SuppressWarnings("serial")
	private static class RenderPanel extends JPanel {

		private final AwtRenderer renderer;

		public RenderPanel(AwtRenderer r, IRenderEnv env) {
			renderer = r;

			setPreferredSize(new Dimension(env.getScreenWidth(), env.getScreenHeight()));
		}

		@Override
		protected void paintComponent(Graphics g) {
			g.drawImage(renderer.getRenderBuffer(), 0, 0, this);
		}

	}

}
