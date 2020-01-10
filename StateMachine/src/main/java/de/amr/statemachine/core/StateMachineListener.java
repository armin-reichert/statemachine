package de.amr.statemachine.core;

public interface StateMachineListener<S, E> {

	void stateCreated(S state);
	
	void stateTimerReset(S state);

	void unhandledEvent(E event);

	void enteringInitialState(S initialState);

	void enteringState(S enteredState);

	void exitingState(S exitedState);

	void firingTransition(Transition<S, E> t, E event);
}
