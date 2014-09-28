package week.of.awesome;

public interface WorldEvents {
	public void onLevelComplete(int levelNum);
	public void onLevelFailed(int levelNum);
	public void onJump();
	public void onRescue();
	public void onToySpawn();
	public void onToyDeath();
}
