package de.amr.schule.markise;

import static de.amr.easy.game.Application.CLOCK;
import static de.amr.easy.game.Application.LOGGER;
import static java.lang.String.format;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.view.Controller;
import de.amr.easy.game.view.View;
import de.amr.statemachine.Match;
import de.amr.statemachine.StateMachine;

/**
 * Modell einer Markise mit Wind- und Regensensor.
 * 
 * @author Armin Reichert & Anna u. Peter Schillo
 */
public class Markise extends GameEntity implements View, Controller {

	private final MarkiseApp app;
	private final Motor motor;
	private final PositionsSensor positionsSensor;
	private final RegenSensor regenSensor;
	private final WindSensor windSensor;
	private final StateMachine<String, String> automat;

	private int position;

	public Markise(MarkiseApp app) {
		this.app = app;

		// Aktoren
		motor = new Motor(this);

		// Sensoren
		positionsSensor = new PositionsSensor(this);
		regenSensor = new RegenSensor();
		windSensor = new WindSensor();

		// Steuerung

		//@formatter:off
		automat = StateMachine.define(String.class, String.class, Match.BY_EQUALITY)
			.description("MarkisenSteuerung")
			.initialState("Eingefahren")

			.states()
			
				.state("Eingefahren")
						.onEntry(motor::stop)
	
				.state("FährtAus")		
					.onEntry(() -> {
						motor.vor();
						Assets.sound("bewegen.mp3").play();
					})
					.onTick(() -> {
						if (position > 50 && !Assets.sound("quietschen.mp3").isRunning()) {
							Assets.sound("quietschen.mp3").play();
						}
					})
					.onExit(() -> {
						Assets.sound("quietschen.mp3").stop();
						Assets.sound("bewegen.mp3").stop();
					})
					
				.state("Ausgefahren")
					.onEntry(motor::stop)
					
				.state("FährtEin")
					.onEntry(() -> {
						if (regenSensor.esRegnet()) {
							motor.schnellZurück();
						} else {
							motor.zurück();
						}
						Assets.sound("bewegen.mp3").play();
					})
					.onExit(() -> {
						Assets.sound("bewegen.mp3").stop();
					})
					
				.state("Gestoppt")
					.onEntry(motor::stop)
			
			.transitions()
			
				.stay("Eingefahren").on("up")
				.stay("Eingefahren").on("stop")
				.when("Eingefahren").then("FährtAus").on("down").condition(() -> !regenSensor.esRegnet())
	
				.stay("FährtAus").on("down")
				.when("FährtAus").then("Ausgefahren").condition(positionsSensor::inEndPosition)
				.when("FährtAus").then("Gestoppt").on("stop")
				.when("FährtAus").then("Gestoppt").condition(regenSensor::esRegnet)
				.when("FährtAus").then("FährtEin").on("up")
				
				.stay("Ausgefahren").on("down")
				.stay("Ausgefahren").on("stop")
				.when("Ausgefahren").then("FährtEin").on("up")
				.when("Ausgefahren").then("FährtEin").condition(regenSensor::esRegnet)
				
				.when("FährtEin").then("Eingefahren").condition(positionsSensor::inStartPosition)
				.when("FährtEin").then("Gestoppt").on("stop")
				.when("FährtEin").then("FährtAus").on("down")

				.stay("Gestoppt").on("stop")
				.when("Gestoppt").then("FährtEin").on("up") 
				.when("Gestoppt").then("FährtAus").on("down") 
				.when("Gestoppt").then("FährtEin").condition(regenSensor::esRegnet)

		.endStateMachine();
		//@formatter:on

		automat.traceTo(LOGGER, CLOCK::getFrequency);
	}

	public void raiseEvent(String event) {
		automat.enqueue(event);
	}

	public float getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	@Override
	public void init() {
		Assets.sound("bewegen.mp3");
		Assets.sound("quietschen.mp3");
		automat.init();
	}

	@Override
	public void update() {
		regenSensor.update();
		windSensor.update();
		automat.update();
		motor.update();
	}

	@Override
	public void draw(Graphics2D g) {
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		// Balken zum Anzeigen der Ausfahrposition
		g.translate(tf.getX(), tf.getY());
		g.setColor(Color.BLUE);
		g.fillRect(0, 0, Math.round(position / 100f * app.settings.width), 30);
		g.translate(-tf.getX(), -tf.getY());

		// Statustext
		g.translate(tf.getX(), tf.getY() + 80);
		g.setFont(new Font("Monospaced", Font.BOLD, 16));
		g.drawString(format("Wetter: %s %s  Geschw: %.1f  Position: %d%%  Zustand: %s",
				regenSensor.esRegnet() ? "Regen" : "Sonnenschein", windSensor.esIstWindig() ? "Windig" : "Windstill",
				tf.getVelocityX(), position, automat.getState()), 0, 0);
		g.translate(-tf.getX(), -(tf.getY() + 80));
	}
}