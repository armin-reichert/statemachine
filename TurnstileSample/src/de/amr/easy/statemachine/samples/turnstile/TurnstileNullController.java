package de.amr.easy.statemachine.samples.turnstile;

/**
 * Controller which does nothing (null object).
 * 
 * @author Armin Reichert
 *
 */
public class TurnstileNullController implements TurnstileController {

	@Override
	public void lock() {

	}

	@Override
	public void unlock() {

	}

	@Override
	public void thankyou() {

	}

	@Override
	public void alarm() {

	}
}
