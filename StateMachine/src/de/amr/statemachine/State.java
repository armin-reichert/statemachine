package de.amr.statemachine;

import java.util.Objects;
import java.util.function.IntSupplier;

/**
 * Implementation of a state in a finite state machine.
 * 
 * @author Armin Reichert
 */
public class State<S, E> {

	/** Constant for defining an unlimited duration. */
	public static final int ENDLESS = Integer.MAX_VALUE;

	/** The label used to identify this state. */
	S id;

	/** The state machine this state belongs to. */
	StateMachine<S, E> machine;

	/** The client code executed when entering this state. */
	Runnable entry;

	/** The client code executed when an update occurs for this state. */
	Runnable update;

	/** The client code executed when leaving this state. */
	Runnable exit;

	/** Function providing the timer duration for this state. */
	IntSupplier fnTimer;

	/** The number of ticks this state will be active. */
	int duration;

	/** Ticks remaining until time-out */
	int ticksRemaining;

	protected State() {
		fnTimer = () -> ENDLESS;
		ticksRemaining = duration = ENDLESS;
	}

	public S id() {
		return id;
	}

	public void setOnEntry(Runnable action) {
		Objects.requireNonNull(action);
		entry = action;
	}

	public void onEntry() {
		if (entry != null) {
			entry.run();
		}
	}

	public void setOnExit(Runnable action) {
		Objects.requireNonNull(action);
		exit = action;
	}

	public void onExit() {
		if (exit != null) {
			exit.run();
		}
	}

	public void setOnTick(Runnable action) {
		Objects.requireNonNull(action);
		update = action;
	}

	public void onTick() {
		if (update != null) {
			update.run();
		}
	}

	/** Tells if this state has timed out. */
	public boolean isTerminated() {
		return ticksRemaining == 0;
	}

	/** Resets the timer to the complete state duration. */
	public void resetTimer() {
		if (fnTimer == null) {
			throw new IllegalStateException(String.format("Timer function is NULL in state '%s'", id));
		}
		ticksRemaining = duration = fnTimer.getAsInt();
	}

	void updateTimer() {
		if (duration != ENDLESS && ticksRemaining > 0) {
			--ticksRemaining;
		}
	}

	/**
	 * Sets a timer for this state.
	 * 
	 * @param fnTimer
	 *                  function providing the time for this state
	 */
	public void setTimer(IntSupplier fnTimer) {
		Objects.requireNonNull(fnTimer);
		this.fnTimer = fnTimer;
	}

	/**
	 * Returns the duration of this state (in ticks).
	 * 
	 * @return the state duration (number of updates until this state times out)
	 */
	public int getDuration() {
		return duration;
	}

	/**
	 * Returns the number of updates until this state will time out.
	 * 
	 * @return the number of updates until timeout occurs
	 */
	public int getTicksRemaining() {
		return ticksRemaining;
	}
}