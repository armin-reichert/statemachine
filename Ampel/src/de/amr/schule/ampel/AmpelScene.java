package de.amr.schule.ampel;

import java.awt.Graphics2D;

import de.amr.easy.game.view.Controller;
import de.amr.easy.game.view.View;

public class AmpelScene implements View, Controller {

	private int width;
	private int height;
	private Ampel ampel;

	public AmpelScene(int width, int height) {
		this.width = width;
		this.height = height;
	}

	@Override
	public void init() {
		ampel = new Ampel(140, 450);
		ampel.init();
	}

	@Override
	public void update() {
		ampel.update();
	}

	@Override
	public void draw(Graphics2D g) {
		ampel.tf.center(width, height);
		ampel.draw(g);
	}
}