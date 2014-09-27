package week.of.awesome.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.math.Vector2;

import week.of.awesome.BasicRenderer;
import week.of.awesome.Level;
import week.of.awesome.LevelLoader;
import week.of.awesome.LevelRenderer;
import week.of.awesome.World;
import week.of.awesome.WorldEvents;

public class StartScreenState implements GameState {
	
	private static final String MENU_TEXT = "Play!";
	private static final int MENU_Y = 150;

	private BasicRenderer renderer;
	private LevelRenderer levelRenderer;
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
	
	public StartScreenState(BasicRenderer renderer) {
		this.renderer = renderer;
		levelRenderer = new LevelRenderer(renderer);
		
		midX = Gdx.graphics.getWidth()/2;
		midY = Gdx.graphics.getHeight()/2;
		levelRenderer.setMapCenterPos(midX, midY);
		
		backgroundTex = renderer.newRepeatingTexture("bottomUIBackground.png");
		highlightBackgroundTex = renderer.newRepeatingTexture("paletteHighlightBackground.png");
		logoTex = renderer.newTexture("ground.png");
		
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

			@Override public void onJump() { }
			@Override public void onRescue() { }
		});
		
		return clickedPlay ? beginPlaying : null;
	}

	@Override
	public void render() {
		renderer.drawRepeating(backgroundTex, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		levelRenderer.drawLevel(world.getLevel(), world.getToys());
		renderer.drawCentered(logoTex, new Vector2(midX, midY), logoTex.getWidth(), logoTex.getHeight());
	
		
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
		world.beginLevel(level);
	}
}
