package de.amr.statemachine.api;

import de.amr.statemachine.core.State;

@FunctionalInterface
public interface StateExitListener<S> {

	void onExit(State<S> state);
}
