package de.amr.statemachine;

public class MatchEventByEquality<E> implements MatchCondition<E> {

	private final E event;

	public MatchEventByEquality(E event) {
		this.event = event;
	}

	@Override
	public boolean matches(E eventOrNull) {
		if (eventOrNull == null) {
			return false;
		}
		return eventOrNull.equals(this.event);
	}
}
