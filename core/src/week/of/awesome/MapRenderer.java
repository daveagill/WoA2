package week.of.awesome;

import java.util.Collection;
import java.util.HashMap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;

public class MapRenderer {
	public static final int WORLD_TO_SCREEN_RATIO = 40;
	
	private static final int TILE_SCREEN_SIZE = Tile.TILE_SIZE * WORLD_TO_SCREEN_RATIO;
	
	private BasicRenderer renderer;
	private Box2DDebugRenderer b2dDebug;
	
	private int stageMidX;
	private int stageMidY;
	
	// map tiles
	private Texture groundTex_walkable_mid;
	private Texture groundTex_walkable_rightCorner;
	private Texture groundTex_walkable_rightLedge;
	private Texture groundTex_walkable_leftCorner;
	private Texture groundTex_walkable_leftLedge;
	private Texture groundTex_walkable_leftAndRightCorner;
	private Texture groundTex_nonWalkable;
	
	private Texture startTex;
	private Texture startClosedTex;
	private Texture goalTex;
	private Texture killerTex;
	private Texture keyTex;
	private Texture lockTex;
	
	// tool tiles
	private Texture jumpSingleTex;
	private Texture jumpDoubleTex;
	private Texture jumpLeftTex;
	private Texture jumpRightTex;
	private Texture blockerTex;
	
	// toys
	private Animation bearAnim;
	private Animation ballAnim;
	private Animation spinningTopAnim;
	private Animation trainAnim;
	private Animation duckAnim;
	
	private float animTime = 0f;
	
	public MapRenderer(BasicRenderer renderer) {
		this.renderer = renderer;
		b2dDebug = new Box2DDebugRenderer();
		

		this.groundTex_walkable_mid = renderer.newTexture("maps/themed/grassy/ground_walkable_mid.png");
		this.groundTex_walkable_rightCorner = renderer.newTexture("maps/themed/grassy/ground_walkable_rightCorner.png");
		this.groundTex_walkable_rightLedge = renderer.newTexture("maps/themed/grassy/ground_walkable_rightLedge.png");
		this.groundTex_walkable_leftCorner = renderer.newTexture("maps/themed/grassy/ground_walkable_leftCorner.png");
		this.groundTex_walkable_leftLedge = renderer.newTexture("maps/themed/grassy/ground_walkable_leftLedge.png");
		this.groundTex_walkable_leftAndRightCorner = renderer.newTexture("maps/themed/grassy/ground_walkable_leftAndRightCorner.png");
		this.groundTex_nonWalkable = renderer.newTexture("maps/themed/grassy/ground_nonWalkable.png");
		
		this.startTex = renderer.newTexture("maps/start.png");
		this.startClosedTex = renderer.newTexture("maps/startClosed.png");
		this.goalTex = renderer.newTexture("maps/goal.png");
		this.killerTex = renderer.newTexture("maps/killer.png");
		this.lockTex = renderer.newTexture("maps/lock.png");
		this.keyTex = renderer.newTexture("maps/key.png");
		
		this.jumpSingleTex = renderer.newTexture("tools/jumpSingle.png");
		this.jumpDoubleTex = renderer.newTexture("tools/jumpDouble.png");
		this.jumpLeftTex = renderer.newTexture("tools/jumpLeft.png");
		this.jumpRightTex = renderer.newTexture("tools/jumpRight.png");
		this.blockerTex = renderer.newTexture("tools/blocker.png");
		
		this.bearAnim = renderer.newAnimation("toys/bear", 2);
		this.ballAnim = renderer.newAnimation("toys/ball", 4);
		this.spinningTopAnim = renderer.newAnimation("toys/spinner", 4);
		this.trainAnim = renderer.newAnimation("toys/train", 3);
		this.duckAnim = renderer.newAnimation("toys/duck", 2);
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
	
	public void drawLevel(Level map, Collection<Toy> toys, boolean drawMap, float dt) {
		animTime += dt;
		useLevelTransform(map);
		if (drawMap) { drawStage(map); }
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
					drawTile(level, t);
				}
			}
		}
	}
	
	private void drawToys(Collection<Toy> toys) {
		for (Toy toy : toys) {
			Vector2 pos = toy.getPosition();
			boolean flipX = toy.getFacing() == Toy.Facing.LEFT;
			Animation anim = lookupAnimationForToy(toy);
			TextureRegion t = anim.getKeyFrame(animTime);
			pos.y = pos.y - Toy.TOY_SIZE/2f + (t.getRegionHeight()/2f)/WORLD_TO_SCREEN_RATIO;
			renderer.drawCentered(t, pos, (float)t.getRegionWidth() / WORLD_TO_SCREEN_RATIO, (float)t.getRegionHeight() / WORLD_TO_SCREEN_RATIO, flipX);
		}
	}
	
	private Animation lookupAnimationForToy(Toy toy) {
		switch (toy.getType()) {
			case BALL: return ballAnim;
			case BEAR: return bearAnim;
			case SPINNER: return spinningTopAnim;
			case TRAIN: return trainAnim;
			case DUCK: return duckAnim;
		}
		
		throw new RuntimeException("No such toy type: " + toy.getType());
	}
	
	private Texture lookupTextureForTile(Level level, Tile tile) {
		if (tile.getType() == Tile.Type.GROUND) {
			int x = (int) tile.getPosition().x;
			int y = (int) tile.getPosition().y;
			
			// classify tile type
			boolean top = isTileAt(level, Tile.Type.GROUND, x, y+1);
			boolean right = isTileAt(level, Tile.Type.GROUND, x+1, y);
			boolean bottom = isTileAt(level, Tile.Type.GROUND, x, y-1);
			boolean left = isTileAt(level, Tile.Type.GROUND, x-1, y);
			boolean isUnderKiller = isTileAt(level, Tile.Type.KILLER, x, y+1);
			
			if (isUnderKiller) {
				return groundTex_walkable_mid;
			}
			
			if (!top && !right && !left && !bottom) {
				return groundTex_nonWalkable;
			}
			
			if (!top && right && left) { 
				return groundTex_walkable_mid;
			}
			
			if (!top && !right && !left) {
				return groundTex_walkable_leftAndRightCorner;
			}
			
			if (!top && !right) {
				if (bottom) { return groundTex_walkable_rightCorner; }
				else        { return groundTex_walkable_rightLedge;  }
			}

			if (!top && !left) {
				if (bottom) { return groundTex_walkable_leftCorner; }
				else        { return groundTex_walkable_leftLedge;  }
			}
			
			return groundTex_nonWalkable;
		}
		
		switch (tile.getType()) {
			case GROUND:      throw new RuntimeException("Ground tiles should be handled already!");
			case START:       return level.getSpawnRemaining() == 0 ? startClosedTex : startTex;
			case GOAL:        return goalTex;
			case JUMP_SINGLE: return jumpSingleTex;
			case JUMP_DOUBLE: return jumpDoubleTex;
			case JUMP_LEFT:   return jumpLeftTex;
			case JUMP_RIGHT:  return jumpRightTex;
			case BLOCKER:     return blockerTex;
			case KILLER:      return killerTex;
			case LOCK:        return lockTex;
			case KEY:         return keyTex;
		}
	
		throw new RuntimeException("No such tile type: " + tile.getType());
	}
	
	public void drawTile(Level level, Tile tile) {
		Vector2 pos = tile.getPosition();
		drawTile(level, tile, new Vector2((int)pos.x, (int)pos.y), 1f);
	}
	
	public void drawTile(Level level, Tile tile, Vector2 position, float scale) {
		Texture texture = lookupTextureForTile(level, tile);
		renderer.drawCentered(texture, position, tile.getWidth() * scale, tile.getHeight() * scale);
	}
	
	private boolean isTileAt(Level level, Tile.Type type, int x, int y) {
		Tile t = level.getTile(x, y);
		if (t == null) { return false; }
		return t.getType() == type;
	}
}
