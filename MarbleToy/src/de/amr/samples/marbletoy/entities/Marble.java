package de.amr.samples.marbletoy.entities;

import de.amr.easy.game.entity.GameEntityUsingSprites;
import de.amr.easy.game.sprite.Sprite;

public class Marble extends GameEntityUsingSprites {

	public Marble(int size) {
		setSprite("s_marble", Sprite.ofAssets("marble.png").scale(size));
		setSelectedSprite("s_marble");
		tf.setWidth(size);
		tf.setHeight(size);
	}

	@Override
	public void update() {
		tf.move();
	}
}