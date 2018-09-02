package de.amr.statemachine;

public class MatchEventByClass<S, E> implements MatchEventStrategy<S, E> {

	private final Class<? extends E> eventType;

	public MatchEventByClass(Class<? extends E> eventType) {
		this.eventType = eventType;
	}

	@Override
	public boolean matches(E event) {
		if (eventType != null) {
			return event != null && eventType.equals(event.getClass());
		}
		return true;
	}
}