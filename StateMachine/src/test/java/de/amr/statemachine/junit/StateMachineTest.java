package de.amr.statemachine.junit;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.amr.statemachine.Match;
import de.amr.statemachine.StateMachine;

public class StateMachineTest {

	public enum State {
		S1, S2, S3
	}

	public enum Event {
		E1, E2, E3
	}

	private StateMachine<State, Event> fsm;

	@Before
	public void buildMachine() {
		/*@formatter:off*/
		fsm = StateMachine.beginStateMachine(State.class, Event.class, Match.BY_EQUALITY)
				.initialState(State.S1)
				.states()
					.state(State.S1)
					.state(State.S2)
					.state(State.S3)
				.transitions()
					.when(State.S1).then(State.S1).on(Event.E1)
					.when(State.S1).then(State.S2).on(Event.E2)
					.when(State.S2).then(State.S3).on(Event.E3)
		.endStateMachine();
		/*@formatter:on*/
	}

	@Test
	public void testInitialization() {
		fsm.init();
		Assert.assertEquals(State.S1, fsm.getState());
	}

	@Test(expected = IllegalStateException.class)
	public void testNoTransition() {
		fsm.init();
		fsm.process(Event.E3);
	}

	@Test
	public void testSelfTransition() {
		fsm.init();
		fsm.process(Event.E1);
		Assert.assertEquals(State.S1, fsm.getState());
	}

	@Test
	public void testTransition() {
		fsm.init();
		fsm.process(Event.E2);
		Assert.assertEquals(State.S2, fsm.getState());
	}

	@Test
	public void testThreeTransitions() {
		fsm.init();
		fsm.process(Event.E1);
		fsm.process(Event.E2);
		fsm.process(Event.E3);
		Assert.assertEquals(State.S3, fsm.getState());
	}
}