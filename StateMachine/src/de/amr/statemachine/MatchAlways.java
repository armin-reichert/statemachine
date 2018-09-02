package de.amr.statemachine;


public class MatchAlways<S,E> implements MatchCondition<S, E> {

	@Override
	public boolean matches(StateMachine<S, E> fsm, Transition<S, E> transition, E event) {
		return true;
	}

}
