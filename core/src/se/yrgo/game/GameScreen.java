package se.yrgo.game;

import java.util.*;
import java.util.concurrent.*;

import com.badlogic.gdx.*;
import com.badlogic.gdx.Input.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.ScreenUtils;

/**
 * The GameScreen class represents the main gameplay screen where the player controls a ship,
 * avoids collisions with aliens, and interacts with the game environment.
 * <p>
 * Implements {@link InputProcessor} to handle user input.
 */
public class GameScreen extends ScreenAdapter implements InputProcessor {
    private static final int ALIEN_WIDTH = 106;
    private static final int ALIEN_HEIGHT = 80;
    private static final float SPEED_START = 50;

    private AlienGame alienGame;
    private SpriteBatch batch;
    private AnimatedSprite alien;
    private List<AnimatedSprite> planets;
    private boolean gameOver = false;
    private float elapsedTime;
    private float speed;

    private String[] planetsArr = { "bloodMoon.png", "earth.png", "jupiter.png", "mars.png", "moon.png", "venus.png" };

    // Gravity and bounce mechanics
    private static final float GRAVITY = -600f; // Gravity affecting the ship
    private static final float BOUNCE_VELOCITY = 400f; // Velocity applied on spacebar press
    private boolean isFirstInput = true; // Prevents gravity before first input

    // Planet spawning control
    private float planetSpawnTimer = 0;
    private static final float PLANET_SPAWN_INTERVAL = 1.5f; // Time between planet spawns (in seconds)
    private static final int MAX_PLANETS_ON_SCREEN = 5; // Maximum number of planets allowed on screen

    /**
     * Constructor for the GameScreen.
     *
     * @param alienGame The main game instance to allow screen transitions.
     */
    public GameScreen(AlienGame alienGame) {
        this.alienGame = alienGame;
        this.batch = new SpriteBatch();
        this.alien = new AnimatedSprite("alien.png", 0, 0, ALIEN_WIDTH, ALIEN_HEIGHT);
        this.planets = new ArrayList<>();
    }

    /**
     * Get random planet image URL, based on planetsArr.
     *
     * @return String image path of a random planet.
     */
    private String randomizePlanet() {
        Random random = new Random();
        int randomPlanetIndex = random.nextInt(planetsArr.length);
        return planetsArr[randomPlanetIndex];
    }

    /**
     * Adds a planet at a random position, ensuring it doesn't overlap with existing planets.
     */
    private void addPlanet() {
        int screenWidth = Gdx.graphics.getWidth();
        int screenHeight = Gdx.graphics.getHeight();
        int minDistance = 100; // Minimum distance between planets (adjust as needed)
        int maxAttempts = 10; // Maximum attempts to find a valid position
        int attempts = 0;

        int x, y;
        boolean positionValid;

        do {
            // Randomize X and Y coordinates
            x = ThreadLocalRandom.current().nextInt(screenWidth / 2, screenWidth); // Spawn planets on the right side
            y = ThreadLocalRandom.current().nextInt(0, screenHeight - ALIEN_HEIGHT);

            positionValid = true;

            // Check if the new planet overlaps with any existing planet
            for (AnimatedSprite planet : planets) {
                float distanceX = Math.abs(planet.getX() - x);
                float distanceY = Math.abs(planet.getY() - y);

                // Check if the distance is too small in either X or Y direction
                if (distanceX < minDistance && distanceY < minDistance) {
                    positionValid = false;
                    break;
                }
            }

            attempts++;
        } while (!positionValid && attempts < maxAttempts);

        if (positionValid) {
            addPlanet(x, y);
        } else {
            // If no valid position is found after maxAttempts, skip adding this planet
            return;
        }
    }

    /**
     * Creates a planet sprite at the specified coordinates and adds it to the list.
     *
     * @param x The X-coordinate of the planet.
     * @param y The Y-coordinate of the planet.
     */
    private void addPlanet(int x, int y) {
        String planetTexturePath = randomizePlanet();
        Texture planetTexture = new Texture(planetTexturePath);

        AnimatedSprite planet = new AnimatedSprite(planetTexture, x, y, planetTexture.getWidth(), planetTexture.getHeight());
        planet.setDeltaX(-speed); // Move the planet to the left
        planets.add(planet);
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

        alien.setBounds(new Rectangle(0, 0, width / 2f, height));
        alien.setPosition(100, height / 2 - ALIEN_HEIGHT / 2);

        // Reset vertical movement and first input flag
        alien.setDeltaY(0);
        isFirstInput = true;

        planets.clear();
        planetSpawnTimer = 0; // Reset the planet spawn timer

        Gdx.input.setInputProcessor(this);
    }

    /**
     * Renders the game screen.
     *
     * @param deltaTime The time elapsed since the last frame.
     */
    @Override
    public void render(float deltaTime) {
        if (gameOver) {
            return;
        }

        elapsedTime += deltaTime;
        planetSpawnTimer += deltaTime;

        // Spawn a new planet if enough time has passed and there are not too many planets on screen
        if (planetSpawnTimer >= PLANET_SPAWN_INTERVAL && planets.size() < MAX_PLANETS_ON_SCREEN) {
            addPlanet();
            planetSpawnTimer = 0; // Reset the timer
        }

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
            alien.setDeltaY(alien.getDeltaY() + GRAVITY * deltaTime);
        }

        speed += 1.5 * deltaTime;

        // Update ship position
        alien.update(deltaTime);

        // Update aliens and remove any that go off-screen
        List<AnimatedSprite> toRemove = new ArrayList<>();
        for (AnimatedSprite planet : planets) {
            planet.update(deltaTime);
            if (planet.getX() < -planet.getWidth()) {
                toRemove.add(planet);
            }
        }

        planets.removeAll(toRemove);
        for (AnimatedSprite planet : toRemove) {
            planet.dispose();
        }

        alienGame.addPoints((int) (toRemove.size() * speed));
    }

    /** Clears the screen and renders all sprites. */
    private void renderScreen() {
        // Set blue background color
        Gdx.gl.glClearColor(0.043f, 0.078f, 0.22f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        batch.begin();
        alien.draw(batch, elapsedTime);

        for (AnimatedSprite planet : planets) {
            planet.draw(batch, elapsedTime);
        }

        batch.end();
    }

    /** Checks for collisions between the ship and aliens or the ground. */
    private void checkForGameOver() {
        for (AnimatedSprite planet : planets) {
            if (planet.overlaps(alien)) {
                gameOver = true;
            }
        }

        if (alien.getY() <= 0) {
            gameOver = true;
        }

        if (alien.getY() + alien.getHeight() >= Gdx.graphics.getHeight()) {
            gameOver = true;
        }
    }

    /** Disposes of all allocated resources when the screen is no longer needed. */
    @Override
    public void dispose() {
        batch.dispose();
        alien.dispose();
        for (AnimatedSprite planet : planets) {
            planet.dispose();
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
            alien.setDeltaY(BOUNCE_VELOCITY);
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
    public boolean scrolled(float amountX, float amountY) {
        return false;
    }

    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }
}