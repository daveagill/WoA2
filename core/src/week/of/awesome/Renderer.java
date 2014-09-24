package week.of.awesome;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.utils.Disposable;

public class Renderer implements Disposable {
	
	private static int MAIN_BACKGROUND_MARGIN = 40;
	public static int WORLD_TO_SCREEN_RATIO = 50;
	
	private static int TILE_SCREEN_SIZE = Tile.TILE_SIZE * WORLD_TO_SCREEN_RATIO;
	
	private static int TILE_PALETTE_Y_BOTTOM = 30;
	private static int TILE_PALETTE_HEIGHT = TILE_SCREEN_SIZE + TILE_PALETTE_Y_BOTTOM;

	private GL20 gl;
	private SpriteBatch batch = new SpriteBatch();
	private Box2DDebugRenderer b2dDebug;
	
	private Collection<Disposable> toDispose = new ArrayList<Disposable>();
	
	private int stageMidX;
	private int stageMidY;
	
	// background
	private Texture outOfBoundsBackgroundTex;
	private Texture mainBackgroundTex;
	private Texture toolPaletteBackgroundTex;
	private Texture paletteHighlightBackgroundTex;
	
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
		
		this.toolPaletteBackgroundTex = newTexture("paletteBackground.png");
		this.toolPaletteBackgroundTex.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
		
		this.paletteHighlightBackgroundTex = newTexture("paletteHighlightBackground.png");
		this.paletteHighlightBackgroundTex.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
		
		this.tileTex = newTexture("PNG Grass/slice03_03.png");
		this.startTex = newTexture("castle.png");
		this.goalTex = newTexture("flagYellow.png");
		this.jumpSingle = newTexture("tundraHalf.png");
		this.jumpLeft = newTexture("slice07_07.png");
		this.jumpRight = newTexture("slice06_06.png");
		
		this.ballTex = newTexture("ball.png");
		this.bearTex = newTexture("bear.png");
		
		this.stageMidX = Gdx.graphics.getWidth() / 2;
		this.stageMidY = TILE_PALETTE_HEIGHT + (Gdx.graphics.getHeight() - TILE_PALETTE_HEIGHT) / 2;
	}
	
	public Vector2 convertToLevelSpaceOrNull(Vector2 position, Level level) {
		Rectangle bounds = getLevelBounds(level);
		
		// n.b. countains() checks an open interval, whereas we want a half open interval, hence the checks at the far end of the bounds
		if (!bounds.contains(position) ||
			bounds.x + bounds.width == position.x || bounds.y + bounds.height == position.y) {
				return null;
		}
		
		Vector2 relativePos = position.cpy().sub(bounds.getPosition(new Vector2()));
		Vector2 posInTileSpace = relativePos.scl(1f / (TILE_SCREEN_SIZE));
		
		return posInTileSpace;
	}
	
	public Tile.Type getTileSelectionOrNull(Vector2 mousePos) {
		for (Tile t : getToolPalette()) {
			Rectangle bounds = new Rectangle(
					t.getPosition().x - TILE_SCREEN_SIZE/2, t.getPosition().y - TILE_SCREEN_SIZE/2,
					t.getWidth() * WORLD_TO_SCREEN_RATIO, t.getHeight() * WORLD_TO_SCREEN_RATIO);
			
			if (bounds.contains(mousePos)) {
				return t.getType();
			}
		}
		
		return null;
	}
	
	private Rectangle getLevelBounds(Level level) {
		int levelWidth = level.getWidth() * WORLD_TO_SCREEN_RATIO;
		int levelHeight = level.getHeight() * WORLD_TO_SCREEN_RATIO;
		int stageX = stageMidX - levelWidth / 2;
		int stageY = stageMidY - levelHeight / 2;
		
		return new Rectangle(stageX, stageY, levelWidth, levelHeight);
	}
	
	private List<Tile> getToolPalette() {
		List<Tile> palette = new ArrayList<Tile>();
		palette.add(new Tile(Tile.Type.BLOCK));
		palette.add(new Tile(Tile.Type.JUMP_SINGLE));
		palette.add(new Tile(Tile.Type.JUMP_DOUBLE));
		palette.add(new Tile(Tile.Type.JUMP_LEFT));
		palette.add(new Tile(Tile.Type.JUMP_RIGHT));
		
		int stepX = TILE_SCREEN_SIZE + 10;
		int leftX = (int) stageMidX - palette.size()/2 * stepX;
		int bottomY = TILE_SCREEN_SIZE/2 + TILE_PALETTE_Y_BOTTOM;
		
		for (int i = 0; i < palette.size(); ++i) {
			palette.get(i).setPosition(new Vector2(leftX + stepX*i, bottomY));
		}
		
		return palette;
	}
	
	private void drawUIAndSetupTranslation(Level level, Vector2 mousePos, Tile activeTile) {
		batch.setTransformMatrix(new Matrix4());
		
		Rectangle bounds = getLevelBounds(level);

		batch.draw(outOfBoundsBackgroundTex, 0, 0, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		batch.draw(
				mainBackgroundTex,
				(int)bounds.x - MAIN_BACKGROUND_MARGIN,
				(int)bounds.y - MAIN_BACKGROUND_MARGIN,
				0, 0,
				(int)bounds.width + MAIN_BACKGROUND_MARGIN*2,
				(int)bounds.height + MAIN_BACKGROUND_MARGIN*2);
		
		drawToolPalette(mousePos, activeTile);
		
		// set the global translation for all things to be rendered, so that 0,0 is the bottom left of the actual level, rather than the screen
		batch.setTransformMatrix(new Matrix4().translate(bounds.x + WORLD_TO_SCREEN_RATIO/2, bounds.y + WORLD_TO_SCREEN_RATIO/2, 0).scale(WORLD_TO_SCREEN_RATIO, WORLD_TO_SCREEN_RATIO, 1));
	}
	
	private void drawStage(Level level) {
		
		for (int y = 0; y < level.getHeight(); ++y) {
			for (int x = 0; x < level.getWidth(); ++x) {
				Tile t = level.getTile(x, y);
				
				if (t != null) {
					drawTile(t);
				}
			}
		}
	}
	
	private void drawToys(Collection<Toy> toys) {
		for (Toy toy : toys) {
			Vector2 pos = toy.getPosition();
			draw(ballTex, pos, Toy.TOY_SIZE + 0.1f, Toy.TOY_SIZE + 0.1f);
		}
	}
	
	private void drawCursorAndDroppableTile(Vector2 cursorPos, Tile droppableTile, Level level) {
		boolean isTileSelected = droppableTile != null;
		if (isTileSelected) {
			boolean isTileDroppable = droppableTile.isPositionViableForLevel(level);
			if (isTileDroppable) {
				drawTile(droppableTile);
			}
			else {
				batch.setColor(1f, 1f, 1f, 0.7f);
				Vector2 drawCenteredPos = droppableTile.getPosition().sub(droppableTile.getWidth()/2f, droppableTile.getHeight()/2f);
				draw(lookupTextureForTile(droppableTile), drawCenteredPos, droppableTile.getWidth(), droppableTile.getHeight());
				batch.setColor(1f, 1f, 1f, 1f);
			}
		}
	}
	
	private void drawToolPalette(Vector2 mousePos, Tile activeTile) {
		List<Tile> palette = getToolPalette();
		
		int backgroundPadding = 30;
		drawRepeating(
				toolPaletteBackgroundTex,
				new Vector2(stageMidX, TILE_PALETTE_Y_BOTTOM + TILE_SCREEN_SIZE/2),
				(int) (palette.get(palette.size()-1).getPosition().x - palette.get(0).getPosition().x + TILE_SCREEN_SIZE + backgroundPadding),
				TILE_SCREEN_SIZE + backgroundPadding);
		
		
		Tile.Type selectionType = getTileSelectionOrNull(mousePos);
		
		for (Tile t : palette) {
			int tileScreenWidth = t.getWidth() * WORLD_TO_SCREEN_RATIO;
			int tileScreenHeight = t.getHeight() * WORLD_TO_SCREEN_RATIO;
			
			if (t.getType() == selectionType || activeTile != null && t.getType() == activeTile.getType()) {
				int highlightPadding = 10;
				drawRepeating(paletteHighlightBackgroundTex, t.getPosition(), tileScreenWidth + highlightPadding, tileScreenHeight + highlightPadding);
			}
			draw(lookupTextureForTile(t), t.getPosition(), tileScreenWidth, tileScreenHeight);
		}
	}
	
	public void drawWorld(World world) {
		gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		batch.begin();
		
		drawUIAndSetupTranslation(world.getLevel(), world.getCursorPos(), world.getDroppableTile());
		drawStage(world.getLevel());
		drawCursorAndDroppableTile(world.getCursorPos(), world.getDroppableTile(), world.getLevel());
		drawToys(world.getToys());
		
		batch.end();
		
		Matrix4 combined = batch.getProjectionMatrix().cpy().mul(batch.getTransformMatrix());
		b2dDebug.render(world.getB2d(), combined);
	}
	
	private Texture lookupTextureForTile(Tile tile) {
		switch (tile.getType()) {
			case BLOCK:       return tileTex;
			case START:       return startTex;
			case GOAL:        return goalTex;
			case JUMP_SINGLE: return jumpSingle;
			case JUMP_DOUBLE: return jumpSingle;
			case JUMP_LEFT:   return jumpLeft;
			case JUMP_RIGHT:  return jumpRight;
		}
	
		throw new RuntimeException("No such type!");
	}
	
	private void drawTile(Tile tile) {
		Texture texture = lookupTextureForTile(tile);
		
		float verticalOffset;
		switch (tile.getType()) {
			case JUMP_SINGLE: verticalOffset = -0.7f; break;
			case JUMP_DOUBLE: verticalOffset = -0.7f; break;
			default: verticalOffset = 0;
		}
		
		Vector2 pos = tile.getPosition();
		draw(texture, new Vector2((int)pos.x, (int)pos.y + verticalOffset), tile.getWidth(), tile.getHeight());
	}
	
	private void draw(Texture t, Vector2 pos, float width, float height) {
		batch.draw(t, pos.x - width/2, pos.y - height/2, width, height);
	}
	
	private void drawRepeating(Texture t, Vector2 pos, int width, int height) {
		batch.draw(t, pos.x - width/2, pos.y - height/2, 0, 0, width, height);
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
