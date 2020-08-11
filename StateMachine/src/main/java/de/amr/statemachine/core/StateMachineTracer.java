package de.amr.statemachine.core;

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import de.amr.statemachine.api.Log;

/**
 * A tracer for the state machine operations.
 * 
 * <p>
 * Removed Java logger because this completely sucks (or I am too stupid).
 * 
 * @author Armin Reichert
 *
 * @param <S> state identifier type
 * @param E   event type
 */
public class StateMachineTracer<S, E> {

	private LogImpl logger = new LogImpl();

	/**
	 * Converts ticks into seconds. Default is 60 ticks per second.
	 */
	public Function<Long, Float> fnTicksToSeconds = ticks -> ticks / 60f;

	/**
	 * Predicates defining which inputs/events are not getting logged.
	 */
	public final List<Predicate<E>> eventLoggingBlacklist = new ArrayList<>();

	/**
	 * @return the logger used for tracing
	 */
	public Log getLogger() {
		return logger;
	}

	/**
	 * Adds an input/event predicate to the blacklist.
	 * 
	 * @param predicate defining when not log an event
	 */
	public void doNotLog(Predicate<E> predicate) {
		eventLoggingBlacklist.add(predicate);
	}

	private void logEventInfo(E event, Supplier<String> fnMessage) {
		if (eventLoggingBlacklist.stream().noneMatch(condition -> condition.test(event))) {
			logger.loginfo(fnMessage.get());
		}
	}

	public void logStateCreated(StateMachine<S, E> fsm, S id) {
		logger.loginfo("%s created state '%s'", fsm.getDescription(), id);
	}

	public void logStateTimerReset(StateMachine<S, E> fsm, S id) {
		logger.loginfo("%s did reset timer for state '%s'", fsm.getDescription(), id);
	}

	public void logUnhandledEvent(StateMachine<S, E> fsm, E event) {
		logger.loginfo("%s in state %s could not handle '%s'", fsm.getDescription(), fsm.getState(), event);
	}

	public void logPublishedEvent(StateMachine<S, E> fsm, E event) {
		logger.loginfo("%s published event %s", fsm.getDescription(), event);
	}

	public void logEnteringInitialState(StateMachine<S, E> fsm, S id) {
		logger.loginfo("%s enters initial state", fsm.getDescription());
		logEnteringState(fsm, id);
	}

	public void logEnteringState(StateMachine<S, E> fsm, S id) {
		State<S> stateEntered = fsm.state(id);
		if (stateEntered.hasTimer()) {
			long duration = stateEntered.getDuration();
			float seconds = fnTicksToSeconds.apply(duration);
			logger.loginfo("%s enters state '%s' for %.2f seconds (%d ticks)", fsm.getDescription(), id, seconds, duration);
		} else {
			logger.loginfo("%s enters state '%s'", fsm.getDescription(), id);
		}
	}

	public void logExitingState(StateMachine<S, E> fsm, S id) {
		logger.loginfo("%s exits state  '%s'", fsm.getDescription(), id);
	}

	public void logFiringTransition(StateMachine<S, E> fsm, Transition<S, E> t, Optional<E> event) {
		if (!event.isPresent()) {
			if (t.from != t.to) {
				if (t.timeoutTriggered) {
					logger.loginfo("%s changes from  '%s' to '%s (timeout)'", fsm.getDescription(), t.from, t.to);
				} else {
					logger.loginfo("%s changes from  '%s' to '%s'", fsm.getDescription(), t.from, t.to);
				}
			} else {
				logger.loginfo("%s stays '%s'", fsm.getDescription(), t.from);
			}
		} else {
			if (t.from != t.to) {
				logEventInfo(event.get(),
						() -> format("%s changes from '%s' to '%s' on '%s'", fsm.getDescription(), t.from, t.to, event.get()));
			} else {
				logEventInfo(event.get(), () -> format("%s stays '%s' on '%s'", fsm.getDescription(), t.from, event.get()));
			}
		}
	}
}