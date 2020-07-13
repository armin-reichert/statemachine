# Finite-State Machine

A finite-state machine implementation with the following features:
- Supports definition of state machines in declarative style using the builder pattern
- Supports *onEntry* and *onExit* actions for states
- Supports registration of state entry and state exit listeners
- Supports *onTick* actions for states which are triggered by a clock
- States can be implemented as subclasses of the generic *State* class
- States can have a timer and trigger a transition on timeout
- State transitions can be triggered by combinations of 
  - conditions (guards)
  - event conditions (match transitions by event value/equality or by event class)
  - state timeout
- Supports transition actions with information about the event that triggered the transition
- Actions can be implemented by lambda expression or function references
- Tracing of state machine processing to some logger

- Drawbacks: No hierarchical states supported

The states are identified by some arbitrary type, normally an enumeration type, string or integer.

## Example 1: Traffic light

A somewhat trivial example is a [traffic light](https://github.com/armin-reichert/statemachine-samples/blob/master/TrafficLight/src/main/java/de/amr/statemachine/samples/trafficlight/TrafficLight.java) controlled using timers.

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
				.state(RED).timeoutAfter(() -> app().clock().sec(3))
				.state(YELLOW).timeoutAfter(() -> app().clock().sec(2))
				.state(GREEN).timeoutAfter(() -> app().clock().sec(5))
			.transitions()
				.when(OFF).then(RED).condition(() -> Keyboard.keyPressedOnce(KeyEvent.VK_SPACE))
				.when(RED).then(GREEN).onTimeout()
				.when(GREEN).then(YELLOW).onTimeout()
				.when(YELLOW).then(RED).onTimeout()
		.endStateMachine();
		//@formatter:off
	}
}
```

## Example 2: Application lifecycle

In my simple [game library](https://github.com/armin-reichert/easy-game), each [application](https://github.com/armin-reichert/easy-game/blob/master/EasyGame/src/main/java/de/amr/easy/game/Application.java) has a lifecycle which is implemented by a finite-state machine.

## Example 3: Menu and controller for [Pong game](https://github.com/armin-reichert/pong)

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

## Example 4: Flappy-Bird game controller 

A slightly more complex example is the game controller of my [Flappy Bird](https://github.com/armin-reichert/birdy) game implementation.

```java
beginStateMachine()
	.description("[Play Scene]")
	.initialState(PLAYING)

.states()

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

	.stay(GAME_OVER)
		.condition(() -> Keyboard.keyPressedOnce(KeyEvent.VK_SPACE))
		.act(() -> BirdyGameApp.setScene(START_SCENE))

	.stay(GAME_OVER)
		.on(TOUCHED_GROUND)
		.act(() -> sound("music/bgmusic.mp3").stop())

.endStateMachine();
```

## Example 5: Pac-Man ghost "AI"

I used this state machine library extensively in my [Pac-Man](https://github.com/armin-reichert/pacman) game, in fact this Pac-man implementation was the main motivation for creating this library at all. 

```java
brain = StateMachine.beginStateMachine(GhostState.class, PacManGameEvent.class)

	.description(this::toString)
	.initialState(LOCKED)

	.states()

		.state(LOCKED)
			.onEntry(() -> {
				followState = LOCKED;
				visible = true;
				if (insanity != Insanity.IMMUNE) {
					insanity = Insanity.HEALTHY;
				}
				moveDir = wishDir = seat.startDir;
				tf.setPosition(seat.position);
				enteredNewTile();
				sprites.forEach(Sprite::resetAnimation);
				showColored();
			})
			.onTick(() -> {
				move();
				// not sure if ghost locked inside house should look frightened
				if (game.pacMan.power > 0) {
					showFrightened();
				} else {
					showColored();
				}
			})

		.state(LEAVING_HOUSE)
			.onEntry(() -> {
				steering().init();
			})
			.onTick(() -> {
				move();
				showColored();
			})
			.onExit(() -> forceMoving(Direction.LEFT))

		.state(ENTERING_HOUSE)
			.onEntry(() -> {
				tf.setPosition(maze.ghostSeats[0].position);
				moveDir = wishDir = Direction.DOWN;
				steering().init();
			})
			.onTick(() -> {
				move();
				showEyes();
			})

		.state(SCATTERING)
			.onTick(() -> {
				updateInsanity(game);
				move();
				showColored();
				checkCollision(game.pacMan);
			})

		.state(CHASING)
			.onTick(() -> {
				updateInsanity(game);
				move();
				showColored();
				checkCollision(game.pacMan);
			})

		.state(FRIGHTENED)
			.timeoutAfter(() -> sec(game.level.pacManPowerSeconds))
			.onTick((state, t, remaining) -> {
				move();
				// one flashing animation takes 0.5 sec
				int flashTicks = sec(game.level.numFlashes * 0.5f);
				if (remaining < flashTicks) {
					showFlashing();
				} else  {
					showFrightened();
				}
				checkCollision(game.pacMan);
			})

		.state(DEAD)
			.timeoutAfter(sec(1)) // time while ghost is drawn as number of scored points
			.onEntry(() -> {
				showPoints(Game.POINTS_GHOST[game.level.ghostsKilledByEnergizer - 1]);
			})
			.onTick((state, t, remaining) -> {
				if (remaining == 0) { // show as eyes returning to ghost home
					move();
					showEyes();
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
```
