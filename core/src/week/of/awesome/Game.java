package week.of.awesome;

import week.of.awesome.states.GameState;
import week.of.awesome.states.InGameState;
import week.of.awesome.states.StartScreenState;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.utils.TimeUtils;

public class Game implements ApplicationListener {
	
	private static final long NANOS_PER_SEC = 1000000000L;
	private static final float FIXED_TIMESTEP = 1f / 60f;
	private static final long FIXED_TIMESTEP_NANOS = (long)(FIXED_TIMESTEP * NANOS_PER_SEC);
	
	private InputMultiplexer inputMultiplexer;
	private BasicRenderer renderer;
	private BackgroundMusic bgMusic;
	
	private GameState currentState;
	private StartScreenState startScreen;
	private InGameState inGame;
	
	
	private long lastFrameTime;
	private long accumulatedTime;
	
	@Override
	public void create () {
		inputMultiplexer = new InputMultiplexer();
		Gdx.input.setInputProcessor(inputMultiplexer);
		
		bgMusic = new BackgroundMusic();
		renderer = new BasicRenderer(inputMultiplexer);
		
		startScreen = new StartScreenState(renderer);
		inGame = new InGameState(renderer, bgMusic);
		
		// wire up gamestates
		startScreen.setBeginPlayingState(inGame);
		inGame.setGameCompletedState(startScreen);
		
		currentState = startScreen;
		currentState.onEnter();
		inputMultiplexer.addProcessor(GameState.getNonNullInputProcessor(currentState));
		
		lastFrameTime = TimeUtils.nanoTime();
	}

	@Override
	public void render () {
		long time = TimeUtils.nanoTime();
		accumulatedTime += (time - lastFrameTime);
		lastFrameTime = time;
		
		while (accumulatedTime >= FIXED_TIMESTEP_NANOS) {
			bgMusic.update(FIXED_TIMESTEP);
			
			GameState nextState = currentState.update(FIXED_TIMESTEP);
			nextState = nextState == null ? currentState : nextState;
			if (nextState != currentState) {
				currentState.onExit();
				nextState.onEnter();
				
				// swap the input processor for the new state's
				InputProcessor inputProcessor = GameState.getNonNullInputProcessor(currentState);
				inputMultiplexer.removeProcessor(inputMultiplexer.getProcessors().size-1);
				inputMultiplexer.addProcessor(inputProcessor);
			}
			currentState = nextState;
			
			accumulatedTime -= FIXED_TIMESTEP_NANOS;
		}
		
		renderer.begin();
		renderer.resetTransform();
		currentState.render();
		renderer.end();
	}

	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dispose() {
		renderer.dispose();
		bgMusic.dispose();
	}
}
