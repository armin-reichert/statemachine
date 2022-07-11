package de.amr.statemachine.api;

public interface Log {

	/**
	 * Logs the message produced by the given supplier.
	 * 
	 * @param messageFormat message format
	 * @param args          message arguments
	 */
	void loginfo(String messageFormat, Object... args);

	boolean isShutUp();

	/**
	 * Tells the logger to shut up.
	 * 
	 * @param shutUp if should shut up
	 */
	void shutUp(boolean shutUp);
}