package week.of.awesome;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Audio;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Disposable;

public class BackgroundMusic implements Disposable {
	
	private static final float MAX_VOLUME = 1f;
	private static final float FADE_DURATION = 1f;

	private Audio audio;
	private List<Music> musics = new ArrayList<Music>();
	
	private Music previouslyPlaying;
	private Music currentlyPlaying;
	
	public BackgroundMusic() {
		this.audio = Gdx.audio;
		
		addMusicFile("Mining by Moonlight.mp3");
		addMusicFile("Chipper Doodle v2.mp3");
		addMusicFile("Wallpaper.mp3");
	}
	
	public void playForLevel(int levelNum) {
		previouslyPlaying = currentlyPlaying;
		currentlyPlaying = musics.get((levelNum-1) % musics.size());
		currentlyPlaying.stop();
		currentlyPlaying.setVolume(0);
		
		// if no previous then just start playing immediately
		if (previouslyPlaying == null) {
			currentlyPlaying.play();
		}
	}
	
	public void update(float dt) {
		if (previouslyPlaying != null) {
			previouslyPlaying.setVolume( Math.max(0, (previouslyPlaying.getVolume() * FADE_DURATION - dt) / FADE_DURATION) );
			if (previouslyPlaying.getVolume() == 0) {
				previouslyPlaying.stop();
				previouslyPlaying = null;
				currentlyPlaying.play();
			}
		}
		else if (currentlyPlaying != null) {
			currentlyPlaying.setVolume( Math.min(MAX_VOLUME, (currentlyPlaying.getVolume() * FADE_DURATION + dt) / FADE_DURATION) );
		}
	}
	
	private void addMusicFile(String filename) {
		FileHandle musicFile = Gdx.files.internal("music/" + filename);
		Music music = audio.newMusic(musicFile);
		music.setLooping(true);
		musics.add( music );
	}

	@Override
	public void dispose() {
		for (Music music : musics) {
			music.dispose();
		}
	}
}
