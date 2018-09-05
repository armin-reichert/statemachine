package de.amr.schule.markise;

import java.awt.Graphics2D;

import de.amr.easy.game.view.Controller;
import de.amr.easy.game.view.View;

public class MarkiseScene implements View, Controller {

	private MarkiseApp app;
	private Markise markise;
	private Fernbedienung remote;

	public MarkiseScene(MarkiseApp app) {
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
		markise = new Markise(app);
		remote = new Fernbedienung(app, markise);
		markise.init();
		markise.tf.setY(getHeight() - 100);
	}

	@Override
	public void update() {
		remote.update();
		markise.update();
	}

	@Override
	public void draw(Graphics2D pen) {
		markise.draw(pen);
		remote.draw(pen);
	}
}