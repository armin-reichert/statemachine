package de.amr.statemachine.api;

import java.util.function.Consumer;
import java.util.function.Predicate;

import de.amr.statemachine.core.MissingTransitionBehavior;
import de.amr.statemachine.core.State;
import de.amr.statemachine.core.StateMachine;
import de.amr.statemachine.core.Tracer;

/**
 * If a class cannot directly inherit from the {@link StateMachine} class it can
 * implement this interface and supply a state machine instance. This interface
 * then delegates all state machine operations to the supplied instance.
 * 
 * @author Armin Reichert
 *
 * @param <S> state identifier type of the finite-state machine
 * @param <E> event type of the finite-state machine
 */
public interface FsmContainer<S, E> extends Fsm<S, E> {

	/**
	 * The contained state machine instance.
	 * 
	 * @return state machine instance
	 */
	Fsm<S, E> fsm();

	@Override
	default void init() {
		fsm().init();
	}

	@Override
	default void update() {
		fsm().update();
	}

	@Override
	default Tracer<S, E> getTracer() {
		return fsm().getTracer();
	}

	@Override
	default void setMissingTransitionBehavior(MissingTransitionBehavior missingTransitionBehavior) {
		fsm().setMissingTransitionBehavior(missingTransitionBehavior);
	}

	@Override
	default void doNotLogEventProcessingIf(Predicate<E> condition) {
		fsm().doNotLogEventProcessingIf(condition);
	}

	@Override
	default void doNotLogEventPublishingIf(Predicate<E> condition) {
		fsm().doNotLogEventPublishingIf(condition);
	}

	@Override
	default void addEventListener(Consumer<E> listener) {
		fsm().addEventListener(listener);
	}

	@Override
	default void removeEventListener(Consumer<E> listener) {
		fsm().removeEventListener(listener);
	}

	@Override
	default void publish(E event) {
		fsm().publish(event);
	}

	@Override
	default void setState(S state) {
		fsm().setState(state);
	}

	@Override
	default void resumeState(S state) {
		fsm().resumeState(state);
	}

	@Override
	default void resetTimer(S state) {
		fsm().resetTimer(state);
	}

	@Override
	default S getState() {
		return fsm().getState();
	}

	@Override
	@SuppressWarnings("unchecked")
	default boolean is(S... states) {
		return fsm().is(states);
	}

	@Override
	default <StateType extends State<S>> StateType state() {
		return fsm().state();
	}

	@Override
	default <StateType extends State<S>> StateType state(S state) {
		return fsm().state(state);
	}

	@Override
	default void process(E event) {
		fsm().process(event);
	}
}