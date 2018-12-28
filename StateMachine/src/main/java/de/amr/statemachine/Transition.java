package de.amr.statemachine;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/**
 * Representation of a state transition.
 * 
 * @author Armin Reichert
 *
 * @param <S>
 *          state identifier type
 * @param <E>
 *          event type
 */
class Transition<S, E> {

	final S from;
	final S to;
	final BooleanSupplier guard;
	final Consumer<E> action;
	final boolean timeout;
	final E event;
	final Class<? extends E> eventClass;

	public Transition(S from, S to, BooleanSupplier guard, Consumer<E> action, E event,
			Class<? extends E> eventClass, boolean timeout) {
		this.from = from;
		this.to = to;
		this.guard = guard;
		this.action = action;
		this.event = event;
		this.eventClass = eventClass;
		this.timeout = timeout;
	}
}