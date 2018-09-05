package de.amr.samples.marbletoy.fsm;

import static de.amr.samples.marbletoy.fsm.LeverControlMealyMachine.ToyState.LLL;
import static de.amr.samples.marbletoy.fsm.LeverControlMealyMachine.ToyState.LLR;
import static de.amr.samples.marbletoy.fsm.LeverControlMealyMachine.ToyState.LRL;
import static de.amr.samples.marbletoy.fsm.LeverControlMealyMachine.ToyState.LRR;
import static de.amr.samples.marbletoy.fsm.LeverControlMealyMachine.ToyState.RLL;
import static de.amr.samples.marbletoy.fsm.LeverControlMealyMachine.ToyState.RLR;
import static de.amr.samples.marbletoy.fsm.LeverControlMealyMachine.ToyState.RRL;
import static de.amr.samples.marbletoy.fsm.LeverControlMealyMachine.ToyState.RRR;

import java.util.function.Consumer;

import de.amr.statemachine.Match;
import de.amr.statemachine.StateMachine;

public class LeverControlMealyMachine {

	public enum ToyState {
		LLL, LLR, LRL, LRR, RLL, RLR, RRL, RRR
	}

	private final StateMachine<ToyState, Character> fsm;
	private final StringBuilder output = new StringBuilder();
	private final Consumer<Character> C = c -> output.append('C');
	private final Consumer<Character> D = c -> output.append('D');

	public LeverControlMealyMachine() {
		//@formatter:off
		fsm = StateMachine.define(ToyState.class, Character.class, Match.BY_EQUALITY)
				.description("Marble-Toy (Mealy)")
				.initialState(ToyState.LLL)
				
				.states()
				
				.transitions()
					.when(LLL).then(RLL).on('A').act(C)
					.when(LLL).then(LRR).on('B').act(C)
					.when(LLR).then(RLR).on('A').act(C)
					.when(LLR).then(LRL).on('B').act(D)
					.when(LRL).then(RRL).on('A').act(C)
					.when(LRL).then(LLL).on('B').act(D)
					.when(LRR).then(RRR).on('A').act(C)
					.when(LRR).then(LLR).on('B').act(D)
					.when(RLL).then(LLR).on('A').act(C)
					.when(RLL).then(RRR).on('B').act(C)
					.when(RLR).then(LLL).on('A').act(D)
					.when(RLR).then(RRL).on('B').act(D)
					.when(RRL).then(LRR).on('A').act(C)
					.when(RRL).then(RLL).on('B').act(D)
					.when(RRR).then(LRL).on('A').act(D)
					.when(RRR).then(RLR).on('B').act(D)
		
		.endStateMachine();
		//@formatter:on
	}

	public boolean process(String input) {
		fsm.init();
		output.setLength(0);
		input.chars().forEach(ch -> {
			fsm.enqueue((char) ch);
			fsm.update();
		});
		return output.length() > 0 && output.charAt(output.length() - 1) == 'D';
	}
}