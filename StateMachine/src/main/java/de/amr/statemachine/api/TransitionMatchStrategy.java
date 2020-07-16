package de.amr.statemachine.api;

/**
 * Enumerates the matching strategies of a state machine.
 * <p>
 * Events/inputs are either matched against transitions by their value (object equality) or by their
 * class.
 * 
 * @author Armin Reichert
 */
public enum TransitionMatchStrategy {

	/** Transition match condition is evaluated by comparing with the input/event value. */
	BY_VALUE,

	/** Transition match condition is evaluated by comparing with the input/event class. */
	BY_CLASS;
}