package com.github.zafarkhaja.semver.compatibility;

import com.github.zafarkhaja.semver.ParseException;

/**
 * Strategy can be implemented to interact when version increment is missing.
 */
public interface MissingIncrementStrategy
{

	int missingIncrementValue() throws ParseException;
}
