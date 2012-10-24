package nl.weeaboo.nvlist.menu;

import java.awt.event.ActionEvent;

import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;

import nl.weeaboo.nvlist.Game;
import nl.weeaboo.vn.impl.nvlist.Novel;

public abstract class RangeMenu<T extends Comparable<T>> extends GameMenuAction {

	private final String label;
	private final char mnemonic;
	private final String itemLabels[];
	private final T itemValues[];
	
	public RangeMenu(String label, char mnemonic, String[] labels, T[] values) {
		this.label = label;
		this.mnemonic = mnemonic;
		this.itemLabels = labels;
		this.itemValues = values;
	}
	
	@Override
	public JMenuItem createItem(Game game, Novel nvl) {
		final JMenu menu = new JMenu(label);
		if (mnemonic != 0) {
			menu.setMnemonic(mnemonic);
		}

		int best = getSelectedIndex(game, itemValues);
		
		ButtonGroup group = new ButtonGroup();
		for (int n = 0; n < Math.min(itemLabels.length, itemValues.length); n++) {
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
	
	protected abstract int getSelectedIndex(Game game, T[] values);
	
	protected JMenuItem createSubItem(Game game, Novel nvl, String lbl, T val) {
		JRadioButtonMenuItem item = new JRadioButtonMenuItem(lbl);
		return item;
	}

	//Returns the index of the 'best' match to cur in values
	public static <T extends Comparable<T>> int getSelectedIndex(T[] values, T cur) {
		int best = -1;
		for (int n = 0; n < values.length; n++) {
			if (values[n].compareTo(cur) == 0) {
				best = n;
			} else if (values[n].compareTo(cur) < 0) {
				if (best < 0 || values[best].compareTo(cur) < 0) {
					best = n;
				}
			}
		}
		return Math.min(values.length-1, Math.max(0, best));
	}
	
	@Override
	public void actionPerformed(JMenuItem item, ActionEvent e, Game game, Novel nvl) {
		if (e.getSource() instanceof Integer) {
			int index = (Integer)e.getSource();
			onItemSelected(game, nvl, index, itemLabels[index], itemValues[index]);
		}
	}
	
	protected abstract void onItemSelected(Game game, Novel nvl, int index, String label, T value);
	
}
