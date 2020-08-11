package de.amr.statemachine.core;

import java.io.OutputStream;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import de.amr.statemachine.api.Log;

public class LogImpl implements Log {

	public static final PrintStream TRASHCAN = new PrintStream(OutputStream.nullOutputStream());
	public static final Log NULL = new LogImpl(TRASHCAN);

	private PrintStream destination;
	private PrintStream out;

	public LogImpl() {
		out = destination = System.out;
	}

	public LogImpl(PrintStream stream) {
		out = destination = stream;
	}

	@Override
	public void loginfo(String message, Object... args) {
		LocalDateTime now = LocalDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SSS");
		String timestamp = formatter.format(now);
		out.println(String.format("[%s] %s", timestamp, String.format(message, args)));
	}

	@Override
	public boolean isShutUp() {
		return out == TRASHCAN;
	}

	@Override
	public void shutUp(boolean shutUp) {
		out = shutUp ? TRASHCAN : destination;
	}
}