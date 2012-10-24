package nl.weeaboo.nvlist.build;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JPanel;

@SuppressWarnings("serial")
public class TranslucentPanel extends JPanel {

	public TranslucentPanel() {
		setOpaque(false);
	}
	
	//Functions
	@Override
	public void paintComponent(Graphics g) {
		Color bg = new Color(0x40FFFFFF, true);
		g.setColor(bg);
		g.fillRect(0, 0, getWidth(), getHeight());
	}
	
	//Getters
	
	//Setters
	
}
