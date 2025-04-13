package se.yrgo.game;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.Texture.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.*;

/**
 * Screen for game over. Shows the points for the last game
 * and will start a new game for any keypress.
 * 
 */
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
    private Difficulty difficulty;

    public GameOverScreen(AlienGame alienGame) {
        int width = Gdx.graphics.getWidth();

        this.alienGame = alienGame;
        this.batch = new SpriteBatch();
        this.bigFont = new BitmapFont();

        this.alienHead = new AnimatedSprite("alien.png", (width / 2) - (106 / 2), 130, 106, 80);
        // should maybe use Label instead of drawing with fonts directly
        // also, scaling bitmap fonts are really ugly

        final Color fontColor = Color.FIREBRICK;

        this.bigFont.setColor(fontColor);
        this.bigFont.getRegion().getTexture().setFilter(TextureFilter.Linear, TextureFilter.Linear);
        this.bigFont.getData().setScale(2.5f);

        this.smallFont = new BitmapFont();
        this.smallFont.setColor(fontColor);
        this.smallFont.getRegion().getTexture().setFilter(TextureFilter.Linear, TextureFilter.Linear);
        this.smallFont.getData().setScale(1.5f);
    }

    @Override
    public void dispose() {
        bigFont.dispose();
        smallFont.dispose();
        alienHead.dispose();
        batch.dispose();
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void render(float delta) {
        elapsedTime += delta;

        ScreenUtils.clear(0.75f, 0.75f, 0.75f, 1);

        int screenWidth = Gdx.graphics.getWidth();
        int screenHeight = Gdx.graphics.getHeight();

        batch.begin();

        // Game Over text
        float gameOverY = screenHeight / 2f - 50;
        bigFont.draw(batch, "Game Over!", 0, gameOverY, screenWidth, Align.center, false);

        // Points text
        String points = String.format("You scored: %d", alienGame.getPoints());
        smallFont.draw(batch, points, 0, gameOverY - 50, screenWidth, Align.center, false);

        // Difficulty prompt
        float difficultyY = gameOverY + 190;
        smallFont.draw(batch, "Change difficulty?", 0, difficultyY, screenWidth, Align.center, false);

        // Difficulty options
        float optionY = difficultyY - 40;
        smallFont.draw(batch, "Easy", screenWidth / 2f - 200, optionY, 100, Align.center, false);
        smallFont.draw(batch, "Medium", screenWidth / 2f - 50, optionY, 100, Align.center, false);
        smallFont.draw(batch, "Hard", screenWidth / 2f + 100, optionY, 100, Align.center, false);

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
        // wait a second before accepting key strokes
        // since the player may hammer the keyboard as part of playing
        if (elapsedTime > 1) {
            alienGame.newGame();
        }

        return true;
    }

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
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
