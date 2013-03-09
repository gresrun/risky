package net.greghaines.risky.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public final class Combinations {
	
	/**
	 * Create all combinations of size 'k' from the given list.
	 * @param list the source list
	 * @param k the combination size
	 * @return the list of combinations
	 */
	public static <T> List<List<T>> combinations(final List<T> list, final int k) {
		final int[] tmp = new int[k];
		Arrays.fill(tmp, -1);
		final List<List<T>> results = new LinkedList<List<T>>();
		generateCombinations(list, 0, 0, tmp, results);
		return results;
	}

	private static <T> void generateCombinations(final List<T> list, final int start, 
			final int depth, final int[] tmp, final List<List<T>> results) {
        if (depth == tmp.length) {
        	final List<T> combo = new ArrayList<T>(depth);
            for (int j = 0; j < depth; j++) {
                combo.add(list.get(tmp[j]));
            }
            results.add(combo);
        } else {
	        for (int i = start; i < list.size(); i++) {
	            tmp[depth] = i;
	            generateCombinations(list, i + 1, depth + 1, tmp, results);
	        }
	    }
	}
	
	private Combinations() {
		// Utility class
	}
}
