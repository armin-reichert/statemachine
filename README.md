# Finite State Machine (Java 8 and newer)

A finite state machine implementation with the following features:
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

Example 1 (traffic light):

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
		//@formatter:off
	}
}
```

Example 2 (controller for a Pong game):

```java

public enum PlayState {
	INIT, PLAYING, SERVING, GAME_OVER;
}

StateMachine.define(PlayState.class, Void.class)
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

More interesting examples can be found in my [Pac-Man](https://github.com/armin-reichert/pacman) game implementation.
