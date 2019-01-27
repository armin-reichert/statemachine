package de.amr.statemachine.junit;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.amr.statemachine.Match;
import de.amr.statemachine.StateMachine;

public class StateMachineTest {

	private StateMachine<String, String> fsm;

	@Before
	public void buildMachine() {
		/*@formatter:off*/
		fsm = StateMachine.beginStateMachine(String.class, String.class, Match.BY_EQUALITY)
				.initialState("Start")
				.states()
					.state("Start")
					.state("Intermediate")
					.state("Final")
				.transitions()
					.when("Start").then("Start").on("E")
					.when("Start").then("Intermediate").on("F")
					.when("Intermediate").then("Final").on("G")
		.endStateMachine();
		/*@formatter:on*/
	}

	@Test
	public void testInitialization() {
		fsm.init();
		Assert.assertEquals("Start", fsm.getState());
	}

	@Test(expected = IllegalStateException.class)
	public void testNoTransition() {
		fsm.init();
		fsm.process("Event1");
	}

	@Test
	public void testSelfTransition() {
		fsm.init();
		fsm.process("E");
		Assert.assertEquals("Start", fsm.getState());
	}
	
	@Test
	public void testTransition() {
		fsm.init();
		fsm.process("F");
		Assert.assertEquals("Intermediate", fsm.getState());
	}
	
	@Test
	public void testThreeTransitions() {
		fsm.init();
		fsm.process("E");
		fsm.process("F");
		fsm.process("G");
		Assert.assertEquals("Final", fsm.getState());
	}

}
