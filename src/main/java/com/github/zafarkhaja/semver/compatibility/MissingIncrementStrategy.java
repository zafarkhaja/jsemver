package com.github.zafarkhaja.semver.compatibility;

import com.github.zafarkhaja.semver.ParseException;

public interface MissingIncrementStrategy
{
	int missingIncrementValue() throws ParseException;
}
