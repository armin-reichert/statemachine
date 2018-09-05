package de.amr.schule.mathebiber2012;

import de.amr.statemachine.Match;
import de.amr.statemachine.StateMachine;

public class Bebrocarina {

	public static void main(String[] args) {
		Bebrocarina acceptor = new Bebrocarina();
		acceptor.prüfeObSpielbar("+ooo+ooo+ooo+ooo+");
		acceptor.prüfeObSpielbar("---o+-o--ooo+");
		acceptor.prüfeObSpielbar("-----o+++++o-----");
		acceptor.prüfeObSpielbar("--+--+--o-+--");
	}

	private final StateMachine<Integer, Character> scanner;

	public Bebrocarina() {
		//@formatter:off
		scanner = StateMachine.define(Integer.class, Character.class, Match.BY_EQUALITY)
			.description("Bebrocarina")
			.initialState(1)

			.states()
			
				.state(1)
				.state(2)
				.state(3)
				.state(4)
				.state(5)
				.state(6)
				.state(-1)
			
			.transitions()

				.stay(1).on('o')
				.when(1).then(2).on('+')
				.when(1).then(-1).on('-')
		
				.when(2).then(2).on('o')
				.when(2).then(3).on('+')
				.when(2).then(1).on('-')
		
				.when(3).then(3).on('o')
				.when(3).then(4).on('+')
				.when(3).then(2).on('-')
		
				.when(4).then(4).on('o')
				.when(4).then(5).on('+')
				.when(4).then(3).on('-')
		
				.when(5).then(5).on('o')
				.when(5).then(6).on('+')
				.when(5).then(4).on('-')
		
				.when(6).then(6).on('o')
				.when(6).then(-1).on('+')
				.when(6).then(5).on('-')
		
				.stay(-1).on('o')
				.stay(-1).on('+')
				.stay(-1).on('-')
		
		.endStateMachine();
		//@formatter:on
	}

	void prüfeObSpielbar(String wort) {
		scanner.init();
		for (int i = 0; i < wort.length(); ++i) {
			scanner.enqueue(wort.charAt(i));
			scanner.update();
		}
		if (scanner.getState() != -1) {
			System.out.println(wort + ": spielbar");
			return;
		}
		System.out.println(wort + ": nicht spielbar");
	}
}