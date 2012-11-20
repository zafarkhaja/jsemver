/*
 * The MIT License
 *
 * Copyright 2012 Zafar Khaja <zafarkhaja@gmail.com>.
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

import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author Zafar Khaja <zafarkhaja@gmail.com>
 */
public class VersionTest {
    
    @Test public void
    mustConsistOfMajorMinorAndPatchVersions() {
        Version version = new Version("1.2.3");
        assertNotNull(version.getMajorVersion());
        assertNotNull(version.getMinorVersion());
        assertNotNull(version.getPatchVersion());
    }
    
    @Test public void
    mustTakeTheFormOfXDotYDotZWhereXyzAreNonNegativeIntegers() {
        Version version = new Version("1.2.3");
        assertEquals(1, version.getMajorVersion());
        assertEquals(2, version.getMinorVersion());
        assertEquals(3, version.getPatchVersion());
    }
    
    @Test public void
    shouldAcceptOnlyNonNegativeMajorMinorAndPatchVersions() {
        String[] versionStrings = new String[] {"-1.2.3", "1.-2.3", "1.2.-3"};
        for (String illegalVersion : versionStrings) {
            try {
                Version version = new Version(illegalVersion);
            } catch (IllegalArgumentException e) {
                continue;
            }
            fail("An expected exception has not been thrown.");
        }
    }
    
    @Test public void
    mustIncreaseEachElementNumericallyByIncrementsOfOne() {
        Version version = new Version("1.2.3");
        version.bumpPatchVersion();
        assertEquals(4, version.getPatchVersion());
        version.bumpMinorVersion();
        assertEquals(3, version.getMinorVersion());
        version.bumpMajorVersion();
        assertEquals(2, version.getMajorVersion());
    }
    
    @Test public void
    mustResetToZeroMinorAndPatchVersionsWhenMajorVersionIsIncremented() {
        Version version = new Version("1.2.3");
        version.bumpMajorVersion();
        assertEquals(2, version.getMajorVersion());
        assertEquals(0, version.getMinorVersion());
        assertEquals(0, version.getPatchVersion());
    }
    
    @Test public void
    mustResetToZeroPatchVersionWhenMinorVersionIsIncremented() {
        Version version = new Version("1.2.3");
        version.bumpMinorVersion();
        assertEquals(1, version.getMajorVersion());
        assertEquals(3, version.getMinorVersion());
        assertEquals(0, version.getPatchVersion());
    }
    
    @Test public void
    mayHavePreReleaseVersionFollowingPatchVersionAppendedWithDash() {
        Version version = new Version("1.2.3-alpha");
        assertEquals("alpha", version.getPreReleaseVersion());
    }
    
    @Test public void
    preReleaseVersionMustCompriseDotSeparatedIdentifiersOfAlphaNumericsAndDash() {
        Version version = new Version("1.0.0-x.7.z.92");
        assertEquals("x.7.z.92", version.getPreReleaseVersion());
    }
    
    @Test public void
    mayHaveBuildVersionFollowingPatchOrPreReleaseVersionsAppendedWithPlus() {
        Version version = new Version("1.2.3+build");
        assertEquals("build", version.getBuildVersion());
    }
    
    @Test public void
    buildVersionMustCompriseDotSeparatedIdentifiersOfAlphaNumericsAndDash() {
        Version version = new Version("1.3.7+build.11.e0f985a");
        assertEquals("build.11.e0f985a", version.getBuildVersion());
    }
    
    @Test public void
    mustCompareMajorMinorAndPatchVersionsNumerically() {
        Version version = new Version("1.2.3");
        assertEquals(1, version.compareTo(new Version("0.2.3")));
        assertEquals(0, version.compareTo(new Version("1.2.3")));
        assertEquals(-1, version.compareTo(new Version("1.2.4")));
    }
    
    @Test public void
    mustComparePreReleaseAndBuildVersionsByComparingEachIdentifierSeparately() {
        Version version1 = new Version("1.3.7+build.2.b8f12d7");
        Version version2 = new Version("1.3.7+build.11.e0f985a");
        assertEquals(-1, version1.compareTo(version2));
    }
    
    @Test public void
    shouldComparePreReleaseVersionsIfNormalVersionsAreEqual() {
        Version version1 = new Version("1.3.7-beta");
        Version version2 = new Version("1.3.7-alpha");
        assertEquals(1, version1.compareTo(version2));
    }
    
    @Test public void
    shouldCompareBuildVersionsIfNormalAndPreReleaseVersionsAreEqual() {
        Version version1 = new Version("1.3.7-beta+build.1");
        Version version2 = new Version("1.3.7-beta+build.2");
        assertEquals(-1, version1.compareTo(version2));
    }
    
    @Test public void
    shouldCompareAccordingToIdentifiersCountIfCommonIdentifiersAreEqual() {
        Version version1 = new Version("1.3.7-beta+build.3");
        Version version2 = new Version("1.3.7-beta+build");
        assertEquals(1, version1.compareTo(version2));
    }
    
    @Test public void
    numericIdentifiersShouldHaveLowerPrecedenceThanNonNumericIdentfiers() {
        Version version1 = new Version("1.3.7-beta+build.3");
        Version version2 = new Version("1.3.7-beta+build.a");
        assertEquals(-1, version1.compareTo(version2));
    }
    
    @Test public void
    shouldHaveGreaterThanConvenienceMethodReturningBoolean() {
        Version version1 = new Version("2.3.7");
        Version version2 = new Version("1.3.7");
        assertTrue(version1.greaterThan(version2));
        assertFalse(version2.greaterThan(version1));
        assertFalse(version1.greaterThan(version1));
    }
    
    @Test public void
    shouldHaveGreaterThanOrEqualsToConvenienceMethodReturningBoolean() {
        Version version1 = new Version("2.3.7");
        Version version2 = new Version("1.3.7");
        assertTrue(version1.greaterThanOrEqualsTo(version2));
        assertFalse(version2.greaterThanOrEqualsTo(version1));
        assertTrue(version1.greaterThanOrEqualsTo(version1));
    }
    
    @Test public void
    shouldHaveLessThanConvenienceMethodReturningBoolean() {
        Version version1 = new Version("2.3.7");
        Version version2 = new Version("1.3.7");
        assertFalse(version1.lessThan(version2));
        assertTrue(version2.lessThan(version1));
        assertFalse(version1.lessThan(version1));
    }
    
    @Test public void
    shouldHaveLessThanOrEqualsToConvenienceMethodReturningBoolean() {
        Version version1 = new Version("2.3.7");
        Version version2 = new Version("1.3.7");
        assertFalse(version1.lessThanOrEqualsTo(version2));
        assertTrue(version2.lessThanOrEqualsTo(version1));
        assertTrue(version1.lessThanOrEqualsTo(version1));
    }
    
    @Test public void
    shouldOverrideEqualsMethodForConvenience() {
        Version version1 = new Version("2.3.7");
        Version version2 = new Version("2.3.7");
        Version version3 = new Version("1.3.7");
        assertTrue(version1.equals(version1));
        assertTrue(version1.equals(version2));
        assertFalse(version1.equals(version3));
    }
    
    @Test public void
    equalsMethodShouldBeReflexive() {
        Version version1 = new Version("2.3.7");
        assertTrue(version1.equals(version1));
    }
    
    @Test public void
    equalsMethodShouldBeSymmetric() {
        Version version1 = new Version("2.3.7");
        Version version2 = new Version("2.3.7");
        assertTrue(version1.equals(version2));
        assertTrue(version2.equals(version1));
    }
    
    @Test public void
    equalsMethodShouldBeTransitive() {
        Version version1 = new Version("2.3.7");
        Version version2 = new Version("2.3.7");
        Version version3 = new Version("2.3.7");
        assertTrue(version1.equals(version2));
        assertTrue(version2.equals(version3));
        assertTrue(version1.equals(version3));
    }
    
    @Test public void
    equalsMethodShouldBeConsistent() {
        Version version1 = new Version("2.3.7");
        Version version2 = new Version("2.3.7");
        assertTrue(version1.equals(version2));
        assertTrue(version1.equals(version2));
        assertTrue(version1.equals(version2));
    }
    
    @Test public void
    equalsMethodShouldReturnFalseIfOtherVersionIsNotInstanceOfVersion() {
        Version version1 = new Version("2.3.7");
        assertFalse(version1.equals(new String("2.3.7")));
    }
    
    @Test public void
    equalsMethodShouldReturnFalseIfOtherVersionIsNull() {
        Version version1 = new Version("2.3.7");
        Version version2 = null;
        assertFalse(version1.equals(version2));
    }
    
    @Test public void
    hashCodeMethodShouldReturnSameHashCodeIfVersionsAreEqual() {
        Version version1 = new Version("2.3.7");
        Version version2 = new Version("2.3.7");
        assertTrue(version1.equals(version2));
        assertEquals(version1.hashCode(), version2.hashCode());
    }
}
