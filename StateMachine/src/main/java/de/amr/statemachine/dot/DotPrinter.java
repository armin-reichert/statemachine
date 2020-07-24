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

	public static String printToString(StateMachine<?, ?> fsm) {
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
			print("  ");
			print(state.id());
			String annotation = state.getAnnotation();
			if (annotation != null) {
				print(" [label=\"" + state.id() + ": " + annotation + "\"]");
			}
			if (state.id().equals(fsm.getState())) {
				print(" [fontcolor=\"red\"]");
			}
			print(";");
			ln();
		});
		ln();
		fsm.transitions().forEach(transition -> {
			print("  " + transition.from + " -> " + transition.to + " [label = \"");
			String annotation = transition.fnAnnotation.get();
			if (annotation != null) {
				annotation = "[" + annotation + "]";
			} else {
				annotation = "";
			}
			if (transition.timeoutTriggered) {
				print("timeout");
				print(annotation);
			} else if (fsm.getMatchStrategy() == TransitionMatchStrategy.BY_CLASS && transition.eventClass() != null) {
				print(transition.eventClass().getSimpleName());
				print(annotation);
			} else if (fsm.getMatchStrategy() == TransitionMatchStrategy.BY_VALUE && transition.eventValue() != null) {
				print(transition.eventValueOrClass);
				print(annotation);
			} else {
				if (annotation.length() > 0) {
					print(annotation);
				} else {
					print("condition");
				}
			}
			print("\"]");
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