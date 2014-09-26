package week.of.awesome;

import week.of.awesome.Tile.Type;

import com.badlogic.gdx.math.Vector2;

public class Spawner {

	private Tile startTile;
	private int spawnNum = 10;
	private float spawnFreq = 0.5f;
	
	private float timer = 0;
	private float numSpawned = 0;
	
	public Spawner(Tile startTile) {
		this.startTile = startTile;
	}
	
	public Vector2 getPosition() {
		return startTile.getPosition();
	}
	
	public void configure(int spawnNum, float spawnFreq) {
		this.spawnNum = spawnNum;
		this.spawnFreq = spawnFreq;
	}
	
	public int getSpawnNum() {
		return spawnNum;
	}
	
	public float getSpawnFreq() {
		return spawnFreq;
	}
	
	public boolean isReadyForSpawn(float dt) {
		if (numSpawned >= spawnNum) { return false; }
		
		timer -= dt;
		if (timer <= 0) {
			timer = spawnFreq;
			++numSpawned;
			return true;
		}
		
		return false;
	}
}
