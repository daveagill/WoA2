package week.of.awesome;

import week.of.awesome.states.GameState;
import week.of.awesome.states.GenericDialogState;
import week.of.awesome.states.InGameState;
import week.of.awesome.states.StartScreenState;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
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
	private Sounds sounds;
	
	private GameState currentState;
	private StartScreenState startScreen;
	private GenericDialogState introScreen;
	private GenericDialogState gameWonScreen;
	private InGameState inGame;
	
	
	private long lastFrameTime;
	private long accumulatedTime;
	
	@Override
	public void create () {
		inputMultiplexer = new InputMultiplexer();
		Gdx.input.setInputProcessor(inputMultiplexer);
		
		// add escape-button to exit handler
		inputMultiplexer.addProcessor(new InputAdapter() {
			@Override
			public boolean keyDown(int keycode) {
				if (keycode == Input.Keys.ESCAPE) {
					Gdx.app.exit();
				}
				return false;
			}
		});
		
		bgMusic = new BackgroundMusic();
		sounds = new Sounds();
		renderer = new BasicRenderer(inputMultiplexer);
		
		startScreen = new StartScreenState(renderer, bgMusic);
		introScreen = new GenericDialogState(renderer);
		gameWonScreen = new GenericDialogState(renderer);
		inGame = new InGameState(renderer, bgMusic, sounds);
		
		introScreen.addDialog("The maniacal Dr. Frankenstein has stolen all the world's toys...");
		introScreen.addDialog("And given them LIFE to use as slaves in his castle!");
		introScreen.addDialog("Luckily the toys managed to escape from the Dr's clutches...");
		introScreen.addDialog("Now they find themselves lost and far from home...");
		introScreen.addDialog("Can you help them find their way?");
		introScreen.addDialog("Good luck!");
		
		gameWonScreen.addDialog("Well I don't believe it...");
		gameWonScreen.addDialog("You have got ALL the remaining toys to safety!");
		gameWonScreen.addDialog("I wonder what adventures those toys will get up to next...");
		gameWonScreen.addDialog("Maybe someone will make an animated CGI movie about it?..");
		gameWonScreen.addDialog("Nah! Not likely.");
		gameWonScreen.addDialog("Thank you for playing!");
		
		// wire up gamestates
		startScreen.setBeginPlayingState(introScreen);
		introScreen.setNextGameState(inGame);
		inGame.setGameCompletedState(gameWonScreen);
		inGame.setGameExitState(startScreen);
		gameWonScreen.setNextGameState(startScreen);
		
		currentState = startScreen;
		currentState.onEnter();
		inputMultiplexer.addProcessor(0, GameState.getNonNullInputProcessor(currentState));
		
		lastFrameTime = TimeUtils.nanoTime();
	}

	@Override
	public void render () {
		long time = TimeUtils.nanoTime();
		accumulatedTime += (time - lastFrameTime);
		lastFrameTime = time;
		
		float amountUpdatedDt = 0; // accumulates how much time we simulated, so we can do the same in rendering
		
		while (accumulatedTime >= FIXED_TIMESTEP_NANOS) {
			bgMusic.update(FIXED_TIMESTEP);
			
			GameState nextState = currentState.update(FIXED_TIMESTEP);
			nextState = nextState == null ? currentState : nextState;
			if (nextState != currentState) {
				currentState.onExit();
				nextState.onEnter();
				
				// swap the input processor for the new state's
				InputProcessor inputProcessor = GameState.getNonNullInputProcessor(nextState);
				inputMultiplexer.removeProcessor(0);
				inputMultiplexer.addProcessor(0, inputProcessor);
			}
			currentState = nextState;
			
			accumulatedTime -= FIXED_TIMESTEP_NANOS;
			amountUpdatedDt += FIXED_TIMESTEP;
		}
		
		renderer.begin();
		renderer.resetTransform();
		currentState.render(amountUpdatedDt);
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
