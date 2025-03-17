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
	private static final int ALIEN_WIDTH = 46;
	private static final int ALIEN_HEIGHT = 20;
	private static final float SPEED_START = 50;

    private AlienGame alienGame;
    private SpriteBatch batch;
    private AnimatedSprite alien;
    private Texture planetTexture;
    private List<AnimatedSprite> planets;
    private boolean gameOver = false;
    private float elapsedTime;
    private float speed;

    private String[] planetsArr = { "bloodMoon.png", "earth.png", "jupiter.png", "mars.png", "moon.png", "venus.png" };


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
        this.batch = new SpriteBatch();
        this.alien = new AnimatedSprite("alien.png", 0, 0, ALIEN_WIDTH, ALIEN_HEIGHT);
        this.planets = new ArrayList<>();
    }

    	/**
	 * get random planet image url, based on planetsArr
	 * 
	 * @return String image path of a random planet
	 */
	private String randomizePlanet() {
		Random random = new Random();
		int randomPlanetIndex = random.nextInt(planetsArr.length);
		String randomPlanet = planetsArr[randomPlanetIndex];

		return randomPlanet;
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
		for (int i = 0; i < 10; ++i) {
			addPlanet(width / 2 + i * 80);
			speed += 1.3;
		}

        Gdx.input.setInputProcessor(this);
    }


    /**
     * Adds an planet at a random Y-coordinate.
     *
     * @param x The X-coordinate where the planet should be placed.
     */
	private void addPlanet(int x) {
		int range = Gdx.graphics.getHeight() - ALIEN_HEIGHT;
		int y = ThreadLocalRandom.current().nextInt(range);
		addPlanet(x, y);
	}

    
    /**
     * Creates an planet sprite at the specified coordinates and adds it to the list.
     *
     * @param x The X-coordinate of the alien.
     * @param y The Y-coordinate of the alien.
     */
	private void addPlanet(int x, int y) {
		String planettexturePath = randomizePlanet();
		this.planetTexture = new Texture(planettexturePath);

		AnimatedSprite planet = new AnimatedSprite(planetTexture, x, y, planetTexture.getWidth(),
				planetTexture.getHeight());
		planet.setDeltaX(-speed);
		planets.add(planet);
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
			if (planet.getX() < -planetTexture.getWidth()) {
				toRemove.add(planet);
			}
		}

		planets.removeAll(toRemove);
		for (AnimatedSprite planet : toRemove) {
			planet.dispose();
			addPlanet(Gdx.graphics.getWidth() + planetTexture.getWidth());
		}

		alienGame.addPoints((int) (toRemove.size() * speed));
	}

      /** Clears the screen and renders all sprites. */
	private void renderScreen() {
		// set blue background color
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

    }

    /** Disposes of all allocated resources when the screen is no longer needed. */
    @Override
	public void dispose() {
		batch.dispose();
		alien.dispose();
		planetTexture.dispose();
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
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'touchCancelled'");
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'mouseMoved'");
    }
}
