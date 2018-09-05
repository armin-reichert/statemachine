package de.amr.schule.garagentor;

import static de.amr.schule.garagentor.Garagentor.TorEreignis.FB_GEDRÜCKT;
import static de.amr.schule.garagentor.Garagentor.TorEreignis.SCHALTER_GEDRÜCKT;
import static de.amr.schule.garagentor.Garagentor.TorZustand.GESCHLOSSEN;
import static de.amr.schule.garagentor.Garagentor.TorZustand.GESTOPPT_BEIM_SCHLIESSEN;
import static de.amr.schule.garagentor.Garagentor.TorZustand.GESTOPPT_BEIM_ÖFFNEN;
import static de.amr.schule.garagentor.Garagentor.TorZustand.OFFEN;
import static de.amr.schule.garagentor.Garagentor.TorZustand.SCHLIESST;
import static de.amr.schule.garagentor.Garagentor.TorZustand.ÖFFNET;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

import de.amr.easy.game.Application;
import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.view.Controller;
import de.amr.easy.game.view.View;
import de.amr.statemachine.Match;
import de.amr.statemachine.StateMachine;

public class Garagentor extends GameEntity implements View, Controller {

	public enum TorZustand {
		GESCHLOSSEN, ÖFFNET, OFFEN, GESTOPPT_BEIM_ÖFFNEN, SCHLIESST, GESTOPPT_BEIM_SCHLIESSEN
	}

	public enum TorEreignis {
		SCHALTER_GEDRÜCKT, FB_GEDRÜCKT,
	}

	private GaragentorApp app;
	private StateMachine<TorZustand, TorEreignis> automat;
	private int position;
	private boolean hindernis;
	private boolean lichtBrennt;

	public Garagentor(GaragentorApp app) {

		this.app = app;

		//@formatter:off
		automat = StateMachine.define(TorZustand.class, TorEreignis.class, Match.BY_EQUALITY)
				.description("Garagentor Steuerung")
				.initialState(GESCHLOSSEN)

		.states()
		
				.state(GESCHLOSSEN)
						.timeoutAfter(() -> Application.CLOCK.sec(5))
						.onEntry(() -> lichtAn())
						.onExit(() -> lichtAus())
				
				.state(ÖFFNET)
					.onTick(() -> position++)
		
				.state(OFFEN)
				
				.state(SCHLIESST)
					.onTick(() -> position--)
				
		.transitions()
		
				.when(GESCHLOSSEN).then(ÖFFNET).on(SCHALTER_GEDRÜCKT)
				.when(GESCHLOSSEN).then(ÖFFNET).on(FB_GEDRÜCKT)
				.stay(GESCHLOSSEN).onTimeout().act(() -> lichtAus())
				
				.when(ÖFFNET).then(OFFEN).condition(() -> endPunktErreicht())
				.when(ÖFFNET).then(GESTOPPT_BEIM_ÖFFNEN).on(SCHALTER_GEDRÜCKT)
				.when(ÖFFNET).then(GESTOPPT_BEIM_ÖFFNEN).on(FB_GEDRÜCKT)
				
				.when(OFFEN).then(SCHLIESST).on(SCHALTER_GEDRÜCKT)
				.when(OFFEN).then(SCHLIESST).on(FB_GEDRÜCKT)
				
				.when(SCHLIESST).then(GESCHLOSSEN).condition(() -> anfangsPunktErreicht())
				.when(SCHLIESST).then(GESTOPPT_BEIM_SCHLIESSEN).on(SCHALTER_GEDRÜCKT)
				.when(SCHLIESST).then(GESTOPPT_BEIM_SCHLIESSEN).on(FB_GEDRÜCKT)
				.when(SCHLIESST).then(ÖFFNET).condition(() -> hindernisErkannt())
				
				.when(GESTOPPT_BEIM_ÖFFNEN).then(SCHLIESST).on(SCHALTER_GEDRÜCKT)
				.when(GESTOPPT_BEIM_ÖFFNEN).then(SCHLIESST).on(FB_GEDRÜCKT)

				.when(GESTOPPT_BEIM_SCHLIESSEN).then(ÖFFNET).on(SCHALTER_GEDRÜCKT)
				.when(GESTOPPT_BEIM_SCHLIESSEN).then(ÖFFNET).on(FB_GEDRÜCKT)

		.endStateMachine();
		//@formatter:on
	}

	public int getWidth() {
		return 800;
	}

	public int getHeight() {
		return 600;
	}

	@Override
	public void init() {
		automat.init();
	}

	@Override
	public void update() {
		if (Keyboard.keyPressedOnce(KeyEvent.VK_SPACE)) {
			automat.enqueue(SCHALTER_GEDRÜCKT);
		} else if (Keyboard.keyPressedOnce(KeyEvent.VK_F)) {
			automat.enqueue(FB_GEDRÜCKT);
		} else if (Keyboard.keyPressedOnce(KeyEvent.VK_H)) {
			if (automat.getState() == GESCHLOSSEN && !hindernis) {
				// Hindernis nicht einschalten
			} else {
				hindernis = !hindernis;
			}
		}
		automat.update();
	}

	@Override
	public void draw(Graphics2D g) {
		g.translate(tf.getX(), tf.getY());
		g.setColor(Color.BLUE);
		int w = position * app.settings.width / 100;
		g.fillRect(0, 0, w, 20);
		g.translate(-tf.getX(), -tf.getY());

		g.translate(tf.getX(), tf.getY() + 40);
		g.setFont(new Font("Monospaced", Font.BOLD, 20));
		g.drawString(String.format("Position: %d, Zustand: %s, Hindernis: %s, %s", position, automat.getState(),
				hindernis ? "Ja" : "Nein", lichtBrennt ? "Licht brennt" : ""), 0, 0);
		g.translate(-tf.getX(), -tf.getY());
	}

	private boolean endPunktErreicht() {
		return position >= 100;
	}

	private boolean anfangsPunktErreicht() {
		return position <= 0;
	}

	private boolean hindernisErkannt() {
		return hindernis;
	}

	private void lichtAn() {
		lichtBrennt = true;
	}

	private void lichtAus() {
		lichtBrennt = false;
	}
}