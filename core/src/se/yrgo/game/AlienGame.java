package se.yrgo.game;

import com.badlogic.gdx.*;

/**
 * This is the class for the main game. It controlls the different
 * screens that exist. Screens lets us divide our program into
 * separate entities that need different handling such as
 * menus, different game modes, end screens etc.
 */
public class AlienGame extends Game {
	private GameScreen gameScreen;
	private GameOverScreen gameOverScreen;

	private int points;
	// added
	private int highScore;
	private Preferences prefs;

	@Override
	public void create() {
		// added
		prefs = Gdx.app.getPreferences("AlienGamePrefs");
		highScore = prefs.getInteger("highScore", 0);

		gameScreen = new GameScreen(this);
		gameOverScreen = new GameOverScreen(this);
		newGame();
	}

	@Override
	public void dispose() {
		gameScreen.dispose();
	}

	public void addPoints(int points) {
		this.points += points;
		// added
		if (this.points > highScore) {
			highScore = this.points;
			prefs.putInteger("highScore", highScore);
			prefs.flush(); // Save to disk
		}
	}

	public int getPoints() {
		return points;
	}

	public void newGame() {
		points = 0;
		setScreen(gameScreen);
	}

	// added
	public int getHighScore() {
		return highScore;
	}

	 public void gameOver() {
	 setScreen(gameOverScreen);
	}
}
