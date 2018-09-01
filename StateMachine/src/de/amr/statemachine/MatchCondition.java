package de.amr.statemachine;

public interface MatchCondition<S, E> {

	boolean matches(StateMachine<S, E> fsm, Transition<S, E> transition, E event);

}
