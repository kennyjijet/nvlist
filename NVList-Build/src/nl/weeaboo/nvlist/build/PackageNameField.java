package nl.weeaboo.nvlist.build;

import nl.weeaboo.awt.DirectValidatingField;

public class PackageNameField extends DirectValidatingField {

	private static final long serialVersionUID = 1L;

	@Override
	public boolean isValid(String text) {
		return BuildUtil.isValidPackage(text);
	}

	@Override
	protected void onValidTextEntered(String text) {
	}
	
}