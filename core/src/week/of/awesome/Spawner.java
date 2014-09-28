package week.of.awesome;

import com.badlogic.gdx.math.Vector2;

public class Spawner {

	private Tile startTile;
	private int spawnAllocation = 10;
	private float spawnFreq = 0.5f;
	
	private float timer = 0;
	private int numSpawned = 0;
	
	private float timerToFirstSpawn = 2f;
	
	public Spawner(Tile startTile) {
		this.startTile = startTile;
	}
	
	public Vector2 getPosition() {
		return startTile.getPosition();
	}
	
	public void configure(float timeToFirstSpawn, int spawnNum, float spawnFreq) {
		this.timerToFirstSpawn = timeToFirstSpawn;
		this.spawnAllocation = spawnNum;
		this.spawnFreq = spawnFreq;
	}
	
	public int getSpawnAllocation() {
		return spawnAllocation;
	}
	
	public float getSpawnFreq() {
		return spawnFreq;
	}
	
	public float getTimeToFirstSpawn() {
		return timerToFirstSpawn;
	}
	
	public int getSpawnRemaining() {
		return spawnAllocation - numSpawned;
	}
	
	public boolean isReadyForSpawn(float dt) {
		if (getSpawnRemaining() <= 0) { return false; }
		
		if (timerToFirstSpawn > 0f) {
			timerToFirstSpawn = Math.max(0f, timerToFirstSpawn -= dt);
			return false;
		}
		
		timer -= dt;
		if (timer <= 0) {
			timer = spawnFreq;
			++numSpawned;
			return true;
		}
		
		return false;
	}
}
