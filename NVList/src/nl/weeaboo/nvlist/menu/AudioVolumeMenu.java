package nl.weeaboo.nvlist.menu;

import nl.weeaboo.settings.Preference;

public class AudioVolumeMenu extends PrefRangeMenu<Double> {

	private static final String labels[] = {
		"0%", "10%", "20%", "30%", "40%", "50%", "60%", "70%", "80%", "90%", "100%"
	};
	
	private static final Double values[] = {
		0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0
	};
	
	public AudioVolumeMenu(Preference<Double> pref, String label) {
		super(pref, label, '\0', labels, values);
	}
	
}
