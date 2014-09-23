package week.of.awesome;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

public class LevelLoader {

	public static Level getLevel(int levelNum) {
		FileHandle file = getLevelFile(levelNum);
		if (!file.exists()) { throw new RuntimeException("No such file for level: " + levelNum); }
		
		String levelData = file.readString();
		 
		// first line is the level's name
		int endOfFirstLineIdx = levelData.indexOf("\n");
		String name = levelData.substring(0, endOfFirstLineIdx);
		
		// rest of the file is the tile data arranged in a token grid
		List<List<Tile>> tileGrid = new ArrayList<List<Tile>>();
		tileGrid.add(new ArrayList<Tile>()); // always at least 1 row
		
		// parse tokens into tiles
		int tokenIdx = endOfFirstLineIdx+1;
		while (tokenIdx < levelData.length()) {
			char tileToken = levelData.charAt(tokenIdx);
			++tokenIdx; // consumed a token
			
			List<Tile> currentRow = tileGrid.get( tileGrid.size()-1 );
			
			if      (tileToken == ' ') { continue; } // spaces are of no consequence
			else if (tileToken == '\n' || tileToken == '\r') { // newlines indicate a new row
				tileGrid.add(new ArrayList<Tile>());
				// next char is likely a newline char too e.g. \n\r
				char nextToken = levelData.charAt(tokenIdx);
				if (nextToken == '\n' || nextToken == '\r') {
					// eat it
					++tokenIdx;
				}
			}
			else if (tileToken == '.') { // dots are empty tiles
				currentRow.add(null);
			}
			else if (tileToken == '#') { // hashes are solid tiles
				currentRow.add(new Tile(Tile.Type.BLOCK));
			}
			else if (tileToken == 'S') { // start cell
				currentRow.add(new Tile(Tile.Type.START));
			}
			else if (tileToken == 'F') { // goal cell
				currentRow.add(new Tile(Tile.Type.GOAL));
			}
			else if (tileToken == 'J') { // jump cell
				// the next token tells us what kind of jump it is
				char jumpTypeToken = levelData.charAt(tokenIdx);
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

		// create a level object that will be the eventual representation of the level
		int width = tileGrid.get(0).size();
		int height = tileGrid.size();
		Level level = new Level(name, width, height);
		
		// add tiles to the level
		int y = height-1;
		for (List<Tile> row : tileGrid) {
			int x = 0;
			for (Tile tile : row) {
				level.setTile(tile, x, y);
				++x;
			}
			--y;
		}
		
		return level;
	}
	
	public boolean hasLevel(int levelNum) {
		return getLevelFile(levelNum).exists();
	}
	
	private static FileHandle getLevelFile(int levelNum) {
		return Gdx.files.internal("levels/level" + levelNum + ".txt");
	}
}
