package week.of.awesome;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

public class Toy {
	public static final float TOY_SIZE = 0.4f;
	private static final float STANDARD_HORIZONTAL_SPEED = 2f;
	
	public static enum Type {
		BALL,
		BEAR
	}
	
	public static enum Facing {
		LEFT, RIGHT
	}
	
	private Type type;
	private Body body;
	private float horizontalVelocity = 0;
	private boolean landedAtLeastOnce = false;
	
	public Toy(Type type, Vector2 position, Physics physics) {
		this.type = type;
		this.body = physics.createToyBody(position, this);
	}
	
	public Type getType() {
		return type;
	}
	
	public Body getBody() {
		return body;
	}
	
	public Vector2 getPosition() {
		return body.getPosition().cpy();
	}
	
	public Facing getFacing() {
		return body.getLinearVelocity().x >= 0 ? Facing.RIGHT : Facing.LEFT;
	}
	
	public void landed() {
		landedAtLeastOnce = true;
	}
	
	public void jump(Vector2 jumpVelocity) {
		horizontalVelocity = jumpVelocity.x == 0 ? horizontalVelocity : jumpVelocity.x;
		body.setLinearVelocity(horizontalVelocity, jumpVelocity.y);
	}
	
	public void update(float dt) {
		// walk when grounded
		boolean grounded = body.getLinearVelocity().y == 0.0f;
		if (landedAtLeastOnce && grounded) {
			float sign = getFacing() == Facing.RIGHT ? 1 : -1;
			body.setLinearVelocity(STANDARD_HORIZONTAL_SPEED * sign, 0);
		}
		
		horizontalVelocity = body.getLinearVelocity().x;
	}
}
