package de.amr.statemachine;

public class MatchEventByEquality<S, E> implements MatchEventStrategy<S, E> {

	private final E eventObject;

	public MatchEventByEquality(E eventObject) {
		this.eventObject = eventObject;
	}

	@Override
	public boolean matches(StateMachine<S, E> fsm, Transition<S, E> transition, E event) {
		if (!transition.guard.getAsBoolean()) {
			return false;
		}
		if (eventObject != null) {
			return event != null && event.equals(eventObject);
		}
		return true;
	}
}
