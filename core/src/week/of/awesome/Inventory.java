package week.of.awesome;

import java.util.HashMap;
import java.util.Map;

public class Inventory {
	private Map<Tile.Type, Integer> quantityByType = new HashMap<Tile.Type, Integer>();
	
	public int getNumAvailable(Tile.Type tileType) {
		Integer quantity = quantityByType.get(tileType);
		return quantity == null ? 0 : quantity;
	}
	
	public void useItem(Tile.Type tileType) {
		quantityByType.put(tileType, getNumAvailable(tileType)-1);
	}
	
	public void addItems(Tile.Type tileType, int amount) {
		quantityByType.put(tileType, getNumAvailable(tileType) + amount);
	}
}
