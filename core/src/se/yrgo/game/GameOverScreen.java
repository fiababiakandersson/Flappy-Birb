package se.yrgo.game;

import java.util.*;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.Texture.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.*;

public class GameOverScreen extends ScreenAdapter implements InputProcessor {
    private AlienGame alienGame;
    private SpriteBatch batch;
    private AnimatedSprite alienHead;
    private BitmapFont bigFont;
    private BitmapFont smallFont;
    private float elapsedTime = 0;
    private Rectangle easyBounds;
    private Rectangle mediumBounds;
    private Rectangle hardBounds;

    // New fields for star decoration
    private Texture starTexture;
    private List<AnimatedSprite> edgeStars;
    private static final int STAR_COUNT = 40;
    private static final int STAR_WIDTH = 21;
    private static final int STAR_HEIGHT = 32;

    public GameOverScreen(AlienGame alienGame) {
        int width = Gdx.graphics.getWidth();
        this.alienGame = alienGame;
        this.batch = new SpriteBatch();

        // Initialize fonts and set them to white
        this.bigFont = new BitmapFont();
        this.smallFont = new BitmapFont();
        this.bigFont.setColor(Color.WHITE);
        this.smallFont.setColor(Color.WHITE);
        this.bigFont.getRegion().getTexture().setFilter(TextureFilter.Linear, TextureFilter.Linear);
        this.bigFont.getData().setScale(2.5f);
        this.smallFont.getRegion().getTexture().setFilter(TextureFilter.Linear, TextureFilter.Linear);
        this.smallFont.getData().setScale(1.5f);

        // Center the alien head image near the bottom
        this.alienHead = new AnimatedSprite("alien.png", (width / 2) - (106 / 2), 250, 106, 80);

        // Load the star texture and initialize the edge stars
        this.starTexture = new Texture(Gdx.files.internal("extrasmallstars.png"));
        edgeStars = new ArrayList<>();
        initializeEdgeStars();
    }

    /**
     * Create stars at random positions along the screen edges.
     * For each star, we randomly choose one edge (top, bottom, left, or right)
     * and then pick a coordinate along that edge.
     */
    private void initializeEdgeStars() {
        Random random = new Random();
        int screenWidth = Gdx.graphics.getWidth();
        int screenHeight = Gdx.graphics.getHeight();

        for (int i = 0; i < STAR_COUNT; i++) {
            int x = 0, y = 0;
            // Randomly choose an edge: 0 = top, 1 = bottom, 2 = left, 3 = right.
            int edge = random.nextInt(4);
            switch (edge) {
                case 0: // top edge
                    x = random.nextInt(screenWidth);
                    y = screenHeight - random.nextInt(400);
                    break;
                case 1: // bottom edge
                    x = random.nextInt(screenWidth);
                    y = random.nextInt(40);
                    break;
                case 2: // left edge
                    x = random.nextInt(60);
                    y = random.nextInt(screenHeight);
                    break;
                case 3: // right edge
                    x = screenWidth - random.nextInt(60);
                    y = random.nextInt(screenHeight);
                    break;
            }
            AnimatedSprite star = new AnimatedSprite(starTexture, x, y, STAR_WIDTH, STAR_HEIGHT);
            edgeStars.add(star);
        }
    }

    @Override
    public void dispose() {
        bigFont.dispose();
        smallFont.dispose();
        alienHead.dispose();
        starTexture.dispose();
        batch.dispose();
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void render(float delta) {

        elapsedTime += delta;

        ScreenUtils.clear(0.043f, 0.078f, 0.22f, 1.0f);

        int screenWidth = Gdx.graphics.getWidth();
        int screenHeight = Gdx.graphics.getHeight();

        batch.begin();

        // Render the decorative edge stars
        for (AnimatedSprite star : edgeStars) {
            star.draw(batch, elapsedTime);
        }

        // Draw "Game Over!" text centered in the middle of the screen
        float gameOverY = screenHeight / 2f - 50;
        bigFont.draw(batch, "Game Over!", 0, gameOverY, screenWidth, Align.center, false);

        // Draw the score below the Game Over text
        String points = String.format("You scored: %d", alienGame.getPoints());
        smallFont.draw(batch, points, 0, gameOverY - 50, screenWidth, Align.center, false);

        // Draw difficulty prompt further above the Game Over text
        float difficultyY = gameOverY + 160;
        smallFont.draw(batch, "Change difficulty?", 0, difficultyY, screenWidth, Align.center, false);

        // Draw difficulty option labels centered below the prompt
        float optionY = difficultyY - 40;
        smallFont.draw(batch, "Easy", screenWidth / 2f - 200, optionY, 100, Align.center, false);
        smallFont.draw(batch, "Medium", screenWidth / 2f - 50, optionY, 100, Align.center, false);
        smallFont.draw(batch, "Hard", screenWidth / 2f + 100, optionY, 100, Align.center, false);

        // Draw the alien (your animated sprite)
        alienHead.draw(batch, elapsedTime);

        batch.end();

        // Define clickable areas for difficulty options
        this.easyBounds = new Rectangle(screenWidth / 2f - 200, optionY - 20, 100, 40);
        this.mediumBounds = new Rectangle(screenWidth / 2f - 50, optionY - 20, 100, 40);
        this.hardBounds = new Rectangle(screenWidth / 2f + 100, optionY - 20, 100, 40);

        if (Gdx.input.justTouched()) {
            int x = Gdx.input.getX();
            int y = Gdx.graphics.getHeight() - Gdx.input.getY(); // Convert to game coordinates

            if (easyBounds.contains(x, y)) {
                alienGame.setDifficulty(Difficulty.EASY);
                alienGame.newGame();
            } else if (mediumBounds.contains(x, y)) {
                alienGame.setDifficulty(Difficulty.MEDIUM);
                alienGame.newGame();
            } else if (hardBounds.contains(x, y)) {
                alienGame.setDifficulty(Difficulty.HARD);
                alienGame.newGame();
            }
        }

    }

    @Override
    public void show() {
        elapsedTime = 0;
        Gdx.input.setInputProcessor(this);
    }

    @Override
    public boolean keyTyped(char character) {
        if (elapsedTime > 1) {
            alienGame.newGame();
        }
        return true;
    }

    // The rest of the InputProcessor methods (keyDown, keyUp, touchDown, etc.)
    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
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
