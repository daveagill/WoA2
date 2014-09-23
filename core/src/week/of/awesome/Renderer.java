package week.of.awesome;

import java.util.ArrayList;
import java.util.Collection;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;

public class Renderer implements Disposable {
	
	private static int MAIN_BACKGROUND_MARGIN = 40;

	private GL20 gl;
	private SpriteBatch batch = new SpriteBatch();
	
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
		
		this.outOfBoundsBackgroundTex = newTexture("outOfBounds.png");
		this.outOfBoundsBackgroundTex.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
		
		this.mainBackgroundTex = newTexture("mainBackground.png");
		this.mainBackgroundTex.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
		
		this.tileTex = newTexture("PNG Grass/slice03_03.png");
		this.startTex = newTexture("bear.png");
		this.goalTex = newTexture("ball.png");
		this.jumpSingle = newTexture("tundraHalf.png");
		this.jumpLeft = newTexture("slice07_07.png");
		this.jumpRight = newTexture("slice06_06.png");
		
		this.ballTex = newTexture("ball.png");
		
		this.stageMidX = Gdx.graphics.getWidth() / 2;
		this.stageMidY = Gdx.graphics.getHeight() / 2;
	}
	
	private void drawBackgroundAndSetupTranslation(Level level) {
		int levelWidth = level.getWidth() * Tile.TILE_SIZE;
		int levelHeight = level.getHeight() * Tile.TILE_SIZE;
		int stageX = stageMidX - levelWidth / 2;
		int stageY = stageMidY - levelHeight / 2;
		
		// set the global translation for all things to be rendered, so that 0,0 is the bottom left of the actual level, rather than the screen
		batch.getTransformMatrix().setTranslation(stageX, stageY, 0);
		
		// we actually don't want the backgrounds affected by this translation, so have to offset
		batch.draw(outOfBoundsBackgroundTex, -stageX, -stageY, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		batch.draw(
				mainBackgroundTex,
				-MAIN_BACKGROUND_MARGIN,
				-MAIN_BACKGROUND_MARGIN,
				0, 0,
				levelWidth + MAIN_BACKGROUND_MARGIN*2,
				levelHeight + MAIN_BACKGROUND_MARGIN*2);
		
		
	}
	
	private void drawStage(Level level) {
		
		for (int y = 0; y < level.getHeight(); ++y) {
			for (int x = 0; x < level.getWidth(); ++x) {
				Tile t = level.getTile(x, y);
				if (t != null) {
					drawTile(t.getType(), x, y, 0, 0);
				}
			}
		}
	}
	
	private void drawToys(Collection<Toy> toys) {
		for (Toy toy : toys) {
			Vector2 pos = toy.getPosition();
			batch.draw(ballTex, pos.x, pos.y);
		}
	}
	
	public void drawWorld(World world) {
		gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		batch.begin();
		
		drawBackgroundAndSetupTranslation(world.getLevel());
		drawStage(world.getLevel());
		drawToys(world.getToys());
		
		batch.end();
	}
	
	private void drawTile(Tile.Type type, int logicalX, int logicalY, int stageX, int stageY) {
		Texture texture = null;
		int verticalOffset = 0;
		
		switch (type) {
			case BLOCK:       texture = tileTex;    break;
			case START:       texture = startTex;   break;
			case GOAL:        texture = goalTex;    break;
			case JUMP_SINGLE: texture = jumpSingle; verticalOffset = -Tile.TILE_SIZE + 10; break;
			case JUMP_DOUBLE: return;
			case JUMP_LEFT:   texture = jumpLeft;   break;
			case JUMP_RIGHT:  texture = jumpRight;  break;
		}
		
		batch.draw(texture, stageX + logicalX * Tile.TILE_SIZE, stageY + logicalY * Tile.TILE_SIZE + verticalOffset, Tile.TILE_SIZE, Tile.TILE_SIZE);
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
