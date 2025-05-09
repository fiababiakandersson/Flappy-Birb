package se.yrgo.game;

import java.util.*;
import java.util.concurrent.*;

import com.badlogic.gdx.*;
import com.badlogic.gdx.Input.*;
import com.badlogic.gdx.audio.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.*;

public class GameScreen extends ScreenAdapter implements InputProcessor {
    private static final int screenWidth = Gdx.graphics.getWidth();
    private static final int screenHeight = Gdx.graphics.getHeight();
    private static final int ALIEN_WIDTH = 130;
    private static final int ALIEN_HEIGHT = 100;

    // Same speed for all difficulties
    private static float PLANET_SPEED = 130f;
    private static final float EASY_PLANET_SPAWN_INTERVAL = 3.0f;
    private static final float MEDIUM_PLANET_SPAWN_INTERVAL = 2.0f;
    private static final float HARD_PLANET_SPAWN_INTERVAL = 1.2f;

    // Max planets allowed on screen per difficulty
    private static final int EASY_MAX_PLANETS = 3;
    private static final int MEDIUM_MAX_PLANETS = 5;
    private static final int HARD_MAX_PLANETS = 6;

    private AlienGame alienGame;
    private SpriteBatch batch;
    private AnimatedSprite alien;
    private List<AnimatedSprite> planets;
    private List<AnimatedSprite> backgroundStars; // For background stars
    private BitmapFont font;
    private String[] planetsArr = { "bloodMoon.png", "earth.png", "jupiter.png", "mars.png", "moon.png", "venus.png" };

    private static final float GRAVITY = -1800f;
    private static final float BOUNCE_VELOCITY = 680f;
    private static final int STAR_COUNT = 50; // Number of background stars

    private boolean gameOver = false;
    private float elapsedTime;
    private boolean isFirstInput = true;
    private float planetSpawnTimer = 0;
    private static float PLANET_SPAWN_INTERVAL;
    private static int MAX_PLANETS_ON_SCREEN;
    private Music gamePlayMusic = Gdx.audio.newMusic(Gdx.files.internal("music/1.MainTheme-320bit(chosic.com).mp3"));
    private Sound jumpingMusic = Gdx.audio.newSound(Gdx.files.internal("music/retro-jump.mp3"));
    private Texture stars = new Texture(Gdx.files.internal("extrasmallstars.png"));

    // New textures for normal and jump state
    private Texture alienFallingTexture;
    private Texture alienJumpTexture;
    private Texture alienNeutralTexture;

    public GameScreen(AlienGame alienGame) {
        this.alienGame = alienGame;
        this.batch = new SpriteBatch();
        // Load both textures
        alienFallingTexture = new Texture("alienFalling.png");
        alienJumpTexture = new Texture("alienJumping.png");
        alienNeutralTexture = new Texture("alienNeutral.png");

        // Initialize alien with the normal texture
        this.alien = new AnimatedSprite(alienFallingTexture, 0, 0, ALIEN_WIDTH, ALIEN_HEIGHT);
        this.planets = new ArrayList<>();
        this.backgroundStars = new ArrayList<>();
        this.font = new BitmapFont();
        font.getData().setScale(2);
        font.setColor(Color.WHITE);

        initializeBackgroundStars();
    }

    private void initializeBackgroundStars() {
        Random random = new Random();

        for (int i = 0; i < STAR_COUNT; i++) {
            int x = random.nextInt(screenWidth);
            int y = random.nextInt(screenHeight);

            // Create star (using stars.png texture)
            AnimatedSprite star = new AnimatedSprite(stars, x, y, 21, 32); // stars.png 171, 256 // smallstars.png 42,
                                                                           // 64

            // make planets move faster depending on difficulty
            float minSpeedFactor = 0.4f; // 40% of PLANET_SPEED
            float maxSpeedFactor = 0.8f; // 70% of PLANET_SPEED

            float speedFactor = minSpeedFactor + random.nextFloat() * (maxSpeedFactor - minSpeedFactor);
            float starSpeed = -PLANET_SPEED * speedFactor;

            star.setDeltaX(starSpeed);

            backgroundStars.add(star);
        }
    }

    private String randomizePlanet() {
        Random random = new Random();
        int randomPlanetIndex = random.nextInt(planetsArr.length);
        return planetsArr[randomPlanetIndex];
    }

    private void addPlanet() {
        int screenWidth = Gdx.graphics.getWidth();
        int screenHeight = Gdx.graphics.getHeight();

        int minDistance = 50;
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

        // System.out.println("attempts: " + attempts + " x: " + x + " y: " + y + "
        // positionValid: " + positionValid);
        if (positionValid) {
            addPlanet(x, y);
        }
    }

    private void addPlanet(int x, int y) {
        String planetTexturePath = randomizePlanet();
        Texture planetTexture = new Texture(planetTexturePath);

        // If cheating in the start, or touching floor/ruff
        if (alien.getDeltaY() == 0f) {
            y = (int) alien.getY();
        } else if (alien.getY() == 0) {
            y = 0;
        } else if (alien.getY() >= (screenHeight - (int) alien.getHeight())) {
            y = (screenHeight - (int) alien.getHeight());
        }

        System.out.println(alienGame.getPoints());

        // Save the game from crashing
        if (alienGame.getPoints() == 2147483640) { // Max: 2147483647
            for (int wall = 0; wall <= screenHeight; wall += 90) {
                AnimatedSprite planet = new AnimatedSprite(planetTexture, x, wall, planetTexture.getWidth(),
                        planetTexture.getWidth());
                planet.setDeltaX(-PLANET_SPEED);
                planets.add(planet);
            }
        } else {
            AnimatedSprite planet = new AnimatedSprite(planetTexture, x, y, planetTexture.getWidth(),
                    planetTexture.getHeight());
            planet.setDeltaX(-PLANET_SPEED);
            planets.add(planet);
        }
        // planet.setDeltaX(-PLANET_SPEED);
        // planets.add(planet);
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
        gameOver = false;

        // Set difficulty-based parameters
        switch (alienGame.getDifficulty()) {
            case EASY:
                PLANET_SPAWN_INTERVAL = EASY_PLANET_SPAWN_INTERVAL;
                MAX_PLANETS_ON_SCREEN = EASY_MAX_PLANETS;
                break;
            case MEDIUM:
                PLANET_SPEED = 180f;
                PLANET_SPAWN_INTERVAL = MEDIUM_PLANET_SPAWN_INTERVAL;
                MAX_PLANETS_ON_SCREEN = MEDIUM_MAX_PLANETS;
                break;
            case HARD:
                PLANET_SPEED = 200f;
                PLANET_SPAWN_INTERVAL = HARD_PLANET_SPAWN_INTERVAL;
                MAX_PLANETS_ON_SCREEN = HARD_MAX_PLANETS;
                break;
        }

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

        if (!gamePlayMusic.isPlaying() && !gameOver) {
            gamePlayMusic.play();
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

        // When falling, revert to the normal texture.
        if (alien.getDeltaY() <= -200) {
            alien.setTexture(alienFallingTexture);
        } else if (alien.getDeltaY() <= 200) {
            alien.setTexture(alienNeutralTexture);
        }

        // System.out.println(alien.getDeltaY());

        alien.update(deltaTime);

        // Update planets
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

        // Update background stars
        for (AnimatedSprite star : backgroundStars) {
            star.update(deltaTime);
            // Wrap stars around when they go off screen
            if (star.getX() < -star.getWidth()) {
                star.setPosition(Gdx.graphics.getWidth(),
                        ThreadLocalRandom.current().nextInt(0, Gdx.graphics.getHeight()));
            }
        }

        alienGame.addPoints(toRemove.size());
    }

    private void renderScreen() {
        Gdx.gl.glClearColor(0.043f, 0.078f, 0.22f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        batch.begin();

        // Draw background stars first
        for (AnimatedSprite star : backgroundStars) {
            star.draw(batch, elapsedTime);
        }

        // Draw game objects
        alien.draw(batch, elapsedTime);
        for (AnimatedSprite planet : planets) {
            planet.draw(batch, elapsedTime);
        }

        // Draw UI
        font.draw(batch, "Score: " + alienGame.getPoints(), 20, Gdx.graphics.getHeight() - 20);
        font.draw(batch, "High Score: " + alienGame.getHighScore(), 20, Gdx.graphics.getHeight() - 50);
        batch.end();
    }

    private void checkForGameOver() {
        for (AnimatedSprite planet : planets) {
            if (planet.overlaps(alien)) {
                gameOver = true;
            }
        }

        if (gameOver) {
            gamePlayMusic.stop();
            alienGame.gameOver();
        }
    }

    @Override
    public void dispose() {
        batch.dispose();
        alien.dispose();
        alienFallingTexture.dispose();
        alienJumpTexture.dispose();
        alienNeutralTexture.dispose();
        font.dispose();
        for (AnimatedSprite planet : planets) {
            planet.dispose();
        }
        for (AnimatedSprite star : backgroundStars) {
            star.dispose();
        }
    }

    // Input handling methods
    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Keys.SPACE) {
            if (isFirstInput) {
                isFirstInput = false;
            }
            long id = jumpingMusic.play(0.5f);
            jumpingMusic.setPitch(id, 0.5f);
            // jumpingMusic.play();

            alien.setDeltaY(BOUNCE_VELOCITY);
            // Switch to the jump texture when the alien jumps
            alien.setTexture(alienJumpTexture);
        }
        return true;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (isFirstInput) {
            isFirstInput = false;
        }
        long id = jumpingMusic.play(0.5f);
        jumpingMusic.setPitch(id, 0.5f);
        // jumpingMusic.play();

        alien.setDeltaY(BOUNCE_VELOCITY);
        // Switch to the jump texture when the alien jumps
        alien.setTexture(alienJumpTexture);
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
