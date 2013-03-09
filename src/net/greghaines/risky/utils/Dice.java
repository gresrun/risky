package net.greghaines.risky.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * A helper class for simulating dice rolls.
 * @author Greg Haines
 */
public final class Dice {

	private static final Random RANDOM = new Random();

	/**
	 * Simulate rolling dice.
	 * @param numDice the number of dice to roll
	 * @return a list of psuedo-random integers between 1 and 6, inclusive, sorted in descending order
	 */
	public static List<Integer> rollDice(final int numDice) {
		final List<Integer> rolls = new ArrayList<Integer>(numDice);
		for (int i = 0; i < numDice; i++) {
			rolls.add(rollDie());
		}
		Collections.sort(rolls, Collections.reverseOrder()); // Sort high to low
		return rolls;
	}

	/**
	 * Simulate rolling a single die.
	 * @return a psuedo-random integer between 1 and 6, inclusive
	 */
	public static int rollDie() {
		return RANDOM.nextInt(6) + 1;
	}
	
	private Dice(){
		// Utility class
	}
}
