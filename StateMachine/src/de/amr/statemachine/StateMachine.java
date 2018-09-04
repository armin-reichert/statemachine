package de.amr.statemachine;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntSupplier;
import java.util.logging.Logger;

/**
 * A finite state machine.
 *
 * @param <S>
 *          type for identifying states, for example an enumeration type.
 * @param <E>
 *          type of events/inputs.
 * 
 * @author Armin Reichert
 */
public class StateMachine<S, E> {

	/**
	 * Creates a new state machine instance and starts its definition building.
	 * 
	 * @param stateLabelClass
	 *                          state label class
	 * @param eventClass
	 *                          event class
	 * 
	 * @param                 <STATE>
	 *                          state type
	 * @param                 <EVENT>
	 *                          event type
	 * @return state machine builder
	 */
	public static <STATE, EVENT> StateMachineBuilder<STATE, EVENT> define(Class<STATE> stateLabelClass,
			Class<EVENT> eventClass) {
		return new StateMachineBuilder<>(stateLabelClass);
	}

	/**
	 * Creates a new state machine instance and starts its definition building.
	 * 
	 * @param stateLabelClass
	 *                          state label class
	 * @param eventClass
	 *                          event class
	 * @param matchStrategy
	 *                          event match strategy
	 * 
	 * @param                 <STATE>
	 *                          state type
	 * @param                 <EVENT>
	 *                          event type
	 * @return state machine builder
	 */
	public static <STATE, EVENT> StateMachineBuilder<STATE, EVENT> define(Class<STATE> stateLabelClass,
			Class<EVENT> eventClass, Match matchStrategy) {
		return new StateMachineBuilder<>(stateLabelClass, matchStrategy);
	}

	private String description;
	private S initialState;
	private S current;
	private final Match matchEventsBy;
	private final Deque<E> eventQ;
	private final Map<S, State<S, E>> stateMap;
	private final Map<S, List<Transition<S, E>>> transitionMap;
	private StateMachineTracer<S, E> tracer;

	/**
	 * Starts the definition building for this state machine.
	 */
	public StateMachineBuilder<S, E> define() {
		return new StateMachineBuilder<>(this);
	}

	/**
	 * Creates a new state machine.
	 * 
	 * @param stateLabelClass
	 *                          type for state identifiers
	 * @param matchStrategy
	 *                          strategy for matching events
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public StateMachine(Class<S> stateLabelClass, Match matchStrategy) {
		this.matchEventsBy = matchStrategy;
		eventQ = new ArrayDeque<>();
		stateMap = stateLabelClass.isEnum() ? new EnumMap(stateLabelClass) : new HashMap<>(7);
		transitionMap = new HashMap<>(7);
		tracer = new StateMachineTracer<>(this, Logger.getGlobal(), () -> 60);
	}

	/**
	 * Creates a new state machine with a default match strategy of "by class".
	 * 
	 * @param stateLabelClass
	 *                          type for state identifiers
	 */
	public StateMachine(Class<S> stateLabelClass) {
		this(stateLabelClass, Match.BY_CLASS);
	}

	/**
	 * Forwards tracing to the given logger.
	 * 
	 * @param log
	 *                           a logger
	 * @param fnTicksPerSecond
	 *                           the update frequency for this machine
	 */
	public void traceTo(Logger log, IntSupplier fnTicksPerSecond) {
		tracer = new StateMachineTracer<>(this, log, fnTicksPerSecond);
	}

	/**
	 * @return the event match strategy
	 */
	public Match getMatchStrategy() {
		return matchEventsBy;
	}

	/**
	 * @return the description text for this state machine (used by tracing)
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the description text for this state machine.
	 * 
	 * @param description
	 *                      description text (used by tracing)
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Sets the initial state for this state machine.
	 * 
	 * @param initialState
	 *                       initial state
	 */
	public void setInitialState(S initialState) {
		if (initialState == null) {
			throw new IllegalStateException("Initial state cannot be NULL");
		}
		this.initialState = initialState;
	}

	/**
	 * @return the initial state of this state machine
	 */
	public S getInitialState() {
		return initialState;
	}

	private List<Transition<S, E>> transitions(S state) {
		if (!transitionMap.containsKey(state)) {
			transitionMap.put(state, new ArrayList<>(3));
		}
		return transitionMap.get(state);
	}

	/**
	 * Adds a state transition.
	 * 
	 * @param from
	 *                     transition source state
	 * @param to
	 *                     transition target state
	 * @param guard
	 *                     condition guarding transition
	 * @param action
	 *                     action for transition
	 * @param event
	 *                     event for matching transition
	 * @param eventClass
	 *                     event class for matching transition
	 * @param timeout
	 *                     tells if this should be a timeout transition
	 * 
	 */
	void addTransition(S from, S to, BooleanSupplier guard, Consumer<E> action, E event,
			Class<? extends E> eventClass, boolean timeout) {
		Objects.requireNonNull(from);
		Objects.requireNonNull(to);
		guard = (guard != null) ? guard : () -> true;
		action = (action != null) ? action : e -> {
		};
		transitions(from).add(new Transition<>(from, to, guard, action, event, eventClass, timeout));
	}

	/**
	 * Adds a timeout transition.
	 * 
	 * @param from
	 *                 transition source state
	 * @param to
	 *                 transition target state
	 * @param guard
	 *                 condition guarding transition
	 * @param action
	 *                 action for transition
	 */
	public void addTransitionOnTimeout(S from, S to, BooleanSupplier guard, Consumer<E> action) {
		addTransition(from, to, guard, action, null, null, true);
	}

	/**
	 * Adds a transition which is fired if the guard condition holds and the current input's class
	 * equals the given event class.
	 * 
	 * @param from
	 *                     transition source state
	 * @param to
	 *                     transition target state
	 * @param guard
	 *                     condition guarding transition
	 * @param action
	 *                     action for transition
	 * @param eventClass
	 *                     class used for matching the current event
	 */
	public void addTransitionOnEventType(S from, S to, BooleanSupplier guard, Consumer<E> action,
			Class<? extends E> eventClass) {
		Objects.requireNonNull(eventClass);
		if (matchEventsBy != Match.BY_CLASS) {
			throw new IllegalStateException("Cannot add transition, wrong match strategy: " + matchEventsBy);
		}
		addTransition(from, to, guard, action, null, eventClass, false);
	}

	/**
	 * Adds a transition which is fired if the guard condition holds and the current input equals the
	 * given event.
	 * 
	 * @param from
	 *                 transition source state
	 * @param to
	 *                 transition target state
	 * @param guard
	 *                 condition guarding transition
	 * @param action
	 *                 action for transition
	 * @param event
	 *                 event object used for matching the current event
	 */
	public void addTransitionOnEventObject(S from, S to, BooleanSupplier guard, Consumer<E> action, E event) {
		Objects.requireNonNull(event);
		if (matchEventsBy != Match.BY_EQUALITY) {
			throw new IllegalStateException("Cannot add transition, wrong match strategy");
		}
		addTransition(from, to, guard, action, event, null, false);
	}

	/**
	 * Adds a transition which is fired when the guard condition holds.
	 * 
	 * @param from
	 *                 transition source state
	 * @param to
	 *                 transition target state
	 * @param guard
	 *                 condition guarding transition
	 * @param action
	 *                 action for transition
	 */
	public void addTransition(S from, S to, BooleanSupplier guard, Consumer<E> action) {
		addTransition(from, to, guard, action, null, null, false);
	}

	/**
	 * Adds an input (event) to the input queue of this state machine.
	 * 
	 * @param event
	 *                some input/event
	 */
	public void enqueue(E event) {
		Objects.nonNull(event);
		eventQ.add(event);
	}

	/**
	 * Processes the given event.
	 * 
	 * @param event
	 *                some input / event
	 */
	public void process(E event) {
		enqueue(event);
		update();
	}

	/**
	 * @return the current state (identifier)
	 */
	public S getState() {
		return current;
	}

	/**
	 * Sets state machine directly to the given state. The state's entry action is executed.
	 * 
	 * @param state
	 *                new state
	 */
	public void setState(S state) {
		current = state;
		state().onEntry();
	}

	/**
	 * @return the state object of the current state
	 */
	public <T extends State<S, E>> T state() {
		if (current == null) {
			throw new IllegalStateException("Cannot access current state object, state machine not initialized.");
		}
		return state(current);
	}

	/**
	 * Returns the object representing the given state. It is created on-demand.
	 * 
	 * @param       <T>
	 *                subtype of default state class
	 * @param state
	 *                a state identifier
	 * @return the state object for the given state identifier
	 */
	@SuppressWarnings("unchecked")
	public <T extends State<S, E>> T state(S state) {
		if (!stateMap.containsKey(state)) {
			return (T) realizeState(state, new State<>());
		}
		return (T) stateMap.get(state);
	}

	/**
	 * Creates or replaces the state object for the given state by the given object.
	 * 
	 * @param             <T>
	 *                      subtype of default state class used for representing the given state
	 * @param state
	 *                      state identifier
	 * @param stateObject
	 *                      state object
	 * @return the new state object
	 */
	public <T extends State<S, E>> T realizeState(S state, T stateObject) {
		stateObject.id = state;
		stateObject.machine = this;
		stateMap.put(state, stateObject);
		return stateObject;
	}

	/**
	 * Resets the timer of the current state.
	 */
	public void resetTimer() {
		state().resetTimer();
	}

	/**
	 * Returns the number of remaining ticks for the current state.
	 */
	public int getRemainingTicks() {
		return state().getRemaining();
	}

	/**
	 * Initializes this state machine by switching to the initial state and executing the initial
	 * state's (optional) entry action.
	 */
	public void init() {
		if (initialState == null) {
			throw new IllegalStateException("Cannot initialize state machine, no initial state defined.");
		}
		tracer.enteringInitialState(initialState);
		current = initialState;
		state(current).resetTimer();
		state(current).onEntry();
	}

	/**
	 * Updates (reads input, fires first matching transition) this state machine. If the event queue
	 * is empty, the machine looks for a transition that doesn't need input and executes it. If no such
	 * transition exists, the {@code onTick} action of the current state is executed.
	 * 
	 * @throws IllegalStateException
	 *                                 if no matching transition is found
	 */
	public void update() {
		if (current == null) {
			throw new IllegalStateException("Cannot update state, state machine not initialized.");
		}
		E eventOrNull = eventQ.poll();
		Optional<Transition<S, E>> matchingTransition = transitions(current).stream()
				.filter(t -> isMatching(t, eventOrNull)).findFirst();
		if (matchingTransition.isPresent()) {
			fireTransition(matchingTransition.get(), eventOrNull);
			return;
		}
		if (eventOrNull != null) {
			tracer.unhandledEvent(eventOrNull);
			throw new IllegalStateException(String.format("%s: No transition defined for state '%s' and event '%s'",
					description, current, eventOrNull));
		}
		state(current).onTick();
		state(current).updateTimer();
	}

	private boolean isMatching(Transition<S, E> t, E eventOrNull) {
		if (!t.guard.getAsBoolean()) {
			return false;
		}
		if (t.timeout) {
			return state(t.from).isTerminated();
		}
		if (eventOrNull == null) {
			return t.event == null && t.eventClass == null;
		}
		if (matchEventsBy == Match.BY_CLASS) {
			return eventOrNull.getClass().equals(t.eventClass);
		} else if (matchEventsBy == Match.BY_EQUALITY) {
			return eventOrNull.equals(t.event);
		}
		return false;
	}

	private void fireTransition(Transition<S, E> t, E eventOrNull) {
		tracer.firingTransition(t, eventOrNull);
		if (current == t.to) {
			// keep state: don't execute exit/entry actions
			t.action.accept(eventOrNull);
		} else {
			// change to new state, execute exit and entry actions
			State<S, E> oldState = state(current);
			State<S, E> newState = state(t.to);
			tracer.exitingState(current);
			oldState.onExit();
			t.action.accept(eventOrNull);
			current = t.to;
			tracer.enteringState(t.to);
			newState.resetTimer();
			newState.onEntry();
		}
	}
}