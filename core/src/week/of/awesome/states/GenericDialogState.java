package week.of.awesome.states;

import java.util.ArrayList;
import java.util.List;

import week.of.awesome.BasicRenderer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;

public class GenericDialogState implements GameState {
	
	private static final int WRAP_PADDING = 50;
	private static final int DISPLAY_DURATION = 5;
	private static final float FADE_DIRATION = 0.5f;
	
	private BasicRenderer renderer;
	
	private BitmapFont textFont;
	private int textWrapWidth;
	
	private Texture transitionBackgroundTex;
	
	private float timerToPauseOnText;
	private float fadePercent;
	private boolean fadingIn;
	private boolean fadingOut;
	
	private List<String> dialog = new ArrayList<String>();
	private int currentTextIdx;
	
	private GameState nextState;
	
	public GenericDialogState(BasicRenderer renderer) {
		this.renderer = renderer;
		
		textFont = renderer.newFont("BABYCAKE");
		
		transitionBackgroundTex = renderer.newRepeatingTexture("UI/paletteBackground.png");
		
		textWrapWidth = Gdx.graphics.getWidth() - WRAP_PADDING*2;
	}
	
	public void setNextGameState(GameState nextState) {
		this.nextState = nextState;
	}
	
	public void addDialog(String text) {
		dialog.add(text);
	}

	@Override
	public void onEnter() {
		timerToPauseOnText = 1f;
		currentTextIdx = 0;
		fadePercent = 0f;
		fadingIn = true;
		fadingOut = false;
	}

	@Override
	public void onExit() { }

	@Override
	public InputProcessor getInputProcessor() {
		return new InputAdapter() {

			@Override
			public boolean touchDown(int screenX, int screenY, int pointer, int button) {
				timerToPauseOnText = 1f;
				fadePercent = 0f;
				fadingIn = true;
				fadingOut = false;
				++currentTextIdx;
				return false;
			}

		};
	}

	@Override
	public GameState update(float dt) {
		if (fadingIn) {
			fadePercent = Math.min(1f, fadePercent + (dt / FADE_DIRATION));
			if (fadePercent >= 1f) {
				fadingIn = false;
				timerToPauseOnText = 1f;
			}
		}
		else if (!fadingIn && !fadingOut) {
			timerToPauseOnText -= (dt / DISPLAY_DURATION);
			if (timerToPauseOnText <= 0) {
				fadingOut = true;
			}
		}
		else if (fadingOut) {
			fadePercent = Math.max(0f, fadePercent - (dt / FADE_DIRATION));
			if (fadePercent <= 0f) {
				fadingOut = false;
				fadingIn = true;
				++currentTextIdx;
			}
		}
		
		if (currentTextIdx == dialog.size()) {
			return nextState;
		}
				
		return null;
	}

	@Override
	public void render(float dt) {
		// defensive coding
		if (currentTextIdx == dialog.size()) { return; }
		
		renderer.drawRepeating(transitionBackgroundTex, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		
		String text = dialog.get(currentTextIdx);
		TextBounds bounds = textFont.getWrappedBounds(text, textWrapWidth);
		float fontX = Gdx.graphics.getWidth()/2 - textWrapWidth/2;
		float fontY = Gdx.graphics.getHeight()/2 + bounds.height/2;
		textFont.drawWrapped(renderer.getBatch(), text, fontX, fontY, textWrapWidth, HAlignment.CENTER);
		
		renderer.getBatch().setColor(1f, 1f, 1f, 1-fadePercent);
		renderer.drawRepeating(transitionBackgroundTex, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		renderer.getBatch().setColor(1f, 1f, 1f, 1f);
	}

}
