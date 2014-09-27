package week.of.awesome;

import java.util.Collection;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;

public class LevelRenderer {
	public static final int WORLD_TO_SCREEN_RATIO = 40;
	
	private static final int TILE_SCREEN_SIZE = Tile.TILE_SIZE * WORLD_TO_SCREEN_RATIO;
	
	private BasicRenderer renderer;
	private Box2DDebugRenderer b2dDebug;
	
	private int stageMidX;
	private int stageMidY;
	
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
	
	public LevelRenderer(BasicRenderer renderer) {
		this.renderer = renderer;
		b2dDebug = new Box2DDebugRenderer();
		
		this.groundTex = renderer.newTexture("ground.png");
		this.startTex = renderer.newTexture("start.png");
		this.goalTex = renderer.newTexture("goal.png");
		this.jumpSingleTex = renderer.newTexture("jumpSingle.png");
		this.jumpDoubleTex = renderer.newTexture("jumpDouble.png");
		this.jumpLeftTex = renderer.newTexture("jumpLeft.png");
		this.jumpRightTex = renderer.newTexture("jumpRight.png");
		this.blockerTex = renderer.newTexture("blocker.png");
		
		this.ballTex = renderer.newTexture("ball.png");
		this.bearTex = renderer.newTexture("bear.png");
	}
	
	public void setMapCenterPos(int stageMidX, int stageMidY) {
		this.stageMidX = stageMidX;
		this.stageMidY = stageMidY;
	}
	
	public Vector2 getLevelSpaceMousePositionOrNull(Level level) {
		Rectangle bounds = getLevelBounds(level);
		
		Vector2 cursorPos = renderer.getMousePos();
		
		// n.b. contains() checks an open interval, whereas we want a half open interval, hence the checks at the far end of the bounds
		if (!bounds.contains(cursorPos) ||
			bounds.x + bounds.width == cursorPos.x || bounds.y + bounds.height == cursorPos.y) {
				return null;
		}
		
		Vector2 relativePos = cursorPos.cpy().sub(bounds.getPosition(new Vector2()));
		Vector2 posInTileSpace = relativePos.scl(1f / (TILE_SCREEN_SIZE));
		
		return posInTileSpace;
	}
	
	public Rectangle getLevelBounds(Level level) {
		int levelWidth = level.getWidth() * WORLD_TO_SCREEN_RATIO;
		int levelHeight = level.getHeight() * WORLD_TO_SCREEN_RATIO;
		int stageX = stageMidX - levelWidth / 2;
		int stageY = stageMidY - levelHeight / 2;
		
		return new Rectangle(stageX, stageY, levelWidth, levelHeight);
	}
	
	public void useLevelTransform(Level level) {
		Rectangle bounds = getLevelBounds(level);
		
		// set the global translation for all things to be rendered, so that 0,0 is the bottom left of the actual level, rather than the screen
		renderer.getBatch().setTransformMatrix(new Matrix4().translate(bounds.x + WORLD_TO_SCREEN_RATIO/2, bounds.y + WORLD_TO_SCREEN_RATIO/2, 0).scale(WORLD_TO_SCREEN_RATIO, WORLD_TO_SCREEN_RATIO, 1));

	}
	
	public void drawLevel(Level map, Collection<Toy> toys) {
		useLevelTransform(map);
		drawStage(map);
		drawToys(toys);
		renderer.resetTransform();
	}
	
	public void drawPhysicsDebug(World world) {
		useLevelTransform(world.getLevel());
		Matrix4 combined = renderer.getBatch().getProjectionMatrix().cpy().mul(renderer.getBatch().getTransformMatrix());
		b2dDebug.render(world.getB2d(), combined);
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
			renderer.drawCentered(bearTex, pos, Toy.TOY_SIZE + 0.1f, Toy.TOY_SIZE + 0.1f);
		}
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
	
	public void drawTile(Tile tile) {
		Vector2 pos = tile.getPosition();
		drawTile(tile, new Vector2((int)pos.x, (int)pos.y), 1f);
	}
	
	public void drawTile(Tile tile, Vector2 position, float scale) {
		Texture texture = lookupTextureForTile(tile);
		renderer.drawCentered(texture, position, tile.getWidth() * scale, tile.getHeight() * scale);
	}
	
}
