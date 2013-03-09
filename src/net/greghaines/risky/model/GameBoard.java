package net.greghaines.risky.model;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import net.greghaines.risky.model.Card.CardType;

public class GameBoard {
	
	private final Map<String,Continent> continents = new TreeMap<String,Continent>();
	private final Map<String,Territory> allTerritories = new TreeMap<String,Territory>();
	private final List<Card> cards = new LinkedList<Card>();
	private final List<Set<Card>> cardSetsTradedIn = new LinkedList<Set<Card>>();

	public GameBoard() {
		addContinent("Asia", 7, "Siberia", "Yakutsk", "Kamchatka", "Ural", "Irkutsk", "Afganistan", "China", "Mongolia", "Japan", "Middle East", "India", "Siam");
		addContinent("Africa", 3, "North Africa", "Egypt", "East Africa", "Congo", "South Africa", "Madagascar");
		addContinent("Austrailia", 2, "Indonesia", "New Guinea", "Western Austrailia", "Eastern Austrailia");
		addContinent("Europe", 5, "Iceland", "Scandinavia", "Ukraine", "Great Britain", "Northern Europe", "Western Europe", "Southern Europe");
		addContinent("North America", 5, "Greenland", "Alaska", "Northwest Territory", "Alberta", "Ontario", "Quebec", "Western United States", "Eastern United States", "Central America");
		addContinent("South America", 2, "Venezuela", "Brazil", "Peru", "Argentina");
		connect("Kamchatka", "Alaska");
		connect("Alaska", "Northwest Territory");
		connect("Alaska", "Alberta");
		connect("Northwest Territory", "Alberta");
		connect("Northwest Territory", "Ontario");
		connect("Northwest Territory", "Greenland");
		connect("Greenland", "Ontario");
		connect("Greenland", "Quebec");
		connect("Greenland", "Iceland");
		connect("Alberta", "Ontario");
		connect("Alberta", "Western United States");
		connect("Ontario", "Western United States");
		connect("Ontario", "Eastern United States");
		connect("Ontario", "Quebec");
		connect("Quebec", "Eastern United States");
		connect("Western United States", "Eastern United States");
		connect("Western United States", "Central America");
		connect("Eastern United States", "Central America");
		connect("Central America", "Venezuela");
		connect("Venezuela", "Brazil");
		connect("Venezuela", "Peru");
		connect("Peru", "Brazil");
		connect("Peru", "Argentina");
		connect("Argentina", "Brazil");
		connect("Brazil", "North Africa");
		connect("Iceland", "Great Britain");
		connect("Iceland", "Scandinavia");
		connect("Great Britain", "Scandinavia");
		connect("Great Britain", "Northern Europe");
		connect("Great Britain", "Western Europe");
		connect("Scandinavia", "Northern Europe");
		connect("Scandinavia", "Ukraine");
		connect("Ukraine", "Northern Europe");
		connect("Ukraine", "Ural");
		connect("Ukraine", "Afganistan");
		connect("Ukraine", "Middle East");
		connect("Ukraine", "Southern Europe");
		connect("Northern Europe", "Western Europe");
		connect("Northern Europe", "Southern Europe");
		connect("Western Europe", "Southern Europe");
		connect("Western Europe", "North Africa");
		connect("Southern Europe", "North Africa");
		connect("Southern Europe", "Egypt");
		connect("Southern Europe", "Middle East");
		connect("North Africa", "Egypt");
		connect("North Africa", "East Africa");
		connect("North Africa", "Congo");
		connect("East Africa", "Middle East");
		connect("East Africa", "Congo");
		connect("East Africa", "South Africa");
		connect("East Africa", "Madagascar");
		connect("South Africa", "Madagascar");
		connect("Ural", "Siberia");
		connect("Ural", "Afganistan");
		connect("Ural", "China");
		connect("Siberia", "Yakutsk");
		connect("Siberia", "Irkutsk");
		connect("Siberia", "Mongolia");
		connect("Siberia", "China");
		connect("Yakutsk", "Irkutsk");
		connect("Yakutsk", "Kamchatka");
		connect("Kamchatka", "Irkutsk");
		connect("Kamchatka", "Mongolia");
		connect("Kamchatka", "Japan");
		connect("Irkutsk", "Mongolia");
		connect("Mongolia", "China");
		connect("Mongolia", "Japan");
		connect("Afganistan", "China");
		connect("Afganistan", "Middle East");
		connect("Afganistan", "India");
		connect("China", "India");
		connect("China", "Siam");
		connect("India", "Siam");
		connect("Siam", "Indonesia");
		connect("Indonesia", "New Guinea");
		connect("Indonesia", "Western Austrailia");
		connect("New Guinea", "Western Austrailia");
		connect("New Guinea", "Eastern Austrailia");
		connect("Western Austrailia", "Eastern Austrailia");
		createCardDeck();
	}

	private void createCardDeck() {
		final CardType[] nonWildTypes = { CardType.INFANTRY, CardType.CALVARY, CardType.ARTILLERY };
		int i = 0;
		// A card for each territory with a non-wild type
		for (final Territory territory : this.allTerritories.values()) {
			final CardType type = nonWildTypes[(++i % nonWildTypes.length)];
			this.cards.add(new Card(territory.getName(), type));
		}
		// Plus two wild cards
		this.cards.add(new Card(null, CardType.WILD));
		this.cards.add(new Card(null, CardType.WILD));
		// Shuffle the deck
		Collections.shuffle(this.cards);
	}

	private Continent addContinent(final String continentName, final int bonusArmies, 
			final String... territoryNames) {
		final Continent continent = new Continent(continentName, bonusArmies, this);
		this.continents.put(continent.getName(), continent);
		for (final String territoryName : territoryNames) {
			this.allTerritories.put(territoryName, createTerritory(territoryName, continent));
		}
		return continent;
	}
	
	private static Territory createTerritory(final String name, final Continent continent) {
		final Territory territory = new Territory(name, continent);
		continent.getTerritories().put(name, territory);
		return territory;
	}
	
	private void connect(final String terrName1, final String terrName2) {
		final Territory terr1 = this.allTerritories.get(terrName1);
		final Territory terr2 = this.allTerritories.get(terrName2);
		terr1.getAdjacentTerritories().put(terrName2, terr2);
		terr2.getAdjacentTerritories().put(terrName1, terr1);
	}

	public Map<String, Continent> getContinents() {
		return this.continents;
	}

	public Map<String, Territory> getAllTerritories() {
		return this.allTerritories;
	}

	public Map<String, Continent> getOccupiedContinents(final Player player) {
		final Map<String,Continent> occupiedContinents = new TreeMap<String,Continent>();
		for (final Entry<String,Continent> e : this.continents.entrySet()) {
			if (player.equals(e.getValue().getOwner())) {
				occupiedContinents.put(e.getKey(), e.getValue());
			}
		}
		return occupiedContinents;
	}

	public Map<String, Territory> getOccupiedTerritories(final Player player) {
		final Map<String,Territory> occupiedTeritories = new TreeMap<String,Territory>();
		for (final Entry<String,Territory> e : this.allTerritories.entrySet()) {
			if (player.equals(e.getValue().getOwner())) {
				occupiedTeritories.put(e.getKey(), e.getValue());
			}
		}
		return occupiedTeritories;
	}

	public Map<String, Territory> getUsableTerritories(final Player player) {
		final Map<String,Territory> usableTeritories = new TreeMap<String,Territory>();
		for (final Entry<String,Territory> e : this.allTerritories.entrySet()) {
			final Territory territory = e.getValue();
			if (territory.isUsable() && player.equals(territory.getOwner())) {
				usableTeritories.put(e.getKey(), territory);
			}
		}
		return usableTeritories;
	}

	public Card drawCard() {
		return this.cards.remove(0);
	}

	public int getNumCardSetsTradedIn() {
		return this.cardSetsTradedIn.size();
	}
	
	public int tradeInCardSet(final Set<Card> cardSet) {
		int numNewArmies = 2;
		this.cardSetsTradedIn.add(cardSet);
		final int numCardSetsTradedIn = getNumCardSetsTradedIn();
		final int baseScore = Math.min(5, numCardSetsTradedIn);
		numNewArmies += 2 * baseScore;
		if (baseScore < numCardSetsTradedIn) {
			final int aboveSix = numCardSetsTradedIn - 6;
			numNewArmies += 3 + (aboveSix * 5);
		}
		return numNewArmies;
	}
}
