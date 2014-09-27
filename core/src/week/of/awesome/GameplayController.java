package week.of.awesome;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.WorldManifold;

public class GameplayController implements WorldEvents {
	
	private static final int START_LEVEL = 1;
	
	private World world;
	private GameplayRenderer renderer;
	private BackgroundMusic bgMusic;
	
	public GameplayController(GameplayRenderer renderer, BackgroundMusic bgMusic) {
		this.renderer = renderer;
		this.bgMusic = bgMusic;
	}
	
	public void startGame(World world) {
		this.world = world;
		bgMusic.playForLevel(START_LEVEL);
		beginLevel(START_LEVEL);
	}
	
	@Override
	public void onLevelComplete(int levelNum) {
		int nextLevelNum = levelNum+1;
		bgMusic.playForLevel(nextLevelNum);
		beginLevel(nextLevelNum);
	}
	
	@Override
	public void onJump() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onRescue() {
		// TODO Auto-generated method stub
		
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
	
	private void beginLevel(int levelNum) {
		if (LevelLoader.hasLevel(levelNum)) {
			// create the next level
			Level level = LevelLoader.getLevel(levelNum);
			world.beginLevel(level);
		}
	}
}
