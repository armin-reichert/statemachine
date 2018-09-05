package de.amr.samples.marbletoy.router;

import de.amr.easy.game.math.Vector2f;

public enum RoutingPoint {
	Initial(0, 0),
	A(178, 15),
	B(424, 15),
	C(178, 405),
	D(424, 405),
	E(62, 204),
	F(545, 204),
	G(175, 325),
	H(424, 325),
	X1(178, 82),
	X2(424, 82),
	X3(301, 204);

	private RoutingPoint(int x, int y) {
		location = Vector2f.of(x, y);
	}

	public Vector2f getLocation() {
		return location;
	}

	private final Vector2f location;
}