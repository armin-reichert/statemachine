package de.amr.statemachine.core;

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.logging.Logger;

/**
 * A tracer for the state machine operations.
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
	public Function<Integer, Float> fnTicksToSeconds = ticks -> ticks / 60f;

	private final StateMachine<S, E> fsm;
	private Logger logger;
	private final List<Predicate<E>> loggingBlacklist;

	public StateMachineTracer(StateMachine<S, E> fsm, Logger log) {
		this.fsm = fsm;
		this.logger = log;
		this.loggingBlacklist = new ArrayList<>();
	}

	public Logger getLogger() {
		return logger;
	}

	public void setLogger(Logger logger) {
		this.logger = logger;
	}

	public void doNotLog(Predicate<E> condition) {
		loggingBlacklist.add(condition);
	}

	private void eventInfo(E event, Supplier<String> fnMessage) {
		if (loggingBlacklist.stream().noneMatch(condition -> condition.test(event))) {
			logger.info(fnMessage);
		}
	}

	public void stateCreated(S id) {
		logger.info(() -> format("%s created state '%s'", fsm.getDescription(), id));
	}

	public void stateTimerRestarted(S id) {
		logger.info(() -> format("%s did reset timer for state '%s'", fsm.getDescription(), id));
	}

	public void unhandledEvent(E event) {
		logger.info(() -> format("%s in state %s could not handle '%s'", fsm.getDescription(), fsm.getState(), event));
	}

	public void enteringInitialState(S id) {
		logger.info(() -> format("%s enters initial state", fsm.getDescription()));
		enteringState(id);
	}

	public void enteringState(S id) {
		State<S> stateEntered = fsm.state(id);
		if (stateEntered.hasTimer()) {
			int duration = stateEntered.getDuration();
			float seconds = fnTicksToSeconds.apply(duration);
			logger.info(() -> format("%s enters state '%s' for %.2f seconds (%d ticks)", fsm.getDescription(), id, seconds,
					duration));
		} else {
			logger.info(() -> format("%s enters state '%s'", fsm.getDescription(), id));
		}
	}

	public void exitingState(S id) {
		logger.info(() -> format("%s exits state  '%s'", fsm.getDescription(), id));
	}

	public void firingTransition(Transition<S, E> t, Optional<E> event) {
		if (!event.isPresent()) {
			if (t.from != t.to) {
				if (t.timeoutEvent) {
					logger.info(() -> format("%s changes from  '%s' to '%s (timeout)'", fsm.getDescription(), t.from, t.to));
				} else {
					logger.info(() -> format("%s changes from  '%s' to '%s'", fsm.getDescription(), t.from, t.to));
				}
			} else {
				logger.info(() -> format("%s stays '%s'", fsm.getDescription(), t.from));
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