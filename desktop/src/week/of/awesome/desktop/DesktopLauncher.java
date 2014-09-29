package week.of.awesome.desktop;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

import week.of.awesome.Game;
import week.of.awesome.GameplayRenderer;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = "Save My Toys!";
		config.width = GameplayRenderer.WORLD_TO_UI_RATIO * 18 + 50;
		config.height = GameplayRenderer.WORLD_TO_UI_RATIO * 15;
		config.resizable = false;
		
		config.addIcon("icons/128x128.png", FileType.Internal);
		config.addIcon("icons/32x32.png", FileType.Internal);
		config.addIcon("icons/16x16.png", FileType.Internal);
		
		new LwjglApplication(new Game(), config);
	}
}
