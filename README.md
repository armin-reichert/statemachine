# Finite-State Machine

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

## Example 3: Flappy-Bird game controller 

A slightly more complex example is the game controller of my [Flappy Bird](https://github.com/armin-reichert/birdy) game implementation.

```java
beginStateMachine()
	.description("[Play Scene]")
	.initialState(PLAYING)

.states()

	.state(STARTING)
		.onEntry(() -> BirdyGameApp.setScene(Scene.START_SCENE))

	.state(PLAYING)
		.onEntry(() -> {
			points = 0;
			start();
		})

	.state(GAME_OVER)
		.onEntry(() -> stop())

.transitions()

	.stay(PLAYING)
		.on(TOUCHED_PIPE)
		.condition(() -> points > 3)
		.act(e -> {
			points -= 3;
			sound("sfx/hit.mp3").play();
			Bird bird = ent.named("bird");
			bird.tf.x += app().settings().getAsInt("obstacle-width") + bird.tf.width;
			bird.dispatch(TOUCHED_PIPE);
		})

	.stay(PLAYING)
		.on(PASSED_OBSTACLE)
		.act(e -> {
			points++;
			sound("sfx/point.mp3").play();
		})

	.when(PLAYING).then(GAME_OVER)
		.on(TOUCHED_PIPE)
		.condition(() -> points <= 3)
		.act(t -> {
			sound("sfx/hit.mp3").play();
			Bird bird = ent.named("bird");
			bird.dispatch(CRASHED);
		})

	.when(PLAYING).then(GAME_OVER)
		.on(TOUCHED_GROUND)
		.act(e -> {
			sound("music/bgmusic.mp3").stop();
			Bird bird = ent.named("bird");
			bird.dispatch(TOUCHED_GROUND);
		})

	.when(PLAYING).then(GAME_OVER)
		.on(LEFT_WORLD)
		.act(e -> {
			Bird bird = ent.named("bird");
			bird.dispatch(LEFT_WORLD);
			sound("music/bgmusic.mp3").stop();
		})

	.when(GAME_OVER).then(STARTING).condition(() -> Keyboard.keyPressedOnce(KeyEvent.VK_SPACE))

	.stay(GAME_OVER)
		.on(TOUCHED_GROUND)
		.act(() -> sound("music/bgmusic.mp3").stop())

.endStateMachine();
```

## Example 4: Pac-Man ghost "AI"

I used this state machine library extensively in my [Pac-Man](https://github.com/armin-reichert/pacman) game, in fact this Pac-man implementation was the main motivation for creating this library at all. 

```java
brain = StateMachine.beginStateMachine(GhostState.class, PacManGameEvent.class)

	.description(this::toString)
	.initialState(LOCKED)

	.states()

		.state(LOCKED)
			.onEntry(() -> {
				followState = getState();
				visible = true;
				setWishDir(maze.ghostHomeDir[seat]);
				setMoveDir(wishDir);
				tf.setPosition(maze.seatPosition(seat));
				enteredNewTile();
				sprites.forEach(Sprite::resetAnimation);
				show("color-" + moveDir);
			})
			.onTick(() -> {
				move();
				show(game.pacMan.powerTicks > 0 ? "frightened" : "color-" + moveDir);
			})

		.state(LEAVING_HOUSE)
			.onEntry(() -> steering().init())
			.onTick(() -> {
				move();
				show("color-" + moveDir);
			})
			.onExit(() -> forceMoving(LEFT))

		.state(ENTERING_HOUSE)
			.onEntry(() -> {
				tf.setPosition(maze.seatPosition(0));
				setWishDir(DOWN);
				steering().init();
			})
			.onTick(() -> {
				move();
				show("eyes-" + moveDir);
			})

		.state(SCATTERING)
			.onTick(() -> {
				move();
				show("color-" + moveDir);
				checkCollision(game.pacMan);
			})

		.state(CHASING)
			.onTick(() -> {
				move();
				show("color-" + moveDir);
				checkCollision(game.pacMan);
			})

		.state(FRIGHTENED)
			.timeoutAfter(() -> sec(game.level.pacManPowerSeconds))
			.onTick((state, t, remaining) -> {
				move();
				show(remaining < sec(2) ? "flashing" : "frightened");
				checkCollision(game.pacMan);
			})

		.state(DEAD)
			.timeoutAfter(sec(1)) // "dying" time
			.onEntry(() -> {
				int points = Game.POINTS_GHOST[game.level.ghostsKilledByEnergizer - 1];
				sprites.select("points-" + points);
			})
			.onTick(() -> {
				if (state().isTerminated()) { // "dead"
					move();
					show("eyes-" + moveDir);
				}
			})

	.transitions()

		.when(LOCKED).then(LEAVING_HOUSE)
			.on(GhostUnlockedEvent.class)

		.when(LEAVING_HOUSE).then(SCATTERING)
			.condition(() -> steering().isComplete() && followState == SCATTERING)

		.when(LEAVING_HOUSE).then(CHASING)
			.condition(() -> steering().isComplete() && followState == CHASING)

		.when(ENTERING_HOUSE).then(LEAVING_HOUSE)
			.condition(() -> steering().isComplete())

		.when(CHASING).then(FRIGHTENED)
			.on(PacManGainsPowerEvent.class)
			.act(() -> forceTurningBack())

		.when(CHASING).then(DEAD)
			.on(GhostKilledEvent.class)

		.when(CHASING).then(SCATTERING)
			.condition(() -> followState == SCATTERING)
			.act(() -> forceTurningBack())

		.when(SCATTERING).then(FRIGHTENED)
			.on(PacManGainsPowerEvent.class)
			.act(() -> forceTurningBack())

		.when(SCATTERING).then(DEAD)
			.on(GhostKilledEvent.class)

		.when(SCATTERING).then(CHASING)
			.condition(() -> followState == CHASING)
			.act(() -> forceTurningBack())

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
			.condition(() -> maze.atGhostHouseDoor(tile()))

.endStateMachine();
/*@formatter:on*/
brain.setMissingTransitionBehavior(MissingTransitionBehavior.LOG);
brain.getTracer().setLogger(PacManStateMachineLogging.LOG);
```
