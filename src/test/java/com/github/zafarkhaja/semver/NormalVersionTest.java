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

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

/**
 *
 * @author Zafar Khaja <zafarkhaja@gmail.com>
 */
@RunWith(Enclosed.class)
public class NormalVersionTest {

    public static class CoreFunctionalityTest {

        @Test
        public void mustConsistOfMajorMinorAndPatchVersions() {
            NormalVersion v = new NormalVersion(1, 2, 3);
            assertEquals(1, v.getMajor());
            assertEquals(2, v.getMinor());
            assertEquals(3, v.getPatch());
        }

        @Test
        public void mustTakeTheFormOfXDotYDotZWhereXyzAreNonNegativeIntegers() {
            NormalVersion v = new NormalVersion(1, 2, 3);
            assertEquals("1.2.3", v.toString());
        }

        @Test
        public void shouldAcceptOnlyNonNegativeMajorMinorAndPatchVersions() {
            int[][] invalidVersions = {{-1, 2, 3}, {1, -2, 3}, {1, 2, -3}};
            for (int[] versionParts : invalidVersions) {
                try {
                    new NormalVersion(
                        versionParts[0],
                        versionParts[1],
                        versionParts[2]
                    );
                } catch (IllegalArgumentException e) {
                    continue;
                }
                fail("Major, minor and patch versions MUST be non-negative integers.");
            }
        }

        @Test
        public void mustIncreaseEachElementNumericallyByIncrementsOfOne() {
            int major = 1, minor = 2, patch = 3;
            NormalVersion v = new NormalVersion(major, minor, patch);
            NormalVersion incrementedPatch = v.incrementPatch();
            assertEquals(patch + 1, incrementedPatch.getPatch());
            NormalVersion incrementedMinor = v.incrementMinor();
            assertEquals(minor + 1, incrementedMinor.getMinor());
            NormalVersion incrementedMajor = v.incrementMajor();
            assertEquals(major + 1, incrementedMajor.getMajor());
        }

        @Test
        public void mustResetMinorAndPatchToZeroWhenMajorIsIncremented() {
            NormalVersion v = new NormalVersion(1, 2, 3);
            NormalVersion incremented = v.incrementMajor();
            assertEquals(2, incremented.getMajor());
            assertEquals(0, incremented.getMinor());
            assertEquals(0, incremented.getPatch());
        }

        @Test
        public void mustResetPatchToZeroWhenMinorIsIncremented() {
            NormalVersion v = new NormalVersion(1, 2, 3);
            NormalVersion incremented = v.incrementMinor();
            assertEquals(1, incremented.getMajor());
            assertEquals(3, incremented.getMinor());
            assertEquals(0, incremented.getPatch());
        }

        @Test
        public void mustCompareMajorMinorAndPatchNumerically() {
            NormalVersion v = new NormalVersion(1, 2, 3);
            assertTrue(0 < v.compareTo(new NormalVersion(0, 2, 3)));
            assertTrue(0 == v.compareTo(new NormalVersion(1, 2, 3)));
            assertTrue(0 > v.compareTo(new NormalVersion(1, 2, 4)));
        }

        @Test
        public void shouldOverrideEqualsMethod() {
            NormalVersion v1 = new NormalVersion(1, 2, 3);
            NormalVersion v2 = new NormalVersion(1, 2, 3);
            NormalVersion v3 = new NormalVersion(3, 2, 1);
            assertTrue(v1.equals(v2));
            assertFalse(v1.equals(v3));
        }

        @Test
        public void shoudBeImmutable() {
            NormalVersion version = new NormalVersion(1, 2, 3);
            NormalVersion incementedMajor = version.incrementMajor();
            assertNotSame(version, incementedMajor);
            NormalVersion incementedMinor = version.incrementMinor();
            assertNotSame(version, incementedMinor);
            NormalVersion incementedPatch = version.incrementPatch();
            assertNotSame(version, incementedPatch);
        }
    }

    public static class EqualsMethodTest {

        @Test
        public void shouldBeReflexive() {
            NormalVersion v = new NormalVersion(1, 2, 3);
            assertTrue(v.equals(v));
        }

        @Test
        public void shouldBeSymmetric() {
            NormalVersion v1 = new NormalVersion(1, 2, 3);
            NormalVersion v2 = new NormalVersion(1, 2, 3);
            assertTrue(v1.equals(v2));
            assertTrue(v2.equals(v1));
        }

        @Test
        public void shouldBeTransitive() {
            NormalVersion v1 = new NormalVersion(1, 2, 3);
            NormalVersion v2 = new NormalVersion(1, 2, 3);
            NormalVersion v3 = new NormalVersion(1, 2, 3);
            assertTrue(v1.equals(v2));
            assertTrue(v2.equals(v3));
            assertTrue(v1.equals(v3));
        }

        @Test
        public void shouldBeConsistent() {
            NormalVersion v1 = new NormalVersion(1, 2, 3);
            NormalVersion v2 = new NormalVersion(1, 2, 3);
            assertTrue(v1.equals(v2));
            assertTrue(v1.equals(v2));
            assertTrue(v1.equals(v2));
        }

        @Test
        public void shouldReturnFalseIfOtherVersionIsOfDifferentType() {
            NormalVersion v = new NormalVersion(1, 2, 3);
            assertFalse(v.equals(new String("1.2.3")));
        }

        @Test
        public void shouldReturnFalseIfOtherVersionIsNull() {
            NormalVersion v1 = new NormalVersion(1, 2, 3);
            NormalVersion v2 = null;
            assertFalse(v1.equals(v2));
        }
    }

    public static class HashCodeMethodTest {

        @Test
        public void shouldReturnSameHashCodeIfVersionsAreEqual() {
            NormalVersion v1 = new NormalVersion(1, 2, 3);
            NormalVersion v2 = new NormalVersion(1, 2, 3);
            assertTrue(v1.equals(v2));
            assertEquals(v1.hashCode(), v2.hashCode());
        }
    }

    public static class ToStringMethodTest {

        @Test
        public void shouldReturnStringRepresentation() {
            NormalVersion v = new NormalVersion(1, 2, 3);
            assertEquals("1.2.3", v.toString());
        }
    }
}
