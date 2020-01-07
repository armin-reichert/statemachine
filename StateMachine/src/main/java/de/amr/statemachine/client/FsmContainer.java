package de.amr.statemachine.client;

import java.util.function.Consumer;

import de.amr.statemachine.core.State;

/**
 * This interface is implemented by entities that implement the
 * {@link Fsm} interface by delegating to a component implementing
 * that interface.
 * 
 * @author Armin Reichert
 *
 * @param <S> state type of the finite-state machine
 * @param <E> event type of the finite-state machine
 */
public interface FsmContainer<S, E> extends Fsm<S, E> {

	/**
	 * The component (delegate) implementing the {@link Fsm} interface.
	 * 
	 * @return delegate component
	 */
	FsmComponent<S, E> fsmComponent();

	@Override
	default void addEventListener(Consumer<E> listener) {
		fsmComponent().addEventListener(listener);
	}

	@Override
	default void removeEventListener(Consumer<E> listener) {
		fsmComponent().removeEventListener(listener);
	}

	@Override
	default void publish(E event) {
		fsmComponent().publish(event);
	}

	@Override
	default void setState(S state) {
		fsmComponent().setState(state);
	}

	@Override
	default S getState() {
		return fsmComponent().getState();
	}

	@Override
	@SuppressWarnings("unchecked")
	default boolean is(S... states) {
		return fsmComponent().is(states);
	}

	@Override
	default State<S, E> state() {
		return fsmComponent().state();
	}

	@Override
	default State<S, E> state(S state) {
		return fsmComponent().state(state);
	}

	@Override
	default void process(E event) {
		fsmComponent().process(event);
	}
}