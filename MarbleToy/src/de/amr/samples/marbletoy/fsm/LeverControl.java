package de.amr.samples.marbletoy.fsm;

import static de.amr.samples.marbletoy.fsm.LeverControl.ToyState.LLL;
import static de.amr.samples.marbletoy.fsm.LeverControl.ToyState.LLL_D;
import static de.amr.samples.marbletoy.fsm.LeverControl.ToyState.LLR;
import static de.amr.samples.marbletoy.fsm.LeverControl.ToyState.LLR_D;
import static de.amr.samples.marbletoy.fsm.LeverControl.ToyState.LRL;
import static de.amr.samples.marbletoy.fsm.LeverControl.ToyState.LRL_D;
import static de.amr.samples.marbletoy.fsm.LeverControl.ToyState.LRR;
import static de.amr.samples.marbletoy.fsm.LeverControl.ToyState.LRR_D;
import static de.amr.samples.marbletoy.fsm.LeverControl.ToyState.RLL;
import static de.amr.samples.marbletoy.fsm.LeverControl.ToyState.RLL_D;
import static de.amr.samples.marbletoy.fsm.LeverControl.ToyState.RLR;
import static de.amr.samples.marbletoy.fsm.LeverControl.ToyState.RLR_D;
import static de.amr.samples.marbletoy.fsm.LeverControl.ToyState.RRL;
import static de.amr.samples.marbletoy.fsm.LeverControl.ToyState.RRL_D;
import static de.amr.samples.marbletoy.fsm.LeverControl.ToyState.RRR;
import static de.amr.samples.marbletoy.fsm.LeverControl.ToyState.RRR_D;

import de.amr.samples.marbletoy.entities.MarbleToy;
import de.amr.statemachine.Match;
import de.amr.statemachine.StateMachine;

public class LeverControl {

	public enum ToyState {
		LLL, LLR, LRL, LRR, RLL, RLR, RRL, RRR, LLL_D, LLR_D, LRL_D, LRR_D, RLL_D, RLR_D, RRL_D, RRR_D;
	};

	private final StateMachine<ToyState, Character> fsm;

	public LeverControl(MarbleToy toy) {
		//@formatter:off
		fsm = StateMachine.define(ToyState.class, Character.class, Match.BY_EQUALITY)
				
				.description("Marble Toy Lever Control")
				.initialState(LLL)

				.states()
				
				.transitions()

					.when(LLL).then(RLL).on('A')
					.when(LLL).then(LRR).on('B')
					.when(LLL_D).then(RLL).on('A')
					.when(LLL_D).then(LRR).on('B')
					.when(LLR).then(RLR).on('A')
					.when(LLR).then(LRL_D).on('B')
					.when(LLR_D).then(RLR).on('A')
					.when(LLR_D).then(LRL_D).on('B')
					.when(LRL).then(RRL).on('A')
					.when(LRL).then(LLL_D).on('B')
					.when(LRL_D).then(RRL).on('A')
					.when(LRL_D).then(LLL_D).on('B')
					.when(LRR).then(RRR).on('A')
					.when(LRR).then(LLR_D).on('B')
					.when(LRR_D).then(RRR).on('A')
					.when(LRR_D).then(LLR_D).on('B')
					.when(RLL).then(LLR).on('A')
					.when(RLL).then(RRR).on('B')
					.when(RLL_D).then(LLR).on('A')
					.when(RLL_D).then(RRR).on('B')
					.when(RLR).then(LLL_D).on('A')
					.when(RLR).then(RRL_D).on('B')
					.when(RLR_D).then(LLL_D).on('A')
					.when(RLR_D).then(RRL_D).on('B')
					.when(RRL).then(LRR).on('A')
					.when(RRL).then(RLL_D).on('B')
					.when(RRL_D).then(LRR).on('A')
					.when(RRL_D).then(RLL_D).on('B')
					.when(RRR).then(LRL_D).on('A')
					.when(RRR).then(RLR_D).on('B')
					.when(RRR_D).then(LRL_D).on('A')
					.when(RRR_D).then(RLR_D).on('B')
		
		.endStateMachine();
		//@formatter:on

		for (ToyState stateID : ToyState.values()) {
			fsm.state(stateID).setOnEntry(() -> updateLevers(toy));
		}
	}
	
	
	public StateMachine<ToyState, Character> getFsm() {
		return fsm;
	}

	public boolean isFinalState() {
		return fsm.getState().name().endsWith("_D");
	}

	void updateLevers(MarbleToy toy) {
		for (int i = 0; i < toy.levers.length; ++i) {
			toy.levers[i].setPointsLeft(isRoutingLeft(i));
		}
	}

	public boolean isRoutingLeft(int leverIndex) {
		return fsm.getState().name().charAt(leverIndex) == 'L';
	}

	public boolean process(String input) {
		fsm.init();
		input.chars().forEach(ch -> {
			fsm.enqueue((char) ch);
			fsm.update();
		});
		return isFinalState();
	}
}