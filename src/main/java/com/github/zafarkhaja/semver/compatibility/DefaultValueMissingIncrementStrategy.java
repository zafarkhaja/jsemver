package com.github.zafarkhaja.semver.compatibility;

import com.github.zafarkhaja.semver.ParseException;

/**
 * Strategy used to set missing increment to a default value.
 */
public class DefaultValueMissingIncrementStrategy implements MissingIncrementStrategy
{
	
	public static final MissingIncrementStrategy ZERO = new DefaultValueMissingIncrementStrategy(0);
	
	public final int defaultIncrement;
	
	private DefaultValueMissingIncrementStrategy(int defaultIncrement)
	{
		this.defaultIncrement = defaultIncrement;
	}
	
	public int missingIncrementValue() throws ParseException {
		return this.defaultIncrement;
	}
}
