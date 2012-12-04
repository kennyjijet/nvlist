package nl.weeaboo.nvlist.build;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import nl.weeaboo.awt.FileBrowseField;

@SuppressWarnings("serial")
public class FancyFileBrowseField extends FileBrowseField {

	public FancyFileBrowseField(boolean justFolders, boolean write) {
		super(justFolders, write);
		
		field.setForeground(Color.BLACK);
		
		final FocusListener focusListener = new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				onFocusChanged();
			}
			@Override
			public void focusLost(FocusEvent e) {
				onFocusChanged();
			}
		};
		
		field.addFocusListener(focusListener);
		button.addFocusListener(focusListener);
		
		int height = 23;
		button.setPreferredSize(new Dimension(4 * height, height));
		field.setPreferredSize(new Dimension(50, height));
		onFocusChanged();
	}

	//Functions
	private void onFocusChanged() {
		button.setVisible(field.isFocusOwner() || button.isFocusOwner() || button.getModel().isRollover());
	}
	
	//Getters
	
	//Setters
	
}
