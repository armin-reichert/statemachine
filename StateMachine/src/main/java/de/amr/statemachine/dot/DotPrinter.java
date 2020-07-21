package de.amr.statemachine.dot;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import de.amr.statemachine.api.TransitionMatchStrategy;
import de.amr.statemachine.core.StateMachine;

/**
 * Prints a state machine graph in <a href="https://graphviz.org">DOT format</a>.
 * 
 * @author Armin Reichert
 */
public class DotPrinter {

	public static String dotText(StateMachine<?, ?> fsm) {
		StringWriter sw = new StringWriter();
		new DotPrinter(sw).print(fsm);
		return sw.toString();
	}

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

	private void ln() {
		pw.println();
	}

	public void print(StateMachine<?, ?> fsm) {
		print("digraph");
		print(" \"" + fsm.getDescription() + "\" {");
		ln();
		print("  rankdir=LR;");
		ln();
		print("  node [shape=ellipse, fontname=\"Courier\" fontsize=\"8\"];");
		ln();
		print("  edge [fontname=\"Courier\" fontsize=\"8\"];");
		ln();
		print("  ");
		fsm.states().forEach(state -> print(state.id() + " "));
		print(";");
		ln();
		fsm.transitions().forEach(transition -> {
			print("  " + transition.from + " -> " + transition.to + " [ label = \"");
			if (transition.timeoutTriggered) {
				print("timeout");
			} else if (fsm.getMatchStrategy() == TransitionMatchStrategy.BY_CLASS && transition.eventClass() != null) {
				print(transition.eventClass().getSimpleName());
			} else if (fsm.getMatchStrategy() == TransitionMatchStrategy.BY_VALUE && transition.eventValue() != null) {
				print(transition.eventValueOrClass);
			} else {
				print("condition");
			}
			print("\" ];");
			ln();
		});
		print("}");
		ln();
	}
}