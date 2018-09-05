package de.amr.schule.markise;

import java.awt.Color;

import de.amr.easy.game.Application;

public class MarkiseApp extends Application {

	public static void main(String[] args) {
		launch(new MarkiseApp());
	}

	public MarkiseApp() {
		settings.title = "Markise Simulation";
		settings.width = 800;
		settings.height = 600;
		settings.bgColor = Color.WHITE;
		CLOCK.setFrequency(5);
	}

	@Override
	public void init() {
		setController(new MarkiseScene(this));
	}
}