package week.of.awesome.states;

import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputProcessor;

public interface GameState {

	public void onEnter();
	public void onExit();
	public InputProcessor getInputProcessor();
	public GameState update(float dt);
	public void render(float dt);
	
	public static InputProcessor getNonNullInputProcessor(GameState state) {
		InputProcessor inputProcessor = state.getInputProcessor();
		if (inputProcessor == null) {
			inputProcessor = new InputAdapter() { };
		}
		return inputProcessor;
	}
	
}
