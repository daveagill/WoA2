package week.of.awesome;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.badlogic.gdx.math.Vector2;

public class Level {
	private List<Tile> tiles;
	private List<Spawner> spawners = new ArrayList<Spawner>();
	private Collection<Tile> goalTiles = new ArrayList<Tile>();
	private String name;
	private int number;
	private int numRescuedNeeded = 5;
	private int width;
	private int height;
	
	
	public Level(int number, String name, List<List<Tile>> tileGrid, int numRescuedNeeded) {
		this.number = number;
		this.name = name;
		this.numRescuedNeeded = numRescuedNeeded;
		
		width = tileGrid.get(0).size();
		height = tileGrid.size();
		tiles = new ArrayList<Tile>(width*height);
		
		// need to init up to full size
		for (int i = 0; i < width * height; ++i) {
			tiles.add(null);
		}
		
		// add tiles to the level
		int y = height-1;
		for (List<Tile> row : tileGrid) {
			int x = 0;
			for (Tile tile : row) {
				setTile(tile, x, y);
				++x;
			}
			--y;
		}
	}
	
	
	public String getName() { return name; }
	public int getNumber() { return number; }
	public int getNumRescuedNeeded() { return numRescuedNeeded; }
	public int getWidth() { return width; }
	public int getHeight() { return height; }
	
	public void setTile(Tile tile, int x, int y) {
		tiles.set(index(x, y), tile);
		
		if (tile == null) { return; }
		
		tile.setPosition(new Vector2(x, y));
		
		if (tile.getType().equals(Tile.Type.START)) {
			spawners.add(new Spawner(tile));
		}
		else if (tile.getType().equals(Tile.Type.GOAL)) {
			goalTiles.add(tile);
		}
	}
	
	public Tile getTile(int x, int y) {
		if (x < 0 || y < 0 || x >= width || y >= height) { return null; }
		return tiles.get(index(x, y));
	}
	
	public List<Spawner> getSpawners() {
		return spawners;
	}
	
	public Collection<Tile> getGoalTiles() {
		return goalTiles;
	}
	
	private int index(int x, int y) {
		return y * width + x;
	}
}
