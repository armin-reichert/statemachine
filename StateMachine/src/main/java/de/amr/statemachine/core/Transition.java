package de.amr.statemachine.core;

import static de.amr.statemachine.api.TransitionMatchStrategy.BY_CLASS;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Representation of a state transition.
 * 
 * @param <S> state identifier type
 * @param <E> event type
 * 
 * @author Armin Reichert
 */
public class Transition<S, E> {

	public final StateMachine<?, ?> fsm;
	public final S from;
	public final S to;
	public final BooleanSupplier guard;
	public final Consumer<E> action;
	public final boolean timeoutTriggered;
	public final Object eventValueOrClass;
	public final Supplier<String> fnAnnotation;

	public Transition(StateMachine<?, ?> fsm, S from, S to, BooleanSupplier guard, Consumer<E> action,
			Object eventValueOrClass, boolean timeoutTriggered, Supplier<String> fnAnnotation) {
		this.fsm = fsm;
		this.from = from;
		this.to = to;
		this.guard = guard != null ? guard : () -> true;
		this.action = action != null ? action : e -> {
		};
		this.eventValueOrClass = eventValueOrClass;
		this.timeoutTriggered = timeoutTriggered;
		this.fnAnnotation = fnAnnotation == null ? () -> null : fnAnnotation;
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
		String text = timeoutTriggered ? "timeout" : "";
		if (eventValueOrClass != null) {
			text = fsm.getMatchStrategy() == BY_CLASS ? eventClass().getSimpleName() : String.valueOf(eventValue());
		}
		if (fnAnnotation != null) {
			text += "[" + fnAnnotation.get() + "]";
		}
		return String.format("\n(%s)--[%s]-->(%s)", from, text, to);
	}
}