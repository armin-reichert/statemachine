package de.amr.statemachine.api;

import de.amr.statemachine.core.State;

@FunctionalInterface
public interface StateEntryListener<S> {

	void onEntry(State<S> state);
}
