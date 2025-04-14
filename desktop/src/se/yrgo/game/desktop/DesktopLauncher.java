package se.yrgo.game.desktop;

import com.badlogic.gdx.backends.lwjgl3.*;

import se.yrgo.game.*;

public class DesktopLauncher {
	public static void main(String[] arg) {
		// this is where we configure how our program starts on the desktop
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.setWindowedMode(700, 1100);
		config.setTitle("Alien Game - Avoid the Planets!");
		config.setForegroundFPS(60);
		config.useVsync(true);
		new Lwjgl3Application(new AlienGame(), config);
	}
}
