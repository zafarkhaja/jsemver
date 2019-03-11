/*
 * The MIT License
 *
 * Copyright 2012-2014 Zafar Khaja <zafarkhaja@gmail.com>.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.github.zafarkhaja.semver;

import com.github.zafarkhaja.semver.compatibility.DefaultValueMissingIncrementStrategy;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Zafar Khaja <zafarkhaja@gmail.com>
 */
public class CompatibleVersionParserTest
{
	
	@Test
	public void shouldParseNormalVersion()
	{
		Version version = VersionParser.parseCompatibleSemVer("1.0.0");
		Version expected = new Version(new NormalVersion(1, 0, 0), MetadataVersion.NULL);
		assertEquals(expected, version);
	}
	
	@Test
	public void missingMajorRevision()
	{
		try {
			VersionParser.parseCompatibleSemVer("alpha");
		} catch (ParseException e) {
			return;
		}
		fail("MAJOR increment is always mandatory!");
	}
	
	@Test
	public void allowMissingPatchRevision()
	{
		Version version = VersionParser.parseCompatibleSemVer("1.0", DefaultValueMissingIncrementStrategy.ZERO);
		Version expected = new Version(new NormalVersion(1, 0, 0), MetadataVersion.NULL);
		assertEquals(expected, version);
	}
	
	@Test
	public void allowMissingPatchRevisionFail()
	{
		try {
			VersionParser.parseCompatibleSemVer("1", DefaultValueMissingIncrementStrategy.ZERO);
		} catch (ParseException e) {
			return;
		}
		fail("MINOR increment is required!");
	}
	
	@Test
	public void allowMissingMinorRevision()
	{
		Version version = VersionParser
			.parseCompatibleSemVer("1", DefaultValueMissingIncrementStrategy.ZERO, DefaultValueMissingIncrementStrategy.ZERO);
		Version expected = new Version(new NormalVersion(1, 0, 0), MetadataVersion.NULL);
		assertEquals(expected, version);
	}

	
}
