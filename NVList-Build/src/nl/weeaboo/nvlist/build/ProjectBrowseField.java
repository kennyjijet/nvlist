package nl.weeaboo.nvlist.build;

import java.awt.Color;
import java.awt.Font;

@SuppressWarnings("serial")
public class ProjectBrowseField extends FancyFileBrowseField {

	public ProjectBrowseField(Color bg) {
		super(true, true); 
		
		setNullFileValid(true);
		setLabel("Project Folder");
		setButtonText("Browse...");
		
		label.setFont(label.getFont().deriveFont(Font.BOLD));
		setLabelWidth(90);

		BuildGUIUtil.setTextFieldDefaults(field, bg);
	}
	
}