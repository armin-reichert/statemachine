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
	
	private void print(String fmt, Object... args) {
		print(String.format(fmt, args));
	}

	private void print(Object value) {
		pw.print(value);
	}

	public void print(StateMachine<?, ?> fsm) {
		print("// created " + LocalDateTime.now());
		print("\ndigraph \"%s\" {", fsm.getDescription());
		print("\n  rankdir=LR;");
		print("\n  node [shape=ellipse fontname=\"sans-serif\" fontsize=%d];", 12);
		print("\n  edge [fontname=\"sans-serif\" fontsize=%d];", 12);
		print("\n\n  // states");
		fsm.states().forEach(state -> {
			print("\n  %s",state.id());
			print(" [label=\"");
			print(state.id());
			String annotation = state.getAnnotation();
			if (annotation != null) {
				print("\\n");
				print(annotation);
			}
			if (state.hasTimer() && !state.isTerminated()) {
				print("\\n%d of %d ticks", state.getTicksConsumed(), state.getDuration());
			}
			print("\"]");
			if (state.id().equals(fsm.getState())) {
				print(" [fontcolor=\"red\"]");
			}
			print(";");
		});
		print("\n\n  // transitions");
		fsm.transitions().forEach(transition -> {
			print("\n  " + transition.from + " -> " + transition.to + " [label = \"");
			String annotation = transition.fnAnnotation.get();
			if (annotation != null) {
				annotation = "[" + annotation + "]";
			} else {
				annotation = "";
			}
			if (transition.timeoutTriggered) {
				print("Timeout");
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
		});
		print("\n}");
	}
}