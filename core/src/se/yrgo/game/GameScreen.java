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
    private static final int ALIEN_WIDTH = 130;
    private static final int ALIEN_HEIGHT = 100;
    private static final float SPEED_START = 150;

    private AlienGame alienGame;
    private SpriteBatch batch;
    private AnimatedSprite alien;
    private List<AnimatedSprite> planets;
    private boolean gameOver = false;
    private float elapsedTime;
    private float speed;
    private BitmapFont font;
    private GlyphLayout glyphLayout;

    private String[] planetsArr = { "bloodMoon.png", "earth.png", "jupiter.png", "mars.png", "moon.png", "venus.png" };

    private static final float GRAVITY = -2700f;
    private static final float BOUNCE_VELOCITY = 650f;
    private boolean isFirstInput = true;

    private float planetSpawnTimer = 0;
    private static final float PLANET_SPAWN_INTERVAL = 1.5f;
    private static final int MAX_PLANETS_ON_SCREEN = 5;

    public GameScreen(AlienGame alienGame) {
        this.alienGame = alienGame;
        this.batch = new SpriteBatch();
        this.alien = new AnimatedSprite("alien.png", 0, 0, ALIEN_WIDTH, ALIEN_HEIGHT);
        this.planets = new ArrayList<>();
        this.font = new BitmapFont();
        this.glyphLayout = new GlyphLayout();
        font.getData().setScale(2);
        font.setColor(Color.WHITE);
    }

    private String randomizePlanet() {
        Random random = new Random();
        int randomPlanetIndex = random.nextInt(planetsArr.length);
        return planetsArr[randomPlanetIndex];
    }

    private void addPlanet() {
        int screenWidth = Gdx.graphics.getWidth();
        int screenHeight = Gdx.graphics.getHeight();

        int minDistance = 50; // originally 100
        int maxAttempts = 10;

        int attempts = 0;

        int x, y;
        boolean positionValid;

        do {
            x = ThreadLocalRandom.current().nextInt(screenWidth - 10, screenWidth);
            y = ThreadLocalRandom.current().nextInt(0, screenHeight - ALIEN_HEIGHT);

            positionValid = true;

            for (AnimatedSprite planet : planets) {
                float distanceX = Math.abs(planet.getX() - x);
                float distanceY = Math.abs(planet.getY() - y);

                if (distanceX < minDistance && distanceY < minDistance) {
                    positionValid = false;
                    break;
                }
            }

            attempts++;
        } while (!positionValid && attempts < maxAttempts);

        if (positionValid) {
            addPlanet(x, y);
        }
    }

    private void addPlanet(int x, int y) {
        String planetTexturePath = randomizePlanet();
        Texture planetTexture = new Texture(planetTexturePath);

        AnimatedSprite planet = new AnimatedSprite(planetTexture, x, y, planetTexture.getWidth(), planetTexture.getHeight());
        planet.setDeltaX(-speed);
        planets.add(planet);
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

        alien.setBounds(new Rectangle(0, 0, width / 2f, height));
        alien.setPosition(100, height / 2 - ALIEN_HEIGHT / 2);

        alien.setDeltaY(0);
        isFirstInput = true;

        planets.clear();
        planetSpawnTimer = 0;

        Gdx.input.setInputProcessor(this);
    }

    @Override
    public void render(float deltaTime) {
        if (gameOver) {
            return;
        }

        elapsedTime += deltaTime;
        planetSpawnTimer += deltaTime;

        if (planetSpawnTimer >= PLANET_SPAWN_INTERVAL && planets.size() < MAX_PLANETS_ON_SCREEN) {
            addPlanet();
            planetSpawnTimer = 0;
        }

        updateState(deltaTime);
        renderScreen();
        checkForGameOver();
    }

    private void updateState(float deltaTime) {
        if (!isFirstInput) {
            alien.setDeltaY(alien.getDeltaY() + GRAVITY * deltaTime);
        }

        speed += 50 * deltaTime; // 1.7 originally

        alien.update(deltaTime);

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

        // counts passed planets
        alienGame.addPoints(toRemove.size());
    }

    private void renderScreen() {
        Gdx.gl.glClearColor(0.043f, 0.078f, 0.22f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        batch.begin();
        alien.draw(batch, elapsedTime);

        for (AnimatedSprite planet : planets) {
            planet.draw(batch, elapsedTime);
        }

        // added
        font.draw(batch, "Score: " + alienGame.getPoints(),
                20, Gdx.graphics.getHeight() - 20);
        font.draw(batch, "High Score: " + alienGame.getHighScore(),
                20, Gdx.graphics.getHeight() - 50);
        batch.end();
    }

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

        // added/modified
        if (gameOver) {
            alienGame.gameOver();
        }
    }

    @Override
    public void dispose() {
        batch.dispose();
        alien.dispose();
        font.dispose();
        for (AnimatedSprite planet : planets) {
            planet.dispose();
        }
    }

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