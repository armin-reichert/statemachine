package de.amr.statemachine;

/**
 * Enumerates the event matching strategies of a state machine. Events are either matched by event
 * object equality or by event class.
 * 
 * @author Armin Reichert
 */
public enum Match {
	BY_EQUALITY, BY_CLASS
}
