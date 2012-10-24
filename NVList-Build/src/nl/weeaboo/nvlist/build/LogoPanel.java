package nl.weeaboo.nvlist.build;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

@SuppressWarnings("serial")
public class LogoPanel extends JPanel {

	private final BufferedImage headerI;
	
	public LogoPanel(String headerImagePath) {
		headerI = getImageRes(headerImagePath);
		setBackground(new Color(headerI.getRGB(headerI.getWidth()/2, headerI.getHeight()-1)));
	
		setBorder(new EmptyBorder(10, 10, 10, 10));
		setPreferredSize(new Dimension(750, 550));
		setLayout(new BorderLayout(5, 5));
	}
	
	//Functions
	@Override
	protected void paintComponent(Graphics graphics) {
		Graphics2D g = (Graphics2D)graphics;
		
		int w = getWidth();
		int h = getHeight();

		g.setBackground(getBackground());
		g.clearRect(0, 0, w, h);
		
		if (headerI != null) {
			int iw = headerI.getWidth();
			int ih = headerI.getHeight();
			g.drawImage(headerI, 0, 0, iw, ih, this);
			if (w > iw) {
				g.drawImage(headerI, iw, 0, w, ih, iw-1, 0, iw, ih, this);
			}
		}
	}
	
	//Getters
	protected static BufferedImage getImageRes(String filename) {
		try {
			return ImageIO.read(LogoPanel.class.getResource("res/" + filename));
		} catch (IOException e) {
			return new BufferedImage(16, 16, BufferedImage.TYPE_INT_RGB);
		}		
	}
	
	//Setters
	
}
