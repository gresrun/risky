package net.greghaines.risky.model;

import static net.greghaines.risky.utils.Dice.rollDice;
import static net.greghaines.risky.utils.IOUtils.printf;
import static net.greghaines.risky.utils.IOUtils.println;
import static net.greghaines.risky.utils.IOUtils.readInteger;
import static net.greghaines.risky.utils.IOUtils.readLine;
import static net.greghaines.risky.utils.IOUtils.readYesNo;
import static net.greghaines.risky.utils.IOUtils.sprintf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class GameSession {

	private final GameBoard gameBoard = new GameBoard();
	private final List<Player> players;
	private Player victor = null;
	
	public GameSession() {
		this.players = readPlayers();
	}
	
	public void play() {
		randomizePlayers(); // Instead of rolling a die to see who places first
		occupyTerritories();
		sendReinforcements();
		randomizePlayers(); // Instead of rolling a die to see who plays first
		gameLoop();
		printf("=== Congratulations, %s! You conquered the world! ===%n", this.victor.getName());
	}

	private void gameLoop() {
		println("All armies placed... Let the game begin!");
		int roundNum = 0;
		while (this.victor == null) {
			printf("--- Round #%d ---%n", ++roundNum);
			for (final Player player : this.players) {
				if (!player.isEliminated()) {
					printf("%s, it is now your turn.%n", player.getName());
					awardReinforcements(player);
					while (player.getNumArmiesInHand() > 0) {
						sendReinforcements(player, false);
					}
					attackLoop(player);
					if (this.victor != null) {
						break; // Someone won the game, exit the game loop
					}
					fortify(player);
				}
			}
		}
	}

	private void attackLoop(final Player player) {
		boolean capturedAtLeastOneTerritory = false;
		boolean attacking = true;
		while (attacking) {
			final Map<String, Territory> usableTerritories = 
					this.gameBoard.getUsableTerritories(player);
			final String attackingTerritoryName = readOption(
					"%s, select a territory from which to attack (type 'done' to end attack phase): ", 
					"Please select a territory or type 'done'...", 
					usableTerritories, player, true);
			if (attackingTerritoryName == null) {
				attacking = false;
			} else {
				final Territory attackingTerritory = usableTerritories.get(attackingTerritoryName);
				final Map<String,Territory> attackableTerritories = 
						attackingTerritory.getAttackableTerritories();
				final String defendingTerritoryName = readOption(
						"%s, select a territory to attack (type 'done' to cancel the attack): ", 
						"Please select a territory or type 'done'...", 
						attackableTerritories, player, true);
				if (defendingTerritoryName != null) {
					final Territory defendingTerritory = 
							attackableTerritories.get(defendingTerritoryName);
					capturedAtLeastOneTerritory |= doAttacks(attackingTerritory, defendingTerritory);
					attacking = !checkForVictory();
				} // Else, cancel this attack and continue the loop
			}
		}
		if (capturedAtLeastOneTerritory) {
			final Card drawnCard = this.gameBoard.drawCard();
			if (drawnCard == null) {
				printf("Sorry, %s, there are no more Risk(y) cards available...%n", player.getName());
			} else {
				player.addCard(drawnCard);
				printf("%s captured at least one territory and drew a '%s' card.%n", player.getName(), drawnCard);
			}
		}
	}

	private boolean checkForVictory() {
		Player player = null;
		boolean allTerritoriesOwnedBySamePlayer = true;
		for (final Territory territory : this.gameBoard.getAllTerritories().values()) {
			if (player == null) {
				player = territory.getOwner();
			} else if (!player.equals(territory.getOwner())) {
				allTerritoriesOwnedBySamePlayer = false;
				break;
			}
		}
		if (allTerritoriesOwnedBySamePlayer) {
			this.victor = player;
		}
		return allTerritoriesOwnedBySamePlayer;
	}

	private boolean doAttacks(final Territory attackingTerritory,
			final Territory defendingTerritory) {
		boolean capturedTerritory = false;
		final AttackInfo attackInfo = new AttackInfo();
		attackInfo.setAttackingTerritory(attackingTerritory);
		attackInfo.setDefendingTerritory(defendingTerritory);
		attackInfo.setAttackingPlayer(attackingTerritory.getOwner());
		attackInfo.setDefendingPlayer(defendingTerritory.getOwner());
		printf("%s is attacking %s in %s from %s!%n", attackInfo.getAttackingPlayer().getName(), 
				attackInfo.getDefendingPlayer().getName(), defendingTerritory.getName(), attackingTerritory.getName());
		boolean attacking = true;
		while (attacking && attackingTerritory.getArmySize() > 1) {
			attackInfo.setNumAttackingArmies(readInteger(
					sprintf("%s, how many armies do you wish to attack with?", attackInfo.getAttackingPlayer().getName()),
					1, Math.min(3, attackingTerritory.getArmySize() - 1)));
			final int numDefendingArmies;
			if (defendingTerritory.getArmySize() > 1) {
				numDefendingArmies = readInteger(
						sprintf("%s, how many armies do you wish to defend with?", attackInfo.getDefendingPlayer().getName()),
						1, Math.min(2, defendingTerritory.getArmySize()));
			} else {
				printf("%s, is defending with their sole army", attackInfo.getDefendingPlayer().getName());
				numDefendingArmies = defendingTerritory.getArmySize();
			}
			attackInfo.setNumDefendingArmies(numDefendingArmies);
			capturedTerritory |= doAttack(attackInfo);
			attacking = handleAttackAftermath(attackInfo);
		}
		return capturedTerritory;
	}

	private static boolean doAttack(final AttackInfo attackInfo) {
		final int attackResult = rollDiceForAttack(attackInfo.getAttackingPlayer(), 
				attackInfo.getNumAttackingArmies(), attackInfo.getDefendingPlayer(), attackInfo.getNumDefendingArmies());
		switch (attackResult) {
		case -2: // Attacker lost 2 armies
			attackInfo.getAttackingTerritory().setArmySize(attackInfo.getAttackingTerritory().getArmySize() - 2);
			printf("%s, lost 2 armies in the attack! (%d armies remain in %s)%n", 
					attackInfo.getAttackingPlayer().getName(), attackInfo.getAttackingTerritory().getArmySize(), 
					attackInfo.getAttackingTerritory().getName());
			break;
		case -1: // Attacker lost 1 army
			attackInfo.getAttackingTerritory().setArmySize(attackInfo.getAttackingTerritory().getArmySize() - 1);
			printf("%s, lost an army in the attack! (%d armies remain in %s)%n", 
					attackInfo.getAttackingPlayer().getName(), attackInfo.getAttackingTerritory().getArmySize(), 
					attackInfo.getAttackingTerritory().getName());
			break;
		case 0: // Both players lost an army
			attackInfo.getAttackingTerritory().setArmySize(attackInfo.getAttackingTerritory().getArmySize() - 1);
			attackInfo.getDefendingTerritory().setArmySize(attackInfo.getDefendingTerritory().getArmySize() - 1);
			printf("Both %s and %s lost an army in the attack! (%d armies remain in %s and %d in %s)%n", 
					attackInfo.getAttackingPlayer().getName(), attackInfo.getDefendingPlayer().getName(), 
					attackInfo.getAttackingTerritory().getArmySize(), attackInfo.getAttackingTerritory().getName(), 
					attackInfo.getDefendingTerritory().getArmySize(), attackInfo.getDefendingTerritory().getName());
			break;
		case 1: // Defender lost 1 army
			attackInfo.getDefendingTerritory().setArmySize(attackInfo.getDefendingTerritory().getArmySize() - 1);
			printf("%s, lost an army while defending the attack! (%d armies remain in %s)%n", 
					attackInfo.getDefendingPlayer().getName(), attackInfo.getDefendingTerritory().getArmySize(), 
					attackInfo.getDefendingTerritory().getName());
			break;
		case 2: // Defender lost 2 armies
			attackInfo.getDefendingTerritory().setArmySize(attackInfo.getDefendingTerritory().getArmySize() - 2);
			printf("%s, lost 2 armies while defending the attack! (%d armies remain in %s)%n", 
					attackInfo.getDefendingPlayer().getName(), attackInfo.getDefendingTerritory().getArmySize(), 
					attackInfo.getDefendingTerritory().getName());
			break;
		}
		return (attackInfo.getDefendingTerritory().getArmySize() == 0);
	}

	private static int rollDiceForAttack(final Player attackingPlayer, 
			final int numAttackingArmies, final Player defendingPlayer, 
			final int numDefendingArmies) {
		final List<Integer> attackerRolls = rollDice(numAttackingArmies);
		printf("%s rolled %s...%n", attackingPlayer.getName(), attackerRolls);
		final List<Integer> defenderRolls = rollDice(numDefendingArmies);
		printf("%s rolled %s...%n", defendingPlayer.getName(), defenderRolls);
		int attackResult = 0;
		while (!attackerRolls.isEmpty() && !defenderRolls.isEmpty()) {
			 final int compareResult = attackerRolls.remove(0).compareTo(defenderRolls.remove(0));
			 if (compareResult > 0) {
				 attackResult++;
			 } else {
				 attackResult--;
			 }
		}
		return attackResult;
	}

	private boolean handleAttackAftermath(final AttackInfo attackInfo) {
		final boolean attacking;
		if (attackInfo.getDefendingTerritory().getArmySize() == 0) {
			attackInfo.getDefendingTerritory().setOwner(attackInfo.getAttackingPlayer());
			printf("%s has captured %s from %s!%n", attackInfo.getAttackingPlayer().getName(), 
					attackInfo.getDefendingTerritory().getName(), attackInfo.getDefendingPlayer().getName());
			if (this.gameBoard.getOccupiedTerritories(attackInfo.getDefendingPlayer()).isEmpty()) {
				attackInfo.getDefendingPlayer().setEliminated(true);
				printf("%s no longer controls any territories and has been eliminated!%n", 
						attackInfo.getDefendingPlayer().getName());
			}
			if (!attackInfo.getDefendingPlayer().isEliminated() || !checkForVictory()) {
				final int maxArmiesToMove = attackInfo.getAttackingTerritory().getArmySize() - 1;
				final int minArmiesToMove = Math.min(maxArmiesToMove, attackInfo.getNumAttackingArmies());
				final int numArmiesToMove;
				if (minArmiesToMove == maxArmiesToMove) {
					numArmiesToMove = minArmiesToMove;
					printf("%s is forced to move %d armies from %s to %s.%n", attackInfo.getAttackingPlayer().getName(), 
							minArmiesToMove, attackInfo.getAttackingTerritory().getName(), attackInfo.getDefendingTerritory().getName());
				} else {
					numArmiesToMove = readInteger(
							sprintf("%s, select the number of armies to move from %s to %s", 
									attackInfo.getAttackingPlayer().getName(), attackInfo.getAttackingTerritory().getName(), 
									attackInfo.getDefendingTerritory().getName()), 
							minArmiesToMove, maxArmiesToMove);
				}
				attackInfo.getAttackingTerritory().setArmySize(attackInfo.getAttackingTerritory().getArmySize() - numArmiesToMove);
				attackInfo.getDefendingTerritory().setArmySize(numArmiesToMove);
			}
			attacking = false;
		} else if (attackInfo.getAttackingTerritory().getArmySize() == 1) {
			printf("%s's attack on %s has halted due to insufficient armies in %s.%n", 
					attackInfo.getAttackingPlayer().getName(), attackInfo.getDefendingTerritory().getName(), 
					attackInfo.getAttackingTerritory().getName());
			attacking = false;
		} else {
			attacking = readYesNo(sprintf("%s, do you with to continue the attack?", 
					attackInfo.getAttackingPlayer().getName()));
		}
		return attacking;
	}

	private void fortify(final Player player) {
		boolean fortifying = true;
		while (fortifying) {
			final Map<String, Territory> usableTerritories = 
					this.gameBoard.getUsableTerritories(player);
			final String sourceTerritoryName = readOption(
					"%s, select a territory from which to fortify (type 'done' to skip fortification): ", 
					"Please select a territory or type 'done'...", 
					usableTerritories, player, true);
			if (sourceTerritoryName == null) {
				fortifying = false;
			} else {
				final Territory sourceTerritory = usableTerritories.get(sourceTerritoryName);
				final Map<String, Territory> fortifiableTerritories = 
						sourceTerritory.getFortifiableTerritories();
				final String targetTerritoryName = readOption(
						"%s, select a territory to fortify (type 'done' to cancel this fortification action): ", 
						"Please select a territory or type 'done'...", 
						fortifiableTerritories, player, true);
				if (targetTerritoryName != null) {
					final Territory targetTerritory = fortifiableTerritories.get(targetTerritoryName);
					final int numArmies;
					if (sourceTerritory.getArmySize() == 2) {
						numArmies = 1;
					} else {
						numArmies = readInteger(sprintf(
								"%s, how many armies do you wish to move?", player.getName()), 
								1, sourceTerritory.getArmySize() - 1);
					}
					sourceTerritory.setArmySize(sourceTerritory.getArmySize() - numArmies);
					targetTerritory.setArmySize(targetTerritory.getArmySize() + numArmies);
					printf("%s fortified %s with %d armies from %s.%n", player.getName(), 
							targetTerritory.getName(), numArmies, sourceTerritory.getName());
					fortifying = false;
				} // Else, cancel this fortification action and continue the loop
			}
		}
	}

	private void awardReinforcements(final Player player) {
		final int numTerritoryArmies = awardTerritoryArmies(player);
		final int numContinentArmies = awardContinentReinforcements(player);
		final int cardArmies = awardCardSetReinforcements(player);
		final int totalNewArmies;
		if (cardArmies == 0) {
			totalNewArmies = numTerritoryArmies + numContinentArmies;
		} else {
			totalNewArmies = Math.max(cardArmies, Math.min(cardArmies + 2, numTerritoryArmies + numContinentArmies));
		}
		printf("%s received %d new armies this turn.%n", player.getName(), totalNewArmies);
		player.setNumArmiesInHand(player.getNumArmiesInHand() + totalNewArmies);
	}
	
	private int awardTerritoryArmies(final Player player) {
		final Map<String, Territory> occupiedTeritories = 
				this.gameBoard.getOccupiedTerritories(player);
		final int numTerritoryArmies = Math.max(3, occupiedTeritories.size() / 3);
		printf("%s controls %d territories (%d armies awarded)%n", player.getName(), 
				occupiedTeritories.size(), numTerritoryArmies);
		return numTerritoryArmies;
	}

	private int awardContinentReinforcements(final Player player) {
		int numContinentArmies = 0;
		final Map<String, Continent> occupiedContients = 
				this.gameBoard.getOccupiedContinents(player);
		for (final Continent occupiedContient : occupiedContients.values()) {
			numContinentArmies += occupiedContient.getBonusArmies();
			printf("%s controls %s (%d armies awarded)%n", player.getName(), occupiedContient.getName(), 
					occupiedContient.getBonusArmies());
		}
		return numContinentArmies;
	}

	private int awardCardSetReinforcements(final Player player) {
		int cardArmies = 0;
		boolean doneWithCards = false;
		while (!doneWithCards) {
			final List<Set<Card>> cardSets = player.calculateCardSets();
			if (cardSets.isEmpty()) {
				doneWithCards = true;
			} else {
				println("NOTE: On a single turn, you may receive no more than 2 extra armies above and " +
						"beyond those you receive for the matched sets of cards you trade in.");
				final Map<String, Set<Card>> cardMap = new LinkedHashMap<String, Set<Card>>();
				for (int i = 0; i < cardSets.size(); i++) {
					cardMap.put(Integer.toString(i), cardSets.get(i));
				}
				final boolean allowDone = (player.getNumCards() < 5);
				String prompt = "%s, select a set to turn in (press enter to list";
				if (allowDone) {
					prompt += " or type 'done' to skip): ";
				} else {
					prompt += "): ";
				}
				final String indexStr = readOption(prompt, "Please select a set number...", 
						cardMap, player, allowDone);
				if (indexStr == null) {
					doneWithCards = true;
				} else {
					final Set<Card> cardSet = cardMap.get(indexStr);
					player.removeCardSet(cardSet);
					final int setArmies = this.gameBoard.tradeInCardSet(cardSet);
					printf("%s received %d armies for turning in a card set.%n", player.getName(), setArmies);
					cardArmies += setArmies;
				}
			}
		}
		return cardArmies;
	}

	private void randomizePlayers() {
		println("Randomizing player order...");
		Collections.shuffle(this.players);
	}

	private void occupyTerritories() {
		final Map<String,Territory> freeTeritories = 
				new TreeMap<String,Territory>(this.gameBoard.getAllTerritories());
		while (!freeTeritories.isEmpty()) {
			for (final Player player : this.players) {
				final String territoryName = readOption(
						"%s, choose a territory to occupy (press enter to list): ", 
						"Please select a free territory...", freeTeritories, player, false);
				final Territory territory = freeTeritories.remove(territoryName);
				territory.setArmySize(1);
				territory.setOwner(player);
				player.setNumArmiesInHand(player.getNumArmiesInHand() - 1);
				if (freeTeritories.isEmpty()) {
					break;
				}
			}
		}
	}

	private void sendReinforcements() {
		println("All territories occupied... send in the reinforcements!");
		int emptyPlayers = 0;
		while (emptyPlayers < this.players.size()) {
			emptyPlayers = 0;
			for (final Player player : this.players) {
				if (player.getNumArmiesInHand() == 0) {
					emptyPlayers++;
				} else {
					sendReinforcements(player, true);
				}
			}
		}
	}

	private void sendReinforcements(final Player player, final boolean oneAtATime) {
		final Map<String, Territory> occupiedTeritories = this.gameBoard.getOccupiedTerritories(player);
		printf("%s, you have %d armies remaining to place on the board.%n", 
				player.getName(), player.getNumArmiesInHand());
		final Territory territory;
		if (occupiedTeritories.size() > 1) {
			final String territoryName = readOption(
					"%s, choose an occupied territory to reinforce (press enter to list): ", 
					"Please select an occupied territory...", occupiedTeritories, player, false);
			territory = occupiedTeritories.get(territoryName);
		} else {
			territory = occupiedTeritories.values().iterator().next();
			printf("%s, has reinforced %s with %d armies.%n", 
					player.getName(), territory.getName(), player.getNumArmiesInHand());
		}
		final int numReinforcements;
		if (oneAtATime || player.getNumArmiesInHand() == 1) {
			numReinforcements = 1;
		} else if (occupiedTeritories.size() > 1) {
			numReinforcements = readInteger("Select number of armies to reinforce with", 
				1, player.getNumArmiesInHand());
		} else {
			numReinforcements = player.getNumArmiesInHand();
		}
		territory.setArmySize(territory.getArmySize() + numReinforcements);
		player.setNumArmiesInHand(player.getNumArmiesInHand() - numReinforcements);
	}

	private static <V> String readOption(final String prompt, final String failMsg, 
			final Map<String, V> optionsMap, final Player player, final boolean allowDone) {
		String keyVal = null;
		while (keyVal == null) {
			keyVal = readLine(prompt, player.getName());
			if (allowDone && "done".equalsIgnoreCase(keyVal)) {
				keyVal = null;
				break;
			}
			if (keyVal == null || !optionsMap.containsKey(keyVal)) {
				println(failMsg);
				for (final V value : optionsMap.values()) {
					printf("\t%s%n", value);
				}
				keyVal = null;
			}
		}
		return keyVal;
	}

	private static List<Player> readPlayers() {
		final int numPlayers = readNumPlayers();
		final List<Player> players = new ArrayList<Player>(numPlayers);
		for (int i = 0; i < numPlayers; i++) {
			final String playerName = readLine("Player %d's name: ", i + 1);
			final Player player = new Player(playerName);
			player.setNumArmiesInHand(50 - (5 * numPlayers));
			players.add(player);
		}
		final StringBuilder buf = new StringBuilder(128);
		buf.append("Welcome ");
		String prefix = "";
		for (int i = 0 ; i < numPlayers; i++) {
			final Player player = players.get(i);
			buf.append(prefix).append(player.getName());
			prefix = (i == (numPlayers - 2)) ? " and " : ", ";
		}
		buf.append("!");
		println(buf.toString());
		return players;
	}

	private static Integer readNumPlayers() {
		Integer numPlayers = null;
		while (numPlayers == null) {
			final String playerStr = readLine("Enter number of players (3-6): ");
			try {
				numPlayers = Integer.valueOf(playerStr);
				if (numPlayers == null || numPlayers < 3 || numPlayers > 6) {
					println("Please enter a number from 3 to 6, inclusive...");
					numPlayers = null;
				}
			} catch (NumberFormatException nfe) {
				println("Bad number");
			}
		}
		return numPlayers;
	}
	
	private static class AttackInfo {
		
		private Territory attackingTerritory;
		private Territory defendingTerritory;
		private Player attackingPlayer;
		private Player defendingPlayer;
		private int numAttackingArmies;
		private int numDefendingArmies;
		
		public Territory getAttackingTerritory() {
			return this.attackingTerritory;
		}
		
		public void setAttackingTerritory(final Territory attackingTerritory) {
			this.attackingTerritory = attackingTerritory;
		}
		
		public Territory getDefendingTerritory() {
			return this.defendingTerritory;
		}
		
		public void setDefendingTerritory(final Territory defendingTerritory) {
			this.defendingTerritory = defendingTerritory;
		}
		
		public Player getAttackingPlayer() {
			return this.attackingPlayer;
		}
		
		public void setAttackingPlayer(final Player attackingPlayer) {
			this.attackingPlayer = attackingPlayer;
		}
		
		public Player getDefendingPlayer() {
			return this.defendingPlayer;
		}
		
		public void setDefendingPlayer(final Player defendingPlayer) {
			this.defendingPlayer = defendingPlayer;
		}
		
		public int getNumAttackingArmies() {
			return this.numAttackingArmies;
		}
		
		public void setNumAttackingArmies(final int numAttackingArmies) {
			this.numAttackingArmies = numAttackingArmies;
		}
		
		public int getNumDefendingArmies() {
			return this.numDefendingArmies;
		}
		
		public void setNumDefendingArmies(final int numDefendingArmies) {
			this.numDefendingArmies = numDefendingArmies;
		}
	}
}
