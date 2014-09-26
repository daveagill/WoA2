package week.of.awesome;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.utils.Disposable;

public class World implements Disposable {
	private Physics physics = new Physics();
	
	private Level level;
	private Collection<Body> physicalStageBodies = new ArrayList<Body>();
	
	private Tile.Type droppableTileType = null;
	
	private Collection<Toy> toys = new ArrayList<Toy>();
	
	private int numRescued = 0;
	
	public World() {
		beginLevel(1);
	}
	
	public com.badlogic.gdx.physics.box2d.World getB2d() {
		return physics.getB2d();
	}
	
	public void setCollisionListener(CollisionListener collisionListener) {
		physics.setCollisionListener(collisionListener);
	}
	
	public boolean gameCompleted() {
		return level == null;
	}
	
	public int getNumRescued() {
		return numRescued;
	}
	
	public Level getLevel() {
		return level;
	}
	
	public Collection<Toy> getToys() {
		return Collections.unmodifiableCollection(toys);
	}
	
	public void rescueToy(Toy toy) {
		toys.remove(toy);
		physics.killBody(toy.getBody());
		++numRescued;
	}
	
	public void killRemainingToys() {
		for (Toy t : toys) {
			physics.killBody(t.getBody());
		}
		toys.clear();
	}
	
	public void confirmDroppableTile(Vector2 position) {
		if (droppableTileType == null || position == null) { return; } // nothing to do
		
		Tile tile = new Tile(droppableTileType, position);
		
		// drop the tile if viable
		if (tile.isPositionViableForLevel(level)) {
			Vector2 tilePos = tile.getPosition();
			level.setTile(tile, (int)tilePos.x, (int)tilePos.y);
			addTilePhysically(tile);
		}
	}
	
	public void selectDroppableTile(Tile.Type tileType) {
		// select the type, deselect if it happens to be the same type
		this.droppableTileType = this.droppableTileType == tileType ? null : tileType;
	}
	
	public Tile.Type getSelectedDroppableTileType() {
		return droppableTileType;
	}
	
	public void update(float dt, GameplayController controller) {		
		physics.update(dt);
		
		// spawn new toys
		for (Spawner spawner : level.getSpawners()) {
			if (spawner.isReadyForSpawn(dt)) {
				Vector2 spawnPos = spawner.getPosition().add(0, 0.5f);
				Toy toy = new Toy(Toy.Type.BALL, spawnPos, physics);
				toys.add(toy);
			}
		}
		
		// update toys
		for (Toy toy : toys) {
			toy.update(dt);
		}
		
		if (toys.isEmpty() && numRescued >= level.getNumRescuedNeeded()) {
			controller.levelComplete();
			beginLevel(level.getNumber() + 1);
		}
	}
	
	private void beginLevel(int levelNum) {
		clean();
		
		if (LevelLoader.hasLevel(levelNum)) {
			// create the next level
			level = LevelLoader.getLevel(levelNum);
			buildPhysicalStageFromLevel();
		}
	}
	
	private void buildPhysicalStageFromLevel() {
		physicalStageBodies = new ArrayList<Body>();
		
		for (int y = 0; y < level.getHeight(); ++y) {
			for (int x = 0; x < level.getWidth(); ++x) {
				Tile t = level.getTile(x, y);
				if (t == null) { continue; }
				addTilePhysically(t);
			}
		}
	}
	
	private void addTilePhysically(Tile t) {
		Body tileBody = null;
		
		switch (t.getType()) {
			case BLOCKER: // fallthrough...
			case GROUND: tileBody = physics.createBlockTileBody(t.getPosition()); break;
			case START: break; // do nothing
			case GOAL: tileBody = physics.createGoalTileBody(t.getPosition()); break;
			case JUMP_SINGLE: // fallthrough...
			case JUMP_DOUBLE: tileBody = physics.createJumpUpTileBody(t.getPosition(), t); break;
			case JUMP_LEFT: // fallthrough...
			case JUMP_RIGHT: tileBody = physics.createCornerJumpTileBody(t.getPosition(), t); break;
		}
		
		if (tileBody != null) {
			tileBody.setUserData(t);
			physicalStageBodies.add(tileBody);
		}
	}

	
	private void clean() {
		// cleanup any previous physical stage
		for (Body b : physicalStageBodies) {
			physics.killBody(b);
		}
		physicalStageBodies.clear();
		
		// cleanup any toys
		killRemainingToys();
		
		// cleanup other state
		this.droppableTileType = null;
		this.numRescued = 0;
		this.level = null;
	}

	@Override
	public void dispose() {
		clean();
		physics.dispose();
	}
}
