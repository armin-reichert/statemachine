package de.amr.statemachine.core;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * A timer used inside the states.
 * 
 * @author Armin Reichert
 */
class StateTimer {

	/** Function providing the duration (in ticks) of this timer. */
	protected Supplier<Long> fnDuration;

	/** The total number of ticks of this timer. */
	protected long duration;

	/** Ticks remaining until the timer expires. */
	protected long remaining;

	public StateTimer(Supplier<Long> fnDuration) {
		this.fnDuration = Objects.requireNonNull(fnDuration);
		// NOTE: in general, reset() cannot yet be called at this point in time!
	}

	/**
	 * Resets the timer and restarts it.
	 */
	public void reset() {
		remaining = duration = fnDuration.get();
	}

	/**
	 * @return {@code true} if the timer has timed out just after this tick
	 */
	public boolean tick() {
		return remaining > 0 ? --remaining == 0 : false;
	}
}