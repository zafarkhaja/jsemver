/*
 * The MIT License
 *
 * Copyright 2012-2023 Zafar Khaja <zafarkhaja@gmail.com>.
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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Zafar Khaja {@literal <zafarkhaja@gmail.com>}
 */
class VersionParserTest {

    @Test
    void shouldParseNormalVersion() {
        Version version = VersionParser.parseVersionCore("1.0.0");
        assertEquals(Version.of(1, 0, 0), version);
    }

    @Test
    void shouldRaiseErrorIfNumericIdentifierHasLeadingZeroes() {
        assertThrows(
            ParseException.class,
            () -> VersionParser.parseVersionCore("01.1.0"),
            "Numeric identifier MUST NOT contain leading zeroes"
        );
    }

    @Test
    void shouldParsePreReleaseVersion() {
        String[] preRelease = VersionParser.parsePreRelease("beta-1.1");
        assertArrayEquals(new String[] {"beta-1", "1"}, preRelease);
    }

    @Test
    void shouldNotAllowDigitsInPreReleaseVersion() {
        assertThrows(
            ParseException.class,
            () -> VersionParser.parsePreRelease("alpha.01"),
            "Should not allow digits in pre-release version"
        );
    }

    @Test
    void shouldRaiseErrorForEmptyPreReleaseIdentifier() {
        assertThrows(
            ParseException.class,
            () -> VersionParser.parsePreRelease("beta-1..1"),
            "Identifiers MUST NOT be empty"
        );
    }

    @Test
    void shouldParseBuildMetadata() {
        String[] build = VersionParser.parseBuild("build.1");
        assertArrayEquals(new String[] {"build", "1"}, build);
    }

    @Test
    void shouldAllowDigitsInBuildMetadata() {
        assertDoesNotThrow(
            () -> VersionParser.parseBuild("build.01"),
            "Should allow digits in build metadata"
        );
    }

    @Test
    void shouldRaiseErrorForEmptyBuildIdentifier() {
        assertThrows(
            ParseException.class,
            () -> VersionParser.parseBuild(".build.01"),
            "Identifiers MUST NOT be empty"
        );
    }

    @Test
    void shouldParseValidSemVer() {
        VersionParser parser = new VersionParser("1.0.0-rc.2+build.05");
        Version v = parser.parse(null);
        assertEquals(Version.of(1, 0, 0, "rc.2", "build.05"), v);
    }

    @Test
    void shouldParseShortVersionCoresWithMajorAndMinorVersionsInLenientMode() {
        VersionParser parser = new VersionParser("1.2-rc.2+build.05", false);
        Version v = parser.parse(null);
        assertEquals(Version.of(1, 2, 0, "rc.2", "build.05"), v);
    }

    @Test
    void shouldParseShortVersionCoresWithMajorVersionInLenientMode() {
        VersionParser parser = new VersionParser("1-rc.2+build.05", false);
        Version v = parser.parse(null);
        assertEquals(Version.of(1, 0, 0, "rc.2", "build.05"), v);
    }

    @ParameterizedTest
    @ValueSource(strings = {"1", "1.2"})
    void shouldNotAllowShortVersionCoresInStrictMode(String s) {
        VersionParser parser = new VersionParser(s, true);
        assertThrows(ParseException.class, () -> parser.parse(null));
    }

    @Test
    void shouldRaiseErrorForIllegalInputString() {
        for (String illegal : new String[] { "", null }) {
            assertThrows(
                IllegalArgumentException.class,
                () -> new VersionParser(illegal),
                "Should raise error for illegal input string"
            );
        }
    }

    @Test
    void shouldSupportLongNumericIdentifiers() {
        long l = Long.MAX_VALUE;
        String version = l + "." + l + "." + l + "-" + l + "+" + l;
        Version expected = Version.of(l, l, l, String.valueOf(l), String.valueOf(l));
        Version actual = VersionParser.parseValidSemVer(version);
        assertEquals(expected, actual);
    }

    @Test
    void shouldCheckForNumericIdentifierOverflows() {
        // Long.MAX_VALUE == 9223372036854775807L;
        Exception e = assertThrows(ParseException.class, () -> Version.parse("1.0.0-9223372036854775808"));
        assertEquals("Numeric identifier overflow", e.getMessage());
    }
}
