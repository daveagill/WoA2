package week.of.awesome;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.utils.Disposable;

public class World implements Disposable {
	private static final float LEVEL_FADE_OUT_DURATION = 0.5f;
	private static final float LEVEL_FADE_IN_DURATION = 1f;
	private static final float LEVEL_PRE_LEVEL_WAIT = 2f;
	
	private Random random = new Random(System.currentTimeMillis());
	
	private Physics physics;
	private WorldEvents worldEvents;
	
	private boolean levelStartedSignalled = false;
	private Level level;
	private List<Body> physicalStageBodies = new ArrayList<Body>();
	
	private Tile.Type droppableTileType = null;
	
	private Collection<Toy> toys = new ArrayList<Toy>();
	private Collection<Toy> dyingToys = new ArrayList<Toy>();
	
	private int numRescued = 0;
	
	private Float transitionPercent = null;
	private Float preTransitionPausePercent = null;
	
	public World() {
		// install a CollisionHandler-factory to respond to physical events
		physics = new Physics(getCollisionHandlerFactory());
	}
	
	private CollisionHandlerFactory getCollisionHandlerFactory() {
		return new CollisionHandlerFactory() {
			public CollisionHandler createCollisionHandlerForAToB(Contact contact, Object a, Object b) {
				if (a instanceof Tile && b instanceof Toy) {
					Tile tile = (Tile)a;
					Toy toy = (Toy)b;
					
					if (tile.getType() == Tile.Type.GROUND ||
					    tile.getType() == Tile.Type.BLOCKER ||
					    tile.getType() == Tile.Type.LOCK) {
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
						return CollisionHandler.onBegin(() -> {
							rescueToy(toy);
							worldEvents.onRescue();
						});
					}
					
					if (tile.getType() == Tile.Type.JUMP_SINGLE) {
						
						return CollisionHandler.onBegin(() -> {
							toy.jump(new Vector2(0, 7f));
							worldEvents.onJump();
						});
					}
					
					if (tile.getType() == Tile.Type.JUMP_DOUBLE) {
						return CollisionHandler.onBegin(() -> {
							toy.jump(new Vector2(0, 10f));
							worldEvents.onJump();
						});
					}
					
					if (tile.getType() == Tile.Type.JUMP_LEFT) {
						return CollisionHandler.onBegin(() -> {
							toy.jump(new Vector2(-6f, 5f));
							worldEvents.onJump();
						});
					}
					
					if (tile.getType() == Tile.Type.JUMP_RIGHT) {
						return CollisionHandler.onBegin(() -> {
							toy.jump(new Vector2(6f, 5f));
							worldEvents.onJump();
						});
					}
					
					if (tile.getType() == Tile.Type.KILLER) {
						return CollisionHandler.onBegin(() -> {
							killToy(toy);
							worldEvents.onToyDeath();
						});
					}
				
					if (tile.getType() == Tile.Type.KEY) {

						// remove the bodies
						removeTilePhysically(tile);
						for (Tile lock : level.getLockTiles()) {
							removeTilePhysically(lock);
						}
						
						level.setTile(null, (int)tile.getPosition().x, (int)tile.getPosition().y);
						level.removeLockTiles();
						worldEvents.onDoorUnlocked();
					}
				}
				
				return null;
			}
		};
	}

	public com.badlogic.gdx.physics.box2d.World getB2d() {
		return physics.getB2d();
	}
	
	public boolean gameCompleted() {
		return level == null;
	}
	
	public int getNumRescued() {
		return numRescued;
	}
	
	public Level getLevel() {
		return level;
	}
	
	public Float getTransitionPercent() { return transitionPercent; }
	public boolean levelNotStartedYet() { return numRescued == 0 && transitionPercent != null && level.hasSpawnRemaining(); }
	
	public Collection<Toy> getToys() {
		return Collections.unmodifiableCollection(toys);
	}
	
	public void killRemainingToys() {
		for (Toy t : toys) {
			killToy(t);
		}
		
		// also need to kill as-yet-unspawned toys
		for (Spawner spawner : level.getSpawners()) {
			spawner.configure(0, 0, 0);
		}
	}
	
	public void confirmDroppableTile(Vector2 position) {
		if (droppableTileType == null || position == null) { return; } // nothing to do
		
		Tile tile = new Tile(droppableTileType, position);
		
		// drop the tile if viable
		if (tile.isPositionViableForLevel(level)) {
			Vector2 tilePos = tile.getPosition();
			level.setTile(tile, (int)tilePos.x, (int)tilePos.y);
			addTilePhysically(tile);
			level.getInventory().useItem(droppableTileType);
			
			// deselect the tile
			this.droppableTileType = null;
		}
	}
	
	public void selectDroppableTile(Tile.Type tileType) {
		// check that we have at least 1 of this tile left
		if (level.getInventory().getNumAvailable(tileType) >= 1) {
			// select the type, deselect if it happens to be the same type
			this.droppableTileType = this.droppableTileType == tileType ? null : tileType;
		}
	}
	
	public Tile.Type getSelectedDroppableTileType() {
		return droppableTileType;
	}
	
	public void update(float dt, WorldEvents worldEvents) {
		// have to fade in before the level starts
		if (levelNotStartedYet()) {
			boolean levelTextFinished = preTransitionPausePercent == null;
			if (levelTextFinished) {
				transitionPercent = Math.min(1f, transitionPercent - (dt/LEVEL_FADE_IN_DURATION));
				if (transitionPercent <= 0f) {
					transitionPercent = null;
				} else {
					return;
				}
			}
			else {
				preTransitionPausePercent = Math.min(1f, preTransitionPausePercent + (dt/LEVEL_PRE_LEVEL_WAIT));
				if (preTransitionPausePercent >= 1f) {
					preTransitionPausePercent = null;
				} else {
					return;
				}
			}
		}
		
		if (!levelStartedSignalled) {
			levelStartedSignalled = true;
			worldEvents.onLevelStart(level.getNumber());
		}
		
		this.worldEvents = worldEvents; // stash this here for the benefit of physics events
		physics.update(dt);
		
		// spawn new toys
		for (Spawner spawner : level.getSpawners()) {
			if (spawner.isReadyForSpawn(dt)) {
				Vector2 spawnPos = spawner.getPosition();
				
				// select a random toy
				Toy.Type toyType = Toy.Type.values()[ random.nextInt(Toy.Type.values().length) ];
				
				Toy toy = new Toy(toyType, spawnPos, physics);
				toys.add(toy);
				worldEvents.onToySpawn();
			}
		}
		
		// update toys
		for (Toy toy : toys) {
			toy.update(dt);
			Vector2 toyPos = toy.getPosition();
			if (toyPos.x < -5 || toyPos.x > level.getWidth()+5 || toyPos.y < -5) {
				killToy(toy);
				worldEvents.onToyDeath();
			}
		}
		killDyingToys();
		
		
		// check if the level won/lost and if so begin a timeout
		boolean levelFinished = toys.isEmpty() && dyingToys.isEmpty() && !level.hasSpawnRemaining();
		if (levelFinished) {
			if (transitionPercent == null) {
				transitionPercent = 0f;
			} else {
				if (transitionPercent >= 1f) {
					boolean isWon = numRescued >= level.getNumRescuedNeeded();
					if (isWon) { worldEvents.onLevelComplete(level.getNumber()); }
					else       { worldEvents.onLevelFailed(level.getNumber());   }
				}
				else {
					transitionPercent = Math.min(1f, transitionPercent + (dt/LEVEL_FADE_OUT_DURATION));
				}
			}
		}
	}
	
	public void beginLevel(Level level, boolean startImmediately) {
		clean();
		this.level = level;
		this.levelStartedSignalled = false;
		if (level == null) { return; }
		
		buildPhysicalStageFromLevel();
		
		if (startImmediately) {
			transitionPercent = null;
			preTransitionPausePercent = null;
		}
		else {
			transitionPercent = 1f;
			preTransitionPausePercent = 0f;
		}
	}
	
	private void rescueToy(Toy toy) {
		killToy(toy);
		++numRescued;
	}
	
	private void killToy(Toy toy) {
		dyingToys.add(toy);
	}
	
	private void killDyingToys() {
		for (Toy t : dyingToys) {
			toys.remove(t);
			physics.killBody(t.getBody());
		}
		dyingToys.clear();
	}
	
	private void buildPhysicalStageFromLevel() {
		physicalStageBodies = new ArrayList<Body>();
		
		for (int y = 0; y < level.getHeight(); ++y) {
			for (int x = 0; x < level.getWidth(); ++x) {
				Tile t = level.getTile(x, y);
				if (t == null) { continue; }
				addTilePhysically(t);
			}
		}
	}
	
	private void addTilePhysically(Tile t) {
		Body tileBody = null;
		
		switch (t.getType()) {
			case BLOCKER: // fallthrough...
			case GROUND: tileBody = physics.createBlockTileBody(t.getPosition()); break;
			case START: break; // do nothing
			case GOAL: tileBody = physics.createGoalTileBody(t.getPosition()); break;
			case JUMP_SINGLE: // fallthrough...
			case JUMP_DOUBLE: tileBody = physics.createJumpUpTileBody(t.getPosition()); break;
			case JUMP_LEFT: // fallthrough...
			case JUMP_RIGHT: tileBody = physics.createCornerJumpTileBody(t.getPosition(), t.getType()); break;
			case KILLER: tileBody = physics.createKillerTileBody(t.getPosition()); break;
			case KEY: tileBody = physics.createKeyTileBody(t.getPosition()); break;
			case LOCK: tileBody = physics.createLockTileBody(t.getPosition()); break;
		}
		
		if (tileBody != null) {
			tileBody.setUserData(t);
			physicalStageBodies.add(tileBody);
		}
	}
	
	private void removeTilePhysically(Tile t) {
		for (int i = 0; i < physicalStageBodies.size(); ++i) {
			Body b = physicalStageBodies.get(i);
			if (b.getUserData() == t) {
				physicalStageBodies.remove(i);
				physics.killBody(b);
				return;
			}
		}
	}

	
	private void clean() {
		if (level == null) { return; } // special case, can't do a clean unless there has been a prior level
		
		// cleanup any previous physical stage
		for (Body b : physicalStageBodies) {
			physics.killBody(b);
		}
		physicalStageBodies.clear();
		
		// cleanup any toys
		killRemainingToys();
		killDyingToys();
		
		// cleanup other state
		this.droppableTileType = null;
		this.numRescued = 0;
		this.level = null;
	}

	@Override
	public void dispose() {
		clean();
		physics.dispose();
	}
}
