package week.of.awesome;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

public class Toy {
	public static enum Type {
		BALL,
		BEAR
	}
	
	private Type type;
	private Body body;
	
	
	public Toy(Type type, Vector2 position, Physics physics) {
		this.type = type;
		this.body = physics.createToyBody(position);
	}
	
	public Type getType() {
		return type;
	}
	
	public Vector2 getPosition() {
		return body.getPosition().cpy();
	}
}
