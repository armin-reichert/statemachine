package de.amr.statemachine.core;

import static de.amr.statemachine.api.EventMatchStrategy.BY_CLASS;

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
	final boolean timeoutEvent;
	final Object eventSlot;

	public Transition(StateMachine<?, ?> fsm, S from, S to, BooleanSupplier guard, Consumer<E> action, Object eventSlot,
			boolean timeoutEvent) {
		this.fsm = fsm;
		this.from = from;
		this.to = to;
		this.guard = guard != null ? guard : () -> true;
		this.action = action != null ? action : e -> {
		};
		this.eventSlot = eventSlot;
		this.timeoutEvent = timeoutEvent;
	}

	@SuppressWarnings("unchecked")
	public E eventValue() {
		return (E) eventSlot;
	}

	@SuppressWarnings("unchecked")
	public Class<? extends E> eventClass() {
		return (Class<? extends E>) eventSlot;
	}

	@Override
	public String toString() {
		String eventText = fsm.getMatchStrategy() == BY_CLASS ? eventClass().getSimpleName() : String.valueOf(eventValue());
		return String.format("\n(%s)--[%s]-->(%s)", from, eventText, to);
	}
}