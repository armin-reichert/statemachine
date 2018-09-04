package de.amr.statemachine;

/**
 * Mixin for state machine controlled game entities.
 * 
 * @author Armin Reichert
 *
 * @param <S>
 *          state identifier type
 * @param <E>
 *          event type
 */
public interface StateMachineClient<S, E> {

	StateMachine<S, E> getStateMachine();

	default S getState() {
		return getStateMachine().getState();
	}

	default void setState(S state) {
		getStateMachine().setState(state);
	}

	default State<S, E> getStateObject() {
		return getStateMachine().state();
	}

	default void processEvent(E e) {
		getStateMachine().process(e);
	}
}
