package se.yrgo.game.desktop;

import com.badlogic.gdx.backends.lwjgl3.*;

import se.yrgo.game.*;

public class DesktopLauncher {
	public static void main(String[] arg) {
		// this is where we configure how our program starts on the desktop
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.setWindowedMode(390, 844);
		new Lwjgl3Application(new AlienGame(), config);
	}
}
