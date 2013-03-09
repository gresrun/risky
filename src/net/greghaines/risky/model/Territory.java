package net.greghaines.risky.model;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public class Territory {
	
	private final String name;
	private final Continent continent;
	private final Map<String,Territory> adjacentTerritories = new TreeMap<String,Territory>();
	private Player owner;
	private int armySize;
	
	public Territory(final String name, final Continent continent) {
		this.name = name;
		this.continent = continent;
	}

	public String getName() {
		return this.name;
	}

	public Continent getContinent() {
		return this.continent;
	}

	public Map<String,Territory> getAdjacentTerritories() {
		return this.adjacentTerritories;
	}
	
	public Map<String,Territory> getAttackableTerritories() {
		final Map<String,Territory> attackableTerritories = new TreeMap<String,Territory>();
		if (this.owner != null && isUsable()) {
			for (final Entry<String,Territory> e : this.adjacentTerritories.entrySet()) {
				if (!this.owner.equals(e.getValue().getOwner())) {
					attackableTerritories.put(e.getKey(), e.getValue());
				}
			}
		}
		return attackableTerritories;
	}
	
	public Map<String,Territory> getFortifiableTerritories() {
		final Map<String,Territory> fortifiableTerritories = new TreeMap<String,Territory>();
		if (this.owner != null && isUsable()) {
			for (final Entry<String,Territory> e : this.adjacentTerritories.entrySet()) {
				if (this.owner.equals(e.getValue().getOwner())) {
					fortifiableTerritories.put(e.getKey(), e.getValue());
				}
			}
		}
		return fortifiableTerritories;
	}

	public Player getOwner() {
		return this.owner;
	}

	public void setOwner(final Player owner) {
		this.owner = owner;
	}

	public int getArmySize() {
		return this.armySize;
	}

	public void setArmySize(final int armySize) {
		this.armySize = armySize;
	}
	
	public boolean isUsable() {
		return (this.armySize > 1);
	}
	
	@Override
	public String toString() {
		final String ownerName = (this.owner == null) ? "NONE" : this.owner.getName();
		return this.name + " (" + ownerName + " => " + this.armySize + ")";
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
		if (!(obj instanceof Territory)) {
			return false;
		}
		final Territory other = (Territory) obj;
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
