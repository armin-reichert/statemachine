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
	 * Starts a state machine definition.
	 * 
	 * @param stateLabelType
	 *                         state label type
	 * @param eventType
	 *                         event type
	 * 
	 * @return state machine builder
	 */
	public static <STATE, EVENT> StateMachineBuilder<STATE, EVENT> define(Class<STATE> stateLabelType,
			Class<EVENT> eventType) {
		return new StateMachineBuilder<>(stateLabelType);
	}

	/**
	 * Starts a state machine definition.
	 * 
	 * @param stateLabelType
	 *                         state label type
	 * @param eventType
	 *                         event type
	 * @param matchStrategy
	 *                         transition match strategy
	 * 
	 * @return state machine builder
	 */
	public static <STATE, EVENT> StateMachineBuilder<STATE, EVENT> define(Class<STATE> stateLabelType,
			Class<EVENT> eventType, Match matchStrategy) {
		return new StateMachineBuilder<>(stateLabelType, matchStrategy);
	}

	private final Match matchStrategy;
	private final Deque<E> eventQ;
	private final Map<S, State<S, E>> stateMap;
	private final Map<S, List<Transition<S, E>>> transitionMap;
	private StateMachineTracer<S, E> tracer;
	private String description;
	private S initialState;
	private S currentState;

	/**
	 * Creates a new state machine.
	 * 
	 * @param stateLabelType
	 *                         type for state identifiers
	 * @param matchStrategy
	 *                         strategy for matching events
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public StateMachine(Class<S> stateLabelType, Match matchStrategy) {
		this.matchStrategy = matchStrategy;
		eventQ = new ArrayDeque<>();
		stateMap = stateLabelType.isEnum() ? new EnumMap(stateLabelType) : new HashMap<>(7);
		transitionMap = new HashMap<>(7);
		tracer = new StateMachineTracer<>(this, Logger.getGlobal(), () -> 60);
	}

	/**
	 * Creates a new state machine.
	 * 
	 * @param stateLabelType
	 *                         type for state identifiers
	 */
	public StateMachine(Class<S> stateLabelType) {
		this(stateLabelType, Match.BY_CLASS);
	}

	/**
	 * Forwards tracing to the given logger.
	 * 
	 * @param log
	 *              a logger
	 */
	public void traceTo(Logger log, IntSupplier fnTicksPerSecond) {
		tracer = new StateMachineTracer<>(this, log, fnTicksPerSecond);
	}

	public Match getMatchStrategy() {
		return matchStrategy;
	}

	/**
	 * @return the description of this state machine
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the description for this state machine.
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
	 * 
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
	 *                         transition source state
	 * @param to
	 *                         transition target state
	 * @param guard
	 *                         condition guarding transition
	 * @param action
	 *                         action for transition
	 * @param matchCondition
	 *                         match condition for transition
	 */
	void addTransition(S from, S to, BooleanSupplier guard, Consumer<E> action,
			MatchEventStrategy<S, E> matchCondition) {
		Objects.requireNonNull(from);
		Objects.requireNonNull(to);
		Objects.requireNonNull(matchCondition);
		action = action != null ? action : e -> {
		};
		guard = guard != null ? guard : () -> true;
		transitions(from).add(new Transition<>(from, to, guard, action, matchCondition, false));
	}

	/**
	 * Adds a state transition.
	 * 
	 * @param from
	 *                  transition source state
	 * @param to
	 *                  transition target state
	 * @param guard
	 *                  condition guarding transition
	 * @param action
	 *                  action for transition
	 * @param timeout
	 *                  if transition is fired on a timeout
	 */
	public void addTransitionOnTimeout(S from, S to, BooleanSupplier guard, Consumer<E> action) {
		Objects.requireNonNull(from);
		Objects.requireNonNull(to);
		action = action != null ? action : e -> {
		};
		guard = guard != null ? guard : () -> true;
		transitions(from).add(new Transition<>(from, to, guard, action, null, true));
	}

	public void addTransitionOnEventType(S from, S to, BooleanSupplier guard, Consumer<E> action,
			Class<? extends E> eventType) {
		if (matchStrategy == Match.BY_CLASS) {
			addTransition(from, to, guard, action, new MatchEventByClass<>(eventType));
		} else if (matchStrategy == Match.BY_EQUALITY) {
			throw new IllegalStateException("Cannot add transition, wrong match strategy");
		} else {
			throw new IllegalStateException("No match strategy defined");
		}
	}

	public void addTransitionOnEventObject(S from, S to, BooleanSupplier guard, Consumer<E> action,
			E eventObject) {
		if (matchStrategy == Match.BY_CLASS) {
			throw new IllegalStateException("Cannot add transition, wrong match strategy");
		} else if (matchStrategy == Match.BY_EQUALITY) {
			addTransition(from, to, guard, action, new MatchEventByEquality<>(eventObject));
		} else {
			throw new IllegalStateException("No match strategy defined");
		}
	}
	
	public void addTransition(S from, S to, BooleanSupplier guard, Consumer<E> action) {
		addTransition(from, to, guard, action, new MatchAlways<>());
	}

	/**
	 * Adds an input ("event") to the queue of this state machine.
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
		Objects.nonNull(event);
		eventQ.add(event);
		update();
	}

	/**
	 * @return the current state (identifier)
	 */
	public S getState() {
		return currentState;
	}

	/**
	 * Sets state machine directly into given state. Entry action is executed.
	 * 
	 * @param state
	 *                new state
	 */
	public void setState(S state) {
		currentState = state;
		getStateObject().onEntry();
	}

	/**
	 * @return the state object of the current state
	 */
	public <C extends State<S, E>> C getStateObject() {
		if (currentState == null) {
			throw new IllegalStateException("Cannot access current state object, state machine not initialized.");
		}
		return state(currentState);
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
	 * Replaces the state object for the given state by the given object.
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
	 * Tells if the time expired for the current stat is the given percentage of the state's total time.
	 * 
	 * @param pct
	 *              percentage value to check for
	 * 
	 * @return {@code true} if the given percentage of the state's time has been consumed. If the
	 *         current state has no timer returns {@code false}.
	 */
	public boolean stateTimeExpiredPct(int pct) {
		State<S, E> stateObject = getStateObject();
		if (stateObject.timerTotalTicks == State.ENDLESS) {
			return false;
		}
		float expiredFraction = 1f - (float) stateObject.ticksRemaining / (float) stateObject.timerTotalTicks;
		return 100 * expiredFraction == pct;
	}

	/**
	 * Resets the timer of the current state.
	 */
	public void resetTimer() {
		getStateObject().resetTimer();
	}

	/**
	 * Returns the number of remaining ticks for the current state.
	 */
	public int getRemainingTicks() {
		return getStateObject().getRemaining();
	}

	/**
	 * Initializes this state machine by switching to the initial state and executing the initial
	 * state's (optional) entry action.
	 */
	public void init() {
		tracer.enteringInitialState(initialState);
		currentState = initialState;
		state(currentState).resetTimer();
		state(currentState).onEntry();
	}

	/**
	 * Triggers an update (reading input, firing transition) of this state machine. If the event queue
	 * is empty, the machine looks for a transition that doesn't need input and executes it. If no such
	 * transition exists, the {@code onTick} action of the current state is executed.
	 * 
	 * @throws IllegalStateException
	 *                                 if no matching transition is found
	 */
	public void update() {
		E event = eventQ.poll();
		Optional<Transition<S, E>> matchingTransition = transitions(currentState).stream()
				.filter(t -> isMatching(t, event)).findFirst();
		if (matchingTransition.isPresent()) {
			fireTransition(matchingTransition.get(), event);
			return;
		}
		if (event != null) {
			tracer.unhandledEvent(event);
			throw new IllegalStateException(String.format("%s: No transition defined for state '%s' and event '%s'",
					description, currentState, event));
		}
		state(currentState).updateTimer();
		state(currentState).onTick();
	}

	private boolean isMatching(Transition<S, E> t, E event) {
		if (!t.guard.getAsBoolean()) {
			return false;
		}
		if (t.timeout) {
			return state(t.from).isTerminated();
		}
		return t.matchCondition.matches(this, t, event);
	}

	private void fireTransition(Transition<S, E> t, E event) {
		tracer.firingTransition(t, event);
		if (currentState == t.to) {
			// keep state: no exit/entry actions are executed
			t.action.accept(event);
		} else {
			// change state, execute exit and entry actions
			State<S, E> oldState = state(t.from);
			State<S, E> newState = state(t.to);
			tracer.exitingState(currentState);
			oldState.onExit();
			t.action.accept(event);
			currentState = t.to;
			tracer.enteringState(t.to);
			newState.resetTimer();
			newState.onEntry();
		}
	}
}