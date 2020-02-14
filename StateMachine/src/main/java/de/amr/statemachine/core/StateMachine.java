package de.amr.statemachine.core;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.logging.Logger;

import de.amr.statemachine.api.EventMatchStrategy;
import de.amr.statemachine.api.Fsm;

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
public class StateMachine<S, E> implements Fsm<S, E> {

	/**
	 * Defines what happens when no transition is found. Either ignore silently, log a message or throw
	 * an exception.
	 */
	public enum MissingTransitionBehavior {
		/** Ignore silently. */
		IGNORE,
		/** Log a message */
		LOG,
		/** Throw an exception. */
		EXCEPTION;
	}

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
	public static <STATE, EVENT> StateMachineBuilder<STATE, EVENT> beginStateMachine(
			Class<STATE> stateLabelClass, Class<EVENT> eventClass) {
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
	public static <STATE, EVENT> StateMachineBuilder<STATE, EVENT> beginStateMachine(
			Class<STATE> stateLabelClass, Class<EVENT> eventClass, EventMatchStrategy matchStrategy) {
		return new StateMachineBuilder<>(stateLabelClass, matchStrategy);
	}

	private Supplier<String> fnDescription;
	private S initialState;
	private S currentState;
	private MissingTransitionBehavior missingTransitionBehavior;
	private final EventMatchStrategy matchEventsBy;
	private final Deque<E> eventQ;
	private final Map<S, State<S, E>> stateMap;
	private final Map<S, List<Transition<S, E>>> transitionMap;
	private final StateMachineTracer<S, E> tracer;
	private final Set<Consumer<E>> listeners = new LinkedHashSet<>();
	private final List<Predicate<E>> loggingBlacklist = new ArrayList<>();

	/**
	 * Creates a new state machine.
	 * 
	 * @param stateLabelClass
	 *                          type for state identifiers
	 * @param matchStrategy
	 *                          strategy for matching events
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public StateMachine(Class<S> stateLabelClass, EventMatchStrategy matchStrategy) {
		fnDescription = () -> "[unnamed FSM]";
		this.matchEventsBy = matchStrategy;
		eventQ = new ArrayDeque<>();
		stateMap = stateLabelClass.isEnum() ? new EnumMap(stateLabelClass)
				: stateLabelClass == Boolean.class ? new BooleanMap() : new HashMap<>(7);
		transitionMap = new HashMap<>(7);
		tracer = new StateMachineTracer<>(this, Logger.getGlobal());
		missingTransitionBehavior = MissingTransitionBehavior.EXCEPTION;
	}

	/**
	 * Creates a new state machine with a default match strategy of "by class".
	 * 
	 * @param stateLabelClass
	 *                          type for state identifiers
	 */
	public StateMachine(Class<S> stateLabelClass) {
		this(stateLabelClass, EventMatchStrategy.BY_CLASS);
	}

	/**
	 * Starts building of state machine.
	 */
	public StateMachineBuilder<S, E> beginStateMachine() {
		return new StateMachineBuilder<>(this);
	}

	@Override
	public String toString() {
		return fnDescription != null ? fnDescription.get() : super.toString();
	}

	@Override
	public StateMachineTracer<S, E> getTracer() {
		return tracer;
	}

	@Override
	public void doNotLogEventProcessingIf(Predicate<E> condition) {
		tracer.doNotLog(condition);
	}

	@Override
	public void doNotLogEventPublishingIf(Predicate<E> condition) {
		loggingBlacklist.add(condition);
	}

	/**
	 * @return the event match strategy
	 */
	public EventMatchStrategy getMatchStrategy() {
		return matchEventsBy;
	}

	/**
	 * @return the description text for this state machine (used by tracing)
	 */
	public String getDescription() {
		return fnDescription.get();
	}

	/**
	 * Sets the description text supplier for this state machine.
	 * 
	 * @param description
	 *                      description text (used by tracing)
	 */
	public void setDescription(Supplier<String> fnDescription) {
		this.fnDescription = Objects.requireNonNull(fnDescription);
	}

	/**
	 * Sets the description text for this state machine.
	 * 
	 * @param description
	 *                      description text (used by tracing)
	 */
	public void setDescription(String description) {
		this.fnDescription = () -> description;
	}

	/**
	 * Defines how the state machine reacts to missing transitions.
	 * 
	 * @param missingTransitionBehavior
	 *                                    behavior in case no transition is available
	 */
	@Override
	public void setMissingTransitionBehavior(MissingTransitionBehavior missingTransitionBehavior) {
		this.missingTransitionBehavior = missingTransitionBehavior;
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
		if (matchEventsBy != EventMatchStrategy.BY_CLASS) {
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
		if (matchEventsBy != EventMatchStrategy.BY_EQUALITY) {
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

	@Override
	public void addEventListener(Consumer<E> listener) {
		listeners.add(listener);
	}

	@Override
	public void removeEventListener(Consumer<E> listener) {
		listeners.remove(listener);
	}

	@Override
	public void publish(E event) {
		if (loggingBlacklist.stream().noneMatch(condition -> condition.test(event))) {
			tracer.getLogger().info(() -> String.format("%s published event '%s'", this, event));
		}
		listeners.forEach(listener -> listener.accept(event));
	}

	/**
	 * Adds an input (event) to the input queue of this state machine.
	 * 
	 * @param event
	 *                some input/event
	 */
	public void enqueue(E event) {
		eventQ.add(Objects.requireNonNull(event));
	}

	protected Collection<E> eventQ() {
		return Collections.unmodifiableCollection(eventQ);
	}

	@Override
	public void process(E event) {
		enqueue(event);
		update();
	}

	@Override
	public S getState() {
		return currentState;
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean is(S... states) {
		return Arrays.stream(states).anyMatch(s -> s.equals(getState()));
	}

	@Override
	public void setState(S state) {
		if (currentState != null) {
			state(currentState).onExit();
		}
		currentState = state;
		state(currentState).onEntry();
		restartTimer(state);
	}

	@Override
	public void restartTimer(S state) {
		StateTimer timer = state(state).timer;
		if (timer != StateTimer.NEVER_ENDING_TIMER) {
			timer.restart();
			tracer.stateTimerRestarted(state);
		}
	}

	@Override
	public void resumeState(S state) {
		currentState = state;
		state().onEntry();
	}

	@Override
	public <StateType extends State<S, E>> StateType state() {
		if (currentState == null) {
			throw new IllegalStateException("Cannot access current state object, state machine not initialized.");
		}
		return state(currentState);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <StateType extends State<S, E>> StateType state(S state) {
		if (!stateMap.containsKey(state)) {
			return (StateType) realizeState(state, new State<>());
		}
		return (StateType) stateMap.get(state);
	}

	/**
	 * Creates or replaces the state object for the given state by the given object.
	 * 
	 * @param             <StateType>
	 *                      subtype of default state class used for representing the given state
	 * @param state
	 *                      state identifier
	 * @param stateObject
	 *                      state object
	 * @return the new state object
	 */
	public <StateType extends State<S, E>> StateType realizeState(S state, StateType stateObject) {
		stateObject.id = state;
		stateMap.put(state, stateObject);
		return stateObject;
	}

	@Override
	public void init() {
		if (initialState == null) {
			throw new IllegalStateException("Cannot initialize state machine, no initial state defined.");
		}
		tracer.enteringInitialState(initialState);
		currentState = initialState;
		restartTimer(currentState);
		state(currentState).onEntry();
	}

	@Override
	public void update() {
		if (currentState == null) {
			throw new IllegalStateException(
					String.format("Cannot update state, state machine '%s' not initialized.", getDescription()));
		}

		// Check if event and/or condition triggers state transition
		Optional<E> optEvent = Optional.ofNullable(eventQ.poll());
		Optional<Transition<S, E>> match = findMatchingTransition(currentState, optEvent);
		if (match.isPresent()) {
			fireTransition(match.get(), optEvent);
			return;
		} else {
			optEvent.ifPresent(event -> {
				switch (missingTransitionBehavior) {
				case IGNORE:
					break;
				case LOG:
					tracer.unhandledEvent(event);
					break;
				case EXCEPTION:
					throw new IllegalStateException(String
							.format("%s: No transition defined for state '%s' and event '%s'", this, currentState, event));
				default:
					throw new IllegalArgumentException("Illegal value: " + missingTransitionBehavior);
				}
			});
		}

		state(currentState).onTick();

		// Check if expired timer triggers state transition
		boolean timeout = state(currentState).timer.tick();
		if (timeout) {
			match = findMatchingTimeoutTransition(currentState);
			if (match.isPresent()) {
				fireTransition(match.get(), Optional.empty());
			}
		}
	}

	private Optional<Transition<S, E>> findMatchingTimeoutTransition(S state) {
		return transitions(state).stream().filter(t -> t.timeout && t.guard.getAsBoolean()).findFirst();
	}

	private Optional<Transition<S, E>> findMatchingTransition(S state, Optional<E> event) {
		return transitions(state).stream().filter(t -> isTransitionMatching(t, event)).findFirst();
	}

	private boolean isTransitionMatching(Transition<S, E> t, Optional<E> event) {
		if (t.timeout) {
			// transition matches only timeout event
			return false;
		}
		if (!t.guard.getAsBoolean()) {
			// guard condition not fulfilled
			return false;
		}
		if (!event.isPresent()) {
			// no event but guard fulfilled
			return t.event == null && t.eventClass == null;
		}
		if (matchEventsBy == EventMatchStrategy.BY_CLASS) {
			// event class matches, guard condition fulfilled
			return event.isPresent() && event.get().getClass().equals(t.eventClass);
		} else if (matchEventsBy == EventMatchStrategy.BY_EQUALITY) {
			// event matches, guard condition fulfilled
			return event.isPresent() && event.get().equals(t.event);
		}
		return false;
	}

	private void fireTransition(Transition<S, E> transition, Optional<E> event) {
		tracer.firingTransition(transition, event);
		if (currentState == transition.to) {
			// loop: don't execute exit/entry actions, don't restart timer
			transition.action.accept(event.orElse(null));
		} else {
			// exit state
			tracer.exitingState(currentState);
			state(currentState).onExit();
			// call action
			transition.action.accept(event.orElse(null));
			// enter new state
			currentState = transition.to;
			tracer.enteringState(currentState);
			restartTimer(currentState);
			state(currentState).onEntry();
		}
	}
}