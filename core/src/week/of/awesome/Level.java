package week.of.awesome;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.badlogic.gdx.math.Vector2;

public class Level {
	private List<Tile> tiles;
	private Collection<Tile> starterTiles = new ArrayList<Tile>();
	private Collection<Tile> goalTiles = new ArrayList<Tile>();
	private String name;
	private int width;
	private int height;
	
	
	public Level(String name, int width, int height) {
		this.name = name;
		this.width = width;
		this.height = height;
		tiles = new ArrayList<Tile>(width*height);
		
		// initialise to an empty map
		for (int i = 0; i < width * height; ++i) {
			tiles.add(null);
		}
	}
	
	
	public String getName() { return name; }
	public int getWidth() { return width; }
	public int getHeight() { return height; }
	
	public void setTile(Tile tile, int x, int y) {
		tiles.set(index(x, y), tile);
		
		if (tile == null) { return; }
		
		tile.setPosition(new Vector2(x, y));
		
		if (tile.getType().equals(Tile.Type.START)) {
			starterTiles.add(tile);
		}
		else if (tile.getType().equals(Tile.Type.GOAL)) {
			goalTiles.add(tile);
		}
	}
	
	public Tile getTile(int x, int y) {
		return tiles.get(index(x, y));
	}
	
	public Collection<Tile> getStarterTiles() {
		return starterTiles;
	}
	
	public Collection<Tile> getGoalTiles() {
		return goalTiles;
	}
	
	private int index(int x, int y) {
		return y * width + x;
	}
}
