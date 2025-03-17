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
 * The GameScreen class represents the main gameplay screen where the player controls a ship,
 * avoids collisions with aliens, and interacts with the game environment.
 * <p>
 * Implements {@link InputProcessor} to handle user input.
 */
public class GameScreen extends ScreenAdapter implements InputProcessor {
    private static final int ALIEN_SIZE = 20;
    private static final int SHIP_WIDTH = 46;
    private static final int SHIP_HEIGHT = 20;
    private static final float SPEED_START = 50;

    private AlienGame alienGame;
    private SpriteBatch batch;
    private AnimatedSprite ship;
    private Texture alienTexture;
    private List<AnimatedSprite> aliens;
    private boolean gameOver = false;
    private float elapsedTime;
    private float speed;

    // Gravity and bounce mechanics
    private static final float GRAVITY = -500f; // Gravity affecting the ship
    private static final float BOUNCE_VELOCITY = 250f; // Velocity applied on spacebar press
    private boolean isFirstInput = true; // Prevents gravity before first input

    /**
     * Constructor for the GameScreen.
     *
     * @param alienGame The main game instance to allow screen transitions.
     */
    public GameScreen(AlienGame alienGame) {
        this.alienGame = alienGame;
        this.alienTexture = new Texture("moon.png");
        this.aliens = new ArrayList<>();
        this.batch = new SpriteBatch();
        this.ship = new AnimatedSprite("oldAlien.png", 0, 0, SHIP_WIDTH, SHIP_HEIGHT);
    }

    /** Called when this screen is hidden. Removes the input processor. */
    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    /**
     * Initializes the game screen when it is displayed.
     * Resets game variables and positions the ship and aliens.
     */
    @Override
    public void show() {
        final int width = Gdx.graphics.getWidth();
        final int height = Gdx.graphics.getHeight();

        elapsedTime = 0;
        speed = SPEED_START;
        gameOver = false;

        ship.setBounds(new Rectangle(0, 0, width / 2f, height));
        ship.setPosition(100, height / 2 - SHIP_HEIGHT / 2);

        // Reset vertical movement and first input flag
        ship.setDeltaY(0);
        isFirstInput = true;

        aliens.clear();
        for (int i = 0; i < 10; ++i) {
            addAlien(width / 2 + i * 80);
            speed += 1.3;
        }

        Gdx.input.setInputProcessor(this);
    }

    /**
     * Adds an alien at a random Y-coordinate.
     *
     * @param x The X-coordinate where the alien should be placed.
     */
    private void addAlien(int x) {
        int range = Gdx.graphics.getHeight() - SHIP_HEIGHT;
        int y = ThreadLocalRandom.current().nextInt(range);
        addAlien(x, y);
    }

    /**
     * Creates an alien sprite at the specified coordinates and adds it to the list.
     *
     * @param x The X-coordinate of the alien.
     * @param y The Y-coordinate of the alien.
     */
    private void addAlien(int x, int y) {
        AnimatedSprite alien = new AnimatedSprite(alienTexture, x, y, ALIEN_SIZE, ALIEN_SIZE);
        alien.setDeltaX(-speed);
        aliens.add(alien);
    }

    /**
     * Renders the game screen.
     *
     * @param deltaTime The time elapsed since the last frame.
     */
    @Override
    public void render(float deltaTime) {
        if (gameOver) {
            alienGame.gameOver();
            return;
        }

        elapsedTime += deltaTime;

        updateState(deltaTime);
        renderScreen();
        checkForGameOver();
    }

    /**
     * Updates the game state, including ship movement and alien behavior.
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

        // Update aliens and remove any that go off-screen
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
            addAlien(Gdx.graphics.getWidth() + ALIEN_SIZE);
        }

        alienGame.addPoints((int) (toRemove.size() * speed));
    }

    /** Clears the screen and renders all sprites. */
    private void renderScreen() {
        ScreenUtils.clear(0.98f, 0.98f, 0.98f, 1);

        batch.begin();
        ship.draw(batch, elapsedTime);
        for (AnimatedSprite alien : aliens) {
            alien.draw(batch, elapsedTime);
        }
        batch.end();
    }

    /** Checks for collisions between the ship and aliens or the ground. */
    private void checkForGameOver() {
        for (AnimatedSprite alien : aliens) {
            if (alien.overlaps(ship)) {
                gameOver = true;
                return;
            }
        }

        if (ship.getY() <= 0) {
            gameOver = true;
        }

    }

    /** Disposes of all allocated resources when the screen is no longer needed. */
    @Override
    public void dispose() {
        batch.dispose();
        ship.dispose();
        alienTexture.dispose();
        for (AnimatedSprite alien : aliens) {
            alien.dispose();
        }
    }

    /**
     * Handles key press events. The SPACE key makes the ship bounce upwards.
     *
     * @param keycode The key that was pressed.
     * @return Always returns true.
     */
    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Keys.SPACE) {
            if (isFirstInput) {
                isFirstInput = false;
            }
            ship.setDeltaY(BOUNCE_VELOCITY);
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
