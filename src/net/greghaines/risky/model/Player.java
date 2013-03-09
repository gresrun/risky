package net.greghaines.risky.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import net.greghaines.risky.model.Card.CardType;
import net.greghaines.risky.utils.Combinations;

public class Player {
	
	private static final AtomicInteger PLAYER_COUNTER = new AtomicInteger(0);
	private static final int CARD_SET_SIZE = 3;
	
	private final int playerID;
	private final String name;
	private final List<Card> cards = new LinkedList<Card>();
	private int numArmiesInHand = 0;
	private boolean eliminated = false;

	public Player(final String name) {
		this.playerID = PLAYER_COUNTER.incrementAndGet();
		this.name = name;
	}

	public int getPlayerID() {
		return this.playerID;
	}

	public String getName() {
		return this.name;
	}

	public boolean isEliminated() {
		return this.eliminated;
	}

	public void setEliminated(final boolean eliminated) {
		this.eliminated = eliminated;
	}
	
	public int getNumCards() {
		return this.cards.size();
	}

	public void addCard(final Card card) {
		this.cards.add(card);
	}
	
	public void removeCardSet(final Set<Card> cardSet) {
		this.cards.removeAll(cardSet);
	}
	
	public List<Set<Card>> calculateCardSets() {
		final List<Set<Card>> cardSets;
		if (this.cards.size() < 3) {
			cardSets = Collections.<Set<Card>>emptyList();
		} else {
			final List<List<Card>> combos = Combinations.combinations(this.cards, CARD_SET_SIZE);
			cardSets = new ArrayList<Set<Card>>(combos.size());
			for (final List<Card> combo : combos) {
				if (isValidCardSet(combo)) {
					cardSets.add(new LinkedHashSet<Card>(combo));
				}
			}
		}
		return cardSets;
	}

	private static boolean isValidCardSet(final List<Card> combo) {
		return isWildSet(combo) || isRunSet(combo) || isThreeOfAKindSet(combo);
	}

	private static boolean isWildSet(final List<Card> combo) {
		boolean hasOnlyOneWild = false;
		for (final Card card : combo) {
			if (CardType.WILD.equals(card.getType())) {
				// There are only 2 wild cards in the deck so, don't have to worry about 3 wilds-case
				hasOnlyOneWild = !hasOnlyOneWild;
			}
		}
		return hasOnlyOneWild;
	}

	private static boolean isRunSet(final List<Card> combo) {
		boolean hasInfantry = false;
		boolean hasCalvary = false;
		boolean hasArtillery = false;
		for (final Card card : combo) {
			switch (card.getType()) {
			case INFANTRY:
				hasInfantry = true;
				break;
			case CALVARY:
				hasCalvary = true;
				break;
			case ARTILLERY:
				hasArtillery = true;
				break;
			default:
				// Don't care about WILD or null
				break;
			}
		}
		return hasInfantry && hasCalvary && hasArtillery;
	}

	private static boolean isThreeOfAKindSet(final List<Card> combo) {
		CardType type = null;
		for (final Card card : combo) {
			if (type == null) {
				type = card.getType();
			} else if (!type.equals(card.getType())) {
				type = null; // null to indicate no match
				break;
			}
		}
		return (type != null);
	}

	public int getNumArmiesInHand() {
		return this.numArmiesInHand;
	}

	public void setNumArmiesInHand(final int numArmiesInHand) {
		this.numArmiesInHand = numArmiesInHand;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + this.playerID;
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
		if (!(obj instanceof Player)) {
			return false;
		}
		final Player other = (Player) obj;
		if (this.playerID != other.playerID) {
			return false;
		}
		return true;
	}
}
