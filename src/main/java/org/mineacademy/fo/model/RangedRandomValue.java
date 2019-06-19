package org.mineacademy.fo.model;

import org.mineacademy.fo.RandomUtil;
import org.mineacademy.fo.Valid;

/**
 * Represents a numerical value in range.
 */
public final class RangedRandomValue extends RangedValue {

	/**
	 * Make a new ranged value with a fixed range
	 *
	 * @param value the fixed range
	 */
	public RangedRandomValue(int value) {
		this(value, value);
	}

	/**
	 * Make a new ranged value between two numbers.-
	 *
	 * @param min the ceiling
	 * @param max the floor
	 */
	public RangedRandomValue(int min, int max) {
		super(Integer.valueOf(min), Integer.valueOf(max));

		Valid.checkBoolean(min >= 0 && max >= 0, "Values may not be negative");
		Valid.checkBoolean(min <= max, "Minimum must be lower or equal maximum");
	}

	/**
	 * Get a value in range between {@link #min} and {@link #max}
	 *
	 * @return a random value
	 */
	public int getRandom() {
		return RandomUtil.nextBetween(getMinInt(), getMaxInt());
	}

	/**
	 * Check if a value is between {@link #min} and {@link #max}
	 *
	 * @param value the value
	 * @return whether or not the value is in range
	 */
	public boolean isInRange(int value) {
		return value >= getMinInt() && value <= getMaxInt();
	}

	/**
	 * Create a {@link RangedValue} from a line
	 * Example: 1-10
	 *          5 - 60
	 *          4
	 */
	public static RangedRandomValue parse(String line) {
		final RangedValue r = RangedValue.parse(line);

		return new RangedRandomValue(r.getMinInt(), r.getMaxInt());
	}
}
