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
class TransitionImpl<S, E> implements Transition<S, E> {

	private final Consumer<E> NULL_ACTION = e -> {

	};

	private final StateMachine<S, E> sm;
	private final S from;
	private final S to;
	private final BooleanSupplier guard;
	private final Consumer<E> action;
	private final Class<? extends E> eventType;
	private final boolean timeout;

	public TransitionImpl(StateMachine<S, E> sm, S from, S to, BooleanSupplier guard, Consumer<E> action,
			Class<? extends E> eventType, boolean timeout) {
		Objects.requireNonNull(sm);
		this.sm = sm;
		Objects.requireNonNull(from);
		this.from = from;
		Objects.requireNonNull(to);
		this.to = to;
		this.guard = guard == null ? () -> true : guard;
		this.action = action == null ? NULL_ACTION : action;
		this.eventType = eventType;
		this.timeout = timeout;
	}

	public boolean canFire(E event) {
		if (!guard.getAsBoolean()) {
			return false;
		}
		if (timeout) {
			return sm.state(from).isTerminated();
		}
		if (eventType != null) {
			return event != null && eventType.equals(event.getClass());
		}
		return true;
	}

	public Consumer<E> action() {
		return action;
	}

	public S from() {
		return from;
	}

	public S to() {
		return to;
	}

	@Override
	public State<S, E> getSourceState() {
		return sm.state(from);
	}

	@Override
	public State<S, E> getTargetState() {
		return sm.state(to);
	}
}