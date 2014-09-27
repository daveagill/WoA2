package week.of.awesome;

import java.util.logging.Logger;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.utils.Disposable;

public class Physics implements Disposable {
	private com.badlogic.gdx.physics.box2d.World b2dSim = new com.badlogic.gdx.physics.box2d.World(new Vector2(0, -10), true);
	private CollisionEventDispatcher collisionListener;
	
	public Physics(CollisionHandlerFactory collisionHandlerFactory) {
		collisionListener = new CollisionEventDispatcher(collisionHandlerFactory);
		b2dSim.setContactListener(collisionListener);
	}

	public com.badlogic.gdx.physics.box2d.World getB2d() {
		return b2dSim;
	}

	public Body createToyBody(Vector2 position, Toy userData) {
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyDef.BodyType.DynamicBody;
		bodyDef.fixedRotation = true;
		bodyDef.position.set(position);
		
		Body body = b2dSim.createBody(bodyDef);
		body.setUserData(userData);
		
		CircleShape circle = new CircleShape();
		circle.setRadius(Toy.TOY_SIZE/2f);
		
		
		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = circle;
		fixtureDef.density = 1;
		fixtureDef.filter.groupIndex = -1; // negative, so never collide
		fixtureDef.friction = 0;
		fixtureDef.restitution = 1;
		
		body.createFixture(fixtureDef);
		
		return body;
	}
	
	public Body createBlockTileBody(Vector2 position) {
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyDef.BodyType.StaticBody;
		bodyDef.position.set(position);
		
		Body body = b2dSim.createBody(bodyDef);
		
		PolygonShape box = new PolygonShape();
		box.setAsBox(Tile.TILE_SIZE/2f, Tile.TILE_SIZE/2f);
		
		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = box;
		fixtureDef.friction = 0;
		fixtureDef.restitution = 1;
		
		body.createFixture(fixtureDef);
		
		return body;
	}
	
	public Body createGoalTileBody(Vector2 position) {
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyDef.BodyType.StaticBody;
		bodyDef.position.set(position);
		
		Body body = b2dSim.createBody(bodyDef);
		
		PolygonShape box = new PolygonShape();
		box.setAsBox(Tile.TILE_SIZE/5f, Tile.TILE_SIZE/2f);
		
		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = box;
		fixtureDef.isSensor = true;
		
		body.createFixture(fixtureDef);
		
		return body;
	}
	
	public Body createJumpUpTileBody(Vector2 position, Tile tile) {
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyDef.BodyType.StaticBody;
		bodyDef.position.set(position);
		
		Body body = b2dSim.createBody(bodyDef);
		body.setUserData(tile);
		
		float heightScale = 10f;
		PolygonShape box = new PolygonShape();
		box.setAsBox(Tile.TILE_SIZE/3f, Tile.TILE_SIZE/heightScale, new Vector2(0, -Tile.TILE_SIZE/2f + Tile.TILE_SIZE/heightScale), 0);
		
		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = box;
		fixtureDef.isSensor = true;
		
		body.createFixture(fixtureDef);
		
		return body;
	}
	
	public Body createCornerJumpTileBody(Vector2 position, Tile tile) {
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyDef.BodyType.StaticBody;
		bodyDef.position.set(position);
		
		Body body = b2dSim.createBody(bodyDef);
		body.setUserData(tile);
		
		float expand = Tile.TILE_SIZE/2f * 0.5f;
		PolygonShape triangle = new PolygonShape();
		if (tile.getType() == Tile.Type.JUMP_LEFT) {
			triangle.set(new Vector2[] {
					new Vector2(Tile.TILE_SIZE/2f, -Tile.TILE_SIZE/2f),
					new Vector2(Tile.TILE_SIZE/2f, expand),
					new Vector2(-expand, -Tile.TILE_SIZE/2f)
					
			});
		}
		else {
			triangle.set(new Vector2[] {
					new Vector2(-Tile.TILE_SIZE/2f, -Tile.TILE_SIZE/2f),
					new Vector2(expand, -Tile.TILE_SIZE/2f),
					new Vector2(-Tile.TILE_SIZE/2f, expand)
			});
		}
		
		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = triangle;
		fixtureDef.isSensor = true;
		
		body.createFixture(fixtureDef);
		
		return body;
	}
	
	public void update(float dt) {
		b2dSim.step(dt, 8, 3);
		if (collisionListener != null) {
			collisionListener.dispatchCollisionEvents();
		}
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
