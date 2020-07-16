package de.amr.statemachine.core;

import static de.amr.statemachine.api.TransitionMatchStrategy.BY_CLASS;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/**
 * Representation of a state transition.
 * 
 * @param <S> state identifier type
 * @param <E> event type
 * 
 * @author Armin Reichert
 */
class Transition<S, E> {

	final StateMachine<?, ?> fsm;
	final S from;
	final S to;
	final BooleanSupplier guard;
	final Consumer<E> action;
	final boolean timeoutTriggered;
	final Object eventValueOrClass;

	public Transition(StateMachine<?, ?> fsm, S from, S to, BooleanSupplier guard, Consumer<E> action,
			Object eventValueOrClass, boolean timeoutTriggered) {
		this.fsm = fsm;
		this.from = from;
		this.to = to;
		this.guard = guard != null ? guard : () -> true;
		this.action = action != null ? action : e -> {
		};
		this.eventValueOrClass = eventValueOrClass;
		this.timeoutTriggered = timeoutTriggered;
	}

	@SuppressWarnings("unchecked")
	public E eventValue() {
		return (E) eventValueOrClass;
	}

	@SuppressWarnings("unchecked")
	public Class<? extends E> eventClass() {
		return (Class<? extends E>) eventValueOrClass;
	}

	@Override
	public String toString() {
		String eventText = timeoutTriggered ? "timeout" : "condition";
		if (eventValueOrClass != null) {
			eventText = fsm.getMatchStrategy() == BY_CLASS ? eventClass().getSimpleName() : String.valueOf(eventValue());
		}
		return String.format("\n(%s)--[%s]-->(%s)", from, eventText, to);
	}
}