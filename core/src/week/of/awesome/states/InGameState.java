package week.of.awesome.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputProcessor;

import week.of.awesome.BackgroundMusic;
import week.of.awesome.BasicRenderer;
import week.of.awesome.GameplayController;
import week.of.awesome.GameplayRenderer;
import week.of.awesome.Sounds;
import week.of.awesome.World;

public class InGameState implements GameState {
	
	private GameplayRenderer renderer;
	private World world;
	private GameplayController controller;
	
	private GameState gameCompletedState;
	private GameState gameExitState;
	
	private boolean userWantsToQuit;
	
	public InGameState(BasicRenderer basicRenderer, BackgroundMusic bgMusic, Sounds sounds) {
		renderer = new GameplayRenderer(basicRenderer);
		controller = new GameplayController(renderer, bgMusic, sounds);
	}
	
	public void setGameCompletedState(GameState gameCompleted) {
		this.gameCompletedState = gameCompleted;
	}
	
	public void setGameExitState(GameState gameExit) {
		this.gameExitState = gameExit;
	}
	
	@Override
	public void onEnter() {
		world = new World();
		controller.startGame(world);
		userWantsToQuit = false;
	}
	
	@Override
	public void onExit() {
		Gdx.graphics.setTitle("Save My Toys!");
		world.dispose();
	}
	
	@Override
	public InputProcessor getInputProcessor() {
		return new InputAdapter() {
			@Override
			public boolean touchUp(int screenX, int screenY, int pointer, int button) {
				controller.mouseUp();
				return false;
			}
			
			@Override
			public boolean keyDown(int keycode) {
				userWantsToQuit = keycode == Input.Keys.ESCAPE;
				
				if (userWantsToQuit) {
					return true; // override the global behaviour!
				}
				
				if (keycode == Input.Keys.N) {
					controller.skipLevel();
				}
				
				return false;
			}
		};
	}

	@Override
	public GameState update(float dt) {
		if (userWantsToQuit) { return gameExitState; }
		
		world.update(dt, controller);
		
		return world.gameCompleted() ? gameCompletedState : this;
	}

	@Override
	public void render(float dt) {
		if (world.gameCompleted()) { return; }
		renderer.drawWorld(world, dt);
	}

}
