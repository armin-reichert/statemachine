package de.amr.statemachine.api;

/**
 * Enumerates the event matching strategies of a state machine. Events are either matched by event
 * object equality or by event class.
 * 
 * @author Armin Reichert
 */
public enum EventMatchStrategy {
	BY_EQUALITY, BY_CLASS
}
