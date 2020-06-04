package de.amr.statemachine.api;

@FunctionalInterface
public interface StateExitListener<S> extends StateChangeListener<S> {

	void onExit(S state);
}
