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
	final MatchCondition<E> matchCondition;

	Transition(S from, S to, BooleanSupplier guard, Consumer<E> action, MatchCondition<E> matchCondition,
			boolean timeout) {
		this.from = from;
		this.to = to;
		this.guard = guard;
		this.action = action;
		this.matchCondition = matchCondition;
		this.timeout = timeout;
	}
}