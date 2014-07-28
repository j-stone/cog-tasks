package jms.cogtasks.helperClasses;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class iconPanel extends JPanel {
	
	private int degrees;
	boolean underRotated;
	
	public iconPanel(ImageIcon p, int d, int width, int height, boolean underRotated) {
		super();
		this.underRotated = underRotated;
		this.setPreferredSize(new Dimension(width,height));
		this.setBackground(Color.WHITE);
		this.degrees = d;
		
		Image img = p.getImage();  
		Image newimg = img.getScaledInstance(100, 100, java.awt.Image.SCALE_SMOOTH);  
		ImageIcon scaledIcon = new ImageIcon(newimg);
		
		
		RotatedIcon ri = new RotatedIcon(scaledIcon, d);
		
		JLabel picLabel = new JLabel(ri);
		
		this.add(picLabel);

	}
	
	public int getDegree() {
		if (this.underRotated)
			return this.degrees + 45;
		else
			return this.degrees;
	}
}
