package de.amr.schule.garagentor;

import java.awt.Graphics2D;

import de.amr.easy.game.view.Controller;
import de.amr.easy.game.view.View;

public class GaragentorScene implements View, Controller {

	private GaragentorApp app;
	private Garagentor tor;

	public GaragentorScene(GaragentorApp app) {
		this.app = app;
	}

	public int getWidth() {
		return app.settings.width;
	}

	public int getHeight() {
		return app.settings.height;
	}

	@Override
	public void init() {
		tor = new Garagentor(app);
		tor.tf.setY(getHeight() - 100);
		tor.init();
	}

	@Override
	public void update() {
		tor.update();
	}

	@Override
	public void draw(Graphics2D g) {
		tor.draw(g);
	}
}