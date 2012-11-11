package nl.weeaboo.nvlist.build;

import java.awt.Color;
import java.awt.Component;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Window;
import java.io.File;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import javax.swing.text.JTextComponent;

import nl.weeaboo.awt.MessageBox;

public final class BuildGUIUtil {

	private BuildGUIUtil() {
	}
	
	public static void recursiveSetOpaque(JComponent c, boolean opaque) {
		c.setOpaque(opaque);
		
		final int L = c.getComponentCount();
		for (int n = 0; n < L; n++) {
			Component child = c.getComponent(n);
			if (child instanceof JComponent) {
				recursiveSetOpaque((JComponent)child, opaque);
			}
		}
	}

	@SuppressWarnings("serial")
	public static void setTextFieldDefaults(JTextComponent c, Color bg) {
		c.setSelectionColor(darker(bg));
		c.setBackground(brighter(bg));
		c.setBorder(new LineBorder(bg.darker()) {
		    public Insets getBorderInsets(Component c)       {
		        return getBorderInsets(c, new Insets(0, 0, 0, 0));
		    }
		    public Insets getBorderInsets(Component c, Insets insets) {
		        insets.set(thickness, 5+thickness, thickness, 5+thickness);
		        return insets;
		    }			
		});
	}
	
	public static Color darker(Color bg) {
		return bg.darker();
	}
	
	public static Color brighter(Color bg) {
		float s = 1.1f;
		float rgb[] = bg.getColorComponents(null);
		return new Color(rgb[0]*s, rgb[1]*s, rgb[2]*s);
	}
	
	public static JFrame createOptimizerGUI(Build build, boolean isAndroid, boolean disposeOnDone)
			throws Exception
	{
		ClassLoader cl = build.getClassLoader();
		Class<?> clazz = cl.loadClass("nl.weeaboo.game.optimizer.OptimizerGUI");
		Constructor<?> constr = clazz.getConstructor(File.class, String.class, Boolean.TYPE);
		Object optObj = constr.newInstance(build.getProjectFolder(), build.getGameId(), isAndroid);
		return (JFrame)clazz.getDeclaredMethod("createFrame", clazz, Boolean.TYPE)
					.invoke(null, optObj, disposeOnDone);		
	}

	public static List<Image> getWindowIcons(Component c) {
		List<Image> result = new ArrayList<Image>();
		Window w = SwingUtilities.getWindowAncestor(c);
		if (w != null) {
			result.addAll(w.getIconImages());
		}
		return result;		
	}
	
	public static MessageBox newMessageBox(String title, Object message) {
		MessageBox mb = new MessageBox(title, message);
		mb.setSelectedColors(new Color(0x897294), new Color(0xa794af), new Color(0xa794af));
		return mb;
	}
	
}
