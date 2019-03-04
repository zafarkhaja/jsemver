package com.github.zafarkhaja.semver.compatibility;

import com.github.zafarkhaja.semver.ParseException;

public class FailOnMissingIncrementStrategy implements MissingIncrementStrategy
{
	
	public static final MissingIncrementStrategy MAJOR = new FailOnMissingIncrementStrategy("MAJOR");
	public static final MissingIncrementStrategy MINOR = new FailOnMissingIncrementStrategy("MINOR");
	public static final MissingIncrementStrategy PATCH = new FailOnMissingIncrementStrategy("PATCH");
	
	public final String increment;
	
	private FailOnMissingIncrementStrategy(String increment)
	{
		this.increment = increment;
	}
	
	public int missingIncrementValue() throws ParseException {
		throw new ParseException(this.increment+" increment is mandatory!");
	}
}
