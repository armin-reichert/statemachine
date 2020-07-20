package de.amr.statemachine.dot;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;

import de.amr.statemachine.api.TransitionMatchStrategy;
import de.amr.statemachine.core.StateMachine;

/**
 * Prints a state machine graph in <a href="https://graphviz.org">DOT format</a>.
 * 
 * @author Armin Reichert
 */
public class DotPrinter {

	private PrintWriter pw;

	public DotPrinter(OutputStream out) {
		pw = new PrintWriter(out);
	}

	public DotPrinter(Writer writer) {
		pw = new PrintWriter(writer);
	}

	public DotPrinter() {
		this(System.out);
	}

	private void print(Object value) {
		pw.print(value);
	}

	private void println() {
		pw.println();
	}

	public void print(StateMachine<?, ?> fsm) {
		print("digraph");
		print(" \"");
		print(fsm.getDescription());
		print("\" {");
		println();
		print("  rankdir=LR;");
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
				} else if (fsm.getMatchStrategy() == TransitionMatchStrategy.BY_VALUE && transition.eventValue() != null) {
					print(transition.eventValueOrClass);
				} else {
					print("condition");
				}
			}
			print("\" ];");
			println();
		});
		print("}");
		println();
	}
}