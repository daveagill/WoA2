package week.of.awesome;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import com.badlogic.gdx.math.Vector2;

public class World {
	private Physics physics = new Physics();
	
	private Level level;
	
	private Collection<Toy> toys = new ArrayList<Toy>();
	
	private static float SPAWN_FREQ = 3;
	private float countDownToSpawn = 0;
	
	public World() {
		level = LevelLoader.getLevel(1);
	}
	
	public Level getLevel() {
		return level;
	}
	
	public Collection<Toy> getToys() {
		return Collections.unmodifiableCollection(toys);
	}
	
	public void update(float dt) {
		physics.update(dt);
		
		countDownToSpawn -= dt;
		
		if (countDownToSpawn < 0) {
			for (Tile startTile : level.getStarterTiles()) {
				Vector2 spawnPos = startTile.getWorldPosition();
				Toy toy = new Toy(Toy.Type.BALL, spawnPos, physics);
				toys.add(toy);
				System.out.println("Spawned: " + spawnPos);
			}
			
			countDownToSpawn = SPAWN_FREQ;
		}
	}
}
