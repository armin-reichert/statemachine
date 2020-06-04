package de.amr.statemachine.api;

@FunctionalInterface
public interface StateEntryListener<S> extends StateChangeListener<S> {

	void onEntry(S state);
}
