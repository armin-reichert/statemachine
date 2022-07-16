package de.amr.statemachine.core;

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.LongFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A tracer for the state machine operations.
 * 
 * @author Armin Reichert
 *
 * @param <S> state identifier type
 * @param E   event type
 */
public class Tracer<S, E> {

	private static final Logger LOGGER = LogManager.getFormatterLogger();

	/**
	 * Converts ticks into seconds. Default is 60 ticks per second.
	 */
	public LongFunction<Float> fnTicksToSeconds = ticks -> ticks / 60f;

	/**
	 * Predicates defining which inputs/events are not getting logged.
	 */
	public final List<Predicate<E>> eventLoggingBlacklist = new ArrayList<>();

	/**
	 * Adds an input/event predicate to the blacklist.
	 * 
	 * @param predicate defining when not log an event
	 */
	public void doNotLog(Predicate<E> predicate) {
		eventLoggingBlacklist.add(predicate);
	}

	private void eventInfo(E event, Supplier<String> fnMessage) {
		if (eventLoggingBlacklist.stream().noneMatch(condition -> condition.test(event))) {
			LOGGER.trace(fnMessage.get());
		}
	}

	public void stateCreated(StateMachine<S, E> fsm, S id) {
		LOGGER.trace("%s created state '%s'", fsm.getDescription(), id);
	}

	public void stateTimerReset(StateMachine<S, E> fsm, S id) {
		LOGGER.trace("%s did reset timer for state '%s'", fsm.getDescription(), id);
	}

	public void unhandledEvent(StateMachine<S, E> fsm, E event) {
		LOGGER.trace("%s in state %s could not handle '%s'", fsm.getDescription(), fsm.getState(), event);
	}

	public void publishedEvent(StateMachine<S, E> fsm, E event) {
		LOGGER.trace("%s published event %s", fsm.getDescription(), event);
	}

	public void enteringInitialState(StateMachine<S, E> fsm, S id) {
		LOGGER.trace("%s enters initial state", fsm.getDescription());
		enteringState(fsm, id);
	}

	public void enteringState(StateMachine<S, E> fsm, S id) {
		State<S> stateEntered = fsm.state(id);
		if (stateEntered.hasTimer()) {
			long duration = stateEntered.getDuration();
			float seconds = fnTicksToSeconds.apply(duration);
			LOGGER.trace("%s enters state '%s' for %.2f seconds (%d ticks)", fsm.getDescription(), id, seconds, duration);
		} else {
			LOGGER.trace("%s enters state '%s'", fsm.getDescription(), id);
		}
	}

	public void exitingState(StateMachine<S, E> fsm, S id) {
		LOGGER.trace("%s exits state  '%s'", fsm.getDescription(), id);
	}

	public void firingTransition(StateMachine<S, E> fsm, Transition<S, E> t, Optional<E> event) {
		if (!event.isPresent()) {
			if (t.from != t.to) {
				if (t.timeoutTriggered) {
					LOGGER.trace("%s changes from  '%s' to '%s (timeout)'", fsm.getDescription(), t.from, t.to);
				} else {
					LOGGER.trace("%s changes from  '%s' to '%s'", fsm.getDescription(), t.from, t.to);
				}
			} else {
				LOGGER.trace("%s stays '%s'", fsm.getDescription(), t.from);
			}
		} else {
			if (t.from != t.to) {
				eventInfo(event.get(),
						() -> format("%s changes from '%s' to '%s' on '%s'", fsm.getDescription(), t.from, t.to, event.get()));
			} else {
				eventInfo(event.get(), () -> format("%s stays '%s' on '%s'", fsm.getDescription(), t.from, event.get()));
			}
		}
	}
}