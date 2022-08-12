package de.amr.statemachine.api;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import de.amr.statemachine.core.MissingTransitionBehavior;
import de.amr.statemachine.core.State;
import de.amr.statemachine.core.Tracer;
import de.amr.statemachine.core.Transition;

/**
 * Interface for accessing state machines.
 * 
 * @author Armin Reichert
 *
 * @param <S> state identifier type of the finite-state machine
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
	 * Sets the tracer for this state machine.
	 * 
	 * @param tracer the tracer (default tracer prints to system out)
	 */
	void setTracer(Tracer<S, E> tracer);

	/**
	 * @return the tracer of this state machine.
	 */
	Tracer<S, E> getTracer();

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
	 * @return stream of all states (state representations, not just the state IDs)
	 */
	Stream<State<S>> states();

	/**
	 * Sets state machine directly to the given state. The state's entry action is executed and a state timer, if defined,
	 * gets reset.
	 * 
	 * @param stateId id of new state
	 */
	void setState(S stateId);

	/**
	 * Resumes given state. Normally not used directly. The difference to {@link #setState(Object)} is that a timer of the
	 * state is not reset.
	 * 
	 * @param stateId id of state to resume
	 */
	void resumeState(S stateId);

	/**
	 * @return the current state (id)
	 */
	S getState();

	/**
	 * @param stateIds list of state IDs
	 * @return tells if this machine currently is in one of the given states
	 */
	@SuppressWarnings("unchecked")
	boolean is(S... stateIds);

	/**
	 * @return internal state representation of current state.
	 */
	default <S0 extends State<S>> S0 state() {
		return state(getState());
	}

	/**
	 * @param stateId state ID
	 * @return internal state representation of the specified state.
	 */
	<S0 extends State<S>> S0 state(S stateId);

	/**
	 * If the state has a timer, this timer is set to its full duration.
	 * 
	 * @param stateId a state identifier
	 */
	void resetTimer(S stateId);

	/**
	 * Adds an input (event) to the input queue of this state machine.
	 * 
	 * @param event some input/event, must not be null
	 */
	void enqueue(E event);

	/**
	 * Lets the state machine immediately process the given input/event.
	 * 
	 * @param event input/event to process, must not be null
	 */
	void process(E event);

	/**
	 * @return stream of all defined transition
	 */
	Stream<Transition<S, E>> transitions();

	/**
	 * @return the last fired transition
	 */
	Optional<Transition<S, E>> lastFiredTransition();
}