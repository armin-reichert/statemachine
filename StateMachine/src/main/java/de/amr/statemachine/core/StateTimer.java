package de.amr.statemachine.core;

import java.util.Objects;
import java.util.function.IntSupplier;

class StateTimer {

	static final StateTimer NEVER_ENDING_TIMER = new StateTimer(() -> Integer.MAX_VALUE);

	/** Function providing the duration for this timer. */
	IntSupplier fnDuration;

	/** The number of ticks of this timer. */
	int duration;

	/** Ticks remaining until time-out. */
	int remaining;

	StateTimer(IntSupplier fnDuration) {
		this.fnDuration = Objects.requireNonNull(fnDuration);
	}

	void restart() {
		remaining = duration = fnDuration.getAsInt();
	}

	boolean tick() {
		if (duration != Integer.MAX_VALUE && remaining > 0) {
			--remaining;
			if (remaining == 0) {
				return true; // timeout
			}
		}
		return false;
	}
}