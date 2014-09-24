package week.of.awesome;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.Vector2;

public class InputMapper {

	private GameplayController controller;
	private Input input;
	
	public InputMapper(GameplayController controller) {
		this.input = Gdx.input;
		this.controller = controller;
		
		input.setInputProcessor(new InputProcessor() {

			@Override
			public boolean keyDown(int keycode) {
				return false;
			}

			@Override
			public boolean keyUp(int keycode) {
				return false;
			}

			@Override
			public boolean keyTyped(char character) {
				return false;
			}

			@Override
			public boolean touchDown(int screenX, int screenY, int pointer, int button) {
				return false;
			}

			@Override
			public boolean touchUp(int screenX, int screenY, int pointer, int button) {
				controller.mouseClicked(getMouseWorldPos(screenX, screenY));
				return false;
			}

			@Override
			public boolean touchDragged(int screenX, int screenY, int pointer) {
				controller.mouseMove(getMouseWorldPos(screenX, screenY));
				return false;
			}

			@Override
			public boolean mouseMoved(int screenX, int screenY) {
				controller.mouseMove(getMouseWorldPos(screenX, screenY));
				return false;
			}

			@Override
			public boolean scrolled(int amount) {
				return false;
			}
			
			
			private Vector2 getMouseWorldPos(int screenX, int screenY) {
				return new Vector2(input.getX(), Gdx.graphics.getHeight() - input.getY());
			}
		});
	}
	
	public void poll() {
		
		
	}
}
