package de.amr.statemachine.junit;

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

	private static class StateImpl extends State<States, Events> {

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
					.state(States.S1).onEntry(entryAction)
					.state(States.S2)
						.onExit(exitAction)
					.state(States.S3)
						.timeoutAfter(() -> 10)
						.onTick(tickAction)
					.state(States.S4).customState(new StateImpl())
				.transitions()
					.when(States.S1).then(States.S1).on(Events.E1)
					.when(States.S1).then(States.S2).on(Events.E2)
					.when(States.S1).then(States.S4).on(Events.E4)
					.when(States.S2).then(States.S3).on(Events.E3)
					.stay(States.S3).onTimeout().act(timeoutAction)
					.stay(States.S4).on(Events.E4).act(selfLoopAction)
		.endStateMachine();
		/*@formatter:on*/
	}

	@Test(expected = IllegalStateException.class)
	public void testNotInitialized() {
		fsm.process(Events.E1);
	}

	@Test
	public void testInitialization() {
		fsm.init();
		Assert.assertEquals(States.S1, fsm.getState());
	}

	@Test(expected = IllegalStateException.class)
	public void testNoTransition() {
		fsm.init();
		fsm.process(Events.E3);
	}

	@Test
	public void testSelfTransition() {
		fsm.init();
		fsm.process(Events.E1);
		Assert.assertEquals(States.S1, fsm.getState());
	}

	@Test
	public void testTransition() {
		fsm.init();
		fsm.process(Events.E2);
		Assert.assertEquals(States.S2, fsm.getState());
	}

	@Test
	public void testThreeTransitions() {
		fsm.init();
		fsm.process(Events.E1);
		fsm.process(Events.E2);
		fsm.process(Events.E3);
		Assert.assertEquals(States.S3, fsm.getState());
	}

	@Test
	public void testEntryAction() {
		fsm.init();
		Assert.assertEquals(1, entryAction.count);
	}

	@Test
	public void testExitAction() {
		fsm.init();
		fsm.process(Events.E2);
		fsm.process(Events.E3);
		Assert.assertEquals(1, exitAction.count);
	}

	@Test
	public void testTickAction() {
		fsm.init();
		fsm.process(Events.E2);
		fsm.process(Events.E3);
		while (!fsm.state().isTerminated()) {
			fsm.update();
		}
		Assert.assertEquals(fsm.state(States.S3).getDuration(), tickAction.count);
	}

	@Test
	public void testTimeoutAction() {
		fsm.init();
		fsm.process(Events.E2);
		fsm.process(Events.E3);
		while (!fsm.state().isTerminated()) {
			fsm.update();
		}
		Assert.assertEquals(1, timeoutAction.count);
	}

	@Test
	public void testStateImpl() {
		fsm.init();
		fsm.process(Events.E4);
		Assert.assertEquals(States.S4, fsm.getState());
		Assert.assertEquals(StateImpl.class, fsm.state().getClass());
		StateImpl s4 = (StateImpl) fsm.state(States.S4);
		Assert.assertTrue(s4 == fsm.state());
		Assert.assertEquals(1, s4.entryCount);
		fsm.process(Events.E4);
		Assert.assertEquals(States.S4, fsm.getState());
		Assert.assertEquals(1, selfLoopAction.count);
	}

}