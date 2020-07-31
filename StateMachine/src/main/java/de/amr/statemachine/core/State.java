package de.amr.statemachine.core;

import java.util.function.Supplier;

import de.amr.statemachine.api.TickAction;

/**
 * A state in a finite-state machine.
 * 
 * @param <S> state identifier type, for example an enumeration type or primitive type
 * 
 * @author Armin Reichert
 */
public class State<S> {

	/** The identifier of this state. */
	S id;

	/** The client code executed when entering this state. */
	public Runnable entryAction = this::onEntry;

	/** The client code executed when a tick occurs for this state. */
	public TickAction<S> tickAction = this::onTick;

	/** The client code executed when leaving this state. */
	public Runnable exitAction = this::onExit;

	/** Timer for this state. */
	StateTimer timer;

	Supplier<String> fnAnnotation = () -> null;

	String annotation;

	protected State() {
		timer = new InfiniteTimer();
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
		if (hasTimer()) {
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
	 * Hook method called by the state machine when this state is entered. May be overridden by state
	 * subclasses.
	 */
	public void onEntry() {
	}

	/**
	 * Hook method called by the state machine when this state is left. May be overridden by state
	 * subclasses.
	 */
	public void onExit() {
	}

	/**
	 * Hook method called by the state machine when this state is ticked. May be overridden by state
	 * subclasses.
	 * 
	 * @param state          the current state
	 * @param ticksConsumed  number of timer ticks already consumed
	 * @param ticksRemaining number of ticks remaining until timer times out
	 */
	public void onTick(State<S> state, long ticksConsumed, long ticksRemaining) {
	}

	// Timer stuff

	/**
	 * Sets a timer for this state.
	 * 
	 * @param fnDuration function providing the duration
	 */
	public void setTimer(Supplier<Long> fnDuration) {
		timer = new StateTimer(fnDuration);
	}

	/**
	 * Sets a constant timer for this state.
	 * 
	 * @param ticks duration as number of ticks
	 */
	public void setTimer(long ticks) {
		setTimer(() -> ticks);
	}

	/** Resets the timer for this state. */
	public void resetTimer() {
		if (hasTimer()) {
			timer.reset();
		}
	}

	/** Tells if this state has a (non-infinite) timer. */
	public boolean hasTimer() {
		return !(timer instanceof InfiniteTimer);
	}

	/** Removes the timer of this state. */
	public void removeTimer() {
		timer = new InfiniteTimer();
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
	public long getDuration() {
		return timer.duration;
	}

	/**
	 * Returns the number of updates until this state will time out.
	 * 
	 * @return remaining number of updates until timeout occurs
	 */
	public long getTicksRemaining() {
		return timer.remaining;
	}

	/**
	 * The number of ticks since the timer for this state was started or reset.
	 * 
	 * @return Number of updates since the timer for this state was started or reset.
	 */
	public long getTicksConsumed() {
		return timer.duration - timer.remaining;
	}

	/**
	 * The current annotation text of this state.
	 * 
	 * @return the annotation text
	 */
	public String getAnnotation() {
		return annotation;
	}
}