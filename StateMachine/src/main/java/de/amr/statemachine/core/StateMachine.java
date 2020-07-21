package de.amr.statemachine.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.stream.Stream;

import de.amr.statemachine.api.Fsm;
import de.amr.statemachine.api.TransitionMatchStrategy;

/**
 * A finite-state machine.
 * 
 * <p>
 * State machines normally should be created using a builder, as shown in the example below.
 * 
 * By default, missing transitions lead to an exception. This can be changed by calling
 * {@link #setMissingTransitionBehavior(MissingTransitionBehavior)} with one of the values defined in 
 * {@link MissingTransitionBehavior}.
 * 
 * Events/inputs by default are matched (against a transition definition) by the class of the event.
 * That means, in the state machine definition, transitions should be defined based on the event class. 
 * 
 * In the example below however, events are unique (no different instances of events of the same type) and
 * transitions are defined based on event instances. In this case, the state machine must be defined with
 * an {@link TransitionMatchStrategy#BY_VALUE}. 
 * 
 * <p>
 * <pre>
 * public class DoorController extends StateMachine&lt;DoorState, DoorEvent&gt; {
 * 
 * 	public enum DoorState {
 * 		OPEN, CLOSED, LOCKED
 * 	}
 * 
 * 	public enum DoorEvent {
 * 		OPEN_DOOR, CLOSE_DOOR, LOCK_DOOR, UNLOCK_DOOR
 * 	}
 * 
 * 	public DoorController() {
 * 		super(DoorState.class, EventMatchStrategy.BY_EQUALITY);
 * 		//@formatter:off
 * 		beginStateMachine()
 * 			.initialState(LOCKED)
 * 			.description("Door")
 * 		.states()
 * 		.transitions()
 * 			.when(LOCKED).then(CLOSED).on(UNLOCK_DOOR)
 * 			.when(CLOSED).then(LOCKED).on(LOCK_DOOR)
 * 			.when(CLOSED).then(OPEN).on(OPEN_DOOR)
 * 			.when(OPEN).then(CLOSED).on(CLOSE_DOOR)
 * 		.endStateMachine();
 * 		//@formatter:on
 * 	}
 * }
 * </pre>
 *
 * @param <S> type for identifying states, for example an enumeration type.
 * @param <E> type of events/inputs.
 * 
 * @author Armin Reichert
 */
public class StateMachine<S, E> implements Fsm<S, E> {

	static {
		try {
			InputStream in = StateMachine.class.getClassLoader()
					.getResourceAsStream("de/amr/statemachine/logging.properties");
			LogManager.getLogManager().readConfiguration(in);
		} catch (NullPointerException | IOException | SecurityException e) {
			throw new RuntimeException("Could not read logging configuration");
		}
	}

	/**
	 * Defines what happens when no transition is found. Either ignore silently, log a message or throw
	 * an exception.
	 */
	public enum MissingTransitionBehavior {
		/** Ignore silently. */
		IGNORE,
		/** Log a message. */
		LOG,
		/** Throw an exception. */
		EXCEPTION;
	}

	/**
	 * Creates a new state machine instance and starts its definition building. The event match strategy
	 * is "by class".
	 * 
	 * @param stateIdentifierClass class of state identifiers e.g. an enumeration class
	 * @param eventClass           class of events/inputs
	 * 
	 * @param <STATE>              state identifier type
	 * @param <EVENT>              event/input type
	 * @return state machine builder
	 */
	public static <STATE, EVENT> StateMachineBuilder<STATE, EVENT> beginStateMachine(Class<STATE> stateIdentifierClass,
			Class<EVENT> eventClass) {
		return new StateMachineBuilder<>(stateIdentifierClass);
	}

	/**
	 * Creates a new state machine instance and starts its definition building.
	 * 
	 * @param stateIdentifierClass class of state identifiers e.g. an enumeration class
	 * @param eventClass           class of events/inputs
	 * @param matchStrategy        strategy for matching events against transition definitions
	 * 
	 * @param <STATE>              state identifier type
	 * @param <EVENT>              event/input type
	 * @return state machine builder
	 */
	public static <STATE, EVENT> StateMachineBuilder<STATE, EVENT> beginStateMachine(Class<STATE> stateIdentifierClass,
			Class<EVENT> eventClass, TransitionMatchStrategy matchStrategy) {
		return new StateMachineBuilder<>(stateIdentifierClass, matchStrategy);
	}

	private Supplier<String> fnDescription;
	private S initialState;
	private S currentState;
	private MissingTransitionBehavior missingTransitionBehavior;
	private final TransitionMatchStrategy matchEventsBy;
	protected final Deque<E> eventQ;
	private final Map<S, State<S>> stateMap;
	private final Map<S, List<Transition<S, E>>> transitionMap;
	private final StateMachineTracer<S, E> tracer;
	private final List<Predicate<E>> loggingBlacklist = new ArrayList<>();
	private final Set<Consumer<E>> eventListeners = new LinkedHashSet<>();
	private Map<S, Set<Consumer<State<S>>>> entryListeners;
	private Map<S, Set<Consumer<State<S>>>> exitListeners;

	/**
	 * Creates a new state machine.
	 * 
	 * @param stateIdentifierClass class of state identifiers e.g. an enumeration class
	 * @param matchStrategy        strategy for matching events
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public StateMachine(Class<S> stateIdentifierClass, TransitionMatchStrategy matchStrategy) {
		Objects.requireNonNull(stateIdentifierClass);
		fnDescription = () -> String.format("[%s]", getClass().getSimpleName());
		matchEventsBy = Objects.requireNonNull(matchStrategy);
		missingTransitionBehavior = MissingTransitionBehavior.EXCEPTION;
		eventQ = new ArrayDeque<>();
		stateMap = stateIdentifierClass.isEnum() ? new EnumMap(stateIdentifierClass)
				: stateIdentifierClass == Boolean.class ? new BooleanMap() : new HashMap<>(7);
		transitionMap = new HashMap<>(7);
		tracer = new StateMachineTracer<>(this, Logger.getGlobal());
	}

	/**
	 * Creates a new state machine with a default match strategy of "by class".
	 * 
	 * @param stateIdentifierClass class of state identifiers e.g. an enumeration class
	 */
	public StateMachine(Class<S> stateIdentifierClass) {
		this(stateIdentifierClass, TransitionMatchStrategy.BY_CLASS);
	}

	/**
	 * Starts building of the state machine.
	 * 
	 * @return new state machine builder
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
	public TransitionMatchStrategy getMatchStrategy() {
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
	 * @param description description text (used by tracing)
	 */
	public void setDescription(Supplier<String> fnDescription) {
		this.fnDescription = Objects.requireNonNull(fnDescription);
	}

	/**
	 * Sets the description text for this state machine.
	 * 
	 * @param description description text (used by tracing)
	 */
	public void setDescription(String description) {
		this.fnDescription = () -> description;
	}

	/**
	 * Defines how the state machine reacts to missing transitions.
	 * 
	 * @param missingTransitionBehavior behavior in case no transition is available
	 */
	@Override
	public void setMissingTransitionBehavior(MissingTransitionBehavior missingTransitionBehavior) {
		this.missingTransitionBehavior = Objects.requireNonNull(missingTransitionBehavior);
	}

	/**
	 * Sets the initial state for this state machine.
	 * 
	 * @param initialState initial state
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

	public Stream<Transition<S, E>> transitions() {
		return states().flatMap(state -> transitions(state.id).stream());
	}

	/**
	 * Adds a state transition.
	 * 
	 * @param from             source state
	 * @param to               target state
	 * @param guard            condition guarding transition
	 * @param action           action performed on transition
	 * @param eventSlot        event value/class if transition is matched by value/class
	 * @param timeoutTriggered if this transition is triggered by a timeout
	 * @param annotation       annotation text
	 */
	void addTransition(S from, S to, BooleanSupplier guard, Consumer<E> action, Object eventSlot, boolean timeout,
			String annotation) {
		Objects.requireNonNull(from);
		Objects.requireNonNull(to);
		transitions(from).add(new Transition<>(this, from, to, guard, action, eventSlot, timeout, annotation));
	}

	/**
	 * Adds a timeout transition.
	 * 
	 * @param from       transition source state
	 * @param to         transition target state
	 * @param guard      condition guarding transition
	 * @param action     action for transition
	 * @param annotation annotation text
	 */
	public void addTransitionOnTimeout(S from, S to, BooleanSupplier guard, Consumer<E> action, String annotation) {
		addTransition(from, to, guard, action, null, true, annotation);
	}

	/**
	 * Adds a transition which is fired if the guard condition holds and the current input's class
	 * equals the given event class.
	 * 
	 * @param from       transition source state
	 * @param to         transition target state
	 * @param guard      condition guarding transition
	 * @param action     action for transition
	 * @param eventClass class used for matching the current event
	 * @param annotation annotation text
	 */
	public void addTransitionOnEventClass(S from, S to, BooleanSupplier guard, Consumer<E> action,
			Class<? extends E> eventClass, String annotation) {
		Objects.requireNonNull(eventClass);
		if (matchEventsBy != TransitionMatchStrategy.BY_CLASS) {
			throw new IllegalStateException("Cannot add transition, wrong match strategy: " + matchEventsBy);
		}
		addTransition(from, to, guard, action, eventClass, false, annotation);
	}

	/**
	 * Adds a transition which is fired if the guard condition holds and the current input equals the
	 * given event.
	 * 
	 * @param from       transition source state
	 * @param to         transition target state
	 * @param guard      condition guarding transition
	 * @param action     action for transition
	 * @param eventValue event value used for matching the current event
	 * @param annotation annotation text
	 */
	public void addTransitionOnEventValue(S from, S to, BooleanSupplier guard, Consumer<E> action, E eventValue,
			String annotation) {
		Objects.requireNonNull(eventValue);
		if (matchEventsBy != TransitionMatchStrategy.BY_VALUE) {
			throw new IllegalStateException("Cannot add transition, wrong match strategy");
		}
		addTransition(from, to, guard, action, eventValue, false, annotation);
	}

	/**
	 * Adds a transition which is fired when the guard condition holds.
	 * 
	 * @param from       transition source state
	 * @param to         transition target state
	 * @param guard      condition guarding transition
	 * @param action     action for transition
	 * @param annotation annotation text
	 */
	public void addTransition(S from, S to, BooleanSupplier guard, Consumer<E> action, String annotation) {
		addTransition(from, to, guard, action, null, false, annotation);
	}

	@Override
	public void addEventListener(Consumer<E> listener) {
		eventListeners.add(listener);
	}

	@Override
	public void removeEventListener(Consumer<E> listener) {
		eventListeners.remove(listener);
	}

	@Override
	public void publish(E event) {
		if (loggingBlacklist.stream().noneMatch(condition -> condition.test(event))) {
			tracer.getLogger().info(() -> String.format("%s published event '%s'", this, event));
		}
		eventListeners.forEach(listener -> listener.accept(event));
	}

	/**
	 * Adds an input (event) to the input queue of this state machine.
	 * 
	 * @param event some input/event
	 */
	public void enqueue(E event) {
		eventQ.add(Objects.requireNonNull(event));
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
		return states.length > 0 ? Arrays.stream(states).anyMatch(s -> s.equals(getState())) : true;
	}

	@Override
	public void setState(S state) {
		if (currentState != null) {
			state(currentState).exitAction.run();
			fireExitListeners(currentState);
		}
		currentState = state;
		restartTimer(state);
		state(currentState).entryAction.run();
		fireEntryListeners(currentState);
	}

	@Override
	public void restartTimer(S state) {
		StateTimer timer = state(state).timer;
		if (timer != StateTimer.NEVER_ENDING_TIMER) {
			timer.reset();
			tracer.stateTimerRestarted(state);
		}
	}

	@Override
	public void resumeState(S state) {
		currentState = state;
		state(currentState).entryAction.run();
		fireEntryListeners(currentState);
	}

	public Stream<State<S>> states() {
		return stateMap.values().stream();
	}

	@Override
	public <StateType extends State<S>> StateType state() {
		if (currentState == null) {
			throw new IllegalStateException("Cannot access current state object, state machine not initialized.");
		}
		return state(currentState);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <StateType extends State<S>> StateType state(S state) {
		if (!stateMap.containsKey(state)) {
			return (StateType) realizeState(state, new State<>());
		}
		return (StateType) stateMap.get(state);
	}

	/**
	 * Creates or replaces the state instance for a state. Using a subclass instead of the generic state
	 * class is useful if the implementation of a state needs additional fields and methods.
	 * 
	 * @param state              state identifier
	 * @param stateClassInstance instance of state class
	 * @return the state instance
	 * 
	 * @param <StateClass> subclass of {@link State} class
	 */
	public <StateClass extends State<S>> StateClass realizeState(S state, StateClass stateClassInstance) {
		Objects.requireNonNull(stateClassInstance);
		stateClassInstance.id = state;
		stateMap.put(state, stateClassInstance);
		return stateClassInstance;
	}

	@Override
	public void init() {
		if (initialState == null) {
			throw new IllegalStateException("Cannot initialize state machine, no initial state defined.");
		}
		currentState = initialState;
		restartTimer(currentState);
		tracer.enteringInitialState(initialState);
		state(currentState).entryAction.run();
		fireEntryListeners(currentState);
	}

	@Override
	public void update() {
		if (currentState == null) {
			throw new IllegalStateException(
					String.format("Cannot update state, state machine '%s' not initialized.", getDescription()));
		}

		boolean timeout = state(currentState).timer.tick();

		// Find a transition matching the current input (ignore timeout-transitions)
		Optional<E> optionalInput = Optional.ofNullable(eventQ.poll());
		Optional<Transition<S, E>> matchingTransition = transitions(currentState).stream()
		//@formatter:off
				.filter(transition -> transition.guard.getAsBoolean())
				.filter(transition -> !transition.timeoutTriggered)
				.filter(transition -> isTransitionMatchingInput(transition, optionalInput))
				.findFirst();
		//@formatter:on
		if (matchingTransition.isPresent()) {
			fireTransition(matchingTransition.get(), optionalInput);
			return;
		}

		// No transition matching current input was found, what to do if input exists?
		optionalInput.ifPresent(input -> {
			switch (missingTransitionBehavior) {
			case LOG:
				tracer.unhandledInput(input);
				break;
			case EXCEPTION:
				throw new IllegalStateException(
						String.format("%s: No transition defined for state '%s' and event/input '%s'", this, currentState, input));
			case IGNORE:
				break;
			default:
				throw new IllegalArgumentException("Illegal value: " + missingTransitionBehavior);
			}
		});

		// No state change, execute tick action, if any
		state(currentState).tickAction.run(state(), state().getTicksConsumed(), state().getTicksRemaining());

		// Check if timeout occurred right now and fire matching transition, if any
		if (timeout) {
			matchingTransition = transitions(currentState).stream()
			//@formatter:off
					.filter(transition -> transition.guard.getAsBoolean())
					.filter(transition -> transition.timeoutTriggered)
					.findFirst();
			//@formatter:on
			if (matchingTransition.isPresent()) {
				fireTransition(matchingTransition.get(), Optional.empty());
			}
		}
	}

	private boolean isTransitionMatchingInput(Transition<S, E> transition, Optional<E> optionalInput) {
		if (!optionalInput.isPresent()) {
			return transition.eventValueOrClass == null;
		}
		if (matchEventsBy == TransitionMatchStrategy.BY_CLASS) {
			return optionalInput.get().getClass().equals(transition.eventClass());
		}
		if (matchEventsBy == TransitionMatchStrategy.BY_VALUE) {
			return optionalInput.get().equals(transition.eventValue());
		}
		throw new IllegalStateException(); // should not happen
	}

	private void fireTransition(Transition<S, E> transition, Optional<E> optionalInput) {
		tracer.firingTransition(transition, optionalInput);
		if (currentState.equals(transition.to)) {
			// loop: don't execute exit/entry actions, don't restart timer
			transition.action.accept(optionalInput.orElse(null));
		} else {
			// exit state
			tracer.exitingState(currentState);
			state(currentState).exitAction.run();
			fireExitListeners(currentState);
			// call action
			transition.action.accept(optionalInput.orElse(null));
			// enter new state
			currentState = transition.to;
			restartTimer(currentState);
			tracer.enteringState(currentState);
			state(currentState).entryAction.run();
			fireEntryListeners(currentState);
		}
	}

	/**
	 * Adds a listener that is executed when the given state is entered. This happenes after the
	 * {@code onEntry} hook method was called.
	 * 
	 * @param state    a state
	 * @param listener entry listener
	 */
	public void addStateEntryListener(S state, Consumer<State<S>> listener) {
		entryListeners(state).add(listener);
	}

	private Set<Consumer<State<S>>> entryListeners(S state) {
		if (entryListeners == null) {
			entryListeners = new LinkedHashMap<>();
		}
		if (!entryListeners.containsKey(state)) {
			entryListeners.put(state, new LinkedHashSet<>());
		}
		return entryListeners.get(state);
	}

	private void fireEntryListeners(S state) {
		if (entryListeners != null && entryListeners.containsKey(state)) {
			entryListeners.get(state).forEach(listener -> listener.accept(state(state)));
		}
	}

	/**
	 * Adds a listener that is executed when the given state is left. This happenes after the
	 * {@code onExit} hook method was called.
	 * 
	 * @param state    a state
	 * @param listener exit listener
	 */
	public void addStateExitListener(S state, Consumer<State<S>> listener) {
		exitListeners(state).add(listener);
	}

	private Set<Consumer<State<S>>> exitListeners(S state) {
		if (exitListeners == null) {
			exitListeners = new LinkedHashMap<>();
		}
		if (!exitListeners.containsKey(state)) {
			exitListeners.put(state, new LinkedHashSet<>());
		}
		return exitListeners.get(state);
	}

	private void fireExitListeners(S state) {
		if (exitListeners != null && exitListeners.containsKey(state)) {
			exitListeners.get(state).forEach(listener -> listener.accept(state(state)));
		}
	}
}