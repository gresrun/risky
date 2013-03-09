package net.greghaines.risky.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class Continent {
	
	private final String name;
	private final int bonusArmies;
	private final GameBoard gameBoard;
	private final Map<String,Territory> territories = new LinkedHashMap<String,Territory>();
	
	public Continent(final String name, final int bonusArmies, final GameBoard gameBoard) {
		this.name = name;
		this.bonusArmies = bonusArmies;
		this.gameBoard = gameBoard;
	}

	public String getName() {
		return this.name;
	}

	public int getBonusArmies() {
		return this.bonusArmies;
	}

	public GameBoard getGameBoard() {
		return this.gameBoard;
	}

	public Map<String,Territory> getTerritories() {
		return this.territories;
	}
	
	public Territory getTerritory(final String name) {
		return this.territories.get(name);
	}
	
	public Player getOwner() {
		Player owner = null;
		for (final Territory territory : this.territories.values()) {
			final Player terrOwner = territory.getOwner();
			if (owner == null) {
				owner = terrOwner;
			} else if (!owner.equals(terrOwner)) {
				owner = null;
				break;
			}
		}
		return owner;
	}
	
	public Map<String,Continent> getAdjacentContinents() {
		final Set<Continent> adjContinents = new HashSet<Continent>();
		for (final Territory territory : this.territories.values()) {
			for (final Territory adjTerritory : territory.getAdjacentTerritories().values()) {
				adjContinents.add(adjTerritory.getContinent());
			}
		}
		adjContinents.remove(this);
		final Map<String,Continent> adjMap = new HashMap<String,Continent>(adjContinents.size());
		for (final Continent adjContinent : adjContinents) {
			adjMap.put(adjContinent.getName(), adjContinent);
		}
		return adjMap;
	}
	
	@Override
	public String toString() {
		return this.name + " (" + this.bonusArmies + ")";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((this.name == null) ? 0 : this.name.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Continent)) {
			return false;
		}
		final Continent other = (Continent) obj;
		if (this.name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!this.name.equals(other.name)) {
			return false;
		}
		return true;
	}
}
