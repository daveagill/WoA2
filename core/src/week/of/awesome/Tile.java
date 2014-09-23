package week.of.awesome;

import com.badlogic.gdx.math.Vector2;

public class Tile {
	public static int TILE_SIZE = 50;
	
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
	
	public Tile(Type type) {
		this.type = type;
	}
	
	public Type getType() { return type; }
	
	public void setWorldPosition(Vector2 position) {
		this.position = position;
	}
	
	public Vector2 getWorldPosition() {
		return position.cpy();
	}
}
