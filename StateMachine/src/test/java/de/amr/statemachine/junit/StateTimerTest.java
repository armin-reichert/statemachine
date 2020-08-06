package de.amr.statemachine.junit;

import static de.amr.statemachine.core.StateMachine.beginStateMachine;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.amr.statemachine.api.TransitionMatchStrategy;
import de.amr.statemachine.core.StateMachine;

public class StateTimerTest {

	private StateMachine<Integer, Void> fsm;
	private int ticks;

	@Before
	public void setup() {
		//@formatter:off
		fsm = beginStateMachine(Integer.class, Void.class, TransitionMatchStrategy.BY_CLASS)
				.initialState(1)
				.states()
					.state(1)
						.timeoutAfter(20)
						.onTick(() -> {
							++ticks;
						})
					.state(2)
				.transitions()
					.when(1).then(2).onTimeout()
		.endStateMachine();
		//@formatter:on
	}

	@Test
	public void testTicks() {
		ticks = 0;
		fsm.init();
		Assert.assertEquals(20, fsm.state(1).getDuration());
		while (fsm.is(1)) {
			fsm.update();
		}
		Assert.assertEquals(20, ticks);

		ticks = 0;
		fsm.setState(1);
		Assert.assertEquals(20, fsm.state(1).getDuration());
		while (fsm.is(1)) {
			fsm.update();
		}
		Assert.assertEquals(20, ticks);
	}
}