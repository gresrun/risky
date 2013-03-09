package net.greghaines.risky;

import static net.greghaines.risky.utils.IOUtils.println;

import net.greghaines.risky.model.GameSession;

/**
 * Risk(y) main.
 * @author Greg Haines
 */
public final class Main {

	/**
	 * Entry point for the game.
	 * @param args <i>ignored</i>
	 */
	public static void main(final String... args) {
		println("===== RISK(y) =====");
		new GameSession().play();
	}

	private Main() {
		// Utility class
	}
}
