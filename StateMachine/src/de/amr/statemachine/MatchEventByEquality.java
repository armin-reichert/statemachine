package de.amr.statemachine;

public class MatchEventByEquality<S, E> implements MatchEventStrategy<S, E> {

	private final E event;

	public MatchEventByEquality(E event) {
		this.event = event;
	}

	@Override
	public boolean matches(E event) {
		if (event != null) {
			return event.equals(this.event);
		}
		return false;
	}
}
