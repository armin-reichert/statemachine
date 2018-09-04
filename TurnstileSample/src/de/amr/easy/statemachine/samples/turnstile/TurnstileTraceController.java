package de.amr.easy.statemachine.samples.turnstile;

class TurnstileTraceController implements TurnstileController {

	public TurnstileTraceController() {
	}

	@Override
	public void lock() {
		System.out.println("Action: lock");
	}

	@Override
	public void unlock() {
		System.out.println("Action: unlock");
	}

	@Override
	public void thankyou() {
		System.out.println("Action: thankyou");
	}

	@Override
	public void alarm() {
		System.out.println("Action: alarm");
	}
}
