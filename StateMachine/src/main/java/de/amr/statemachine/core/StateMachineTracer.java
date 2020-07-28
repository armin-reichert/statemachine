package de.amr.statemachine.core;

import static java.lang.String.format;

import java.io.OutputStream;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

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

	/**
	 * Converts ticks into seconds. Default is 60 ticks per second.
	 */
	public Function<Long, Float> fnTicksToSeconds = ticks -> ticks / 60f;

	/**
	 * Output destination.
	 */
	private PrintStream out = System.err;

	/**
	 * Predicates defining which inputs/events are not getting logged.
	 */
	public final List<Predicate<E>> eventLoggingBlacklist = new ArrayList<>();

	/**
	 * Logs the message produced by the given supplier.
	 * 
	 * @param fnMessage message supplier
	 */
	public void loginfo(Supplier<String> fnMessage) {
		LocalDateTime now = LocalDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SSS");
		String timestamp = formatter.format(now);
		out.println(String.format("[%s] %s", timestamp, fnMessage.get()));
	}

	public StateMachineTracer() {
		shutUp(true);
	}

	/**
	 * Tells the tracer to shut up its mouth.
	 * 
	 * @param shutUp if should shut up
	 */
	public void shutUp(boolean shutUp) {
		out = shutUp ? new PrintStream(OutputStream.nullOutputStream()) : System.err;
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
			loginfo(fnMessage);
		}
	}

	public void logStateCreated(StateMachine<S, E> fsm, S id) {
		loginfo(() -> format("%s created state '%s'", fsm.getDescription(), id));
	}

	public void logStateTimerReset(StateMachine<S, E> fsm, S id) {
		loginfo(() -> format("%s did reset timer for state '%s'", fsm.getDescription(), id));
	}

	public void logUnhandledEvent(StateMachine<S, E> fsm, E event) {
		loginfo(() -> format("%s in state %s could not handle '%s'", fsm.getDescription(), fsm.getState(), event));
	}

	public void logEnteringInitialState(StateMachine<S, E> fsm, S id) {
		loginfo(() -> format("%s enters initial state", fsm.getDescription()));
		logEnteringState(fsm, id);
	}

	public void logEnteringState(StateMachine<S, E> fsm, S id) {
		State<S> stateEntered = fsm.state(id);
		if (stateEntered.hasTimer()) {
			long duration = stateEntered.getDuration();
			float seconds = fnTicksToSeconds.apply(duration);
			loginfo(() -> format("%s enters state '%s' for %.2f seconds (%d ticks)", fsm.getDescription(), id, seconds,
					duration));
		} else {
			loginfo(() -> format("%s enters state '%s'", fsm.getDescription(), id));
		}
	}

	public void logExitingState(StateMachine<S, E> fsm, S id) {
		loginfo(() -> format("%s exits state  '%s'", fsm.getDescription(), id));
	}

	public void logFiringTransition(StateMachine<S, E> fsm, Transition<S, E> t, Optional<E> event) {
		if (!event.isPresent()) {
			if (t.from != t.to) {
				if (t.timeoutTriggered) {
					loginfo(() -> format("%s changes from  '%s' to '%s (timeout)'", fsm.getDescription(), t.from, t.to));
				} else {
					loginfo(() -> format("%s changes from  '%s' to '%s'", fsm.getDescription(), t.from, t.to));
				}
			} else {
				loginfo(() -> format("%s stays '%s'", fsm.getDescription(), t.from));
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