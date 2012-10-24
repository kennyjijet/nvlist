package nl.weeaboo.nvlist.menu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractButton;

class SubItemActionListener implements ActionListener {

	private final AbstractButton parent;
	private final int index;
	
	public SubItemActionListener(AbstractButton p, int idx) {
		parent = p;
		index = idx;
	}
	
	public void actionPerformed(ActionEvent e) {
		e = new ActionEvent(index, e.getID(), e.getActionCommand());
		for (ActionListener al : parent.getActionListeners()) {
			al.actionPerformed(e);
		}
	}
	
}