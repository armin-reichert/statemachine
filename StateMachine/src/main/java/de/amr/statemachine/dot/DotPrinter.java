package de.amr.statemachine.dot;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.time.LocalDateTime;

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
		print("// generated at " + LocalDateTime.now());
		ln();
		print("digraph");
		print(" \"" + fsm.getDescription() + "\" {");
		ln();
		print("  rankdir=LR;");
		ln();
		print("  node [shape=ellipse, fontname=\"Arial\" fontsize=\"8\"];");
		ln();
		print("  edge [fontname=\"Arial\" fontsize=\"8\"];");
		ln();
		fsm.states().forEach(state -> {
			print("  " + state.id());
			if (state.id().equals(fsm.getState())) {
				print(" [fontcolor=\"red\"]");
			}
			print(";");
			ln();
		});
		ln();
		fsm.transitions().forEach(transition -> {
			print("  " + transition.from + " -> " + transition.to + " [label = \"");
			if (transition.timeoutTriggered) {
				print("timeout");
				if (transition.annotation != null) {
					print("[" + transition.annotation + "]");
				}
			} else if (fsm.getMatchStrategy() == TransitionMatchStrategy.BY_CLASS && transition.eventClass() != null) {
				print(transition.eventClass().getSimpleName());
				if (transition.annotation != null) {
					print("[" + transition.annotation + "]");
				}
			} else if (fsm.getMatchStrategy() == TransitionMatchStrategy.BY_VALUE && transition.eventValue() != null) {
				print(transition.eventValueOrClass);
				if (transition.annotation != null) {
					print("[" + transition.annotation + "]");
				}
			} else {
				if (transition.annotation != null) {
					print("[" + transition.annotation + "]");
				} else {
					print("condition");
				}
			}
			print("\" ]");
			fsm.lastFiredTransition().ifPresent(last -> {
				if (last == transition) {
					print(" [fontcolor=\"red\"]");
				}
			});
			print(";");
			ln();
		});
		print("}");
		ln();
	}
}