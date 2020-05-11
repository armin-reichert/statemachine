# Finite-State Machine (Java 8 and newer)

A finite-state machine implementation with the following features:
- Supports definition of state machines in declarative style using the builder pattern
- Supports *onEntry* and *onExit* actions for states
- Supports *onTick* actions for states which are triggered by a clock
- States can be implemented as subclasses of the generic *State* class
- States can have a timer and trigger a transition on timeout
- State transitions can be triggered by combinations of 
  - conditions (guards)
  - event conditions (match by equality or by event class)
  - state timeout
- Supports transition actions with information about the event that triggered the transition
- Actions can be implemented by lambda expression or function references
- Tracer for state machine processing included
- Drawbacks: No hierarchical states supported

The states are identified by some arbitrary type, normally an enumeration type, string or integer.

## Example 1: Traffic light

```java
public class TrafficLight extends StateMachine<Light, Void> {

	public enum Light {
		OFF, RED, YELLOW, GREEN;
	}

	public TrafficLight() {
		//@formatter:off
		super(Light.class);
		beginStateMachine()
			.description("Traffic Light")
			.initialState(OFF)
			.states()
				.state(OFF)
				.state(RED).timeoutAfter(app().clock.sec(3))
				.state(YELLOW).timeoutAfter(app().clock.sec(2))
				.state(GREEN).timeoutAfter(app().clock.sec(5))
			.transitions()
				.when(OFF).then(RED).condition(() -> Keyboard.keyPressedOnce(KeyEvent.VK_SPACE))
				.when(RED).then(GREEN).onTimeout()
				.when(GREEN).then(YELLOW).onTimeout()
				.when(YELLOW).then(RED).onTimeout()
		.endStateMachine();
		//@formatter:on
	}
}
```

## Example 2: Menu and controller for [Pong game](https://github.com/armin-reichert/pong)

### Menu controller

```java
beginStateMachine()
	.description("Pong Menu")
	.initialState(Player1_Player2)

	.states()

		// for clarity, all states are listed, would also work without!
		.state(Player1_Player2)
		.state(Player1_Computer)
		.state(Computer_Player2)
		.state(Computer_Computer)

	.transitions()

		.when(Player1_Player2)	.then(Player1_Computer)	.condition(this::nextEntrySelected)
		.when(Player1_Computer)	.then(Computer_Player2)	.condition(this::nextEntrySelected)
		.when(Computer_Player2)	.then(Computer_Computer).condition(this::nextEntrySelected)
		.when(Computer_Computer).then(Player1_Player2)	.condition(this::nextEntrySelected)

		.when(Player1_Player2)	.then(Computer_Computer).condition(this::prevEntrySelected)
		.when(Computer_Computer).then(Computer_Player2)	.condition(this::prevEntrySelected)
		.when(Computer_Player2)	.then(Player1_Computer)	.condition(this::prevEntrySelected)
		.when(Player1_Computer)	.then(Player1_Player2)	.condition(this::prevEntrySelected)

.endStateMachine();
```

### Game Controller:

```java
public enum PlayState {
	INIT, PLAYING, SERVING, GAME_OVER;
}

beginStateMachine()
	.description("Pong")	
	.initialState(INIT)

.states()
	.state(INIT).onEntry(this::initEntities)
	.state(SERVING).timeoutAfter(app().clock.sec(2)).onEntry(this::prepareService)
	.state(PLAYING).onTick(this::updateEntities)
	.state(GAME_OVER)

.transitions()
	.when(INIT).then(SERVING).act(this::resetScores)
	.when(SERVING).then(PLAYING).onTimeout().act(this::serveBall)
	.stay(PLAYING).condition(this::leftPaddleHitsBall).act(this::returnBallWithLeftPaddle)
	.stay(PLAYING).condition(this::rightPaddleHitsBall).act(this::returnBallWithRightPaddle)
	.when(PLAYING).then(SERVING).condition(this::isBallOutLeft).act(this::assignPointToRightPlayer)
	.when(PLAYING).then(SERVING).condition(this::isBallOutRight).act(this::assignPointToLeftPlayer)
	.when(PLAYING).then(GAME_OVER).condition(() -> leftPlayerWins() || rightPlayerWins())
	.when(GAME_OVER).then(INIT).condition(() -> Keyboard.keyPressedOnce(KeyEvent.VK_SPACE))

.endStateMachine();
```

## Example 3: Pac-Man ghost behavior

```java
beginStateMachine(GhostState.class, PacManGameEvent.class)

	.description(Ghost.this::toString)
	.initialState(LOCKED)

	.states()

		.state(LOCKED)
			.onEntry(() -> {
				cast().placeOnSeat(this);
				setVisible(true);
				followState = getState();
				sprites.select("color-" + moveDir());
				sprites.forEach(Sprite::resetAnimation);
			})
			.onTick((state, t, remaining) -> {
					step(cast().pacMan.hasPower() ? "frightened" : "color-" + moveDir());
			})

		.state(LEAVING_HOUSE)
			.onEntry(() -> steering().init())
			.onTick(() -> {
				step("color-" + moveDir());
			})

		.state(ENTERING_HOUSE)
			.onEntry(() -> steering().init())
			.onTick(() -> step("eyes-" + moveDir()))

		.state(SCATTERING)
			.onTick(() -> {
				step("color-" + moveDir());
				checkPacManCollision();
			})

		.state(CHASING)
			.onTick(() -> {
				step("color-" + moveDir());
				checkPacManCollision();
			})

		.state(FRIGHTENED)
			.timeoutAfter(() -> sec(game().level().pacManPowerSeconds))
			.onTick((state, t, remaining) -> {
				step(remaining < sec(2) ? "flashing" : "frightened");
				checkPacManCollision();
			})

		.state(DEAD)
			.timeoutAfter(sec(1)) // "dying" time
			.onEntry(() -> {
				int points = POINTS_GHOST[game().level().ghostsKilledByEnergizer - 1];
				sprites.select("points-" + points);
			})
			.onTick(() -> {
				if (state().isTerminated()) { // "dead"
					step("eyes-" + moveDir());
				}
			})

	.transitions()

		.when(LOCKED).then(LEAVING_HOUSE)
			.on(GhostUnlockedEvent.class)

		.stay(LOCKED)
			.on(PacManGainsPowerEvent.class)

		.when(LEAVING_HOUSE).then(SCATTERING)
			.condition(() -> steering().isComplete() && followState == SCATTERING)
			.act(() -> forceMove(Direction.LEFT))

		.when(LEAVING_HOUSE).then(CHASING)
			.condition(() -> steering().isComplete() && followState == CHASING)
			.act(() -> forceMove(Direction.LEFT))

		.stay(LEAVING_HOUSE)
			.on(PacManGainsPowerEvent.class)

		.when(ENTERING_HOUSE).then(LEAVING_HOUSE)
			.condition(() -> steering().isComplete())

		.stay(ENTERING_HOUSE)
			.on(PacManGainsPowerEvent.class)

		.when(CHASING).then(FRIGHTENED)
			.on(PacManGainsPowerEvent.class)
			.act(() -> turnBack())

		.when(CHASING).then(DEAD)
			.on(GhostKilledEvent.class)

		.when(CHASING).then(SCATTERING)
			.condition(() -> followState == SCATTERING)
			.act(() -> turnBack())

		.when(SCATTERING).then(FRIGHTENED)
			.on(PacManGainsPowerEvent.class)
			.act(() -> turnBack())

		.when(SCATTERING).then(DEAD)
			.on(GhostKilledEvent.class)

		.when(SCATTERING).then(CHASING)
			.condition(() -> followState == CHASING)
			.act(() -> turnBack())

		.stay(FRIGHTENED)
			.on(PacManGainsPowerEvent.class)
			.act(() -> restartTimer(FRIGHTENED))

		.when(FRIGHTENED).then(DEAD)
			.on(GhostKilledEvent.class)

		.when(FRIGHTENED).then(SCATTERING)
			.onTimeout()
			.condition(() -> followState == SCATTERING)

		.when(FRIGHTENED).then(CHASING)
			.onTimeout()
			.condition(() -> followState == CHASING)

		.when(DEAD).then(ENTERING_HOUSE)
			.condition(() -> maze().inFrontOfGhostHouseDoor(tile()))
			.act(() -> {
				tf.setPosition(cast().seatPosition(0));
				setWishDir(Direction.DOWN);
			})

.endStateMachine();
```

More examples can be found in my [Pac-Man](https://github.com/armin-reichert/pacman) game implementation.
