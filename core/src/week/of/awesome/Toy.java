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
	private float horizontalSpeed = 0;
	private boolean isJumping = false;
	
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
		// only really landing if not landed on a jump
		if (isJumping) { return; }
		System.out.println("landed");
		body.setLinearVelocity(body.getLinearVelocity().x, 0);
		horizontalSpeed = STANDARD_HORIZONTAL_SPEED;
	}
	
	public void falling() {
	}
	
	public void jump(Vector2 velocity) {
		body.setLinearVelocity(body.getLinearVelocity().x, velocity.y);
		horizontalSpeed = velocity.x == 0 ? STANDARD_HORIZONTAL_SPEED : Math.abs(velocity.x);
		isJumping = true;
		System.out.println("jump");
	}
	
	public void update(float dt) {
		
		if (body.getLinearVelocity().y > 0) {
			body.setLinearVelocity(body.getLinearVelocity().x, 0);
		}
		
		boolean grounded = body.getLinearVelocity().y == 0.0f;
		System.out.println(body.getLinearVelocity().y);
		
		if (grounded) {
			float sign = getFacing() == Facing.RIGHT ? 1 : -1;
			
			while (Math.abs( body.getLinearVelocity().x ) < horizontalSpeed) {
				body.applyLinearImpulse(new Vector2(0.1f * sign, 0f), body.getWorldCenter(), true);
			}
			
			while (Math.abs( body.getLinearVelocity().x ) > horizontalSpeed) {
				body.applyLinearImpulse(new Vector2(-0.1f * sign, 0f), body.getWorldCenter(), true);
			}
		}
		
		// reset jumping state
		isJumping = false;
	}
}
