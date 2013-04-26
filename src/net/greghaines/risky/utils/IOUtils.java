package net.greghaines.risky.utils;

import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Utility methods for reading from and writing to the terminal.
 * @author Greg Haines
 */
public final class IOUtils {

	/**
	 * Prints the prompt and then reads an integer from the terminal in the range of min to max, inclusive.
	 * @param prompt the question prompt
	 * @param min the minimum value to accept
	 * @param max the maximum value to accept
	 * @return a valid integer, never null
	 */
	public static Integer readInteger(final String prompt, final int min, final int max) {
		return readInteger(prompt, min, max, null);
	}

	/**
	 * Prints the prompt and then reads an integer from the terminal in the range of min to max, inclusive.
	 * @param prompt the question prompt
	 * @param min the minimum value to accept
	 * @param max the maximum value to accept
	 * @param exitString option that will allow the result to be null (e.g.: 'x' to exit)
	 * @return a valid integer or null if exitString is not null and the user chose that option
	 */
	public static Integer readInteger(final String prompt, final int min, final int max, final String exitString) {
		if (min > max) {
			throw new IllegalArgumentException("min should not be greater than max (min=" + 
						min + ",max=" + max + ")");
		}
		Integer val = null;
		while (val == null) {
			final String valStr = (exitString == null) 
				? readLine("%s (%d-%d): ", prompt, min, max)
				: readLine("%s (%d-%d) or '%s' to exit", prompt, min, max, exitString);
			try {
				if (exitString != null && exitString.equals(valStr)) {
					break;
				}
				val = Integer.valueOf(valStr);
				if (val == null || val < min || val > max) {
					printf("Please enter number greater than or equal to %d and less than or equal to %d...%n", min, max);
					val = null;
				}
			} catch (NumberFormatException nfe) {
				println("Bad number");
			}
		}
		return val;
	}

	/**
	 * Prints the prompt and then reads a yes or no from the terminal.
	 * @param prompt the question prompt
	 * @return true if the response was a 'yes'
	 */
	public static boolean readYesNo(final String prompt) {
		Boolean val = null;
		while (val == null) {
			final String valStr = readLine("%s (y/n): ", prompt);
			val = ("y".equalsIgnoreCase(valStr) || "yes".equalsIgnoreCase(valStr));
			if (!val && !"n".equalsIgnoreCase(valStr) && !"no".equalsIgnoreCase(valStr)) {
				println("Please enter a y or n...");
				val = null;
			}
		}
		return val;
	}

	/**
	 * Returns a formatted string using the specified format string and arguments.
	 * @param message a printf-style formatted message
	 * @param args optional arguments for the formatted string
	 * @return a formatted string
	 * @see String#format(String,Object...)
	 */
	public static String sprintf(final String message, final Object... args) {
		return String.format(message, args);
	}

	/**
	 * Prints a formatted message to the terminal.
	 * @param message a printf-style formatted message
	 * @param args optional arguments for the formatted string
	 */
	public static void printf(final String message, final Object... args) {
		final Console console = System.console();
		if (console == null) {
			System.out.printf(message, args);
		} else {
			console.printf(message, args);
		}
	}

	/**
	 * Prints a message to the terminal.
	 * @param message the message to print
	 */
	public static void println(final String message) {
		final Console console = System.console();
		if (console == null) {
			System.out.println(message);
		} else {
			console.printf("%s%n", message);
		}
	}

	/**
	 * Prints the formatted prompt and then reads a single line from the terminal.
	 * @param prompt a printf-style formatted prompt
	 * @param args optional arguments for the formatted prompt
	 * @return the line read from the terminal
	 */
	public static String readLine(final String prompt, final Object... args) {
		String line = null;
		final Console console = System.console();
		if (console == null) {
			System.out.printf(prompt, args);
			final BufferedReader bufferedReader = new BufferedReader(
					new InputStreamReader(System.in));
			try {
				line = bufferedReader.readLine();
			} catch (IOException ioe) {
				// Ignore
			}
		} else {
			line = console.readLine(prompt, args);
		}
		return line;
	}

	/**
	 * Prompt the user to select an option.
	 * @param prompt the prompt message
	 * @param failMsg the message to show if bad input is received
	 * @param optionsMap the options to choose from; keys are shown as the options, selected value is returned
	 * @param allowDone whether the use must select an option or may exit
	 * @return the selected value or null if allowDone is true and the user selected to exit
	 */
	public static <V> V readOption(final String prompt, final String failMsg, 
			final Map<String, V> optionsMap, final boolean allowDone) {
		if (!allowDone && optionsMap.isEmpty()) {
			throw new IllegalArgumentException("Must have at least one option if allowDone is false");
		}
		V selection = null;
		if (!optionsMap.isEmpty()) {
			final List<Entry<String, V>> options = new ArrayList<Entry<String, V>>(optionsMap.entrySet());
			if (!allowDone && options.size() == 1) {
				selection = options.get(0).getValue();
			} else {
				final String exitString = (allowDone) ? "done" : null;
				final int min = 1;
				final int max = options.size();
				Integer val = null;
				while (val == null) {
					println(prompt);
					for (int i = 0; i < options.size(); i++) {
						printf("\t%d) %s%n", i + 1, options.get(i).getKey());
					}
					final String valStr = (exitString == null) 
						? readLine("Select (%d-%d): ", min, max)
						: readLine("Select (%d-%d) or '%s' to cancel: ", min, max, exitString);
					try {
						if (exitString != null && exitString.equals(valStr)) {
							break;
						}
						val = Integer.valueOf(valStr);
						if (val == null || val < min || val > max) {
							println(failMsg);
							val = null;
						}
					} catch (NumberFormatException nfe) {
						println("Bad number");
					}
				}
				selection = (val == null) ? null : options.get(val - 1).getValue();
			}
		}
		return selection;
	}

	private IOUtils(){
		// Utility class
	}
}
