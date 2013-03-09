package net.greghaines.risky.utils;

import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;

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
	 * @return a valid integer
	 */
	public static Integer readInteger(final String prompt, final int min, final int max) {
		if (min > max) {
			throw new IllegalArgumentException("min should not be greater than max (min=" + 
						min + ",max=" + max + ")");
		}
		Integer val = null;
		while (val == null) {
			final String valStr = readLine("%s (%d-%d): ", prompt, min, max);
			try {
				val = Integer.valueOf(valStr);
				if (val == null || val < min || val > max) {
					printf("Please enter number greater than %d and less than %d...%n", min, max);
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

	private IOUtils(){
		// Utility class
	}
}
