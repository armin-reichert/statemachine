package de.amr.statemachine;

public class MatchByEqualityCondition<S, E> implements MatchCondition<S, E> {

	private final E eventObject;

	public MatchByEqualityCondition(E eventObject) {
		this.eventObject = eventObject;
	}

	@Override
	public boolean matches(StateMachine<S, E> fsm, Transition<S, E> transition, E event) {
		if (!transition.guard.getAsBoolean()) {
			return false;
		}
		if (transition.timeout) {
			return fsm.state(transition.from).isTerminated();
		}
		if (eventObject != null) {
			return event != null && event.equals(eventObject);
		}
		return true;
	}
}
