package de.amr.statemachine;

/**
 * State transition as seen by state machine clients.
 * 
 * @author Armin Reichert
 *
 * @param <S>
 *          type of state identifiers
 * @param <E>
 *          type of inputs (events)
 */
public interface Transition<S, E> {

	/**
	 * The state which is changed by this transition.
	 * 
	 * @return state object
	 */
	public State<S, E> getSourceState();

	/**
	 * The state where this transition leads to.
	 * 
	 * @return state object
	 */
	public State<S, E> getTargetState();
}