package recommendation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale.Category;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.catalina.tribes.util.StringManager;

import java.util.Set;

import com.sun.corba.se.pept.transport.Connection;

import db.DBConnection;
import db.DBConnectionFactory;
import db.mysql.MySQLConnection;
import entity.Item;


// Recommendation based on geo distance and similar categories.
public class GeoRecommendation {

  public List<Item> recommendItems(String userId, double lat, double lon) {
		List<Item> recommendedItems = new ArrayList<>();

		// Step 1, get all favorited itemids
		DBConnection connection = DBConnectionFactory.getConnection();
		Set<String> favoriteItemIds = connection.getFavoriteItemIds(userId);
		
		// Step 2, get all categories,  sort by count
		// {"sports": 5, "music": 3, "art": 2}
		Map<String, Integer> allCategories = new HashMap<String, Integer>();
		for(String itemId : favoriteItemIds) {
			Set<String> categories = connection.getCategories(itemId);
			for(String category : categories) {
				allCategories.put(category, allCategories.getOrDefault(category, 0) + 1);
			}
		}
		List<Entry<String, Integer>> categoryList = new ArrayList<>(allCategories.entrySet());
		Collections.sort(categoryList, (Entry<String, Integer> e1, Entry<String, Integer> e2) -> {
			return Integer.compare(e2.getValue(), e1.getValue());
		});
		
		// Step 3, search based on category, filter out favorite items
		Set<String> visitedItemIds = new HashSet<>();
		
		for(Entry<String, Integer> category : categoryList) {
			List<Item> items = connection.searchItems(lat, lon, category.getKey());
			
			for(Item item : items) {
				if(!favoriteItemIds.contains(item.getItemId()) && !visitedItemIds.contains(item.getItemId())) {
					recommendedItems.add(item);
				}
			}
		}
		
		connection.close();
		return recommendedItems;
  }
}
