package de.amr.statemachine.api;

import de.amr.statemachine.core.State;

/**
 * A transition action can get access to the state object and the timer values when the action is
 * executed.
 * 
 * @author Armin Reichert
 *
 * @param <S> state identifier type
 */
@FunctionalInterface
public interface TickAction<S> {

	/**
	 * @param state          the state before the state transition has been executed
	 * @param ticksConsumed  ticks consumed in this state
	 * @param ticksRemaining ticks remaining until tiemout
	 */
	void run(State<S> state, long ticksConsumed, long ticksRemaining);
}