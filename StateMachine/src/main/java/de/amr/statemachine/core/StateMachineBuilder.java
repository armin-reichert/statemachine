package de.amr.statemachine.core;

import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import java.util.logging.Logger;

import de.amr.statemachine.api.EventMatchStrategy;
import de.amr.statemachine.api.TickAction;

/**
 * Builder for state machine instances.
 * 
 * @author Armin Reichert
 *
 * @param <S>
 *          Type for identifying states
 * @param <E>
 *          Type of events
 */
public class StateMachineBuilder<S, E> {

	private final Logger log = Logger.getGlobal();
	private StateMachine<S, E> sm;

	/**
	 * Creates a builder for a state machine of the given state type and event match strategy.
	 * 
	 * @param stateType
	 *                        type used for state identification
	 * @param matchStrategy
	 *                        how events are matched
	 */
	public StateMachineBuilder(Class<S> stateType, EventMatchStrategy matchStrategy) {
		sm = new StateMachine<>(stateType, matchStrategy);
	}

	/**
	 * Creates a builder for a state machine of the given state type and a class-based event match
	 * strategy.
	 * 
	 * @param stateType
	 *                    type used for state identification
	 */
	public StateMachineBuilder(Class<S> stateType) {
		sm = new StateMachine<>(stateType);
	}

	/**
	 * Internal constructor for builder.
	 * 
	 * @param sm
	 *             state machine using this builder
	 */
	StateMachineBuilder(StateMachine<S, E> sm) {
		this.sm = sm;
	}

	/**
	 * Assigns the given description supplier to the constructed state machine.
	 * 
	 * @param fnDescription
	 *                        description supplier
	 * @return the builder
	 */
	public StateMachineBuilder<S, E> description(Supplier<String> fnDescription) {
		sm.setDescription(Objects.requireNonNull(fnDescription));
		return this;
	}

	/**
	 * Assigns the given description to the constructed state machine.
	 * 
	 * @param text
	 *               some description text
	 * @return the builder
	 */
	public StateMachineBuilder<S, E> description(String text) {
		return description(() -> text);
	}

	/**
	 * Defines the initial state of the constructed state machine.
	 * 
	 * @param state
	 *                the initial state
	 * @return the builder
	 */
	public StateMachineBuilder<S, E> initialState(S state) {
		sm.setInitialState(state);
		return this;
	}

	/**
	 * Starts the state building section.
	 * 
	 * @return the state builder
	 */
	public StateBuilder states() {
		return new StateBuilder();
	}

	/**
	 * Builder for the states.
	 */
	public class StateBuilder {

		private S stateId;
		private Runnable entryAction, exitAction;
		private TickAction<S> tickAction;
		private boolean entryActionSet, exitActionSet, tickActionSet;
		private IntSupplier fnTimer;

		private void clear() {
			stateId = null;
			entryAction = exitAction = null;
			tickAction = null;
			entryActionSet = exitActionSet = tickActionSet = false;
			fnTimer = null;
		}

		/**
		 * Starts the construction of a state.
		 * 
		 * @param stateId
		 *                  state identifier
		 * @return the builder
		 */
		public StateBuilder state(S stateId) {
			// commit previous build if any
			if (this.stateId != null) {
				commit();
			}
			clear();
			// start new build
			this.stateId = stateId;
			return this;
		}

		/**
		 * Replaces the standard state instance by the given custom state instance.
		 * 
		 * @param                     <C>
		 *                              type of state instance
		 * @param customStateInstance
		 *                              custom state instance
		 * @return the builder
		 */
		public <C extends State<S, E>> StateBuilder customState(C customStateInstance) {
			if (customStateInstance == null) {
				throw new IllegalArgumentException("Custom state object cannot be NULL");
			}
			sm.realizeState(stateId, customStateInstance);
			return this;
		}

		/**
		 * Defines the timer function for this state.
		 * 
		 * @param fnTimer
		 *                  timer function (returning duration in ticks)
		 * @return the builder
		 */
		public StateBuilder timeoutAfter(IntSupplier fnTimer) {
			if (fnTimer == null) {
				throw new IllegalStateException("Timer function cannot be null for state " + stateId);
			}
			this.fnTimer = fnTimer;
			return this;
		}

		/**
		 * Defines a constant timer for this state.
		 * 
		 * @param fixedTime
		 *                    number of state updates until timeout
		 * @return the builder
		 */
		public StateBuilder timeoutAfter(int fixedTime) {
			if (fixedTime < 0) {
				throw new IllegalStateException("Timer value must be positive for state " + stateId);
			}
			this.fnTimer = () -> fixedTime;
			return this;
		}

		/**
		 * Defines the action to be executed when this state is entered. Calling this method twice leads to
		 * an error.
		 * 
		 * @param action
		 *                 some action
		 * @return the builder
		 */
		public StateBuilder onEntry(Runnable action) {
			if (entryActionSet) {
				log.info(() -> String.format("ERROR: entry action already set: state %s in FSM %s", stateId,
						sm.getDescription()));
				throw new IllegalStateException();
			}
			entryAction = action;
			entryActionSet = true;
			return this;
		}

		/**
		 * Defines the action to be executed when this state is left. Calling this method twice leads to an
		 * error.
		 * 
		 * @param action
		 *                 some action
		 * @return the builder
		 */
		public StateBuilder onExit(Runnable action) {
			if (exitActionSet) {
				log.info(() -> String.format("ERROR: exit action already set: state %s in FSM %s", stateId,
						sm.getDescription()));
				throw new IllegalStateException();
			}
			exitAction = action;
			exitActionSet = true;
			return this;
		}

		/**
		 * Defines the action to be executed when this state is ticked. Calling this method twice leads to
		 * an error.
		 * 
		 * @param action
		 *                 some action
		 * @return the builder
		 */
		public StateBuilder onTick(TickAction<S> action) {
			if (tickActionSet) {
				log.info(() -> String.format("ERROR: tick action already set: state %s in FSM %s", stateId,
						sm.getDescription()));
				throw new IllegalStateException();
			}
			tickAction = action;
			tickActionSet = true;
			return this;
		}

		/**
		 * Defines the action to be executed when this state is ticked. Calling this method twice leads to
		 * an error.
		 * 
		 * @param action
		 *                 some action
		 * @return the builder
		 */
		public StateBuilder onTick(Runnable action) {
			return onTick((state, ticksConsumed, ticksRemaining) -> action.run());
		}

		private StateBuilder commit() {
			State<S, E> state = sm.state(stateId);
			state.entryAction = entryAction;
			state.exitAction = exitAction;
			state.tickAction = tickAction;
			if (fnTimer != null) {
				state.timer = new StateTimer(fnTimer);
			}
			else {
				state.timer = StateTimer.NEVER_ENDING_TIMER;
			}
			return this;
		}

		/**
		 * Starts the transition building section.
		 * 
		 * @return the transition builder
		 */
		public TransitionBuilder transitions() {
			// commit previous build if any
			if (this.stateId != null) {
				commit();
			}
			return new TransitionBuilder();
		}
	}

	/**
	 * Transition builder.
	 */
	public class TransitionBuilder {

		private boolean transitionBuildingStarted;
		private S sourceStateId;
		private S targetStateId;
		private BooleanSupplier guard;
		private boolean timeoutCondition;
		private E event;
		private Class<? extends E> eventType;
		private Consumer<E> action;

		private void clear() {
			transitionBuildingStarted = false;
			sourceStateId = null;
			targetStateId = null;
			guard = null;
			timeoutCondition = false;
			event = null;
			eventType = null;
			action = null;
		}

		/**
		 * Builds a loop transition for the given state.
		 * 
		 * @param stateId
		 *                  state identfier
		 * @return the builder
		 */
		public TransitionBuilder stay(S stateId) {
			return when(stateId);
		}

		/**
		 * Starts a transition from the given state.
		 * 
		 * @param sourceStateId
		 *                        source state identifier
		 * @return the builder
		 */
		public TransitionBuilder when(S sourceStateId) {
			if (sourceStateId == null) {
				throw new IllegalArgumentException("Transition source state must not be NULL");
			}
			if (this.sourceStateId != null) {
				commit();
			}
			clear();
			transitionBuildingStarted = true;
			this.sourceStateId = this.targetStateId = sourceStateId;
			return this;
		}

		/**
		 * Finishes a transition to the given target state.
		 * 
		 * @param targetStateId
		 *                        target state identifier
		 * @return the builder
		 */
		public TransitionBuilder then(S targetStateId) {
			if (targetStateId == null) {
				throw new IllegalArgumentException("Transition target state must not be NULL");
			}
			if (!transitionBuildingStarted) {
				throw new IllegalArgumentException("Transition building must be started with when(...) or stay(...)");
			}
			this.targetStateId = targetStateId;
			return this;
		}

		/**
		 * Sets a condition (guard) for the currently constructed transition.
		 * 
		 * @param guard
		 *                condition that must be fulfilled for firing this transition
		 * @return the builder
		 */
		public TransitionBuilder condition(BooleanSupplier guard) {
			if (guard == null) {
				throw new IllegalArgumentException("Transition guard must not be NULL");
			}
			if (!transitionBuildingStarted) {
				throw new IllegalArgumentException("Transition building must be started with when(...) or stay(...)");
			}
			this.guard = guard;
			return this;
		}

		/**
		 * Specifies that this transition should fire when the timer (if any) of the source state expires.
		 * 
		 * @return the builder
		 */
		public TransitionBuilder onTimeout() {
			if (timeoutCondition) {
				throw new IllegalArgumentException("Transition timeout condition must only be set once");
			}
			if (!transitionBuildingStarted) {
				throw new IllegalArgumentException("Transition building must be started with when(...) or stay(...)");
			}
			this.timeoutCondition = true;
			return this;
		}

		/**
		 * Specifies that this transition should fire when an event of the given type has been processed.
		 * 
		 * @param eventType
		 *                    event type
		 * @return the builder
		 */
		public TransitionBuilder on(Class<? extends E> eventType) {
			if (eventType == null) {
				throw new IllegalArgumentException("Event type of transition must not be NULL");
			}
			if (!transitionBuildingStarted) {
				throw new IllegalArgumentException("Transition building must be started with when(...) or stay(...)");
			}
			this.eventType = eventType;
			return this;
		}

		/**
		 * Specifies that this transition should fire when the given input has been processed.
		 * 
		 * @param input
		 *                input to the machine
		 * @return the builder
		 */
		public TransitionBuilder on(E input) {
			if (input == null) {
				throw new IllegalArgumentException("Input of transition must not be NULL");
			}
			if (!transitionBuildingStarted) {
				throw new IllegalArgumentException("Transition building must be started with when(...) or stay(...)");
			}
			this.event = input;
			return this;
		}

		/**
		 * Specifies the action that is executed when this transition fires.
		 * 
		 * @param action
		 *                 some action consuming the event leading to this transition
		 * @return the builder
		 */
		public TransitionBuilder act(Consumer<E> action) {
			if (action == null) {
				throw new IllegalArgumentException("Transition action must not be NULL");
			}
			if (!transitionBuildingStarted) {
				throw new IllegalArgumentException("Transition building must be started with when(...) or stay(...)");
			}
			this.action = action;
			return this;
		}

		/**
		 * Specifies the action that is executed when this transition fires.
		 * 
		 * @param action
		 *                 some action
		 * @return the builder
		 */
		public TransitionBuilder act(Runnable action) {
			if (action == null) {
				throw new IllegalArgumentException("Transition action must not be NULL");
			}
			if (!transitionBuildingStarted) {
				throw new IllegalArgumentException("Transition building must be started with when(...) or stay(...)");
			}
			this.action = e -> action.run();
			return this;
		}

		private TransitionBuilder commit() {
			if (timeoutCondition && event != null) {
				throw new IllegalStateException(
						"Cannot specify both timeout and event object for the same transition");
			}
			if (timeoutCondition && eventType != null) {
				throw new IllegalStateException("Cannot specify both timeout and event type for the same transition");
			}
			if (timeoutCondition) {
				sm.addTransitionOnTimeout(sourceStateId, targetStateId, guard, action);
			}
			else {
				sm.addTransition(sourceStateId, targetStateId, guard, action, event, eventType, false);
			}
			clear();
			return this;
		}

		/**
		 * Ends building of the state machine.
		 * 
		 * @return the constructed state machine
		 */
		public StateMachine<S, E> endStateMachine() {
			if (sourceStateId != null) {
				commit();
			}
			return sm;
		}
	}
}