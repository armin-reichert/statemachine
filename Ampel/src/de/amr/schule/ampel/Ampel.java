package de.amr.schule.ampel;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;

import de.amr.easy.game.Application;
import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.view.View;
import de.amr.statemachine.StateMachine;

/**
 * Die Ampel.
 * 
 * @author Armin Reichert & Anna Schillo
 *
 */
public class Ampel extends GameEntity implements View {

	private final StateMachine<String, Void> automat;

	public Ampel(int width, int height) {
		tf.setWidth(100);
		tf.setHeight(3 * width);
		//@formatter:off
		automat = StateMachine.define(String.class, Void.class)
		.description("Ampel")
		.initialState("Aus")
		.states()
			.state("Aus")
			.state("Rot").timeoutAfter(()->3*60)
			.state("Gelb").timeoutAfter(()->1*60)
			.state("Grün").timeoutAfter(()->5*60)
		.transitions()
			.when("Aus").then("Rot").condition(() -> Keyboard.keyPressedOnce(KeyEvent.VK_SPACE))
			.when("Rot").then("Grün").onTimeout()
			.when("Grün").then("Gelb").onTimeout()
			.when("Gelb").then("Rot").onTimeout()
		.endStateMachine();
		//@formatter:off
	}

	@Override
	public void init() {
		automat.traceTo(Application.LOGGER, Application.CLOCK::getFrequency);
		automat.init();
	}

	@Override
	public void update() {
		automat.update();
	}

	@Override
	public void draw(Graphics2D g) {
		g.translate(tf.getX(), tf.getY());
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(Color.LIGHT_GRAY);
		g.fillRect(0, 0, tf.getWidth(), tf.getHeight());
		int inset = 3;
		int diameter = tf.getWidth() - inset * 2;
		if (automat.getState().equals("Rot")) {
			g.setColor(Color.RED);
			g.fillOval(inset, inset, diameter, diameter);
		} else if (automat.getState().equals("Gelb")) {
			g.setColor(Color.YELLOW);
			g.fillOval(inset, inset + tf.getHeight() / 3, diameter, diameter);
		} else if (automat.getState().equals("Grün")) {
			g.setColor(Color.GREEN);
			g.fillOval(inset, inset + tf.getHeight() * 2 / 3, diameter, diameter);
		}
		g.setStroke(new BasicStroke(inset));
		g.setColor(Color.BLACK);
		g.drawOval(inset, inset, diameter, diameter);
		g.drawOval(inset, inset + tf.getHeight() / 3, diameter, diameter);
		g.drawOval(inset, inset + tf.getHeight() * 2 / 3, diameter, diameter);
		g.translate(-tf.getX(), -tf.getY());
	}
}