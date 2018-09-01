package de.amr.statemachine;

public class MatchByClassCondition<S, E> implements MatchCondition<S, E> {
	
	private final Class<? extends E> eventType;
	
	public MatchByClassCondition(Class<? extends E> eventType) {
		this.eventType = eventType;
	}

	@Override
	public boolean matches(StateMachine<S,E> fsm, Transition<S, E> transition, E event) {
		if (!transition.guard.getAsBoolean()) {
			return false;
		}
		if (transition.timeout) {
			return fsm.state(transition.from).isTerminated();
		}
		if (eventType != null) {
			return event != null && eventType.equals(event.getClass());
		}
		return true;
	}

}
