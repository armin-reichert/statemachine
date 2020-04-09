package de.amr.statemachine.junit;

import static de.amr.statemachine.junit.StateMachineTest.Events.E1;
import static de.amr.statemachine.junit.StateMachineTest.Events.E2;
import static de.amr.statemachine.junit.StateMachineTest.Events.E3;
import static de.amr.statemachine.junit.StateMachineTest.Events.E4;
import static de.amr.statemachine.junit.StateMachineTest.States.S1;
import static de.amr.statemachine.junit.StateMachineTest.States.S2;
import static de.amr.statemachine.junit.StateMachineTest.States.S3;
import static de.amr.statemachine.junit.StateMachineTest.States.S4;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.amr.statemachine.api.EventMatchStrategy;
import de.amr.statemachine.core.State;
import de.amr.statemachine.core.StateMachine;

public class StateMachineTest {

	public enum States {
		S1, S2, S3, S4
	}

	public enum Events {
		E1, E2, E3, E4
	}

	private static class Action implements Runnable {

		int count;

		@Override
		public void run() {
			count++;
		}
	}

	private static class StateImpl extends State<States> {

		int entryCount;

		@Override
		public void onEntry() {
			entryCount++;
		}
	}

	private StateMachine<States, Events> fsm;
	private Action entryAction, exitAction, tickAction, timeoutAction, selfLoopAction;

	@Before
	public void buildMachine() {
		entryAction = new Action();
		exitAction = new Action();
		tickAction = new Action();
		timeoutAction = new Action();
		selfLoopAction = new Action();
		/*@formatter:off*/
		fsm = StateMachine.beginStateMachine(States.class, Events.class, EventMatchStrategy.BY_EQUALITY)
				.initialState(States.S1)
				.states()
					.state(S1).onEntry(entryAction)
					.state(S2).onExit(exitAction)
					.state(S3)
						.timeoutAfter(() -> 10)
						.onTick(tickAction)
					.state(S4).customState(new StateImpl())
				.transitions()
					.when(S1).then(S1).on(E1)
					.when(S1).then(S2).on(E2)
					.when(S1).then(S4).on(E4)
					.when(S2).then(S3).on(E3)
					.stay(S3).onTimeout().act(timeoutAction)
					.stay(S4).on(E4).act(selfLoopAction)
		.endStateMachine();
		/*@formatter:on*/
	}

	@Test(expected = IllegalStateException.class)
	public void testNotInitialized() {
		fsm.process(E1);
	}

	@Test
	public void testInitialization() {
		fsm.init();
		Assert.assertEquals(S1, fsm.getState());
	}

	@Test(expected = IllegalStateException.class)
	public void testNoTransition() {
		fsm.init();
		fsm.process(E3);
	}

	@Test
	public void testSelfTransition() {
		fsm.init();
		fsm.process(E1);
		Assert.assertEquals(S1, fsm.getState());
	}

	@Test
	public void testTransition() {
		fsm.init();
		fsm.process(E2);
		Assert.assertEquals(S2, fsm.getState());
	}

	@Test
	public void testThreeTransitions() {
		fsm.init();
		fsm.process(E1);
		fsm.process(E2);
		fsm.process(E3);
		Assert.assertEquals(S3, fsm.getState());
	}

	@Test
	public void testEntryAction() {
		fsm.init();
		Assert.assertEquals(1, entryAction.count);
	}

	@Test
	public void testExitAction() {
		fsm.init();
		fsm.process(E2);
		fsm.process(E3);
		Assert.assertEquals(1, exitAction.count);
	}

	@Test
	public void testTickAction() {
		fsm.init();
		fsm.process(E2);
		fsm.process(E3);
		while (!fsm.state().isTerminated()) {
			fsm.update();
		}
		Assert.assertEquals(fsm.state(S3).getDuration(), tickAction.count);
	}

	@Test
	public void testTimeoutAction() {
		fsm.init();
		fsm.process(E2);
		fsm.process(E3);
		while (!fsm.state().isTerminated()) {
			fsm.update();
		}
		Assert.assertEquals(1, timeoutAction.count);
	}

	@Test
	public void testStateImpl() {
		fsm.init();
		fsm.process(E4);
		Assert.assertEquals(S4, fsm.getState());
		Assert.assertEquals(StateImpl.class, fsm.state().getClass());
		StateImpl s4 = (StateImpl) fsm.state(S4);
		Assert.assertTrue(s4 == fsm.state());
		Assert.assertEquals(1, s4.entryCount);
		fsm.process(E4);
		Assert.assertEquals(S4, fsm.getState());
		Assert.assertEquals(1, selfLoopAction.count);
	}

}