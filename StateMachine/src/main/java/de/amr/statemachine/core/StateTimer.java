package de.amr.statemachine.core;

import java.util.Objects;
import java.util.function.IntSupplier;

/**
 * A timer used inside the states.
 * 
 * @author Armin Reichert
 */
class StateTimer {

	static final StateTimer NEVER_ENDING_TIMER = new StateTimer() {

		{
			fnDuration = () -> Integer.MAX_VALUE;
			remaining = duration = Integer.MAX_VALUE;
		}

		@Override
		boolean tick() {
			return false;
		}
	};

	/** Function providing the duration (in ticks) of this timer. */
	IntSupplier fnDuration;

	/** The total number of ticks of this timer. */
	int duration;

	/** Ticks remaining until the timer expires. */
	int remaining;

	private StateTimer() {
	}

	public StateTimer(IntSupplier fnDuration) {
		this.fnDuration = Objects.requireNonNull(fnDuration);
		// NOTE: in general, the duration function cannot yet be called at this point in time
	}

	/**
	 * Resets the timer and restarts it.
	 */
	void reset() {
		remaining = duration = fnDuration.getAsInt();
	}

	/**
	 * @return {@code true} if the timer has timed out just after this tick
	 */
	boolean tick() {
		if (duration != Integer.MAX_VALUE && remaining > 0) {
			--remaining;
			if (remaining == 0) {
				return true;
			}
		}
		return false;
	}
}