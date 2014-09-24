package week.of.awesome;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.WorldManifold;

public class GameplayController {
	
	private World world;
	
	public GameplayController(World world) {
		this.world = world;
		
		// install a collision listener that will direct collision events to this controller
		world.setCollisionListener(new CollisionListener(this));
	}

	public CollisionHandler createCollisionHandlerForAToB(Contact contact, Object a, Object b) {
		if (a instanceof Tile && b instanceof Toy) {
			Tile tile = (Tile)a;
			Toy toy = (Toy)b;
			
			if (tile.getType() == Tile.Type.BLOCK) {
				
				boolean vertical = contact.getWorldManifold().getNormal().y > 0.5f;
				if (vertical) {
					return new CollisionHandler() {
						public void onBegin() { toy.landed(); }
						//public void onEnd() { toy.falling(); }
					};
				}
			}
			
			if (tile.getType() == Tile.Type.GOAL) {
				return CollisionHandler.onBegin(() -> { world.removeToy(toy); });
			}
			
			if (tile.getType() == Tile.Type.JUMP_SINGLE) {
				
				return CollisionHandler.onBegin(() -> { toy.jump(new Vector2(0, 8f)); });
			}
			
			if (tile.getType() == Tile.Type.JUMP_DOUBLE) {
				return CollisionHandler.onBegin(() -> { toy.jump(new Vector2(0, 8f)); });
			}
			
			if (tile.getType() == Tile.Type.JUMP_LEFT) {
				return CollisionHandler.onBegin(() -> { toy.jump(new Vector2(-5f, 8f)); });
			}
			
			if (tile.getType() == Tile.Type.JUMP_RIGHT) {
				return CollisionHandler.onBegin(() -> { toy.jump(new Vector2(5f, 5f)); });
			}
		
		}
		
		return null;
	}
}
