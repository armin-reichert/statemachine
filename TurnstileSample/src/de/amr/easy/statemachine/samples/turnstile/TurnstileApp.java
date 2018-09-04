package de.amr.easy.statemachine.samples.turnstile;

import java.util.Random;
import java.util.stream.IntStream;

public class TurnstileApp {

	private static Random rand = new Random();

	private static TurnstileEvent randomEvent() {
		TurnstileEvent[] events = TurnstileEvent.values();
		return events[rand.nextInt(events.length)];
	}

	public static void main(String[] args) {
		TurnstileImplementation t = new TurnstileImplementation();
		t.setController(new TurnstileTraceController());
		t.init();

		System.out.println(t.getState());
		IntStream.range(0, 5).forEach(i -> {
			TurnstileEvent event = randomEvent();
			System.out.println(event.name());
			t.event(event);
			System.out.println("->" + t.getState());
		});

		t.setController(new TurnstileNullController());
		IntStream.range(0, 5).forEach(i -> {
			TurnstileEvent event = randomEvent();
			System.out.println(event.name());
			t.event(event);
			System.out.println("->" + t.getState());
		});
	}
}
