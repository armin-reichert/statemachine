package de.amr.statemachine.core;

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.logging.Logger;

/**
 * A tracer for the state machine operations.
 * 
 * @author Armin Reichert
 *
 * @param   <S>
 *            state identifier type
 * @param E
 *            event type
 */
public class StateMachineTracer<S, E> implements StateMachineListener<S, E> {

	private final StateMachine<S, E> fsm;
	private final int ticksPerSecond;
	private Logger logger;
	private final List<Predicate<E>> loggingBlacklist;

	public StateMachineTracer(StateMachine<S, E> sm, Logger log, int ticksPerSecond) {
		this.fsm = sm;
		this.logger = log;
		this.ticksPerSecond = ticksPerSecond;
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

	@Override
	public void stateCreated(S state) {
		logger.info(() -> format("%s created state '%s'", fsm.getDescription(), state));
	}

	@Override
	public void stateTimerRestarted(S state) {
		logger.info(() -> format("%s did reset timer for state '%s'", fsm.getDescription(), state));
	}

	@Override
	public void unhandledEvent(E event) {
		logger.info(() -> format("%s in state %s could not handle '%s'", fsm.getDescription(), fsm.getState(), event));
	}

	@Override
	public void enteringInitialState(S initialState) {
		logger.info(() -> format("%s entering initial state (%s)", fsm.getDescription(), initialState));
		enteringState(initialState);
	}

	@Override
	public void enteringState(S enteredState) {
		int duration = fsm.state(enteredState).fnTimer.getAsInt();
		if (duration != State.ENDLESS) {
			float seconds = (float) duration / ticksPerSecond;
			logger.info(() -> format("%s entering state '%s' for %.2f seconds (%d frames)", fsm.getDescription(),
					enteredState, seconds, duration));
		}
		else {
			logger.info(() -> format("%s entering state '%s'", fsm.getDescription(), enteredState));
		}
	}

	@Override
	public void exitingState(S exitedState) {
		logger.info(() -> format("%s exiting state  '%s'", fsm.getDescription(), exitedState));
	}

	@Override
	public void firingTransition(Transition<S, E> t, E event) {
		if (event == null) {
			if (t.from != t.to) {
				if (t.timeout) {
					logger.info(() -> format("%s changing from  '%s' to '%s (timeout)'", fsm.getDescription(), t.from, t.to));
				}
				else {
					logger.info(() -> format("%s changing from  '%s' to '%s'", fsm.getDescription(), t.from, t.to));
				}
			}
			else {
				logger.info(() -> format("%s stays '%s'", fsm.getDescription(), t.from));
			}
		}
		else {
			if (t.from != t.to) {
				eventInfo(event,
						() -> format("%s changing from '%s' to '%s' on '%s'", fsm.getDescription(), t.from, t.to, event));
			}
			else {
				eventInfo(event, () -> format("%s stays '%s' on '%s'", fsm.getDescription(), t.from, event));
			}
		}
	}
}