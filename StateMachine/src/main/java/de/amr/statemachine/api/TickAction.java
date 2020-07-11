package de.amr.statemachine.api;

import de.amr.statemachine.core.State;

@FunctionalInterface
public interface TickAction<S> {

	void run(State<S> state, int ticksConsumed, int ticksRemaining);
}