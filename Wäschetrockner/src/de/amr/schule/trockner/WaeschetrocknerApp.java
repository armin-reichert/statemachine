package de.amr.schule.trockner;

import de.amr.easy.game.Application;

public class WaeschetrocknerApp extends Application {

	public static void main(String[] args) {
		launch(new WaeschetrocknerApp());
	}

	public WaeschetrocknerApp() {
		settings.title = "Wäschetrockner Simulation";
		settings.width = 800;
		settings.height = 360;
	}

	@Override
	public void init() {
		setController(new WaeschetrocknerUI(settings.width, settings.height, new Waeschetrockner()));
	}
}