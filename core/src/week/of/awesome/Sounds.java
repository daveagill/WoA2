package week.of.awesome;

import java.util.ArrayList;
import java.util.Collection;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Disposable;

public class Sounds implements Disposable {

	private Collection<Sound> allSounds = new ArrayList<Sound>();
	private Sound jump;
	private Sound spawn;
	private Sound die;
	private Sound rescue;
	
	public Sounds() {
		this.jump = newSound("jump.wav");
		this.spawn = newSound("spawn.wav");
		this.die = newSound("die.wav");
		this.rescue = newSound("Rescue.wav");
	}
	
	public void playJump() {
		jump.play();
	}
	
	public void playSpawn() {
		spawn.play();
	}
	
	public void playDie() {
		die.play();
	}
	
	public void playRescued() {
		rescue.play();
	}
	
	private Sound newSound(String filename) {
		FileHandle soundFile = Gdx.files.internal("sounds/" + filename);
		Sound sound = Gdx.audio.newSound(soundFile);
		allSounds.add(sound);
		return sound;
	}

	@Override
	public void dispose() {
		for (Sound s : allSounds) {
			s.dispose();
		}
	}
}
