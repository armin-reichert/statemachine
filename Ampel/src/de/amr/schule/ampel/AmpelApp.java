package de.amr.schule.ampel;

import de.amr.easy.game.Application;

/**
 * Simuliert eine Ampel mithilfe eines Zustandsautomaten.
 * 
 * @author Armin Reichert & Anna Schillo
 */
public class AmpelApp extends Application {

	public static void main(String[] args) {
		launch(new AmpelApp());
	}

	public AmpelApp() {
		settings.title = "Ampel Simulation";
		settings.height = 600;
		settings.width = 600;
	}

	@Override
	public void init() {
		setController(new AmpelScene(600,600));
	}
}