package de.amr.samples.fsm.lamp;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class Lamp extends JPanel {

	private Icon bulbOnImage;
	private Icon bulbOffImage;

	private void loadImages() {
		BufferedImage bulbs;
		try {
			bulbs = ImageIO.read(getClass().getResourceAsStream("/bulbs.png"));
			int w = bulbs.getWidth() / 2, h = bulbs.getHeight();
			bulbOffImage = new ImageIcon(bulbs.getSubimage(0, 0, w, h));
			bulbOnImage = new ImageIcon(bulbs.getSubimage(w, 0, w, h));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private final JLabel bulbView;
	private final JButton lightSwitch;

	public Lamp() {
		loadImages();
		bulbView = new JLabel("", bulbOffImage, JLabel.CENTER);
		lightSwitch = new JButton();
		lightSwitch.setText("Einschalten");
		lightSwitch.setIcon(new ImageIcon("assets/lightbulb.png"));
		setBackground(Color.BLACK);
		setLayout(new BorderLayout());
		add(bulbView, BorderLayout.CENTER);
		add(lightSwitch, BorderLayout.SOUTH);
	}

	public JButton getLightSwitch() {
		return lightSwitch;
	}

	public void switchOn() {
		bulbView.setIcon(bulbOnImage);
		lightSwitch.setText("Ausschalten");
		lightSwitch.setIcon(new ImageIcon("assets/lightbulb_off.png"));
	}

	public void switchOff() {
		bulbView.setIcon(bulbOffImage);
		lightSwitch.setText("Einschalten");
		lightSwitch.setIcon(new ImageIcon("assets/lightbulb.png"));
	}
}
