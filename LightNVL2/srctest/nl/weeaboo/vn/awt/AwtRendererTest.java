package nl.weeaboo.vn.awt;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

import nl.weeaboo.common.Dim;
import nl.weeaboo.common.Rect;
import nl.weeaboo.common.Rect2D;
import nl.weeaboo.game.entity.Entity;
import nl.weeaboo.game.entity.Scene;
import nl.weeaboo.game.entity.World;
import nl.weeaboo.vn.BasicPartRegistry;
import nl.weeaboo.vn.ILayer;
import nl.weeaboo.vn.IRenderEnv;
import nl.weeaboo.vn.ITransformablePart;
import nl.weeaboo.vn.TestUtil;
import nl.weeaboo.vn.core.impl.Screen;
import nl.weeaboo.vn.image.IImagePart;
import nl.weeaboo.vn.math.Vec2;
import nl.weeaboo.vn.render.impl.DrawBuffer;

public class AwtRendererTest {

	public static void main(String[] args) {
		final IRenderEnv env = TestUtil.BASIC_ENV;
		final BasicPartRegistry pr = new BasicPartRegistry();
		World world = new World(pr);
		Scene scene = world.createScene();
		final Screen screen = TestUtil.newScreen(pr, scene);

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
		final Vec2 baseSpeed = new Vec2(2, 1.5);

		Timer timer = new Timer(10, new ActionListener() {

			final DrawBuffer buf = new DrawBuffer(pr);

			@Override
			public void actionPerformed(ActionEvent event) {
			    Vec2 speed = new Vec2(baseSpeed);
			    speed.scale(Math.pow(2, renderPanel.speed));

			    Vec2 mp = new Vec2(renderPanel.mousePos.x, renderPanel.mousePos.y);
			    Rect realClip = env.getRealClip();
                mp.x = (mp.x - realClip.x) * env.getWidth() / realClip.w;
			    mp.y = (mp.y - realClip.y) * env.getHeight() / realClip.h;

				for (Entity e : entities) {
					ITransformablePart tp = e.getPart(pr.transformable);
					Rect2D r = tp.getBounds();

					double x = tp.getX() + speed.x;
					if (x >= env.getWidth() + r.w/2) {
						x = -r.w / 2;
					}
					double y = tp.getY() + speed.y;
					if (y >= env.getHeight() + r.h/2) {
						y = -r.h / 2;
					}
					tp.setPos(x, y);
					tp.setRotation(tp.getRotation() + 1 + .01 * e.getId());

					// Change color on mouse-over
					tp.setColorRGB(tp.contains(mp.x, mp.y) ? 0xFF0000 : 0xFFFFFF);
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

		private Point mousePos = new Point(0, 0);
		private int speed = 0;

		public RenderPanel(AwtRenderer r, IRenderEnv env) {
			renderer = r;

			Dim screenSize = env.getScreenSize();

			setFocusable(true);
			setPreferredSize(new Dimension(screenSize.w, screenSize.h));

			addMouseMotionListener(new MouseMotionListener() {
                @Override
                public void mouseDragged(MouseEvent e) {
                }
                @Override
                public void mouseMoved(MouseEvent e) {
                    mousePos.setLocation(e.getPoint());
                }
			});

			addKeyListener(new KeyAdapter() {
			    @Override
                public void keyPressed(KeyEvent e) {
			        switch (e.getKeyCode()) {
                    case KeyEvent.VK_ADD:
                        speed++;
                        break;
                    case KeyEvent.VK_SUBTRACT:
                        speed--;
                        break;
                    default:
                        // Do nothing
			        }

			        speed = Math.max(-4, Math.min(4, speed));
			    }
			});
		}

		@Override
		protected void paintComponent(Graphics g) {
			g.drawImage(renderer.getRenderBuffer(), 0, 0, this);
		}

	}

}
