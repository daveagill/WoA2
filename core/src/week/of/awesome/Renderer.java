package week.of.awesome;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.utils.Disposable;

public class Renderer implements Disposable {
	
	private static final int MAIN_BACKGROUND_MARGIN = 30;
	public static final int WORLD_TO_SCREEN_RATIO = 40;
	
	private static final int TILE_SCREEN_SIZE = Tile.TILE_SIZE * WORLD_TO_SCREEN_RATIO;
	
	private static final int BOTTOM_UI_PANE_HEIGHT = 80; //TILE_SCREEN_SIZE + TILE_PALETTE_Y_BOTTOM;

	private GL20 gl;
	private SpriteBatch batch = new SpriteBatch();
	private Box2DDebugRenderer b2dDebug;
	
	private Collection<Disposable> toDispose = new ArrayList<Disposable>();
	
	private int stageMidX;
	private int stageMidY;
	private int bottomUiWidth = Gdx.graphics.getWidth() - 100;
	private int screenWidth;
	private int screenHeight;
	
	// mouse cursor
	private Vector2 mousePos = new Vector2();
	private boolean mousePressed = false;
	
	private boolean mouseLostTile = false;
	
	// tool palette stuff
	private List<Tile> toolPalette;
	private int uiPaletteLeftX;
	private int uiPaletteLeftY;
	private int uiPaletteWidth;
	private int uiPaletteHeight;
	
	// kill button stuff
	private Rectangle uiKillButtonBounds;
	
	// fonts
	BitmapFont font;
	
	// background
	private Texture outOfBoundsBackgroundTex;
	private Texture mainBackgroundTex;
	private Texture bottomUiBackgroundTex;
	private Texture toolPaletteBackgroundTex;
	private Texture paletteHighlightBackgroundTex;
	
	// UI
	private Texture killAllButtonTex;
	private Texture killAllButtonPressedTex;
	
	// tiles
	private Texture groundTex;
	private Texture startTex;
	private Texture goalTex;
	private Texture jumpSingleTex;
	private Texture jumpDoubleTex;
	private Texture jumpLeftTex;
	private Texture jumpRightTex;
	private Texture blockerTex;
	
	// toys
	private Texture bearTex;
	private Texture ballTex;
	
	public Renderer() {
		gl = Gdx.gl;
		gl.glClearColor(0, 0, 1, 1);
		
		b2dDebug = new Box2DDebugRenderer();
		
		font = new BitmapFont(Gdx.files.getFileHandle("fonts/BABYCAKE_small.fnt", FileType.Internal));
		
		this.outOfBoundsBackgroundTex = newTexture("outOfBounds.png");
		this.outOfBoundsBackgroundTex.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
		
		this.mainBackgroundTex = newTexture("mainBackground.png");
		this.mainBackgroundTex.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
		
		this.bottomUiBackgroundTex = newTexture("bottomUIBackground.png");
		this.bottomUiBackgroundTex.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
		
		this.toolPaletteBackgroundTex = newTexture("paletteBackground.png");
		this.toolPaletteBackgroundTex.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
		
		this.paletteHighlightBackgroundTex = newTexture("paletteHighlightBackground.png");
		this.paletteHighlightBackgroundTex.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
		
		this.killAllButtonTex = newTexture("killAll.png");
		this.killAllButtonPressedTex = newTexture("killAll_down.png");
		
		this.groundTex = newTexture("ground.png");
		this.startTex = newTexture("start.png");
		this.goalTex = newTexture("goal.png");
		this.jumpSingleTex = newTexture("jumpSingle.png");
		this.jumpDoubleTex = newTexture("jumpDouble.png");
		this.jumpLeftTex = newTexture("jumpLeft.png");
		this.jumpRightTex = newTexture("jumpRight.png");
		this.blockerTex = newTexture("blocker.png");
		
		this.ballTex = newTexture("ball.png");
		this.bearTex = newTexture("bear.png");
		
		this.stageMidX = Gdx.graphics.getWidth() / 2;
		this.stageMidY = BOTTOM_UI_PANE_HEIGHT + (Gdx.graphics.getHeight() - BOTTOM_UI_PANE_HEIGHT) / 2;
		this.screenWidth = Gdx.graphics.getWidth();
		this.screenHeight = Gdx.graphics.getHeight();
		
		calculateToolPaletteUI();
		calculateResetButtonUI();
	}
	
	public void notifyMouseMove(Vector2 mousePos) {
		boolean mouseEnteredBottomUI = this.mousePos.y > BOTTOM_UI_PANE_HEIGHT && mousePos.y <= BOTTOM_UI_PANE_HEIGHT;
		boolean mouseExitedBottomUI = this.mousePos.y <= BOTTOM_UI_PANE_HEIGHT && mousePos.y > BOTTOM_UI_PANE_HEIGHT;
		if (mouseEnteredBottomUI) { mouseLostTile = true; } // mouse will lose tile when it enters the bottom UI area
		if (mouseExitedBottomUI) { mouseLostTile = false; } // mouse will regain tile when it leaves
		this.mousePos = mousePos;
	}
	public void notifyMouseDown() {
		this.mousePressed = true;
		boolean aTileWasSelected = getTileSelectionOrNull() != null;
		if (aTileWasSelected) { mouseLostTile = false; } // mouse gains tile
	}
	public void notifyMouseUp() { this.mousePressed = false; }
	
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
	
	public Tile.Type getTileSelectionOrNull() {
		if (mousePos == null) { return null; }
		
		for (Tile t : toolPalette) {
			Rectangle bounds = new Rectangle(
					t.getPosition().x - TILE_SCREEN_SIZE/2, t.getPosition().y - TILE_SCREEN_SIZE/2,
					t.getWidth() * WORLD_TO_SCREEN_RATIO, t.getHeight() * WORLD_TO_SCREEN_RATIO);
			
			if (bounds.contains(mousePos)) {
				return t.getType();
			}
		}
		
		return null;
	}
	
	public boolean isMouseWithinKillAllButton() {
		return uiKillButtonBounds.contains(mousePos);
	}
	
	private Rectangle getLevelBounds(Level level) {
		int levelWidth = level.getWidth() * WORLD_TO_SCREEN_RATIO;
		int levelHeight = level.getHeight() * WORLD_TO_SCREEN_RATIO;
		int stageX = stageMidX - levelWidth / 2;
		int stageY = stageMidY - levelHeight / 2;
		
		return new Rectangle(stageX, stageY, levelWidth, levelHeight);
	}
	
	private void useUITransform() {
		batch.setTransformMatrix(new Matrix4());
	}
	
	private void useLevelTransform(Level level) {
		Rectangle bounds = getLevelBounds(level);
		
		// set the global translation for all things to be rendered, so that 0,0 is the bottom left of the actual level, rather than the screen
		batch.setTransformMatrix(new Matrix4().translate(bounds.x + WORLD_TO_SCREEN_RATIO/2, bounds.y + WORLD_TO_SCREEN_RATIO/2, 0).scale(WORLD_TO_SCREEN_RATIO, WORLD_TO_SCREEN_RATIO, 1));

	}
	
	private void calculateToolPaletteUI() {
		this.toolPalette = new ArrayList<Tile>();
		toolPalette.add(new Tile(Tile.Type.BLOCKER));
		toolPalette.add(new Tile(Tile.Type.JUMP_SINGLE));
		toolPalette.add(new Tile(Tile.Type.JUMP_DOUBLE));
		toolPalette.add(new Tile(Tile.Type.JUMP_LEFT));
		toolPalette.add(new Tile(Tile.Type.JUMP_RIGHT));
		
		int stepX = TILE_SCREEN_SIZE + 10;
		int leftX = stageMidX - bottomUiWidth/2 + TILE_SCREEN_SIZE/2;
		int bottomY = BOTTOM_UI_PANE_HEIGHT/2;
		
		for (int i = 0; i < toolPalette.size(); ++i) {
			toolPalette.get(i).setPosition(new Vector2(leftX + stepX*i, bottomY));
		}
		
		int marginPadding = 10;
		uiPaletteLeftX = (int) toolPalette.get(0).getPosition().x - TILE_SCREEN_SIZE/2 - marginPadding;
		uiPaletteLeftY = (int) toolPalette.get(0).getPosition().y - TILE_SCREEN_SIZE/2 - marginPadding;
		uiPaletteWidth = (int) (toolPalette.get(toolPalette.size()-1).getPosition().x + TILE_SCREEN_SIZE/2 - uiPaletteLeftX + marginPadding);
		uiPaletteHeight = TILE_SCREEN_SIZE + marginPadding*2;
	}
	
	private void calculateResetButtonUI() {
		int paletteRightX = uiPaletteLeftX + uiPaletteWidth;
		int paddingBetweenPaletteAndKillButton = 20;

		this.uiKillButtonBounds = new Rectangle(
				paletteRightX + paddingBetweenPaletteAndKillButton,
				BOTTOM_UI_PANE_HEIGHT/2 - killAllButtonTex.getHeight()/2,
				killAllButtonTex.getWidth(),
				killAllButtonTex.getHeight());
		 
	}
	
	private void drawStage(Level level) {
		Rectangle bounds = getLevelBounds(level);
		
		useUITransform();
		batch.draw(outOfBoundsBackgroundTex, 0, BOTTOM_UI_PANE_HEIGHT, 0, 0, screenWidth, screenHeight);
		batch.draw(
				mainBackgroundTex,
				(int)bounds.x - MAIN_BACKGROUND_MARGIN,
				(int)bounds.y - MAIN_BACKGROUND_MARGIN,
				0, 0,
				(int)bounds.width + MAIN_BACKGROUND_MARGIN*2,
				(int)bounds.height + MAIN_BACKGROUND_MARGIN*2);
		
		useLevelTransform(level);
		
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
	
	private void drawCursorAndDroppableTile(Tile.Type selectedTileType, Level level) {
		boolean isTileSelected = selectedTileType != null;
		if (!mouseLostTile && isTileSelected) {
			
			Vector2 levelSpaceCursorPos = convertToLevelSpaceOrNull(mousePos, level);
			boolean isWithinLevel = levelSpaceCursorPos != null;
			
			Tile tile = new Tile(selectedTileType, levelSpaceCursorPos);
			boolean isTileDroppable = isWithinLevel && tile.isPositionViableForLevel(level);
			
			if (isTileDroppable) {
				useLevelTransform(level);
				drawTile(tile);
				useUITransform();
			}
			else {
				batch.setColor(1f, 1f, 1f, 0.7f);
				draw(lookupTextureForTile(tile), mousePos, tile.getWidth() * TILE_SCREEN_SIZE, tile.getHeight() * TILE_SCREEN_SIZE);
				batch.setColor(1f, 1f, 1f, 1f);
			}
		}
	}
	
	private void drawToolPalette(Tile.Type selectedTileType) {
	
		drawRepeating(
				toolPaletteBackgroundTex,
				uiPaletteLeftX,
				uiPaletteLeftY,
				uiPaletteWidth,
				uiPaletteHeight);
		
		
		Tile.Type selectionType = getTileSelectionOrNull();
		
		for (Tile t : toolPalette) {
			int tileScreenWidth = t.getWidth() * WORLD_TO_SCREEN_RATIO;
			int tileScreenHeight = t.getHeight() * WORLD_TO_SCREEN_RATIO;
			Vector2 tilePos = t.getPosition();
			int highlightPadding = 5;
			
			if (t.getType() == selectionType || selectedTileType != null && t.getType() == selectedTileType) {
				int highlightLeftX = (int) (tilePos.x - tileScreenWidth/2 - highlightPadding);
				int highlightLeftY = (int) (tilePos.y - tileScreenHeight/2 - highlightPadding);
				int highlightWidth = tileScreenWidth + highlightPadding*2;
				int highlightHeight = tileScreenHeight + highlightPadding*2;
				drawRepeating(paletteHighlightBackgroundTex, highlightLeftX, highlightLeftY, highlightWidth, highlightHeight);
			}
			draw(lookupTextureForTile(t), t.getPosition(), tileScreenWidth, tileScreenHeight);
		}
	}
	
	private void drawBottomUI(World world) {
		drawRepeating(
				bottomUiBackgroundTex,
				stageMidX - screenWidth/2,
				0,
				screenWidth,
				BOTTOM_UI_PANE_HEIGHT);
		
		drawToolPalette(world.getSelectedDroppableTileType());
		
		// draw kill all button
		Vector2 killButtonCenter = new Vector2(uiKillButtonBounds.x + killAllButtonTex.getWidth()/2, uiKillButtonBounds.y + killAllButtonTex.getHeight()/2);
		Texture killButtonTex = this.isMouseWithinKillAllButton() && mousePressed ? this.killAllButtonPressedTex : this.killAllButtonTex;
		draw(killButtonTex, killButtonCenter, killAllButtonTex.getWidth(), killAllButtonTex.getHeight());
		
		// draw the stats
		String numRescued = world.getNumRescued() + " Rescued";
		String numRequired = world.getLevel().getNumRescuedNeeded() + " Needed";
		int statsRightX = stageMidX + bottomUiWidth/2;
		int fontY = (int) (BOTTOM_UI_PANE_HEIGHT/2 + font.getLineHeight());
		int fudgeY = 3;
		font.drawMultiLine(batch, numRescued + "\n" + numRequired, statsRightX, fontY + fudgeY, 0, HAlignment.RIGHT);
	}
	
	public void drawWorld(World world) {
		gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		batch.begin();
		
		drawStage(world.getLevel());
		drawToys(world.getToys());
		
		useUITransform();
		drawBottomUI(world);
		drawCursorAndDroppableTile(world.getSelectedDroppableTileType(), world.getLevel());
				
		batch.end();
		
		/*useLevelTransform(world.getLevel());
		Matrix4 combined = batch.getProjectionMatrix().cpy().mul(batch.getTransformMatrix());
		b2dDebug.render(world.getB2d(), combined);*/
	}
	
	private Texture lookupTextureForTile(Tile tile) {
		switch (tile.getType()) {
			case GROUND:      return groundTex;
			case START:       return startTex;
			case GOAL:        return goalTex;
			case JUMP_SINGLE: return jumpSingleTex;
			case JUMP_DOUBLE: return jumpDoubleTex;
			case JUMP_LEFT:   return jumpLeftTex;
			case JUMP_RIGHT:  return jumpRightTex;
			case BLOCKER:     return blockerTex;
		}
	
		throw new RuntimeException("No such type!");
	}
	
	private void drawTile(Tile tile) {
		Texture texture = lookupTextureForTile(tile);
		Vector2 pos = tile.getPosition();
		draw(texture, new Vector2((int)pos.x, (int)pos.y), tile.getWidth(), tile.getHeight());
	}
	
	private void draw(Texture t, Vector2 pos, float width, float height) {
		batch.draw(t, pos.x - width/2, pos.y - height/2, width, height);
	}
	
	private void drawRepeating(Texture t, int x, int y, int width, int height) {
		batch.draw(t, x, y, 0, 0, width, height);
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
