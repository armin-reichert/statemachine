package de.amr.samples.fsm.lamp;

import static de.amr.samples.fsm.lamp.LampControl.LampState.OFF;
import static de.amr.samples.fsm.lamp.LampControl.LampState.ON;

import de.amr.samples.fsm.lamp.LampControl.LampEvent;
import de.amr.samples.fsm.lamp.LampControl.LampState;
import de.amr.statemachine.StateMachine;
import de.amr.statemachine.StateMachineClient;

public class LampControl implements StateMachineClient<LampState, LampEvent> {

	// states
	public enum LampState {
		ON, OFF
	}

	// events
	public static interface LampEvent {
	}
	
	public static class ToggleEvent implements LampEvent {
	}

	private StateMachine<LampState, LampEvent> fsm;

	@Override
	public StateMachine<LampState, LampEvent> getStateMachine() {
		return fsm;
	}

	public LampControl(Lamp lamp) {

		//@formatter:off
		fsm = StateMachine
			.define(LampState.class, LampEvent.class)
			.initialState(OFF)
			.states()
				.state(ON)
				.state(OFF)
			.transitions()
				.when(OFF)
					.on(ToggleEvent.class).act(lamp::switchOn)
					.then(ON)
				.when(ON)
					.on(ToggleEvent.class).act(lamp::switchOff)
					.then(OFF)
		.endStateMachine();
		//@formatter:off
	}

	public void toggle() {
		fsm.process(new ToggleEvent());
		fsm.update();
	}
}