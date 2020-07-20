package de.amr.statemachine.dot;

import java.io.PrintStream;

import de.amr.statemachine.api.TransitionMatchStrategy;
import de.amr.statemachine.core.StateMachine;

/**
 * Prints a state machine graph in <a href="https://graphviz.org">DOT format</a>.
 * 
 * @author Armin Reichert
 */
public class DotPrinter {

	private PrintStream out;

	public DotPrinter(PrintStream out) {
		this.out = out;
	}

	public DotPrinter() {
		this(System.out);
	}

	private void print(Object value) {
		out.print(value);
	}

	private void println() {
		out.println();
	}

	public void print(StateMachine<?, ?> fsm) {
		print("digraph");
		print(" \"");
		print(fsm.getDescription());
		print("\" {");
		println();
		print("rankdir=LR;");
		println();
		print("  node [shape=ellipse];");
		fsm.states().forEach(state -> {
			print(" " + state.id());
		});
		println();
		fsm.transitions().forEach(transition -> {
			print("  ");
			print(transition.from);
			print(" -> ");
			print(transition.to);
			print(" [ label = \"");
			if (transition.timeoutTriggered) {
				print("timeout");
			} else {
				if (fsm.getMatchStrategy() == TransitionMatchStrategy.BY_CLASS && transition.eventClass() != null) {
					print(transition.eventClass().getSimpleName());
				}
				if (fsm.getMatchStrategy() == TransitionMatchStrategy.BY_VALUE && transition.eventValue() != null) {
					print(transition.eventValueOrClass);
				}
			}
			print("\" ];");
			println();
		});
		print("}");
		println();
	}
}