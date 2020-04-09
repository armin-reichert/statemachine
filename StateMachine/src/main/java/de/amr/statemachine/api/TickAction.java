package de.amr.statemachine.api;

import de.amr.statemachine.core.State;

@FunctionalInterface
public interface TickAction<S> extends Runnable {

	@Override
	default void run() {
		run(null, 0, 0);
	}

	void run(State<S> state, int ticksConsumed, int ticksRemaining);
}