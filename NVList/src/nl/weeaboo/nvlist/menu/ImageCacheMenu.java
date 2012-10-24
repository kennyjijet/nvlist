package nl.weeaboo.nvlist.menu;

import static nl.weeaboo.game.BaseGameConfig.IMAGE_CACHE_SIZE;

import java.awt.event.ActionEvent;

import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;

import nl.weeaboo.common.Dim;
import nl.weeaboo.gl.GLUtil;
import nl.weeaboo.nvlist.Game;
import nl.weeaboo.settings.IConfig;
import nl.weeaboo.settings.Preference;
import nl.weeaboo.vn.impl.nvlist.Novel;

public class ImageCacheMenu extends GameMenuAction {

	private final String label;
	private final Preference<String> pref;
	private final String itemLabels[];
	private final String itemValues[];
	
	public ImageCacheMenu() {
		label = "Image cache size";
		pref = IMAGE_CACHE_SIZE;
		
		String[] strings = new String[] {
			"Off",
			"-",
			"16 MiB", "32 MiB", "64 MiB", "128 MiB", "256 MiB",
			"-",
			"5 pages", "10 pages", "20 pages", "40 pages", "80 pages"
		};
		
		this.itemLabels = strings.clone();
		
		strings[0] = "0";
		this.itemValues = strings.clone();
	}
	
	@Override
	public JMenuItem createItem(Game game, Novel nvl) {
		final JMenu menu = new JMenu(label);

		IConfig config = game.getConfig();
		int best = getSelectedIndex(itemValues, config.get(pref));
		
		ButtonGroup group = new ButtonGroup();
		for (int n = 0; n < Math.min(itemLabels.length, itemValues.length); n++) {
			if (itemValues[n].startsWith("-")) {
				menu.addSeparator();
				continue;
			}
			
			JMenuItem item = createSubItem(game, nvl, itemLabels[n], itemValues[n]);
			item.addActionListener(new SubItemActionListener(menu, n));
			group.add(item);
			if (n == best) {
				item.setSelected(true);
			}
			menu.add(item);			
		}
		return menu;
	}
	
	protected JRadioButtonMenuItem createSubItem(Game game, Novel nvl, String lbl, String val) {
		JRadioButtonMenuItem item = new JRadioButtonMenuItem(lbl);
		return item;
	}

	//Returns the index of the 'best' match to cur in values
	public static int getSelectedIndex(String[] values, String cur) {
		int best = -1;
		for (int n = 0; n < values.length; n++) {
			if (values[n].startsWith("-")) continue;
			
			if (compare(values[n], cur) == 0) {
				best = n;
			} else {
				if (compare(values[n], cur) < 0) {
					if (best < 0 || compare(values[best], cur) < 0) {
						best = n;
					}
				}
			}
		}
		return Math.min(values.length-1, Math.max(0, best));
	}
	
	private static int compare(String a, String b) {
		a = a.toLowerCase();
		b = b.toLowerCase();
		
		if (a.contains("pages") && !b.contains("pages")) return 1;
		if (!a.contains("pages") && b.contains("pages")) return -1;
		
		int av = GLUtil.parseCacheSize(a, new Dim(999, 999));
		int bv = GLUtil.parseCacheSize(b, new Dim(999, 999));
		
		if (av < bv) return -1;
		if (av > bv) return 1;
		
		return 0;
	}
	
	@Override
	public void actionPerformed(JMenuItem item, ActionEvent e, Game game, Novel nvl) {
		if (e.getSource() instanceof Integer) {
			int index = (Integer)e.getSource();
			onItemSelected(game, nvl, index, itemLabels[index], itemValues[index]);
		}
	}
	
	protected void onItemSelected(Game game, Novel nvl, int index,
			String label, String value)
	{
		game.getConfig().set(pref, value);
	}
	
}
