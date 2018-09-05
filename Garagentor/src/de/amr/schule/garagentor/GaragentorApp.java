package de.amr.schule.garagentor;

import java.awt.Color;

import de.amr.easy.game.Application;

/**
 * Simuliert unser Garagentor.
 * 
 * @author Armin Reichert, Anna und Peter Schillo
 */
public class GaragentorApp extends Application {

	public static void main(String[] args) {
		launch(new GaragentorApp());
	}

	public GaragentorApp() {
		settings.title = "Garagentor Simulation";
		settings.width = 800;
		settings.height = 600;
		settings.bgColor = Color.WHITE;
		CLOCK.setFrequency(10);
	}

	@Override
	public void init() {
		setController(new GaragentorScene(this));
	}
}