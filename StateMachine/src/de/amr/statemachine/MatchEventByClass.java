package de.amr.statemachine;

public class MatchEventByClass<E> implements MatchCondition<E> {

	private final Class<? extends E> eventClass;

	public MatchEventByClass(Class<? extends E> eventClass) {
		this.eventClass = eventClass;
	}

	@Override
	public boolean matches(E eventOrNull) {
		if (eventOrNull == null) {
			return false;
		}
		return eventClass.equals(eventOrNull.getClass());
	}
}