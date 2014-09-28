package week.of.awesome.states;

import java.util.Collections;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.math.Vector2;

import week.of.awesome.BackgroundMusic;
import week.of.awesome.BasicRenderer;
import week.of.awesome.Level;
import week.of.awesome.LevelLoader;
import week.of.awesome.MapRenderer;
import week.of.awesome.World;
import week.of.awesome.WorldEvents;

public class StartScreenState implements GameState {
	
	private static final String MENU_TEXT = "Play!";
	private static final int MENU_Y = 100;

	private BackgroundMusic bgMusic;
	private BasicRenderer renderer;
	private MapRenderer mapRenderer;
	private World world;
	
	private GameState beginPlaying;
	
	private Texture backgroundTex;
	private Texture highlightBackgroundTex;
	private Texture logoTex;
	
	private BitmapFont font;
	
	// really just for convenience
	private int midX;
	private int midY;
	
	// indicate user click play
	boolean clickedPlay;
	
	public StartScreenState(BasicRenderer renderer, BackgroundMusic bgMusic) {
		this.bgMusic = bgMusic;
		this.renderer = renderer;
		mapRenderer = new MapRenderer(renderer);
		
		midX = Gdx.graphics.getWidth()/2;
		midY = Gdx.graphics.getHeight()/2;
		mapRenderer.setMapCenterPos(midX, midY);
		
		backgroundTex = renderer.newRepeatingTexture("UI/menuScreenBackground.png");//("UI/bottomUIBackground.png");
		highlightBackgroundTex = renderer.newRepeatingTexture("UI/paletteHighlightBackground.png");
		logoTex = renderer.newTexture("UI/logo.png");
		
		font = renderer.newFont("BABYCAKE");
	}
	
	public void setBeginPlayingState(GameState beginPlaying) {
		this.beginPlaying = beginPlaying;
	}
	
	@Override
	public void onEnter() {
		world = new World();
		restartLevelScreen();
		clickedPlay = false;
		
		bgMusic.playForLevel(1);
	}
	
	@Override
	public void onExit() {
		world.dispose();
	}
	
	@Override
	public InputProcessor getInputProcessor() { return null; }

	@Override
	public GameState update(float dt) {
		world.update(dt, new WorldEvents() {

			@Override
			public void onLevelComplete(int levelNum) {
				restartLevelScreen();
			}
			@Override public void onLevelFailed(int levelNum) {
				restartLevelScreen(); // hopefully not necessary!
			}
			
			@Override public void onJump() { }
			@Override public void onRescue() { }
			@Override public void onToySpawn() { }
			@Override public void onToyDeath() { }
		});
		
		return clickedPlay ? beginPlaying : null;
	}

	@Override
	public void render(float dt) {
		// background
		renderer.drawRepeating(backgroundTex, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		
		// idle level tiles only
		mapRenderer.drawLevel(world.getLevel(), Collections.emptyList(), true, dt);
		
		// logo
		renderer.drawCentered(logoTex, new Vector2(midX, midY), logoTex.getWidth(), logoTex.getHeight());
		
		// idle level toys (over top of logo!)
		mapRenderer.drawLevel(world.getLevel(), world.getToys(), false, dt);
		
		// draw the menu...
		
		// ... start game highlight
		int startTopY = MENU_Y;
		int startBottomY = MENU_Y - (int)font.getLineHeight();
		if (renderer.getMousePos().y >= startBottomY && renderer.getMousePos().y <= startTopY) {
			renderer.drawRepeating(highlightBackgroundTex, 0, MENU_Y - (int)font.getLineHeight(), Gdx.graphics.getWidth(),  (int)font.getLineHeight());
			clickedPlay = renderer.isMousePressed();
		}
		
		font.drawMultiLine(renderer.getBatch(), MENU_TEXT, midX, MENU_Y, 0, HAlignment.CENTER);
	}
	
	private void restartLevelScreen() {
		Level level = LevelLoader.getScreen("StartScreen");
		world.beginLevel(level, true);
	}
}
