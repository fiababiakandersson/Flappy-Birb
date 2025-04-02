package se.yrgo.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Align;

public class MenuScreen extends ScreenAdapter {
    private AlienGame alienGame;
    private SpriteBatch batch;
    private BitmapFont titleFont;
    private BitmapFont optionFont;
    private Rectangle easyBounds;
    private Rectangle mediumBounds;
    private Rectangle hardBounds;

    public MenuScreen(AlienGame alienGame) {
        this.alienGame = alienGame;
        this.batch = new SpriteBatch();

        this.titleFont = new BitmapFont();
        this.titleFont.getData().setScale(3);
        this.titleFont.setColor(Color.WHITE);

        this.optionFont = new BitmapFont();
        this.optionFont.getData().setScale(2);
        this.optionFont.setColor(Color.WHITE);

        int centerX = Gdx.graphics.getWidth() / 2;
        int centerY = Gdx.graphics.getHeight() / 2;

        // Define clickable areas for difficulty options
        this.easyBounds = new Rectangle(centerX - 150, centerY + 50, 300, 60);
        this.mediumBounds = new Rectangle(centerX - 150, centerY - 30, 300, 60);
        this.hardBounds = new Rectangle(centerX - 150, centerY - 110, 300, 60);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.043f, 0.078f, 0.22f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        // Draw title
        titleFont.draw(batch, "Alien Game", 0, Gdx.graphics.getHeight() - 100,
                Gdx.graphics.getWidth(), Align.center, false);

        // Draw difficulty options
        optionFont.draw(batch, "Easy", easyBounds.x, easyBounds.y + 40,
                easyBounds.width, Align.center, false);
        optionFont.draw(batch, "Medium", mediumBounds.x, mediumBounds.y + 40,
                mediumBounds.width, Align.center, false);
        optionFont.draw(batch, "Hard", hardBounds.x, hardBounds.y + 40,
                hardBounds.width, Align.center, false);

        batch.end();

        // Handle touch input
        if (Gdx.input.justTouched()) {
            int x = Gdx.input.getX();
            int y = Gdx.graphics.getHeight() - Gdx.input.getY(); // Convert to game coordinates

            if (easyBounds.contains(x, y)) {
                alienGame.setDifficulty(AlienGame.Difficulty.EASY);
                alienGame.newGame();
            } else if (mediumBounds.contains(x, y)) {
                alienGame.setDifficulty(AlienGame.Difficulty.MEDIUM);
                alienGame.newGame();
            } else if (hardBounds.contains(x, y)) {
                alienGame.setDifficulty(AlienGame.Difficulty.HARD);
                alienGame.newGame();
            }
        }
    }

    @Override
    public void dispose() {
        batch.dispose();
        titleFont.dispose();
        optionFont.dispose();
    }
}