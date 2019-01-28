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

	private static class Action implements Runnable {

		int count;

		@Override
		public void run() {
			count++;
		}
	}

	private StateMachine<State, Event> fsm;
	private Action entryAction, exitAction, tickAction, timeoutAction;

	@Before
	public void buildMachine() {
		entryAction = new Action();
		exitAction = new Action();
		tickAction = new Action();
		timeoutAction = new Action();
		/*@formatter:off*/
		fsm = StateMachine.beginStateMachine(State.class, Event.class, Match.BY_EQUALITY)
				.initialState(State.S1)
				.states()
					.state(State.S1).onEntry(entryAction)
					.state(State.S2)
						.onExit(exitAction)
					.state(State.S3)
						.timeoutAfter(() -> 10)
						.onTick(tickAction)
				.transitions()
					.when(State.S1).then(State.S1).on(Event.E1)
					.when(State.S1).then(State.S2).on(Event.E2)
					.when(State.S2).then(State.S3).on(Event.E3)
					.stay(State.S3).onTimeout().act(timeoutAction)
		.endStateMachine();
		/*@formatter:on*/
	}

	@Test(expected = IllegalStateException.class)
	public void testNotInitialized() {
		fsm.process(Event.E1);
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

	@Test
	public void testEntryAction() {
		fsm.init();
		Assert.assertEquals(1, entryAction.count);
	}

	@Test
	public void testExitAction() {
		fsm.init();
		fsm.process(Event.E2);
		fsm.process(Event.E3);
		Assert.assertEquals(1, exitAction.count);
	}

	@Test
	public void testTickAction() {
		fsm.init();
		fsm.process(Event.E2);
		fsm.process(Event.E3);
		while (!fsm.state().isTerminated()) {
			fsm.update();
		}
		Assert.assertEquals(fsm.state(State.S3).getDuration(), tickAction.count);
	}

	@Test
	public void testTimeoutAction() {
		fsm.init();
		fsm.process(Event.E2);
		fsm.process(Event.E3);
		while (!fsm.state().isTerminated()) {
			fsm.update();
		}
		Assert.assertEquals(1, timeoutAction.count);
	}

}