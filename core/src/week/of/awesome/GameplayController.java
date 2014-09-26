package week.of.awesome;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.WorldManifold;

public class GameplayController {
	
	private World world;
	private Renderer renderer;
	private BackgroundMusic bgMusic;
	
	public GameplayController(World world, Renderer renderer, BackgroundMusic bgMusic) {
		this.world = world;
		this.renderer = renderer;
		this.bgMusic = bgMusic;
		
		// install a collision listener that will direct collision events to this controller
		world.setCollisionListener(new CollisionListener(this));
		
		bgMusic.playForLevel(0);
	}
	
	public void update(float dt) {
		world.update(dt, this);
		bgMusic.updateFade(dt);
	}
	
	public void levelComplete() {
		bgMusic.playForLevel(world.getLevel().getNumber()+1);
	}
	
	public void mouseMove(Vector2 screenPos) {
		renderer.notifyMouseMove(screenPos);
	}
	
	public void mouseDown(Vector2 screenPos) {
		renderer.notifyMouseDown();
	}
	
	public void mouseUp(Vector2 screenPos) {
		renderer.notifyMouseUp();
		
		Vector2 levelSpacePos = renderer.convertToLevelSpaceOrNull(screenPos, world.getLevel());
		boolean isWithinLevel = levelSpacePos != null;
		
		if (isWithinLevel) {
			world.confirmDroppableTile(levelSpacePos);
		}
		else {
			world.selectDroppableTile( renderer.getTileSelectionOrNull() );
		}
		
		
		
		if (renderer.isMouseWithinKillAllButton()) {
			world.killRemainingToys();
		}
	}

	public CollisionHandler createCollisionHandlerForAToB(Contact contact, Object a, Object b) {
		if (a instanceof Tile && b instanceof Toy) {
			Tile tile = (Tile)a;
			Toy toy = (Toy)b;
			
			if (tile.getType() == Tile.Type.GROUND) {
				boolean vertical = contact.getWorldManifold().getNormal().y > 0.5f;
				boolean falling = toy.getBody().getLinearVelocity().y < 0;
				if (vertical && falling) {
					return CollisionHandler.onPreSolve(() -> {
						contact.setRestitution(0); // prevent any bouncing
						toy.landed();
					});
				}
			}
			
			if (tile.getType() == Tile.Type.GOAL) {
				return CollisionHandler.onBegin(() -> { world.rescueToy(toy); });
			}
			
			if (tile.getType() == Tile.Type.JUMP_SINGLE) {
				
				return CollisionHandler.onBegin(() -> { toy.jump(new Vector2(0, 5f)); });
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
