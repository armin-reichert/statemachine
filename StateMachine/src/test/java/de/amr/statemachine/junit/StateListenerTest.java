package de.amr.statemachine.junit;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.amr.statemachine.core.State;
import de.amr.statemachine.core.StateMachine;

public class StateListenerTest {

	StateMachine<Integer, Void> fsm;
	int stateEntryCount;
	int stateExitCount;

	@Before
	public void createFixture() {
		//@formatter:off
		fsm = StateMachine.beginStateMachine(Integer.class, Void.class)
				.initialState(0)
				.states()
					.state(0).timeoutAfter(1).onEntry(() -> ++stateEntryCount).onExit(() -> ++stateExitCount)
					.state(1).timeoutAfter(1).onEntry(() -> ++stateEntryCount).onExit(() -> ++stateExitCount)
					.state(2).onEntry(() -> ++stateEntryCount).onExit(() -> ++stateExitCount)
				.transitions()
					.when(0).then(1).onTimeout()
					.when(1).then(2).onTimeout()
				.endStateMachine();
		//@formatter:on
	}

	private void callEntryListener(State<Integer> state) {
		System.out.println("Entry listener");
		++stateEntryCount;
	}

	private void callExitListener(State<Integer> state) {
		System.out.println("Exit listener");
		++stateExitCount;
	}

	@Test
	public void testStateEntry() {
		fsm.init();
		while (fsm.getState() != 2) {
			fsm.update();
		}
		Assert.assertEquals(3, stateEntryCount);
	}

	@Test
	public void testStateExit() {
		fsm.init();
		while (fsm.getState() != 2) {
			fsm.update();
		}
		Assert.assertEquals(2, stateExitCount);
	}

	@Test
	public void testStateEntryListener() {
		fsm.addStateEntryListener(0, this::callEntryListener);
		fsm.addStateEntryListener(1, this::callEntryListener);
		fsm.init();
		while (fsm.getState() != 2) {
			fsm.update();
		}
		Assert.assertEquals(5, stateEntryCount);
	}

	@Test
	public void testStateExitListener() {
		fsm.addStateExitListener(0, this::callExitListener);
		fsm.addStateExitListener(1, this::callExitListener);
		fsm.init();
		while (fsm.getState() != 2) {
			fsm.update();
		}
		Assert.assertEquals(4, stateExitCount);
	}
}
