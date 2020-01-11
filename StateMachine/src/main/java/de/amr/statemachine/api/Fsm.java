package de.amr.statemachine.api;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.logging.Logger;

import de.amr.statemachine.core.State;
import de.amr.statemachine.core.StateMachine.MissingTransitionBehavior;

/**
 * Interface for access by state machine clients.
 * 
 * @author Armin Reichert
 *
 * @param <S> state type of the finite-state machine
 * @param <E> event type of the finite-state machine
 */
public interface Fsm<S, E> {

	/**
	 * Initialization hook.
	 */
	void init();

	/**
	 * Update hook.
	 */
	void update();

	/**
	 * Adds listener for events
	 * 
	 * @param listener event listener
	 */
	void addEventListener(Consumer<E> listener);

	/**
	 * Removes listener for events,
	 * 
	 * @param listener event listener
	 */
	void removeEventListener(Consumer<E> listener);

	/**
	 * Published an event to the registered listeners.
	 * 
	 * @param event event to be published
	 */
	void publish(E event);

	/**
	 * Sets the logger where trace messages will be written.
	 * 
	 * @param logger the logger
	 */
	void setLogger(Logger logger);

	/**
	 * Supresses logging for an event.
	 * 
	 * @param condition when event is not logged
	 */
	void doNotLogEventProcessingIf(Predicate<E> condition);

	/**
	 * Supresses logging for publishing an event.
	 * 
	 * @param condition when event is not logged
	 */
	void doNotLogEventPublishingIf(Predicate<E> condition);

	/**
	 * Defines how the state machine reacts to missing transitions.
	 * 
	 * @param missingTransitionBehavior behavior in case no transition is available
	 */
	void setMissingTransitionBehavior(MissingTransitionBehavior missingTransitionBehavior);

	/**
	 * Sets state machine directly to the given state. The state's entry action is
	 * executed and a state timer, if defined, gets reset.
	 * 
	 * @param state new state
	 */
	void setState(S state);

	/**
	 * Sets the new state of this entity. Normally not used directly. The difference
	 * to {@link #setState(Object)} is that a timer of the state is not reset.
	 * 
	 * @param state the new state
	 */
	void resumeState(S state);

	/**
	 * @return the current state of this entity
	 */
	S getState();

	/**
	 * 
	 * @param states list of states
	 * @return tells if this entity currently is in one of the given states
	 */
	@SuppressWarnings("unchecked")
	boolean is(S... states);

	/**
	 * @return internal state object corresponding to current state. Gives access to
	 *         timer.
	 */
	<StateType extends State<S, E>> StateType state();

	/**
	 * @return internal state object corresponding to specified state. Gives access
	 *         to timer.
	 */
	<StateType extends State<S, E>> StateType state(S state);

	/**
	 * If the state has a timer, this timer is set to its full duration.
	 * 
	 * @param state a state identifier
	 */
	void restartTimer(S state);

	/**
	 * Lets the controlling state machine immedialtely process the given event.
	 * 
	 * @param event event to process
	 */
	void process(E event);
}