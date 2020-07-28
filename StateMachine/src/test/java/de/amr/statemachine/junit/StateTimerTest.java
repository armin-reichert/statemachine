package de.amr.statemachine.junit;

import static de.amr.statemachine.core.StateMachine.beginStateMachine;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.amr.statemachine.api.TransitionMatchStrategy;
import de.amr.statemachine.core.StateMachine;

public class StateTimerTest {

	static final int N_TICKS = 20;
	StateMachine<Integer, Void> fsm;
	int ticks;

	@Before
	public void setup() {
		//@formatter:off
		fsm = beginStateMachine(Integer.class, Void.class, TransitionMatchStrategy.BY_CLASS)
				.initialState(1)
				.states()
					.state(1)
						.timeoutAfter(N_TICKS)
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
	public void test20Ticks() {
		ticks = 0;
		fsm.init();
		while (fsm.is(1)) {
			fsm.update();
		}
		Assert.assertEquals(N_TICKS, ticks);

		ticks = 0;
		fsm.setState(1);
		while (fsm.is(1)) {
			fsm.update();
		}
		Assert.assertEquals(N_TICKS, ticks);
	}
}
