package week.of.awesome;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader;

public class LevelLoader {

	public static Level getLevel(int levelNum) {
		return loadLevelFile(getLevelFile("level" + levelNum), levelNum);
	}
	
	public static Level getScreen(String screenName) {
		return loadLevelFile(getLevelFile(screenName), -1);
	}
	
	public static Level loadLevelFile(FileHandle file, int levelNum) {
		if (!file.exists()) { throw new RuntimeException("No such level file: " + file.name()); }
		
		
		XmlReader.Element xml = null;
		try {
			xml = new XmlReader().parse(file);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		String name = xml.get("name");
		int numRescuedNeeded = xml.getInt("rescue");
		
		String tileData = xml.getChildByName("map").getText();
		List<List<Tile>> tileGrid = parseTileGrid(tileData);
		
		Level level = new Level(levelNum, name, tileGrid, numRescuedNeeded);
		
		// configure the spawn points
		int spawnerIdx = 0;
		Array<XmlReader.Element> startTilesSpawnConfigs = xml.getChildByName("spawnerConfigs").getChildrenByName("spawn");
		for (XmlReader.Element spawnConfig : startTilesSpawnConfigs) {
			Spawner spawner = level.getSpawners().get(spawnerIdx);
			
			float timeToFirstSpawn = spawnConfig.getFloat("firstSpawnDelay", spawner.getTimeToFirstSpawn());
			int spawnNum = spawnConfig.getInt("amount", spawner.getSpawnAllocation());
			float spawnFreq = spawnConfig.getFloat("freq", spawner.getSpawnFreq());
			
			spawner.configure(timeToFirstSpawn, spawnNum, spawnFreq);
			++spawnerIdx;
		}
		
		// load the inventory
		XmlReader.Element inventoryXml = xml.getChildByName("inventory");
		if (inventoryXml != null) {
			level.getInventory().addItems( Tile.Type.BLOCKER, parseInventoryItem(inventoryXml, "blockers") );
			level.getInventory().addItems( Tile.Type.JUMP_SINGLE, parseInventoryItem(inventoryXml, "jumpSingle") );
			level.getInventory().addItems( Tile.Type.JUMP_DOUBLE, parseInventoryItem(inventoryXml, "jumpDouble") );
			level.getInventory().addItems( Tile.Type.JUMP_LEFT, parseInventoryItem(inventoryXml, "jumpLeft") );
			level.getInventory().addItems( Tile.Type.JUMP_RIGHT, parseInventoryItem(inventoryXml, "jumpRight") );
		}
		
		// sanity check
		if (level.getSpawnRemaining() < level.getNumRescuedNeeded()) {
			throw new RuntimeException("Level is impossible to solve!");
		}
		
		return level;
	}
	
	public static boolean hasLevel(int levelNum) {
		return getLevelFile("level" + levelNum).exists();
	}
	
	private static List<List<Tile>> parseTileGrid(String tileData) {
		List<List<Tile>> tileGrid = new ArrayList<List<Tile>>();
		
		List<Tile> currentRow = new ArrayList<Tile>();
		
		// parse tokens into tiles
		int tokenIdx = 0;
		while (tokenIdx < tileData.length()) {
			char tileToken = tileData.charAt(tokenIdx);
			++tokenIdx; // consumed a token
			
			if      (tileToken == ' ' || tileToken == '\t') { continue; } // spaces/tabs are of no consequence
			else if (tileToken == '\n' || tileToken == '\r') { // newlines indicate a new row
				tileGrid.add(currentRow);
				currentRow = new ArrayList<Tile>();
				
				// next char is likely a newline char too e.g. \n\r
				char nextToken = tileData.charAt(tokenIdx);
				if (nextToken == '\n' || nextToken == '\r') {
					// eat it
					++tokenIdx;
				}
			}
			else if (tileToken == '.') { // dots are empty tiles
				currentRow.add(null);
			}
			else if (tileToken == '#') { // hashes are solid tiles
				currentRow.add(new Tile(Tile.Type.GROUND));
			}
			else if (tileToken == 'X') { // crosses are killer tiles
				currentRow.add(new Tile(Tile.Type.KILLER));
			}
			else if (tileToken == 'S') { // start cell
				currentRow.add(new Tile(Tile.Type.START));
			}
			else if (tileToken == 'F') { // goal cell
				currentRow.add(new Tile(Tile.Type.GOAL));
			}
			else if (tileToken == 'J') { // jump cell
				// the next token tells us what kind of jump it is
				char jumpTypeToken = tileData.charAt(tokenIdx);
				++tokenIdx; // consumed a token
				
				if (jumpTypeToken == '1') {
					currentRow.add(new Tile(Tile.Type.JUMP_SINGLE));
				}
				else if (jumpTypeToken == '2') {
					currentRow.add(new Tile(Tile.Type.JUMP_DOUBLE));
				}
				else if (jumpTypeToken == 'L') {
					currentRow.add(new Tile(Tile.Type.JUMP_LEFT));
				}
				else if (jumpTypeToken == 'R') {
					currentRow.add(new Tile(Tile.Type.JUMP_RIGHT));
				}
				else {
					throw new RuntimeException("Illegal jump type token: " + jumpTypeToken);
				}
			}
			else {
				throw new RuntimeException("Illegal tile token: " + tileToken);
			}
		}
		
		// in case there isn't a trailing newline to append the final row, we do it here
		if (!currentRow.isEmpty()) {
			tileGrid.add(currentRow);
		}
		
		return tileGrid;
	}
	
	private static int parseInventoryItem(XmlReader.Element inventoryXml, String childElementName) {
		XmlReader.Element itemXml = inventoryXml.getChildByName(childElementName);
		if (itemXml == null) { return 0; }
		return itemXml.getInt("available", 0);
	}
	
	private static FileHandle getLevelFile(String levelFileName) {
		return Gdx.files.internal("levels/" + levelFileName + ".txt");
	}
}
