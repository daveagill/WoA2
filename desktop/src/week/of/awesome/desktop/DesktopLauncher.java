package week.of.awesome.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

import week.of.awesome.Game;
import week.of.awesome.GameplayRenderer;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = "Rescue My Toys!";
		config.width = GameplayRenderer.WORLD_TO_UI_RATIO * 17;
		config.height = GameplayRenderer.WORLD_TO_UI_RATIO * 15;
		config.resizable = false;
		new LwjglApplication(new Game(), config);
	}
}
