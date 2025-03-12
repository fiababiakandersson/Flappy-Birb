package se.yrgo.game;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ScreenUtils;

/**
 * The `GameScreen` class represents the main gameplay screen of the game.
 * It handles the rendering, updating, and input processing for the game,
 * including the ship's movement, alien spawning, and collision detection.
 * The ship is controlled using the SPACE button, which makes it bounce upward,
 * while gravity pulls it downward.
 */
public class GameScreen extends ScreenAdapter implements InputProcessor {
	private static final int ALIEN_SIZE = 20; // Size of each alien in pixels
	private static final int SHIP_WIDTH = 46; // Width of the ship in pixels
	private static final int SHIP_HEIGHT = 20; // Height of the ship in pixels
	private static final float SPEED_START = 50; // Initial speed of the aliens

	private AlienGame alienGame; // Reference to the main game class
	private SpriteBatch batch; // SpriteBatch for rendering graphics
	private AnimatedSprite ship; // The player's ship
	private Texture alienTexture; // Texture for the aliens
	private List<AnimatedSprite> aliens; // List of active aliens
	private boolean gameOver = false; // Flag to track if the game is over
	private float elapsedTime; // Time elapsed since the game started
	private float speed; // Current speed of the aliens

	// Gravity and bounce mechanics
	private final float GRAVITY = -500f; // Gravity force (pulls the ship down)
	private final float BOUNCE_VELOCITY = 300f; // Upward velocity applied when SPACE is pressed
	private boolean isFirstInput = true; // Flag to track the first SPACE press

	/**
	 * Constructs a new `GameScreen` instance.
	 *
	 * @param alienGame The main game class that controls the screens.
	 */
	public GameScreen(AlienGame alienGame) {
		this.alienGame = alienGame;
		this.alienTexture = new Texture("moon.png");
		this.aliens = new ArrayList<>();
		this.batch = new SpriteBatch();
		this.ship = new AnimatedSprite("oldAlien.png", 0, 0, SHIP_WIDTH, SHIP_HEIGHT);
	}

	@Override
	public void hide() {
		Gdx.input.setInputProcessor(null); // Disable input processing when the screen is hidden
	}

	@Override
	public void show() {
		final int width = Gdx.graphics.getWidth();
		final int height = Gdx.graphics.getHeight();

		elapsedTime = 0;
		speed = SPEED_START;
		gameOver = false;

		// Set the ship's bounds and initial position
		ship.setBounds(new Rectangle(0, 0, width / 2f, height));
		ship.setPosition(100, height / 2 - SHIP_HEIGHT / 2);

		// Reset ship's vertical speed and first input flag
		ship.setDeltaY(0);
		isFirstInput = true;

		// Clear existing aliens and spawn new ones
		aliens.clear();
		for (int i = 0; i < 10; ++i) {
			addAlien(width / 2 + i * 80);
			speed += 1.3;
		}

		Gdx.input.setInputProcessor(this); // Enable input processing for this screen
	}

	/**
	 * Adds a new alien at a random y-position.
	 *
	 * @param x The x-coordinate where the alien will be spawned.
	 */
	private void addAlien(int x) {
		int range = Gdx.graphics.getHeight() - SHIP_HEIGHT;
		int y = ThreadLocalRandom.current().nextInt(range);
		addAlien(x, y);
	}

	/**
	 * Adds a new alien at the specified position.
	 *
	 * @param x The x-coordinate of the alien.
	 * @param y The y-coordinate of the alien.
	 */
	private void addAlien(int x, int y) {
		AnimatedSprite alien = new AnimatedSprite(alienTexture, x, y, ALIEN_SIZE, ALIEN_SIZE);
		alien.setDeltaX(-speed); // Set the alien's horizontal speed
		aliens.add(alien);
	}

	@Override
	public void render(float deltaTime) {
		if (gameOver) {
			alienGame.gameOver(); // Switch to the game over screen
			return;
		}

		elapsedTime += deltaTime;

		updateState(deltaTime); // Update game state
		renderScreen(); // Render the game screen
		checkForGameOver(); // Check for game over conditions
	}

	/**
	 * Updates the game state, including the ship's position, aliens' positions, and gravity.
	 *
	 * @param deltaTime The time elapsed since the last frame.
	 */
	private void updateState(float deltaTime) {
		// Apply gravity only after the first SPACE press
		if (!isFirstInput) {
			ship.setDeltaY(ship.getDeltaY() + GRAVITY * deltaTime);
		}

		// Update ship position
		ship.update(deltaTime);

		// Update aliens and remove off-screen aliens
		List<AnimatedSprite> toRemove = new ArrayList<>();
		for (AnimatedSprite alien : aliens) {
			alien.update(deltaTime);
			if (alien.getX() < -ALIEN_SIZE) {
				toRemove.add(alien);
			}
		}

		aliens.removeAll(toRemove);
		for (AnimatedSprite alien : toRemove) {
			alien.dispose();
			addAlien(Gdx.graphics.getWidth() + ALIEN_SIZE); // Spawn a new alien
		}

		alienGame.addPoints((int) (toRemove.size() * speed)); // Add points for each alien passed
	}

	/**
	 * Renders the game screen, including the ship and aliens.
	 */
	private void renderScreen() {
		ScreenUtils.clear(0.98f, 0.98f, 0.98f, 1); // Clear the screen with a light gray color

		batch.begin();
		ship.draw(batch, elapsedTime); // Draw the ship
		for (AnimatedSprite alien : aliens) {
			alien.draw(batch, elapsedTime); // Draw each alien
		}
		batch.end();
	}

	/**
	 * Checks for game over conditions, such as collisions with aliens or the ground.
	 */
	private void checkForGameOver() {
		// Check for collision with aliens
		for (AnimatedSprite alien : aliens) {
			if (alien.overlaps(ship)) {
				gameOver = true;
				return;
			}
		}

		// Check if the ship has fallen to the ground
		if (ship.getY() <= 0) {
			gameOver = true;
		}
	}

	@Override
	public void dispose() {
		// Dispose of resources to prevent memory leaks
		batch.dispose();
		ship.dispose();
		alienTexture.dispose();
		for (AnimatedSprite alien : aliens) {
			alien.dispose();
		}
	}

	@Override
	public boolean keyDown(int keycode) {
		// Use SPACE to make the ship bounce upward
		if (keycode == Keys.SPACE) {
			if (isFirstInput) {
				isFirstInput = false; // Disable first input flag
			}
			ship.setDeltaY(BOUNCE_VELOCITY); // Apply upward velocity
		}
		return true;
	}

	@Override
	public boolean keyUp(int keycode) {
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		return false;
	}

	@Override
	public boolean scrolled(float amountX, float amountY) {
		return false;
	}

	@Override
	public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
		return false;
	}
}