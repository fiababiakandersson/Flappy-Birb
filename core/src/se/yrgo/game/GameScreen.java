package se.yrgo.game;

import java.util.*;
import java.util.concurrent.*;

import com.badlogic.gdx.*;
import com.badlogic.gdx.Input.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.*;

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

	private String[] planets = { "bloodMoon.png", "earth.png", "jupiter.png", "mars.png", "moon.png", "venus.png" };

	public GameScreen(AlienGame alienGame) {
		this.alienGame = alienGame;
		this.alienTexture = new Texture("earth.png");
		this.aliens = new ArrayList<>();
		this.batch = new SpriteBatch();
		this.ship = new AnimatedSprite("alien.png", 0, 0, SHIP_WIDTH, SHIP_HEIGHT);
	}

	@Override
	public void hide() {
		Gdx.input.setInputProcessor(null);
	}

	@Override
	public void show() {
		final int width = Gdx.graphics.getWidth();
		final int height = Gdx.graphics.getHeight();

		elapsedTime = 0;
		speed = SPEED_START;
		gameOver = false;

		ship.setBounds(new Rectangle(0, 0, width / 2f, height));
		ship.setPosition(100, height / 2 - SHIP_HEIGHT / 2);

		aliens.clear();
		for (int i = 0; i < 10; ++i) {
			addAlien(width / 2 + i * 80);
			speed += 1.3;
		}

		Gdx.input.setInputProcessor(this);
	}

	private String randomizePlanet() {
		Random random = new Random();
		int randomPlanetIndex = random.nextInt(planets.length);
		String randomPlanet = planets[randomPlanetIndex];

		return randomPlanet;
	}

	private void addAlien(int x) {
		int range = Gdx.graphics.getHeight() - SHIP_HEIGHT;
		int y = ThreadLocalRandom.current().nextInt(range);
		addAlien(x, y);
	}

	private void addAlien(int x, int y) {
		AnimatedSprite alien = new AnimatedSprite(alienTexture, x, y, ALIEN_SIZE, ALIEN_SIZE);
		alien.setDeltaX(-speed);
		aliens.add(alien);
	}

	@Override
	public void render(float deltaTime) {
		// set background color
		// Gdx.gl.glClearColor(0.043f, 0.078f, 0.22f, 1.0f);
		// Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT); //
		// ensure full clearing

		if (gameOver) {
			return;
		}

		elapsedTime += deltaTime;

		updateState(deltaTime);
		renderScreen();
		checkForGameOver();
	}

	private void updateState(float deltaTime) {
		speed += 1.5 * deltaTime;

		ship.update(deltaTime);

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

	private void renderScreen() {
		ScreenUtils.clear(0.98f, 0.98f, 0.98f, 1);

		batch.begin();
		ship.draw(batch, elapsedTime);
		for (AnimatedSprite alien : aliens) {
			alien.draw(batch, elapsedTime);
		}
		batch.end();
	}

	private void checkForGameOver() {
		for (AnimatedSprite alien : aliens) {
			if (alien.overlaps(ship)) {
				gameOver = true;
			}
		}
	}

	@Override
	public void dispose() {
		batch.dispose();
		ship.dispose();
		alienTexture.dispose();
		for (AnimatedSprite alien : aliens) {
			alien.dispose();
		}
	}

	@Override
	public boolean keyDown(int keycode) {
		final float SPEED_CHANGE = 30;
		if (keycode == Keys.UP) {
			ship.setDeltaY(ship.getDeltaY() + SPEED_CHANGE);
		} else if (keycode == Keys.DOWN) {
			ship.setDeltaY(ship.getDeltaY() - SPEED_CHANGE);
		} else if (keycode == Keys.LEFT) {
			ship.setDeltaX(ship.getDeltaX() - SPEED_CHANGE);
		} else if (keycode == Keys.RIGHT) {
			ship.setDeltaX(ship.getDeltaX() + SPEED_CHANGE);
		} else if (keycode == Keys.ESCAPE) {
			gameOver = true;
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
	public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
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
}
