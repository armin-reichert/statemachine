package de.amr.statemachine;

import java.util.Objects;
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

	private final Consumer<E> NULL_ACTION = e -> {

	};

	final StateMachine<S, E> sm;
	final S from;
	final S to;
	final BooleanSupplier guard;
	final Consumer<E> action;
	final boolean timeout;
	final MatchCondition<S, E> matchCondition;

	public Transition(StateMachine<S, E> sm, S from, S to, BooleanSupplier guard, Consumer<E> action,
			MatchCondition<S, E> matchCondition, boolean timeout) {
		Objects.requireNonNull(sm);
		this.sm = sm;
		Objects.requireNonNull(from);
		this.from = from;
		Objects.requireNonNull(to);
		this.to = to;
		this.guard = guard == null ? () -> true : guard;
		this.action = action == null ? NULL_ACTION : action;
		this.matchCondition = matchCondition;
		this.timeout = timeout;
	}
}