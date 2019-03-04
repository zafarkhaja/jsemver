package com.github.zafarkhaja.semver.compatibility;

import com.github.zafarkhaja.semver.ParseException;

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
