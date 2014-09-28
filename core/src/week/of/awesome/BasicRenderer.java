package week.of.awesome;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

public class BasicRenderer implements Disposable {
	
	public static interface MouseListener {
		public void onMouseDown();
		public void onMouseUp();
		public void onMouseMoved(Vector2 prevPos);
	}
	
	private GL20 gl;
	private SpriteBatch batch = new SpriteBatch();
	
	private Collection<Disposable> toDispose = new ArrayList<Disposable>();
	private Map<String, Texture> textureCache = new HashMap<String, Texture>();
	private Map<String, BitmapFont> fontCache = new HashMap<String, BitmapFont>();

	private Vector2 mousePos = new Vector2();
	private boolean mousePressed = false;
	private List<MouseListener> mouseListeners = new ArrayList<MouseListener>();
	
	public BasicRenderer(InputMultiplexer inputMultiplexer) {
		gl = Gdx.gl;
		gl.glClearColor(0, 0, 1, 1);
		
		inputMultiplexer.addProcessor(new InputProcessor() {

			@Override public boolean keyDown(int keycode)     { return false; }
			@Override public boolean keyUp(int keycode)       { return false; }
			@Override public boolean keyTyped(char character) { return false; }
			@Override public boolean scrolled(int amount)     { return false; }

			@Override
			public boolean touchDown(int screenX, int screenY, int pointer, int button) {
				mousePressed = true;
				for (MouseListener listener : mouseListeners) {
					listener.onMouseDown();
				}
				return false;
			}

			@Override
			public boolean touchUp(int screenX, int screenY, int pointer, int button) {
				mousePressed = false;
				for (MouseListener listener : mouseListeners) {
					listener.onMouseUp();
				}
				return false;
			}

			@Override
			public boolean touchDragged(int screenX, int screenY, int pointer) {
				mouseMoved(screenX, screenY);
				return false;
			}

			@Override
			public boolean mouseMoved(int screenX, int screenY) {
				Vector2 prevPos = mousePos;
				mousePos = toSreenSpace(screenX, screenY);
				for (MouseListener listener : mouseListeners) {
					listener.onMouseMoved(prevPos);
				}
				return false;
			}

		});
	}
	
	public void installMouseListener(MouseListener listener) {
		this.mouseListeners.add(listener);
	}
	
	private static Vector2 toSreenSpace(int screenX, int screenY) {
		return new Vector2(screenX, Gdx.graphics.getHeight() - screenY);
	}
	
	public Vector2 getMousePos() { return mousePos; }
	public boolean isMousePressed() { return mousePressed; }
	
	public void begin() {
		gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		batch.begin();
	}
	
	public void end() {
		batch.end();
	}
	
	public void resetTransform() {
		batch.setTransformMatrix(new Matrix4());
	}
	
	public SpriteBatch getBatch() {
		return batch;
	}
	
	public void drawCentered(Texture t, Vector2 pos, float width, float height) {
		drawCentered(t, pos, width, height, false);
	}
	
	public void drawCentered(Texture t, Vector2 pos, float width, float height, boolean flipX) {
		float actualWidth = flipX ? -width : width;
		batch.draw(t, pos.x - actualWidth/2, pos.y - height/2, actualWidth, height);
	}
	
	public void drawCentered(TextureRegion t, Vector2 pos, float width, float height, boolean flipX) {
		float actualWidth = flipX ? -width : width;
		batch.draw(t, pos.x - actualWidth/2, pos.y - height/2, actualWidth, height);
	}
	
	public void drawRepeating(Texture t, int x, int y, int width, int height) {
		batch.draw(t, x, y, 0, 0, width, height);
	}
	
	public Texture newTexture(String path) {
		Texture t = textureCache.get(path);
		if (t == null) {
			t = new Texture(path);
			toDispose.add(t);
			textureCache.put(path, t);
		}
		return t;
	}
	
	public Texture newRepeatingTexture(String path) {
		Texture t = newTexture(path);
		t.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
		return t;
	}
	
	public Animation newAnimation(String pathAndNameStart, int numFrames) {
		Array<TextureRegion> frames = new Array<TextureRegion>(numFrames);
		for (int i = 0; i < numFrames; ++i) {
			Texture t = newTexture(pathAndNameStart + "_frame" + (i+1) + ".png");
			frames.add(new TextureRegion(t));
		}
		return new Animation(1f/numFrames, frames, Animation.PlayMode.LOOP);
	}
	
	public BitmapFont newFont(String name) {
		BitmapFont font = fontCache.get(name);
		if (font == null) {
			font = new BitmapFont(Gdx.files.getFileHandle("fonts/" + name + ".fnt", FileType.Internal));
			toDispose.add(font);
		}
		return font;
	}
	
	@Override
	public void dispose() {
		for (Disposable d : toDispose) {
			d.dispose();
		}
	}
}
