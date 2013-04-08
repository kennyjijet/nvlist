package nl.weeaboo.nvlist.menu;

import static nl.weeaboo.game.GameConfig.IMAGE_FOLDER_MAX;
import static nl.weeaboo.game.GameConfig.IMAGE_FOLDER_MIN;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.JMenuItem;

import nl.weeaboo.image.FileResolutionOption;
import nl.weeaboo.image.FileResolutionPicker;
import nl.weeaboo.image.ResolutionOption;
import nl.weeaboo.nvlist.Game;
import nl.weeaboo.settings.Preference;
import nl.weeaboo.vn.impl.nvlist.Novel;

public class ImageFolderSelectorMenu extends GameMenuAction {
	
	private JGameMenu gmenu;
	
	public ImageFolderSelectorMenu() {
	}
	
	//Functions
	@Override
	protected JMenuItem createItem(Game game, Novel nvl) {
		if (gmenu == null) {		
			gmenu = new JGameMenu(game, "Image folder selection", '\0');
			
			//Generate a list of possible image folder resolutions
			FileResolutionPicker picker = game.getImageResolutionPicker();
			List<FileResolutionOption> options = new ArrayList<FileResolutionOption>(picker.getOptions());
			Collections.sort(options, new Comparator<ResolutionOption>() {
				@Override
				public int compare(ResolutionOption a, ResolutionOption b) {
					int aw = (a != null ? a.getWidth() : 0);
					int bw = (b != null ? b.getWidth() : 0);
					return aw - bw;
				}
			});
			
			String[] labels = new String[1 + options.size()];
			Integer[] minValues = new Integer[labels.length];			
			Integer[] maxValues = new Integer[labels.length];			
			int t = 0;
			labels[t] = "Unlimited";
			minValues[t] = 0;
			maxValues[t] = 0;
			t++;
			for (ResolutionOption opt : options) {
				labels[t] = opt.getWidth() + "x" + opt.getHeight();
				minValues[t] = Math.min(opt.getWidth(), opt.getHeight());
				maxValues[t] = Math.max(opt.getWidth(), opt.getHeight());
				t++;
			}			
			
			//Create resolution menus
			ResolutionMenu minMenu = new ResolutionMenu(IMAGE_FOLDER_MIN, "Minimum Resolution", labels, minValues);
			gmenu.add(minMenu);
	
			ResolutionMenu maxMenu = new ResolutionMenu(IMAGE_FOLDER_MAX, "Maximum Resolution", labels, maxValues);
			gmenu.add(maxMenu);		
		}
		return gmenu;
	}

	@Override
	public void actionPerformed(JMenuItem item, ActionEvent e, Game game, Novel nvl) {		
	}
	
	@Override
	public <T> void onPropertyChanged(JMenuItem item, Preference<T> pref, T oldval, T newval) {
		gmenu.onPropertyChanged(pref, oldval, newval);
	}	
	
	//Getters
	
	//Setters
	
	//Inner Classes
	private static class ResolutionMenu extends PrefRangeMenu<Integer> {

		public ResolutionMenu(Preference<Integer> pref, String label, String[] labels, Integer[] values) {
			super(pref, label, '\0', labels, values);
		}
		
	}
	
}
