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

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

/**
 *
 * @author Zafar Khaja {@literal <zafarkhaja@gmail.com>}
 */
@RunWith(Enclosed.class)
public class MetadataVersionTest {

    public static class CoreFunctionalityTest {

        @Test
        public void mustCompareEachIdentifierSeparately() {
            MetadataVersion v1 = new MetadataVersion(
                new String[] {"beta", "2", "abc"}
            );
            MetadataVersion v2 = new MetadataVersion(
                new String[] {"beta", "1", "edf"}
            );
            assertTrue(0 < v1.compareTo(v2));
        }

        @Test
        public void shouldCompareIdentifiersCountIfCommonIdentifiersAreEqual() {
            MetadataVersion v1 = new MetadataVersion(
                new String[] {"beta", "abc"}
            );
            MetadataVersion v2 = new MetadataVersion(
                new String[] {"beta", "abc", "def"}
            );
            assertTrue(0 > v1.compareTo(v2));
        }

        @Test
        public void shouldComapareDigitsOnlyIdentifiersNumerically() {
            MetadataVersion v1 = new MetadataVersion(
                new String[] {"alpha", "123"}
            );
            MetadataVersion v2 = new MetadataVersion(
                new String[] {"alpha", "321"}
            );
            assertTrue(0 > v1.compareTo(v2));
        }

        @Test
        public void shouldCompareMixedIdentifiersLexicallyInAsciiSortOrder() {
            MetadataVersion v1 = new MetadataVersion(
                new String[] {"beta", "abc"}
            );
            MetadataVersion v2 = new MetadataVersion(
                new String[] {"beta", "111"}
            );
            assertTrue(0 < v1.compareTo(v2));
        }

        @Test
        public void shouldReturnNegativeWhenComparedToNullMetadataVersion() {
            MetadataVersion v1 = new MetadataVersion(new String[] {});
            MetadataVersion v2 = MetadataVersion.NULL;
            assertTrue(0 > v1.compareTo(v2));
        }

        @Test
        public void shouldOverrideEqualsMethod() {
            MetadataVersion v1 = new MetadataVersion(
                new String[] {"alpha", "123"}
            );
            MetadataVersion v2 = new MetadataVersion(
                new String[] {"alpha", "123"}
            );
            MetadataVersion v3 = new MetadataVersion(
                new String[] {"alpha", "321"}
            );
            assertTrue(v1.equals(v2));
            assertFalse(v1.equals(v3));
        }

        @Test
        public void shouldProvideIncrementMethod() {
            MetadataVersion v1 = new MetadataVersion(
                new String[] {"alpha", "1"}
            );
            MetadataVersion v2 = v1.increment();
            assertEquals("alpha.2", v2.toString());
        }

        @Test
        public void shouldAppendOneAsLastIdentifierIfLastOneIsAlphaNumericWhenIncrementing() {
            MetadataVersion v1 = new MetadataVersion(
                new String[] {"alpha"}
            );
            MetadataVersion v2 = v1.increment();
            assertEquals("alpha.1", v2.toString());
        }

        @Test
        public void shouldBeImmutable() {
            MetadataVersion v1 = new MetadataVersion(
                new String[] {"alpha", "1"}
            );
            MetadataVersion v2 = v1.increment();
            assertNotSame(v1, v2);
        }
    }

    public static class NullMetadataVersionTest {

        @Test
        public void shouldReturnEmptyStringOnToString() {
            MetadataVersion v = MetadataVersion.NULL;
            assertTrue(v.toString().isEmpty());
        }

        @Test
        public void shouldReturnZeroOnHashCode() {
            MetadataVersion v = MetadataVersion.NULL;
            assertEquals(0, v.hashCode());
        }

        @Test
        public void shouldBeEqualOnlyToItsType() {
            MetadataVersion v1 = MetadataVersion.NULL;
            MetadataVersion v2 = MetadataVersion.NULL;
            MetadataVersion v3 = new MetadataVersion(new String[] {});
            assertTrue(v1.equals(v2));
            assertTrue(v2.equals(v1));
            assertFalse(v1.equals(v3));
        }

        @Test
        public void shouldReturnPositiveWhenComparedToNonNullMetadataVersion() {
            MetadataVersion v1 = MetadataVersion.NULL;
            MetadataVersion v2 = new MetadataVersion(new String[] {});
            assertTrue(0 < v1.compareTo(v2));
        }

        @Test
        public void shouldReturnZeroWhenComparedToNullMetadataVersion() {
            MetadataVersion v1 = MetadataVersion.NULL;
            MetadataVersion v2 = MetadataVersion.NULL;
            assertTrue(0 == v1.compareTo(v2));
        }

        @Test
        public void shouldThrowNullPointerExceptionIfIncremented() {
            try {
                MetadataVersion.NULL.increment();
            } catch (NullPointerException e) {
                return;
            }
            fail("Should throw NullPointerException when incremented");
        }
    }

    public static class EqualsMethodTest {

        @Test
        public void shouldBeReflexive() {
            MetadataVersion v = new MetadataVersion(
                new String[] {"alpha", "123"}
            );
            assertTrue(v.equals(v));
        }

        @Test
        public void shouldBeSymmetric() {
            MetadataVersion v1 = new MetadataVersion(
                new String[] {"alpha", "123"}
            );
            MetadataVersion v2 = new MetadataVersion(
                new String[] {"alpha", "123"}
            );
            assertTrue(v1.equals(v2));
            assertTrue(v2.equals(v1));
        }

        @Test
        public void shouldBeTransitive() {
            MetadataVersion v1 = new MetadataVersion(
                new String[] {"alpha", "123"}
            );
            MetadataVersion v2 = new MetadataVersion(
                new String[] {"alpha", "123"}
            );
            MetadataVersion v3 = new MetadataVersion(
                new String[] {"alpha", "123"}
            );
            assertTrue(v1.equals(v2));
            assertTrue(v2.equals(v3));
            assertTrue(v1.equals(v3));
        }

        @Test
        public void shouldBeConsistent() {
            MetadataVersion v1 = new MetadataVersion(
                new String[] {"alpha", "123"}
            );
            MetadataVersion v2 = new MetadataVersion(
                new String[] {"alpha", "123"}
            );
            assertTrue(v1.equals(v2));
            assertTrue(v1.equals(v2));
            assertTrue(v1.equals(v2));
        }

        @Test
        public void shouldReturnFalseIfOtherVersionIsOfDifferentType() {
            MetadataVersion v = new MetadataVersion(
                new String[] {"alpha", "123"}
            );
            assertFalse(v.equals(new String("alpha.123")));
        }

        @Test
        public void shouldReturnFalseIfOtherVersionIsNull() {
            MetadataVersion v1 = new MetadataVersion(
                new String[] {"alpha", "123"}
            );
            MetadataVersion v2 = null;
            assertFalse(v1.equals(v2));
        }
    }

    public static class HashCodeMethodTest {

        @Test
        public void shouldReturnSameHashCodeIfVersionsAreEqual() {
            MetadataVersion v1 = new MetadataVersion(
                new String[] {"alpha", "123"}
            );
            MetadataVersion v2 = new MetadataVersion(
                new String[] {"alpha", "123"}
            );
            assertTrue(v1.equals(v2));
            assertEquals(v1.hashCode(), v2.hashCode());
        }
    }

    public static class ToStringMethodTest {

        @Test
        public void shouldReturnStringRepresentation() {
            String value = "beta.abc.def";
            MetadataVersion v = new MetadataVersion(value.split("\\."));
            assertEquals(value, v.toString());
        }
    }
}
