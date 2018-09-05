package de.amr.samples.marbletoy;

import java.awt.Color;

import de.amr.easy.game.Application;
import de.amr.samples.marbletoy.entities.MarbleToy;
import de.amr.samples.marbletoy.fsm.LeverControl;

public class MarbleToySimulation extends Application {

	public static void main(String[] args) {
		launch(new MarbleToySimulation());
	}

	public MarbleToySimulation() {
		settings.title = "Marble Toy State Machine";
		settings.width = 600;
		settings.height = 410;
		settings.bgColor = Color.WHITE;
	}

	@Override
	public void init() {
		MarbleToy toy = new MarbleToy();
		toy.setLeverControl(new LeverControl(toy));
		setController(toy);
	}
}