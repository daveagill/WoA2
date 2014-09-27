package week.of.awesome;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class GameplayRenderer {
	private static final int MAIN_BACKGROUND_MARGIN = 30;
	private static final int BOTTOM_UI_PANE_HEIGHT = 90;
	
	public static final int WORLD_TO_UI_RATIO = 40;
	private static final int TILE_UI_SIZE = Tile.TILE_SIZE * WORLD_TO_UI_RATIO;
	private static final int PALETTE_TILE_MARGIN = 10;
	private static final int PALETTE_FONT_VERTICAL_ALLOWANCE = 16;
	
	private BasicRenderer renderer;
	private LevelRenderer levelRenderer;
	
	//private int stageMidX;
	//private int stageMidY;
	private int bottomUiWidth = Gdx.graphics.getWidth() - 100;
	private int screenWidth;
	private int screenHeight;
	
	// mouse cursor
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
	BitmapFont statsFont;
	BitmapFont inventoryFont;
	
	// background
	private Texture outOfBoundsBackgroundTex;
	private Texture mainBackgroundTex;
	private Texture bottomUiBackgroundTex;
	private Texture toolPaletteBackgroundTex;
	private Texture paletteHighlightBackgroundTex;
	
	// UI
	private Texture killAllButtonTex;
	private Texture killAllButtonPressedTex;
	
	
	public GameplayRenderer(BasicRenderer renderer) {
		this.renderer = renderer;
		this.levelRenderer = new LevelRenderer(renderer);
		
		statsFont = renderer.newFont("BABYCAKE_small");
		inventoryFont = renderer.newFont("Montserrat-Bold");
		
		this.outOfBoundsBackgroundTex = renderer.newRepeatingTexture("outOfBounds.png");
		this.mainBackgroundTex = renderer.newRepeatingTexture("mainBackground.png");
		this.bottomUiBackgroundTex = renderer.newRepeatingTexture("bottomUIBackground.png");
		this.toolPaletteBackgroundTex = renderer.newRepeatingTexture("paletteBackground.png");
		this.paletteHighlightBackgroundTex = renderer.newRepeatingTexture("paletteHighlightBackground.png");
		
		this.killAllButtonTex = renderer.newTexture("killAll.png");
		this.killAllButtonPressedTex = renderer.newTexture("killAll_down.png");
		
		this.screenWidth = Gdx.graphics.getWidth();
		this.screenHeight = Gdx.graphics.getHeight();
		
		int stageMidX = Gdx.graphics.getWidth() / 2;
		int stageMidY = BOTTOM_UI_PANE_HEIGHT + (Gdx.graphics.getHeight() - BOTTOM_UI_PANE_HEIGHT) / 2;
		levelRenderer.setMapCenterPos(stageMidX, stageMidY);
		
		calculateToolPaletteUI();
		calculateResetButtonUI();
		
		renderer.installMouseListener(getMouseListener());
	}
	
	public Vector2 getScreenSpaceMouse() {
		return renderer.getMousePos();
	}
	
	private BasicRenderer.MouseListener getMouseListener() {
		return new BasicRenderer.MouseListener() {

			@Override
			public void onMouseDown() {
				boolean aTileWasSelected = getTileSelectionOrNull() != null;
				if (aTileWasSelected) { mouseLostTile = false; } // mouse gains tile
			}

			@Override
			public void onMouseUp() { }

			@Override
			public void onMouseMoved(Vector2 prevPos) {
				boolean mouseEnteredBottomUI = prevPos.y > BOTTOM_UI_PANE_HEIGHT && renderer.getMousePos().y <= BOTTOM_UI_PANE_HEIGHT;
				boolean mouseExitedBottomUI = prevPos.y <= BOTTOM_UI_PANE_HEIGHT && renderer.getMousePos().y > BOTTOM_UI_PANE_HEIGHT;
				if (mouseEnteredBottomUI) { mouseLostTile = true; } // mouse will lose tile when it enters the bottom UI area
				if (mouseExitedBottomUI) { mouseLostTile = false; } // mouse will regain tile when it leaves
			}

		};
	}
	
	public Vector2 getLevelSpaceMousePositionOrNull(Level level) {
		return levelRenderer.getLevelSpaceMousePositionOrNull(level);
	}
	
	public Tile.Type getTileSelectionOrNull() {
		if (renderer.getMousePos() == null) { return null; }
		
		for (Tile t : toolPalette) {
			Rectangle bounds = new Rectangle(
					t.getPosition().x - TILE_UI_SIZE/2, t.getPosition().y - TILE_UI_SIZE/2 - PALETTE_FONT_VERTICAL_ALLOWANCE - PALETTE_TILE_MARGIN,
					t.getWidth() * WORLD_TO_UI_RATIO, t.getHeight() * WORLD_TO_UI_RATIO + PALETTE_FONT_VERTICAL_ALLOWANCE + PALETTE_TILE_MARGIN);
			
			if (bounds.contains(renderer.getMousePos())) {
				return t.getType();
			}
		}
		
		return null;
	}
	
	public boolean isMouseWithinKillAllButton() {
		return uiKillButtonBounds.contains(renderer.getMousePos());
	}
	
	
	
	private void useUITransform() {
		renderer.resetTransform();
	}
	
	
	private void calculateToolPaletteUI() {
		this.toolPalette = new ArrayList<Tile>();
		toolPalette.add(new Tile(Tile.Type.BLOCKER));
		toolPalette.add(new Tile(Tile.Type.JUMP_SINGLE));
		toolPalette.add(new Tile(Tile.Type.JUMP_DOUBLE));
		toolPalette.add(new Tile(Tile.Type.JUMP_LEFT));
		toolPalette.add(new Tile(Tile.Type.JUMP_RIGHT));
		
		int stepX = TILE_UI_SIZE + 10;
		int leftX = screenWidth/2 - bottomUiWidth/2 + TILE_UI_SIZE/2;
		int bottomY = BOTTOM_UI_PANE_HEIGHT/2 + PALETTE_FONT_VERTICAL_ALLOWANCE/2;
		
		for (int i = 0; i < toolPalette.size(); ++i) {
			toolPalette.get(i).setPosition(new Vector2(leftX + stepX*i, bottomY));
		}

		uiPaletteLeftX = (int) toolPalette.get(0).getPosition().x - TILE_UI_SIZE/2 - PALETTE_TILE_MARGIN;
		uiPaletteLeftY = (int) toolPalette.get(0).getPosition().y - TILE_UI_SIZE/2 - PALETTE_FONT_VERTICAL_ALLOWANCE - PALETTE_TILE_MARGIN;
		uiPaletteWidth = (int) (toolPalette.get(toolPalette.size()-1).getPosition().x + TILE_UI_SIZE/2 - uiPaletteLeftX + PALETTE_TILE_MARGIN);
		uiPaletteHeight = TILE_UI_SIZE + PALETTE_FONT_VERTICAL_ALLOWANCE + PALETTE_TILE_MARGIN*2;
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
	
	private void drawCursorAndDroppableTile(Tile.Type selectedTileType, Level level) {
		boolean isTileSelected = selectedTileType != null;
		if (!mouseLostTile && isTileSelected) {
			
			Vector2 levelSpaceCursorPos = levelRenderer.getLevelSpaceMousePositionOrNull(level);
			boolean isWithinLevel = levelSpaceCursorPos != null;
			
			Tile tile = new Tile(selectedTileType, levelSpaceCursorPos);
			boolean isTileDroppable = isWithinLevel && tile.isPositionViableForLevel(level);
			
			if (isTileDroppable) {
				levelRenderer.useLevelTransform(level);
				levelRenderer.drawTile(tile);
				useUITransform();
			}
			else {
				renderer.getBatch().setColor(1f, 1f, 1f, 0.7f);
				levelRenderer.drawTile(tile, renderer.getMousePos(), WORLD_TO_UI_RATIO);
				renderer.getBatch().setColor(1f, 1f, 1f, 1f);
			}
		}
	}
	
	private void drawToolPalette(Tile.Type selectedTileType) {
	
		renderer.drawRepeating(
				toolPaletteBackgroundTex,
				uiPaletteLeftX,
				uiPaletteLeftY,
				uiPaletteWidth,
				uiPaletteHeight);
		
		
		Tile.Type selectionType = getTileSelectionOrNull();
		
		for (Tile t : toolPalette) {
			int tileScreenWidth = t.getWidth() * WORLD_TO_UI_RATIO;
			int tileScreenHeight = t.getHeight() * WORLD_TO_UI_RATIO;
			Vector2 tilePos = t.getPosition();
			final int highlightPadding = PALETTE_TILE_MARGIN/2;
			
			if (t.getType() == selectionType || selectedTileType != null && t.getType() == selectedTileType) {
				int highlightLeftX = (int) (tilePos.x - tileScreenWidth/2 - highlightPadding);
				int highlightLeftY = (int) (tilePos.y - tileScreenHeight/2 - highlightPadding);
				int highlightWidth = tileScreenWidth + highlightPadding*2;
				int highlightHeight = tileScreenHeight + highlightPadding*2;
				renderer.drawRepeating(paletteHighlightBackgroundTex, highlightLeftX, highlightLeftY, highlightWidth, highlightHeight);
			}
			levelRenderer.drawTile(t, tilePos, WORLD_TO_UI_RATIO);
			
			float fontY = tilePos.y - tileScreenHeight/2 - PALETTE_FONT_VERTICAL_ALLOWANCE/2;
			inventoryFont.drawMultiLine(renderer.getBatch(), "x3", tilePos.x, fontY, 0, HAlignment.CENTER);
		}
	}
	
	private void drawBottomUI(World world) {
		renderer.drawRepeating(
				bottomUiBackgroundTex,
				0,
				0,
				screenWidth,
				BOTTOM_UI_PANE_HEIGHT);
		
		drawToolPalette(world.getSelectedDroppableTileType());
		
		// draw kill all button
		Vector2 killButtonCenter = new Vector2(uiKillButtonBounds.x + killAllButtonTex.getWidth()/2, uiKillButtonBounds.y + killAllButtonTex.getHeight()/2);
		Texture killButtonTex = this.isMouseWithinKillAllButton() && renderer.isMousePressed() ? this.killAllButtonPressedTex : this.killAllButtonTex;
		renderer.drawCentered(killButtonTex, killButtonCenter, killAllButtonTex.getWidth(), killAllButtonTex.getHeight());
		
		// draw the stats
		String numRescued = world.getNumRescued() + " Rescued";
		String numRequired = world.getLevel().getNumRescuedNeeded() + " Needed";
		int statsRightX = screenWidth/2 + bottomUiWidth/2;
		int fontY = (int) (BOTTOM_UI_PANE_HEIGHT/2 + statsFont.getLineHeight());
		int fudgeY = 3;
		statsFont.drawMultiLine(renderer.getBatch(), numRescued + "\n" + numRequired, statsRightX, fontY + fudgeY, 0, HAlignment.RIGHT);
	}
	
	public void drawWorld(World world) {
		renderer.drawRepeating(outOfBoundsBackgroundTex, 0, BOTTOM_UI_PANE_HEIGHT, screenWidth, screenHeight);
		Rectangle bounds = levelRenderer.getLevelBounds(world.getLevel());
		
		renderer.drawRepeating(
				mainBackgroundTex,
				(int)bounds.x - MAIN_BACKGROUND_MARGIN,
				(int)bounds.y - MAIN_BACKGROUND_MARGIN,
				(int)bounds.width + MAIN_BACKGROUND_MARGIN*2,
				(int)bounds.height + MAIN_BACKGROUND_MARGIN*2);
		
		levelRenderer.drawLevel(world.getLevel(), world.getToys());
		
		drawBottomUI(world);
		drawCursorAndDroppableTile(world.getSelectedDroppableTileType(), world.getLevel());
				
		
		//levelRenderer.drawPhysicsDebug(world);
	}
	
}
