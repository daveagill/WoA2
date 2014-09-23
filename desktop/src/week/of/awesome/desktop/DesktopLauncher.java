package week.of.awesome.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

import week.of.awesome.Game;
import week.of.awesome.Tile;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = "Rescue My Toys!";
		config.width = Tile.TILE_SIZE * 15;
		config.height = Tile.TILE_SIZE * 15;
		config.resizable = false;
		new LwjglApplication(new Game(), config);
	}
}