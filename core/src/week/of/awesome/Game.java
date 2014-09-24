package week.of.awesome;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.utils.TimeUtils;

public class Game implements ApplicationListener {
	
	private static final long NANOS_PER_SEC = 1000000000L;
	private static final float FIXED_TIMESTEP = 1f / 60f;
	private static final long FIXED_TIMESTEP_NANOS = (long)(FIXED_TIMESTEP * NANOS_PER_SEC);
	
	private Renderer renderer;
	private World world;
	private GameplayController controller;
	private InputMapper input;
	
	private long lastFrameTime;
	private long accumulatedTime;
	
	@Override
	public void create () {
		renderer = new Renderer();
		world = new World();
		controller = new GameplayController(world, renderer);
		input = new InputMapper(controller);
		
		lastFrameTime = TimeUtils.nanoTime();
	}

	@Override
	public void render () {
		long time = TimeUtils.nanoTime();
		accumulatedTime += (time - lastFrameTime);
		lastFrameTime = time;
		
		while (accumulatedTime >= FIXED_TIMESTEP_NANOS) {
			input.poll();
			world.update(FIXED_TIMESTEP);
			accumulatedTime -= FIXED_TIMESTEP_NANOS;
		}
		
		renderer.drawWorld(world);
	}

	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dispose() {
		renderer.dispose();
	}
}
