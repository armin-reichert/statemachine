package de.amr.easy.statemachine.samples.turnstile;

public interface TurnstileController {

	/**
	 * Locks the turnstile.
	 */
	void lock();

	/**
	 * Unlocks the turnstile.
	 */
	void unlock();

	/**
	 * Says thanks ;).
	 */
	void thankyou();

	/**
	 * Plays an alarm sound.
	 */
	void alarm();
}
