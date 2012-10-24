package nl.weeaboo.nvlist.build;

import java.awt.Color;
import java.awt.Font;

@SuppressWarnings("serial")
public class EngineBrowseField extends FancyFileBrowseField {

	public EngineBrowseField(Color bg) {
		super(true, false);
		
		setLabel("NVList Folder");		
		setButtonText("Browse...");
		
		label.setFont(label.getFont().deriveFont(Font.BOLD));
		setLabelWidth(90);

		BuildGUIUtil.setTextFieldDefaults(field, bg);
	}
	
}