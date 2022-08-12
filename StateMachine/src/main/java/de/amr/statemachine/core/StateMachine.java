package de.amr.statemachine.core;

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

	/**
	 * Creates a new state machine instance and starts its definition building.
	 * 
	 * @param stateIdClass  class of state identifiers e.g. an enumeration class
	 * @param eventClass    class of events/inputs
	 * @param matchStrategy strategy for matching events against transition definitions
	 * 
	 * @param <S0>          state identifier type
	 * @param <E0>          event/input type
	 * @return state machine builder
	 */
	public static <S0, E0> StateMachineBuilder<S0, E0> beginStateMachine(Class<S0> stateIdClass, Class<E0> eventClass,
			TransitionMatchStrategy matchStrategy) {
		return new StateMachineBuilder<>(stateIdClass, matchStrategy);
	}

	private S initialStateId;
	private S currentStateId;
	private Transition<S, E> lastFiredTransition;
	private MissingTransitionBehavior missingTransitionBehavior = MissingTransitionBehavior.EXCEPTION;
	private final TransitionMatchStrategy matchEventsBy;
	private final Map<S, State<S>> stateMap;
	private final Map<S, List<Transition<S, E>>> transitionMap = new HashMap<>(7);
	private Deque<E> eventQ;

	private Set<Consumer<E>> eventListeners;
	private Map<S, Set<Consumer<State<S>>>> entryListeners;
	private Map<S, Set<Consumer<State<S>>>> exitListeners;

	private Supplier<String> fnDescription = () -> String.format("[%s]", getClass().getSimpleName());
	private Tracer<S, E> tracer = new Tracer<>();
	private final List<Predicate<E>> publishingBlacklist = new ArrayList<>();

	/**
	 * Creates a new state machine with a transition match strategy of "by class".
	 * 
	 * @param stateIdClass type of state identifiers, e.g. an enumeration class
	 */
	public StateMachine(Class<S> stateIdClass) {
		this(stateIdClass, TransitionMatchStrategy.BY_CLASS);
	}

	/**
	 * Creates a new state machine.
	 * 
	 * @param stateIdClass  class of state identifiers, e.g. some enumeration class
	 * @param matchStrategy strategy for matching events against transitions
	 */
	public StateMachine(Class<S> stateIdClass, TransitionMatchStrategy matchStrategy) {
		stateMap = selectMapImplementation(Objects.requireNonNull(stateIdClass));
		matchEventsBy = Objects.requireNonNull(matchStrategy);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Map<S, State<S>> selectMapImplementation(Class<S> stateIdClass) {
		Map<S, State<S>> map = null;
		if (stateIdClass.isEnum()) {
			map = new EnumMap(stateIdClass);
		} else if (stateIdClass.equals(Boolean.class)) {
			map = new BooleanMap();
		} else {
			map = new HashMap<>();
		}
		return map;
	}

	/**
	 * Starts building of this state machine.
	 * 
	 * @return new state machine builder
	 */
	public StateMachineBuilder<S, E> beginStateMachine() {
		return new StateMachineBuilder<>(this);
	}

	@Override
	public Tracer<S, E> getTracer() {
		return tracer;
	}

	@Override
	public void setTracer(Tracer<S, E> tracer) {
		this.tracer = Objects.requireNonNull(tracer);
	}

	@Override
	public void doNotLogEventProcessingIf(Predicate<E> condition) {
		tracer.doNotLog(Objects.requireNonNull(condition));
	}

	@Override
	public void doNotLogEventPublishingIf(Predicate<E> condition) {
		Objects.requireNonNull(condition);
		publishingBlacklist.add(condition);
	}

	/**
	 * @return the strategy for matching inputs/events against transitions
	 */
	public TransitionMatchStrategy getMatchStrategy() {
		return matchEventsBy;
	}

	/**
	 * @return the description of this state machine (used by tracing)
	 */
	public String getDescription() {
		return fnDescription.get();
	}

	/**
	 * Sets the description for this state machine.
	 * 
	 * @param fnDescription description text supplier
	 */
	public void setDescription(Supplier<String> fnDescription) {
		this.fnDescription = Objects.requireNonNull(fnDescription);
	}

	/**
	 * Sets a fixed description for this state machine.
	 * 
	 * @param description description text
	 */
	public void setDescription(String description) {
		setDescription(() -> description);
	}

	/**
	 * Defines how the state machine reacts to missing transitions.
	 * 
	 * @param behavior behavior in case no transition for the current event is defined
	 */
	@Override
	public void setMissingTransitionBehavior(MissingTransitionBehavior behavior) {
		this.missingTransitionBehavior = Objects.requireNonNull(behavior);
	}

	/**
	 * Sets the initial state (id) for this state machine.
	 * 
	 * @param stateId identifier of initial state
	 */
	public void setInitialState(S stateId) {
		this.initialStateId = Objects.requireNonNull(stateId);
	}

	/**
	 * @return the initial state (id) of this state machine
	 */
	public S getInitialState() {
		return initialStateId;
	}

	// create lists on demand
	private List<Transition<S, E>> transitions(S stateId) {
		transitionMap.computeIfAbsent(stateId, id -> new ArrayList<>(3));
		return transitionMap.get(stateId);
	}

	@Override
	public Stream<Transition<S, E>> transitions() {
		return stateMap.keySet().stream().flatMap(stateId -> transitions(stateId).stream());
	}

	@Override
	public Optional<Transition<S, E>> lastFiredTransition() {
		return Optional.ofNullable(lastFiredTransition);
	}

	/**
	 * Adds a new state transition.
	 * 
	 * @param from             source state id
	 * @param to               target state id
	 * @param guard            condition guarding transition
	 * @param action           action performed on transition
	 * @param eventSlot        event value/class if transition is matched by value/class
	 * @param timeoutTriggered if this transition is triggered by a timeout
	 * @param fnAnnotation     annotation text
	 */
	void addNewTransition(S from, S to, BooleanSupplier guard, Consumer<E> action, Object eventSlot, boolean timeout,
			Supplier<String> fnAnnotation) {
		transitions(from).add(new Transition<>(this, from, to, guard, action, eventSlot, timeout, fnAnnotation));
	}

	/**
	 * Adds a timeout-triggered transition.
	 * 
	 * @param from         source state id
	 * @param to           target state id
	 * @param guard        condition guarding transition
	 * @param action       action for transition
	 * @param fnAnnotation annotation text
	 */
	public void addTransitionOnTimeout(S from, S to, BooleanSupplier guard, Consumer<E> action,
			Supplier<String> fnAnnotation) {
		addNewTransition(from, to, guard, action, null, true, fnAnnotation);
	}

	/**
	 * Adds a transition which is fired if the guard condition holds and the current input's class equals the given class.
	 * 
	 * @param from         source state id
	 * @param to           target state id
	 * @param guard        condition guarding transition
	 * @param action       action for transition
	 * @param eventClass   class used for matching the current event
	 * @param fnAnnotation annotation text
	 */
	public void addTransitionOnEventClass(S from, S to, BooleanSupplier guard, Consumer<E> action,
			Class<? extends E> eventClass, Supplier<String> fnAnnotation) {
		Objects.requireNonNull(eventClass);
		if (matchEventsBy != TransitionMatchStrategy.BY_CLASS) {
			throw new IllegalStateException("Cannot add transition, wrong match strategy: " + matchEventsBy);
		}
		addNewTransition(from, to, guard, action, eventClass, false, fnAnnotation);
	}

	/**
	 * Adds a transition which is fired if the guard condition holds and the current input equals the given value.
	 * 
	 * @param from         source state id
	 * @param to           target state id
	 * @param guard        condition guarding transition
	 * @param action       action for transition
	 * @param value        value used for matching the current input/event
	 * @param fnAnnotation annotation text
	 */
	public void addTransitionOnEventValue(S from, S to, BooleanSupplier guard, Consumer<E> action, E value,
			Supplier<String> fnAnnotation) {
		Objects.requireNonNull(value);
		if (matchEventsBy != TransitionMatchStrategy.BY_VALUE) {
			throw new IllegalStateException("Cannot add transition, wrong match strategy");
		}
		addNewTransition(from, to, guard, action, value, false, fnAnnotation);
	}

	/**
	 * Adds a transition which is fired when the guard condition holds.
	 * 
	 * @param from         source state id
	 * @param to           target state id
	 * @param guard        condition guarding transition
	 * @param action       action for transition
	 * @param fnAnnotation annotation text
	 */
	public void addTransition(S from, S to, BooleanSupplier guard, Consumer<E> action, Supplier<String> fnAnnotation) {
		addNewTransition(from, to, guard, action, null, false, fnAnnotation);
	}

	private Set<Consumer<E>> eventListeners() {
		if (eventListeners == null) {
			eventListeners = new LinkedHashSet<>();
		}
		return eventListeners;
	}

	@Override
	public void addEventListener(Consumer<E> listener) {
		eventListeners().add(listener);
	}

	@Override
	public void removeEventListener(Consumer<E> listener) {
		eventListeners().remove(listener);
	}

	@Override
	public void publish(E event) {
		if (publishingBlacklist.stream().noneMatch(predicate -> predicate.test(event))) {
			tracer.publishedEvent(this, event);
		}
		eventListeners().forEach(listener -> listener.accept(event));
	}

	@Override
	public void enqueue(E event) {
		eventQ().add(Objects.requireNonNull(event));
	}

	@Override
	public void process(E event) {
		enqueue(event);
		update();
	}

	@Override
	public S getState() {
		return currentStateId;
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean is(S... stateIds) {
		if (stateIds.length == 0) {
			throw new IllegalArgumentException();
		}
		return Arrays.stream(stateIds).anyMatch(s -> s.equals(currentStateId));
	}

	@Override
	public void setState(S stateId) {
		enterState(stateId, true);
	}

	@Override
	public void resumeState(S stateId) {
		enterState(stateId, false);
	}

	private void enterState(S stateId, boolean resetTimer) {
		if (currentStateId != null) {
			state(currentStateId).exitAction.run();
			fireExitListeners(currentStateId);
		}
		currentStateId = Objects.requireNonNull(stateId);
		if (resetTimer) {
			resetTimer(stateId);
		}
		state(currentStateId).entryAction.run();
		fireEntryListeners(currentStateId);
		lastFiredTransition = null;
	}

	@Override
	public void resetTimer(S stateId) {
		Objects.requireNonNull(stateId);
		state(stateId).timer.reset();
		if (state(stateId).hasTimer()) {
			tracer.stateTimerReset(this, stateId);
		}
	}

	@Override
	public Stream<State<S>> states() {
		return stateMap.values().stream();
	}

	@Override
	public <S0 extends State<S>> S0 state() {
		if (currentStateId == null) {
			throw new IllegalStateException("Cannot access current state object, state machine not initialized.");
		}
		return state(currentStateId);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <S0 extends State<S>> S0 state(S stateId) {
		if (!stateMap.containsKey(stateId)) {
			// realize state by generic state object
			return (S0) realizeState(stateId, new State<>());
		}
		return (S0) stateMap.get(stateId);
	}

	/**
	 * Creates or replaces the implementation of a state. Using a subclass instead of the generic state class is useful if
	 * the implementation of a state needs additional fields and methods.
	 * 
	 * @param stateId   state identifier
	 * @param stateImpl instance of state class
	 * @return the state instance
	 * 
	 * @param <S0> subclass of {@link State} class
	 */
	public <S0 extends State<S>> S0 realizeState(S stateId, S0 stateImpl) {
		Objects.requireNonNull(stateId);
		Objects.requireNonNull(stateImpl);
		stateImpl.id = stateId;
		stateMap.put(stateId, stateImpl);
		return stateImpl;
	}

	@Override
	public void init() {
		if (initialStateId == null) {
			throw new IllegalStateException("Cannot initialize state machine, no initial state defined.");
		}
		currentStateId = initialStateId;
		lastFiredTransition = null;
		resetTimer(currentStateId);
		tracer.enteringInitialState(this, initialStateId);
		state(currentStateId).entryAction.run();
		fireEntryListeners(currentStateId);
	}

	@Override
	public void update() {
		if (currentStateId == null) {
			throw new IllegalStateException(
					String.format("Cannot update state, state machine '%s' not initialized.", getDescription()));
		}
		State<S> currentState = state(currentStateId);
		currentState.annotation = currentState.fnAnnotation.get();

		// Find a transition matching the current input (ignore timeout-transitions)
		Optional<E> optionalInput = eventQ != null ? Optional.ofNullable(eventQ().poll()) : Optional.empty();
		Optional<Transition<S, E>> matchingTransition = transitions(currentStateId).stream()
		//@formatter:off
			.filter(transition -> !transition.timeoutTriggered)
			.filter(transition -> transition.guard.getAsBoolean())
			.filter(transition -> isTransitionMatchingInput(transition, optionalInput))
			.findFirst();
		//@formatter:on
		if (matchingTransition.isPresent()) {
			fireTransition(matchingTransition.get(), optionalInput);
			return;
		}

		// No transition is matching current input, what to do?
		optionalInput.ifPresent(input -> {
			switch (missingTransitionBehavior) {
			case IGNORE:
				break;
			case LOG:
				tracer.unhandledEvent(this, input);
				break;
			case EXCEPTION:
				throw new IllegalStateException(String.format("%s: No transition defined for state '%s' and event/input '%s'",
						this, currentStateId, input));
			default:
				throw new IllegalArgumentException("Illegal value: " + missingTransitionBehavior);
			}
		});

		// Check if timeout occurred right now and fire matching transition, if any
		boolean timeout = currentState.timer.tick();
		if (timeout) {
			matchingTransition = transitions(currentStateId).stream()
			//@formatter:off
				.filter(transition -> transition.timeoutTriggered)
				.filter(transition -> transition.guard.getAsBoolean())
				.findFirst();
			//@formatter:on
			if (matchingTransition.isPresent()) {
				fireTransition(matchingTransition.get(), Optional.empty());
			}
		}

		// execute tick action, if any
		currentState.tickAction.run(currentState, currentState.getTicksConsumed(), currentState.getTicksRemaining());
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
		tracer.firingTransition(this, transition, optionalInput);
		if (currentStateId.equals(transition.to)) {
			// loop: don't execute exit/entry actions, don't restart timer
			transition.action.accept(optionalInput.orElse(null));
		} else {
			// exit current state
			tracer.exitingState(this, currentStateId);
			state(currentStateId).exitAction.run();
			fireExitListeners(currentStateId);
			// maybe call action
			transition.action.accept(optionalInput.orElse(null));
			// enter new state
			currentStateId = transition.to;
			resetTimer(currentStateId);
			tracer.enteringState(this, currentStateId);
			state(currentStateId).entryAction.run();
			fireEntryListeners(currentStateId);
		}
		lastFiredTransition = transition;
	}

	public Deque<E> eventQ() {
		if (eventQ == null) {
			eventQ = new ArrayDeque<>();
		}
		return eventQ;
	}

	/**
	 * Adds a listener that is executed when the given state is entered. This happenes after the {@code onEntry} hook
	 * method was called.
	 * 
	 * @param state    a state
	 * @param listener entry listener
	 */
	public void addStateEntryListener(S state, Consumer<State<S>> listener) {
		entryListeners(state).add(listener);
	}

	// Create map on demand
	private Set<Consumer<State<S>>> entryListeners(S state) {
		if (entryListeners == null) {
			entryListeners = new LinkedHashMap<>();
		}
		entryListeners.computeIfAbsent(state, s -> new LinkedHashSet<>());
		return entryListeners.get(state);
	}

	private void fireEntryListeners(S state) {
		if (entryListeners != null && entryListeners.containsKey(state)) {
			entryListeners.get(state).forEach(listener -> listener.accept(state(state)));
		}
	}

	/**
	 * Adds a listener that is executed when the given state is left. This happenes after the {@code onExit} hook method
	 * was called.
	 * 
	 * @param state    a state
	 * @param listener exit listener
	 */
	public void addStateExitListener(S state, Consumer<State<S>> listener) {
		exitListeners(state).add(listener);
	}

	// Create map on demand
	private Set<Consumer<State<S>>> exitListeners(S state) {
		if (exitListeners == null) {
			exitListeners = new LinkedHashMap<>();
		}
		exitListeners.computeIfAbsent(state, s -> new LinkedHashSet<>());
		return exitListeners.get(state);
	}

	private void fireExitListeners(S state) {
		if (exitListeners != null && exitListeners.containsKey(state)) {
			exitListeners.get(state).forEach(listener -> listener.accept(state(state)));
		}
	}
}