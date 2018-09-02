package de.amr.statemachine;

public interface MatchEventStrategy<S, E> {

	boolean matches(E event);

}
