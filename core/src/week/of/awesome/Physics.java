package week.of.awesome;

import java.util.logging.Logger;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.utils.Disposable;

public class Physics implements Disposable {
	private com.badlogic.gdx.physics.box2d.World b2dSim = new com.badlogic.gdx.physics.box2d.World(new Vector2(0, -10), true);


	public Body createToyBody(Vector2 position) {
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyDef.BodyType.DynamicBody;
		bodyDef.fixedRotation = true;
		bodyDef.position.set(position);
		
		Body body = b2dSim.createBody(bodyDef);
		
		CircleShape circle = new CircleShape();
		circle.setRadius(1);
		
		
		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = circle;
		fixtureDef.density = 1;
		
		body.createFixture(fixtureDef);
		
		return body;
	}
	
	public void update(float dt) {
		b2dSim.step(dt, 8, 3);
	}
	
	public void killBody(Body body) {
		b2dSim.destroyBody(body);
	}
	
	@Override
	public void dispose() {
		if (b2dSim.getBodyCount() > 0) {
			Logger.getGlobal().warning(b2dSim.getBodyCount() + " bodies were not correctly released by disposing the b2d world.");
		}
		b2dSim.dispose();
	}
}
