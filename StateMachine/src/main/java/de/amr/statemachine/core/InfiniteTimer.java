package de.amr.statemachine.core;

/**
 * A timer that never times out but that can still be used inside states for example to implement
 * initial wait times. The remaining ticks counter gets decremented such that the passed time value
 * in a state's tick action can be used. When the remaining time reaches zero, the timer is reset.
 * 
 * @author Armin Reichert
 */
public class InfiniteTimer extends StateTimer {

	public static final long INFINITY = Long.MAX_VALUE;

	public InfiniteTimer() {
		super(() -> INFINITY);
		reset();
	}

	@Override
	public boolean tick() {
		if (remaining == 0) {
			remaining = INFINITY;
		}
		return super.tick();
	}
}