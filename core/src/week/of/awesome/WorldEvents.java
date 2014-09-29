package week.of.awesome;

public interface WorldEvents {
	public void onLevelStart(int levelNum);
	public void onLevelComplete(int levelNum);
	public void onLevelFailed(int levelNum);
	public void onJump();
	public void onRescue();
	public void onToySpawn();
	public void onToyDeath();
	public void onDoorUnlocked();
}
