package org.mineacademy.fo.remain;

import org.bukkit.boss.BarStyle;

/**
 * A wrapper for {@link BarStyle}
 */
public enum CompBarStyle {

	/**
	 * Makes the boss bar solid (no segments)
	 */
	SOLID,

	/**
	 * Splits the boss bar into 6 segments
	 */
	SEGMENTED_6,

	/**
	 * Splits the boss bar into 10 segments
	 */
	SEGMENTED_10,

	/**
	 * Splits the boss bar into 12 segments
	 */
	SEGMENTED_12,

	/**
	 * Splits the boss bar into 20 segments
	 */
	SEGMENTED_20,
}