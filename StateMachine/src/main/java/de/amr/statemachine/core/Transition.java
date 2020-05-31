package de.amr.statemachine.core;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/**
 * Representation of a state transition.
 * 
 * <p>
 * The event value and event class fields are exclusive. If transitions are matched by event value,
 * the {@code eventValue} field is used, if they are matched by the event class, the
 * {@code eventClass} field is used.
 *
 * @param <S> state identifier type
 * @param <E> event type
 * 
 * @author Armin Reichert
 */
class Transition<S, E> {

	final S from;
	final S to;
	final BooleanSupplier guard;
	final Consumer<E> action;
	final boolean timeoutEvent;
	final E eventValue;
	final Class<? extends E> eventClass;

	public Transition(S from, S to, BooleanSupplier guard, Consumer<E> action, E eventValue,
			Class<? extends E> eventClass, boolean timeoutEvent) {
		this.from = from;
		this.to = to;
		this.guard = guard != null ? guard : () -> true;
		this.action = action != null ? action : e -> {
		};
		this.eventValue = eventValue;
		this.eventClass = eventClass;
		this.timeoutEvent = timeoutEvent;
	}

	@Override
	public String toString() {
		String eventText = eventClass != null ? eventClass.getSimpleName() : String.valueOf(eventValue);
		return String.format("\n(%s)--[%s]-->(%s)", from, eventText, to);
	}
}