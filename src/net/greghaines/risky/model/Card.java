package net.greghaines.risky.model;

public class Card {
	
	public enum CardType {
		INFANTRY, CALVARY, ARTILLERY, WILD;
	}
	
	private final String territoryName;
	private final CardType type;
	
	public Card(final String territoryName, final CardType type) {
		this.territoryName = territoryName;
		this.type = type;
	}

	public String getTerritoryName() {
		return this.territoryName;
	}

	public CardType getType() {
		return this.type;
	}
	
	public boolean isWild() {
		return CardType.WILD.equals(this.type);
	}
	
	@Override
	public String toString() {
		return (isWild()) ? this.type.name() : this.territoryName + " - " + this.type;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((this.territoryName == null) ? 0 : this.territoryName
						.hashCode());
		result = prime * result
				+ ((this.type == null) ? 0 : this.type.hashCode());
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
		if (!(obj instanceof Card)) {
			return false;
		}
		final Card other = (Card) obj;
		if (this.territoryName == null) {
			if (other.territoryName != null) {
				return false;
			}
		} else if (!this.territoryName.equals(other.territoryName)) {
			return false;
		}
		if (this.type != other.type) {
			return false;
		}
		return true;
	}
}
