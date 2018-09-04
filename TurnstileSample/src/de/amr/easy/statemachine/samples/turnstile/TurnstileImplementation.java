package de.amr.easy.statemachine.samples.turnstile;

import static de.amr.easy.statemachine.samples.turnstile.TurnstileEvent.COIN;
import static de.amr.easy.statemachine.samples.turnstile.TurnstileEvent.PASS;
import static de.amr.easy.statemachine.samples.turnstile.TurnstileState.LOCKED;
import static de.amr.easy.statemachine.samples.turnstile.TurnstileState.UNLOCKED;

import de.amr.statemachine.Match;
import de.amr.statemachine.StateMachine;

/**
 * @see https://www.linkedin.com/pulse/automata-based-programming-general-purpose-finite-state-kolarova-1.
 * 
 * @author Armin Reichert
 */
public class TurnstileImplementation extends StateMachine<TurnstileState, TurnstileEvent>
		implements Turnstile {

	private TurnstileController controller;

	public TurnstileImplementation() {
		this(new TurnstileNullController());
	}

	public TurnstileImplementation(TurnstileController c) {
		super(TurnstileState.class, Match.BY_EQUALITY);
		controller = c;
		//@formatter:off
		define()
			.description("TurnstileStateMachine")
			.initialState(LOCKED)
			.states()
				.state(LOCKED)
				.state(UNLOCKED)
			.transitions()
				.stay(LOCKED)
					.on(PASS).act(controller::alarm)
				.when(LOCKED).then(UNLOCKED)
					.on(COIN).act(controller::unlock)
				.when(UNLOCKED).then(LOCKED)
					.on(PASS).act(controller::lock)
				.stay(UNLOCKED)
					.on(COIN).act(controller::thankyou)
			.endStateMachine();
		//@formatter:on
	}

	public void setController(TurnstileController controller) {
		this.controller = controller;
	}

	@Override
	public void event(TurnstileEvent event) {
		process(event);
	}
}