package de.amr.schule.markise;

import java.awt.event.KeyEvent;

import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.view.Controller;

public class WindSensor implements Controller {

	private float windSpeed;

	public boolean esIstWindig() {
		return windSpeed > 10;
	}

	@Override
	public void init() {
		windSpeed = 0;
	}

	@Override
	public void update() {
		if (Keyboard.keyDown(KeyEvent.VK_W)) {
			windSpeed += 1;
		} else if (Keyboard.keyDown(KeyEvent.VK_Q)) {
			windSpeed -= 1;
			if (windSpeed <= 0) {
				windSpeed = 0;
			}
		}
	}
}