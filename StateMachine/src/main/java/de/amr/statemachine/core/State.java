package de.amr.statemachine.core;

import java.util.Objects;
import java.util.function.IntSupplier;

import de.amr.statemachine.api.TickAction;

/**
 * A state in a finite-state machine.
 * 
 * @param <S> state identifier type, for example an enumeration type or
 *            primitive type
 * 
 * @author Armin Reichert
 */
public class State<S> {

	/** The identifier of this state. */
	S id;

	/** The client code executed when entering this state. */
	Runnable entryAction;

	/** The client code executed when a tick occurs for this state. */
	TickAction<S> tickAction;

	/** The client code executed when leaving this state. */
	Runnable exitAction;

	/** Timer for this state. */
	StateTimer timer;

	protected State() {
		timer = StateTimer.NEVER_ENDING_TIMER;
	}

	@Override
	public String toString() {
		String s = String.format("(%s", id);
		if (entryAction != null)
			s += " entry";
		if (tickAction != null)
			s += " tick";
		if (exitAction != null)
			s += " exit";
		if (timer != StateTimer.NEVER_ENDING_TIMER) {
			s += " timer";
		}
		s += ")";
		return s;
	}

	/**
	 * The identifier of this state.
	 * 
	 * @return identifier
	 */
	public S id() {
		return id;
	}

	/**
	 * Sets the action to be executed whenever this state is entered, either via a
	 * transition of by setting the state directly.
	 * 
	 * @param action the entry action
	 */
	public void setOnEntry(Runnable action) {
		entryAction = Objects.requireNonNull(action);
	}

	/**
	 * Hook method called by the state machine when this state is entered. May be
	 * overridden by state subclasses.
	 */
	public void onEntry() {
		if (entryAction != null) {
			entryAction.run();
		}
	}

	/**
	 * Sets the action to be executed whenever this state is left, either via a
	 * transition of by setting a new state directly.
	 * 
	 * @param action the exit action
	 */
	public void setOnExit(Runnable action) {
		exitAction = Objects.requireNonNull(action);
	}

	/**
	 * Hook method called by the state machine when this state is left. May be
	 * overridden by state subclasses.
	 */
	public void onExit() {
		if (exitAction != null) {
			exitAction.run();
		}
	}

	/**
	 * Sets the action to be executed whenever this state is "ticked".
	 * 
	 * @param action the tick action
	 */
	public void setOnTick(Runnable action) {
		Objects.requireNonNull(action);
		tickAction = (state, ticksConsumed, ticksRemaining) -> action.run();
	}

	/**
	 * Sets the action to be executed whenever this state is "ticked".
	 * 
	 * @param action the tick action. When called, the closure contains the state,
	 *               the ticks consumed and the ticks remaining.
	 */
	public void setOnTick(TickAction<S> action) {
		tickAction = Objects.requireNonNull(action);
	}

	/**
	 * Hook method called by the state machine when this state is "ticked". May be
	 * overridden by state subclasses.
	 */
	public void onTick() {
		if (tickAction != null) {
			tickAction.run(this, getTicksConsumed(), getTicksRemaining());
		}
	}

	// Timer stuff

	/**
	 * Sets a timer for this state and restarts the timer.
	 * 
	 * @param fnDuration function providing the duration
	 */
	public void setTimer(IntSupplier fnDuration) {
		timer = new StateTimer(fnDuration);
		timer.restart();
	}

	/**
	 * Sets a constant timer for this state and restarts the timer.
	 * 
	 * @param ticks duration as number of ticks
	 */
	public void setTimer(int ticks) {
		setTimer(() -> ticks);
	}

	/** Tells if this state has a timer. */
	public boolean hasTimer() {
		return timer != StateTimer.NEVER_ENDING_TIMER;
	}

	/** Removes the timer of this state. */
	public void removeTimer() {
		timer = StateTimer.NEVER_ENDING_TIMER;
	}

	/** Tells if this state timer, if any, reached its end. */
	public boolean isTerminated() {
		return timer.remaining == 0;
	}

	/**
	 * Returns the duration of this state (in ticks).
	 * 
	 * @return the state duration (total number of updates until timeout)
	 */
	public int getDuration() {
		return timer.duration;
	}

	/**
	 * Returns the number of updates until this state will time out.
	 * 
	 * @return remaining number of updates until timeout occurs
	 */
	public int getTicksRemaining() {
		return timer.remaining;
	}

	/**
	 * The number of updates since the (optional) timer for this state was started
	 * or reset.
	 * 
	 * @return Number of updates since the timer for this state was started or
	 *         reset. If there is no timer assigned to this state, <code>0</code> is
	 *         returned.
	 */
	public int getTicksConsumed() {
		return hasTimer() ? timer.duration - timer.remaining : 0;
	}
}