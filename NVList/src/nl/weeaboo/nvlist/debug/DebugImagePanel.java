package nl.weeaboo.nvlist.debug;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import nl.weeaboo.common.Dim;
import nl.weeaboo.common.ScaleUtil;
import nl.weeaboo.game.GameLog;
import nl.weeaboo.gl.texture.GLTexRect;
import nl.weeaboo.gl.texture.TextureException;
import nl.weeaboo.vn.IButtonDrawable;
import nl.weeaboo.vn.IDrawable;
import nl.weeaboo.vn.IImageDrawable;
import nl.weeaboo.vn.IImageState;
import nl.weeaboo.vn.ILayer;
import nl.weeaboo.vn.IPanel;
import nl.weeaboo.vn.ITextDrawable;
import nl.weeaboo.vn.ITexture;
import nl.weeaboo.vn.IViewport;
import nl.weeaboo.vn.impl.base.Layer;
import nl.weeaboo.vn.impl.nvlist.Novel;
import nl.weeaboo.vn.impl.nvlist.TextureAdapter;

@SuppressWarnings("serial")
public class DebugImagePanel extends JPanel {

	private final Object lock;
	private final Novel novel;
	
	private final LinkedHashMap<Class<?>, ImageIcon> drawableIcons;
	private final JTree tree;
	private final Timer timer;
	
	private int disableTreeSelection;
	
	public DebugImagePanel(Object l, Novel nvl) {
		lock = l;
		novel = nvl;
		
		drawableIcons = new LinkedHashMap<Class<?>, ImageIcon>();
		registerDrawableIcon("layer", ILayer.class);
		registerDrawableIcon("viewport", IViewport.class);
		registerDrawableIcon("panel", IPanel.class);
		registerDrawableIcon("button", IButtonDrawable.class);
		registerDrawableIcon("text", ITextDrawable.class);
		registerDrawableIcon("image", IImageDrawable.class);
		registerDrawableIcon("unknown", null);
		
		tree = new JTree();
		tree.setBorder(new EmptyBorder(5, 5, 5, 5));
		tree.setCellRenderer(new DefaultTreeCellRenderer() {
			public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected,
					boolean expanded, boolean leaf, int row, boolean hasFocus)
			{
				Component c = super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);		        
				if (c instanceof JLabel) {
					JLabel label = (JLabel)c;
					if (value instanceof LayerNode) {
						label.setIcon(drawableIcons.get(ILayer.class));
					} else if (value instanceof DrawableNode) {
						IDrawable d = ((DrawableNode)value).drawable;
						
						ImageIcon icon = null;
						for (Entry<Class<?>, ImageIcon> entry : drawableIcons.entrySet()) {
							Class<?> clazz = entry.getKey();
							if (clazz != null && clazz.isInstance(d)) {
								icon = entry.getValue();
								break;
							}
						}
						label.setIcon(icon != null ? icon : drawableIcons.get(null));
					} else {
						label.setIcon(null);
					}
				}
				return c;
			}
		});
		
		JScrollPane treePane = new JScrollPane(tree);
		treePane.setPreferredSize(new Dimension(100, 200));
		
		final PreviewPanel previewPanel = new PreviewPanel(lock);
		previewPanel.setPreferredSize(new Dimension(100, 50));
		tree.addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent e) {
				if (disableTreeSelection <= 0) {
					TreePath path = tree.getSelectionPath();
					if (path != null) {
						previewPanel.setSelected(path.getLastPathComponent());
					} else {
						previewPanel.setSelected(null);
					}
				}
			}
		});
		
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitPane.setBorder(null);
		splitPane.setTopComponent(treePane);
		splitPane.setBottomComponent(previewPanel);
		
		setLayout(new BorderLayout(5, 5));
		add(splitPane, BorderLayout.CENTER);
		
		addHierarchyListener(new HierarchyListener() {
			public void hierarchyChanged(HierarchyEvent e) {
				Component c = e.getChanged();
				if (c == null) c = DebugImagePanel.this;
				
				if (!c.isDisplayable() || !c.isVisible() || !isVisible()) {
					timer.stop();
				} else if (c.isVisible() && isVisible()) {
					timer.start();
					update();
				}
			}
		});
		
		timer = new Timer(1000, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				update();
			}
		});
		timer.start();
	}
	
	//Functions
	private void registerDrawableIcon(String filename, Class<?> clazz) {
		ImageIcon icon = new ImageIcon(getClass().getResource("res/drawable-" + filename + ".png"));
		drawableIcons.put(clazz, icon);
	}
	
	public void update() {
		disableTreeSelection++;
		try {
			Enumeration<TreePath> enumeration = tree.getExpandedDescendants(new TreePath(tree.getModel().getRoot()));
			TreePath[] selected = tree.getSelectionPaths();
			
			TreeNode rootNode;				
			synchronized (lock) {
				IImageState imageState = novel.getImageState();
				rootNode = new LayerNode(imageState.getRootLayer());
			}
			
			tree.setModel(new DefaultTreeModel(rootNode));
			
			//Re-expand
			if (enumeration != null) {
				while (enumeration.hasMoreElements()) {
					TreePath path = enumeration.nextElement();
					tree.expandPath(path);
				}
			}
			tree.setSelectionPaths(selected);
		} finally {
			disableTreeSelection--;
		}
	}
	
	//Getters
	
	//Setters
	
	//Inner Classes
	private static class ObjectTreeNode extends DefaultMutableTreeNode {

		private int hashCode;
		
		public ObjectTreeNode(Object userObject) {
			super(userObject);
			
			this.hashCode = (userObject != null ? userObject.hashCode() : 0);
		}
		
		@Override
		public int hashCode() {
			return hashCode;
		}
		
		@Override
		public boolean equals(Object other) {
			Object obj = getUserObject();
			if (other instanceof DefaultMutableTreeNode) {
				other = ((DefaultMutableTreeNode)other).getUserObject();
			}
			return obj == other;
		}
		
		@Override
		public void setUserObject(Object obj) {
			super.setUserObject(obj);
			
			hashCode = (obj != null ? obj.hashCode() : 0);
		}
	}
	
	private static class LayerNode extends DefaultMutableTreeNode {
		
		public final ILayer layer;
		private final int hashCode;
		
		public LayerNode(ILayer layer) {
			this.layer = layer;
			this.hashCode = layer.hashCode();
			
			IDrawable[] drawables = layer.getContents();
			Arrays.sort(drawables, Layer.zBackToFrontComparator);
			for (IDrawable d : drawables) {
				if (d instanceof ILayer) {
					ILayer l = (ILayer)d;
					add(new LayerNode(l));
				} else {
					add(new DrawableNode(d));
				}
			}
		}
		
		@Override
		public int hashCode() {
			return hashCode;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof LayerNode) {
				LayerNode ln = (LayerNode)obj;
				return layer == ln.layer;
			}
			return false;
		}
		
		@Override
		public String toString() {
			//We're calling some getters without a lock, from the wrong thread.
			//Unless the implementation is very odd, the worst we'll get is outdated values.
			
			String core = String.format("Layer [%.0f,%.0f,%.0f,%.0f] z=%d",
					layer.getX(), layer.getY(), layer.getWidth(), layer.getHeight(), layer.getZ());
			
			if (!layer.isVisible(.001)) {
				//HTML tags are very heavy to render, only include them when needed.
				return String.format("<html>%s %s</html>", core, "<font color=red size=-2>invisible</font>"); 
			} else {
				return core;
			}			
			
		}
		
	}
	
	private static class DrawableNode extends ObjectTreeNode {
		
		public final IDrawable drawable;
		
		public DrawableNode(IDrawable d) {
			super(d);
			
			this.drawable = d;
		}
		
		@Override
		public String toString() {
			String core = String.format("[%.0f,%.0f,%.0f,%.0f] z=%d",
					drawable.getX(), drawable.getY(), drawable.getWidth(), drawable.getHeight(),
					drawable.getZ());
			
			if (!drawable.isVisible(.001)) {
				//HTML tags are very heavy to render, only include them when needed.
				return String.format("<html>%s %s</html>", core, "<font color=red size=-2>invisible</font>"); 
			} else {
				return core;
			}			
		}
		
	}
	
	private static class PreviewPanel extends JPanel {
		
		private final Object lock;
		
		private BufferedImage icon;
		
		public PreviewPanel(Object lock) {
			this.lock = lock;
			
			setBackground(Color.BLACK);
		}
		
		@Override
		protected void paintComponent(Graphics graphics) {
			super.paintComponent(graphics);
			
			Graphics2D g = (Graphics2D)graphics;
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			int w = getWidth();
			int h = getHeight();
			
			if (icon != null) {	
				Dim d = ScaleUtil.scaleProp(icon.getWidth(), icon.getHeight(), w, h);
				g.drawImage(icon, (w-d.w)/2, (h-d.h)/2, d.w, d.h, this);
			}
		}
		
		public void setSelected(Object value) {			
			if (value instanceof MutableTreeNode) {
				DefaultMutableTreeNode mtn = (DefaultMutableTreeNode)value;
				value = mtn.getUserObject();
			}
			
			if (value instanceof IImageDrawable) {
				IImageDrawable id = (IImageDrawable)value;
				setTexture(id.getTexture());
			} else {
				setTexture(null);
			}
		}
		
		public void setTexture(ITexture itex) {
			icon = null;
			
			if (itex instanceof TextureAdapter) {
				TextureAdapter adapter = (TextureAdapter)itex;
				GLTexRect tr = adapter.getTexRect();
				if (tr != null) {
					synchronized (lock) {
						try {
							icon = tr.toBufferedImage();
						} catch (TextureException e) {
							GameLog.w("Error getting pixels from texture", e);
						} catch (RuntimeException e) {
							GameLog.w("Error getting pixels from texture", e);
						}
					}
				}
			}
			
			repaint();
		}
	}
	
}
