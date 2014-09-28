package week.of.awesome;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.WorldManifold;

public class GameplayController implements WorldEvents {
	
	private static final int START_LEVEL = 1;
	
	private World world;
	private GameplayRenderer renderer;
	private BackgroundMusic bgMusic;
	private Sounds sounds;
	
	public GameplayController(GameplayRenderer renderer, BackgroundMusic bgMusic, Sounds sounds) {
		this.renderer = renderer;
		this.bgMusic = bgMusic;
		this.sounds = sounds;
	}
	
	public void startGame(World world) {
		this.world = world;
		bgMusic.playForLevel(START_LEVEL);
		beginLevel(START_LEVEL, false);
	}
	
	@Override
	public void onLevelComplete(int levelNum) {
		int nextLevelNum = levelNum+1;
		if (beginLevel(nextLevelNum, false)) {
			bgMusic.playForLevel(nextLevelNum);
		} else {
			world.beginLevel(null, false);
		}
	}
	
	@Override
	public void onLevelFailed(int levelNum) {
		beginLevel(levelNum, true); // restart 
	}
	
	@Override
	public void onJump() {
		sounds.playJump();
	}

	@Override
	public void onRescue() {
		sounds.playRescued();
	}
	
	@Override
	public void onToySpawn() {
		sounds.playSpawn();
	}
	
	@Override
	public void onToyDeath() {
		sounds.playDie();
	}
	
	public void mouseUp() {
		Vector2 levelSpacePos = renderer.getLevelSpaceMousePositionOrNull(world.getLevel());
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
	
	private boolean beginLevel(int levelNum, boolean beginImmediately) {
		if (LevelLoader.hasLevel(levelNum)) {
			// create the next level
			Level level = LevelLoader.getLevel(levelNum);
			world.beginLevel(level, beginImmediately);
			return true;
		}
		return false;
	}
}
