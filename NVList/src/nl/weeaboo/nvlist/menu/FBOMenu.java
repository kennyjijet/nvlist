package nl.weeaboo.nvlist.menu;

import static nl.weeaboo.game.GameConfig.FBO;
import static nl.weeaboo.game.GameConfig.FBO_MIPMAP;

import java.awt.event.ActionEvent;

import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;

import nl.weeaboo.nvlist.Game;
import nl.weeaboo.settings.IConfig;
import nl.weeaboo.vn.impl.nvlist.Novel;

public class FBOMenu extends GameMenuAction {
	
	//Functions
	@Override
	protected JMenuItem createItem(Game game, Novel nvl) {
		JMenu menu = new JMenu("Frame buffer object (FBO)");
		
		ButtonGroup group = new ButtonGroup();
		
		JRadioButtonMenuItem autoItem = new JRadioButtonMenuItem("Automatic");
		autoItem.addActionListener(new SubItemActionListener(menu, 0));
		group.add(autoItem);
		menu.add(autoItem);

		menu.addSeparator();
		
		JRadioButtonMenuItem offItem = new JRadioButtonMenuItem("Off");
		offItem.addActionListener(new SubItemActionListener(menu, 1));
		group.add(offItem);
		menu.add(offItem);
		
		JRadioButtonMenuItem onItem = new JRadioButtonMenuItem("On");
		onItem.addActionListener(new SubItemActionListener(menu, 2));
		group.add(onItem);
		menu.add(onItem);
		
		JRadioButtonMenuItem onAndMipmapItem = new JRadioButtonMenuItem("On + Mipmap Minification");
		onAndMipmapItem.addActionListener(new SubItemActionListener(menu, 3));
		group.add(onAndMipmapItem);
		menu.add(onAndMipmapItem);
		
		IConfig config = game.getConfig();
		if ("true".equalsIgnoreCase(config.get(FBO))) {
			if (config.get(FBO_MIPMAP)) {
				onAndMipmapItem.setSelected(true);
			} else {
				onItem.setSelected(true);
			}
		} else if ("auto".equalsIgnoreCase(config.get(FBO))) {
			autoItem.setSelected(true);
		} else {
			offItem.setSelected(true);
		}
		
		return menu;
	}
	
	@Override
	public void actionPerformed(JMenuItem item, ActionEvent e, Game game, Novel nvl) {
		IConfig config = game.getConfig();
		if (e.getSource() instanceof Integer) {
			int index = (Integer)e.getSource();
			
			String fbo;
			
			switch (index) {
			case 0: fbo = "auto";  config.set(FBO_MIPMAP, true);  break;
			case 1: fbo = "false"; break;
			case 2: fbo = "true";  config.set(FBO_MIPMAP, false); break;
			case 3: fbo = "true";  config.set(FBO_MIPMAP, true);  break;
			default: return; //Invalid
			}
			
			if (fbo != null) {
				config.set(FBO, fbo);
			}
		}
	}

	//Getters
	
	//Setters
	
}
