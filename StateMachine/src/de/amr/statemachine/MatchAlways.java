package de.amr.statemachine;

public class MatchAlways<S, E> implements MatchEventStrategy<S, E> {

	@Override
	public boolean matches(E event) {
		return true;
	}
}