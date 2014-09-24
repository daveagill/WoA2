package week.of.awesome;

import java.util.ArrayList;
import java.util.Collection;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.utils.Disposable;

public class Renderer implements Disposable {
	
	private static int MAIN_BACKGROUND_MARGIN = 40;
	public static int WORLD_TO_SCREEN_RATIO = 50;

	private GL20 gl;
	private SpriteBatch batch = new SpriteBatch();
	private Box2DDebugRenderer b2dDebug;
	
	private Collection<Disposable> toDispose = new ArrayList<Disposable>();
	
	private int stageMidX;
	private int stageMidY;
	
	// background
	private Texture outOfBoundsBackgroundTex;
	private Texture mainBackgroundTex;
	
	// tiles
	private Texture tileTex;
	private Texture startTex;
	private Texture goalTex;
	private Texture jumpSingle;
	private Texture jumpLeft;
	private Texture jumpRight;
	
	// toys
	private Texture bearTex;
	private Texture ballTex;
	
	public Renderer() {
		gl = Gdx.gl;
		gl.glClearColor(0, 0, 1, 1);
		
		b2dDebug = new Box2DDebugRenderer();
		
		this.outOfBoundsBackgroundTex = newTexture("outOfBounds.png");
		this.outOfBoundsBackgroundTex.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
		
		this.mainBackgroundTex = newTexture("mainBackground.png");
		this.mainBackgroundTex.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
		
		this.tileTex = newTexture("PNG Grass/slice03_03.png");
		this.startTex = newTexture("castle.png");
		this.goalTex = newTexture("flagYellow.png");
		this.jumpSingle = newTexture("tundraHalf.png");
		this.jumpLeft = newTexture("slice07_07.png");
		this.jumpRight = newTexture("slice06_06.png");
		
		this.ballTex = newTexture("ball.png");
		
		this.stageMidX = Gdx.graphics.getWidth() / 2;
		this.stageMidY = Gdx.graphics.getHeight() / 2;
	}
	
	private void drawBackgroundAndSetupTranslation(Level level) {
		batch.setTransformMatrix(new Matrix4());
		
		int levelWidth = level.getWidth() * WORLD_TO_SCREEN_RATIO;
		int levelHeight = level.getHeight() * WORLD_TO_SCREEN_RATIO;
		int stageX = stageMidX - levelWidth / 2;
		int stageY = stageMidY - levelHeight / 2;		

		batch.draw(outOfBoundsBackgroundTex, 0, 0, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		batch.draw(
				mainBackgroundTex,
				stageX - MAIN_BACKGROUND_MARGIN,
				stageY - MAIN_BACKGROUND_MARGIN,
				0, 0,
				levelWidth + MAIN_BACKGROUND_MARGIN*2,
				levelHeight + MAIN_BACKGROUND_MARGIN*2);
		
		// set the global translation for all things to be rendered, so that 0,0 is the bottom left of the actual level, rather than the screen
		batch.setTransformMatrix(new Matrix4().translate(stageX + WORLD_TO_SCREEN_RATIO/2, stageY + WORLD_TO_SCREEN_RATIO/2, 0).scale(WORLD_TO_SCREEN_RATIO, WORLD_TO_SCREEN_RATIO, 1));
	}
	
	private void drawStage(Level level) {
		
		for (int y = 0; y < level.getHeight(); ++y) {
			for (int x = 0; x < level.getWidth(); ++x) {
				Tile t = level.getTile(x, y);
				if (t != null) {
					drawTile(t, x, y);
				}
			}
		}
	}
	
	private void drawToys(Collection<Toy> toys) {
		for (Toy toy : toys) {
			Vector2 pos = toy.getPosition();
			draw(ballTex, pos.x, pos.y, Toy.TOY_SIZE + 0.1f, Toy.TOY_SIZE + 0.1f);
		}
	}
	
	public void drawWorld(World world) {
		gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		batch.begin();
		
		drawBackgroundAndSetupTranslation(world.getLevel());
		drawStage(world.getLevel());
		drawToys(world.getToys());
		
		batch.end();
		
		Matrix4 combined = batch.getProjectionMatrix().cpy().mul(batch.getTransformMatrix());
		b2dDebug.render(world.getB2d(), combined);
	}
	
	private void drawTile(Tile tile, int x, int y) {
		Texture texture = null;
		float verticalOffset = 0;
		
		switch (tile.getType()) {
			case BLOCK:       texture = tileTex;    break;
			case START:       texture = startTex; verticalOffset = 0; break;
			case GOAL:        texture = goalTex;    break;
			case JUMP_SINGLE: texture = jumpSingle; verticalOffset = -0.7f; break;
			case JUMP_DOUBLE: return;
			case JUMP_LEFT:   texture = jumpLeft;   break;
			case JUMP_RIGHT:  texture = jumpRight;  break;
		}
		
		draw(texture, x, y + verticalOffset, tile.getWidth(), tile.getHeight());
	}
	
	private void draw(Texture t, float x, float y, float width, float height) {
		batch.draw(t, x - width/2, y - height/2, width, height);
	}
	
	private Texture newTexture(String path) {
		Texture t = new Texture(path);
		toDispose.add(t);
		return t;
	}
	
	@Override
	public void dispose() {
		for (Disposable d : toDispose) {
			d.dispose();
		}
	}

}
