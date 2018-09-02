package de.amr.statemachine;

public class MatchAlways<E> implements MatchCondition<E> {

	@Override
	public boolean matches(E eventOrNull) {
		return true;
	}
}