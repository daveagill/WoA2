package week.of.awesome;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

public class World {
	private Physics physics = new Physics();
	
	private Level level;
	private Collection<Body> physicalStageBodies;
	
	private Collection<Toy> toys = new ArrayList<Toy>();
	
	private static float SPAWN_FREQ = 1000f;
	private float countDownToSpawn = 0;
	
	public World() {
		level = LevelLoader.getLevel(1);
		buildPhysicalStageFromLevel();
	}
	
	public com.badlogic.gdx.physics.box2d.World getB2d() {
		return physics.getB2d();
	}
	
	public void setCollisionListener(CollisionListener collisionListener) {
		physics.setCollisionListener(collisionListener);
	}
	
	public Level getLevel() {
		return level;
	}
	
	public Collection<Toy> getToys() {
		return Collections.unmodifiableCollection(toys);
	}
	
	public void removeToy(Toy toy) {
		toys.remove(toy);
		physics.killBody(toy.getBody());
	}
	
	public void update(float dt) {
		physics.update(dt);
		
		// spawn new toys
		countDownToSpawn -= dt;
		if (countDownToSpawn < 0) {
			for (Tile startTile : level.getStarterTiles()) {
				Vector2 spawnPos = startTile.getPosition().add(0, 0.5f);
				Toy toy = new Toy(Toy.Type.BALL, spawnPos, physics);
				toys.add(toy);
			}
			countDownToSpawn = SPAWN_FREQ;
		}
		
		// update toys
		for (Toy toy : toys) {
			toy.update(dt);
		}
	}
	
	private void buildPhysicalStageFromLevel() {
		physicalStageBodies = new ArrayList<Body>();
		
		for (int y = 0; y < level.getHeight(); ++y) {
			for (int x = 0; x < level.getWidth(); ++x) {
				Tile t = level.getTile(x, y);
				if (t == null) { continue; }
				
				Body tileBody = null;
				
				switch (t.getType()) {
					case BLOCK: tileBody = physics.createBlockTileBody(t.getPosition()); break;
					case START: break; // do nothing
					case GOAL: tileBody = physics.createGoalTileBody(t.getPosition()); break;
					case JUMP_SINGLE: // fallthrough
					case JUMP_DOUBLE: tileBody = physics.createJumpUpTileBody(t.getPosition(), t); break;
					case JUMP_LEFT: // fallthrough
					case JUMP_RIGHT: tileBody = physics.createCornerJumpTileBody(t.getPosition(), t); break;
				}
				
				if (tileBody != null) {
					tileBody.setUserData(t);
					physicalStageBodies.add(tileBody);
				}
			}
		}
	}
}
