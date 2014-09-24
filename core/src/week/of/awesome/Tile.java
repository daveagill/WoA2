package week.of.awesome;

import com.badlogic.gdx.math.Vector2;

public class Tile {
	public static int TILE_SIZE = 1;
	
	public static enum Type {
		BLOCK,
		START,
		GOAL,
		JUMP_SINGLE,
		JUMP_DOUBLE,
		JUMP_LEFT,
		JUMP_RIGHT
	}
	
	private Type type;
	private Vector2 position;
	
	public Tile(Type type, Vector2 position) {
		this.type = type;
		this.position = position;
	}
	
	public Tile(Type type) {
		this(type, new Vector2());
	}
	
	public Type getType() { return type; }
	
	public void setPosition(Vector2 position) {
		this.position = position;
	}
	
	public Vector2 getPosition() {
		return position.cpy();
	}
	
	public int getWidth() {
		if (type == Type.START) { return TILE_SIZE * 2; }
		return TILE_SIZE;
	}
	
	public int getHeight() {
		if (type == Type.START) { return TILE_SIZE * 2; }
		return TILE_SIZE;
	}
}
