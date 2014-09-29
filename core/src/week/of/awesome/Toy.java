package week.of.awesome;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

public class Toy {
	public static final float TOY_SIZE = 0.4f;
	private static final float STANDARD_HORIZONTAL_SPEED = 2f;
	private static final float VERTICAL_JUMP_HORIZONTAL_SPEED = 1.6f;
	
	public static enum Type {
		BALL,
		BEAR,
		SPINNER,
		TRAIN,
		DUCK
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
		float sign = getFacing() == Facing.RIGHT ? 1 : -1;
		horizontalVelocity = jumpVelocity.x == 0 ? VERTICAL_JUMP_HORIZONTAL_SPEED*sign : jumpVelocity.x;
		body.setLinearVelocity(horizontalVelocity, jumpVelocity.y);
	}
	
	public void update(float dt) {
		float verticalVelocity = body.getLinearVelocity().y;
		float sign = getFacing() == Facing.RIGHT ? 1 : -1;
		
		// walk when grounded
		boolean grounded = body.getLinearVelocity().y == 0.0f;
		if (landedAtLeastOnce && grounded) {
			horizontalVelocity = STANDARD_HORIZONTAL_SPEED * sign;
			verticalVelocity = 0;
			//body.setLinearVelocity(STANDARD_HORIZONTAL_SPEED * sign, 0);
		}
		
		boolean hasBouncedOffWall = Math.signum(body.getLinearVelocity().x) != Math.signum(horizontalVelocity);
		if (hasBouncedOffWall) {
			horizontalVelocity = STANDARD_HORIZONTAL_SPEED * sign;
		}
		
		//horizontalVelocity = body.getLinearVelocity().x;
		body.setLinearVelocity(Math.abs(horizontalVelocity) * sign, verticalVelocity);
	}
}
