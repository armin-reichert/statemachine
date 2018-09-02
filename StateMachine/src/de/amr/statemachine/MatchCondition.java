package de.amr.statemachine;

public interface MatchCondition<E> {

	boolean matches(E event);

}
