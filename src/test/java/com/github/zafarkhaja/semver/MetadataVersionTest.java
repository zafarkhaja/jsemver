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

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Zafar Khaja {@literal <zafarkhaja@gmail.com>}
 */
class MetadataVersionTest {

    @Nested
    class CoreFunctionality {

        @Test
        void mustCompareEachIdentifierSeparately() {
            MetadataVersion v1 = new MetadataVersion(
                new String[] {"beta", "2", "abc"}
            );
            MetadataVersion v2 = new MetadataVersion(
                new String[] {"beta", "1", "edf"}
            );
            assertTrue(0 < v1.compareTo(v2));
        }

        @Test
        void shouldCompareIdentifiersCountIfCommonIdentifiersAreEqual() {
            MetadataVersion v1 = new MetadataVersion(
                new String[] {"beta", "abc"}
            );
            MetadataVersion v2 = new MetadataVersion(
                new String[] {"beta", "abc", "def"}
            );
            assertTrue(0 > v1.compareTo(v2));
        }

        @Test
        void shouldCompareDigitsOnlyIdentifiersNumerically() {
            MetadataVersion v1 = new MetadataVersion(
                new String[] {"alpha", "123"}
            );
            MetadataVersion v2 = new MetadataVersion(
                new String[] {"alpha", "321"}
            );
            assertTrue(0 > v1.compareTo(v2));
        }

        @Test
        void shouldCompareMixedIdentifiersLexicallyInAsciiSortOrder() {
            MetadataVersion v1 = new MetadataVersion(
                new String[] {"beta", "abc"}
            );
            MetadataVersion v2 = new MetadataVersion(
                new String[] {"beta", "111"}
            );
            assertTrue(0 < v1.compareTo(v2));
        }

        @Test
        void shouldReturnNegativeWhenComparedToNullMetadataVersion() {
            MetadataVersion v1 = new MetadataVersion(new String[] {});
            MetadataVersion v2 = MetadataVersion.NULL;
            assertTrue(0 > v1.compareTo(v2));
        }

        @Test
        void shouldOverrideEqualsMethod() {
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
        void shouldProvideIncrementMethod() {
            MetadataVersion v1 = new MetadataVersion(
                new String[] {"alpha", "1"}
            );
            MetadataVersion v2 = v1.increment();
            assertEquals("alpha.2", v2.toString());
        }

        @Test
        void shouldAppendOneAsLastIdentifierIfLastOneIsAlphaNumericWhenIncrementing() {
            MetadataVersion v1 = new MetadataVersion(
                new String[] {"alpha"}
            );
            MetadataVersion v2 = v1.increment();
            assertEquals("alpha.1", v2.toString());
        }

        @Test
        void shouldBeImmutable() {
            MetadataVersion v1 = new MetadataVersion(
                new String[] {"alpha", "1"}
            );
            MetadataVersion v2 = v1.increment();
            assertNotSame(v1, v2);
        }
    }

    @Nested
    class NullMetadataVersion {

        @Test
        void shouldReturnEmptyStringOnToString() {
            MetadataVersion v = MetadataVersion.NULL;
            assertTrue(v.toString().isEmpty());
        }

        @Test
        void shouldReturnZeroOnHashCode() {
            MetadataVersion v = MetadataVersion.NULL;
            assertEquals(0, v.hashCode());
        }

        @Test
        void shouldBeEqualOnlyToItsType() {
            MetadataVersion v1 = MetadataVersion.NULL;
            MetadataVersion v2 = MetadataVersion.NULL;
            MetadataVersion v3 = new MetadataVersion(new String[] {});
            assertTrue(v1.equals(v2));
            assertTrue(v2.equals(v1));
            assertFalse(v1.equals(v3));
        }

        @Test
        void shouldReturnPositiveWhenComparedToNonNullMetadataVersion() {
            MetadataVersion v1 = MetadataVersion.NULL;
            MetadataVersion v2 = new MetadataVersion(new String[] {});
            assertTrue(0 < v1.compareTo(v2));
        }

        @Test
        void shouldReturnZeroWhenComparedToNullMetadataVersion() {
            MetadataVersion v1 = MetadataVersion.NULL;
            MetadataVersion v2 = MetadataVersion.NULL;
            assertTrue(0 == v1.compareTo(v2));
        }

        @Test
        void shouldThrowNullPointerExceptionIfIncremented() {
            assertThrows(
                NullPointerException.class,
                MetadataVersion.NULL::increment,
                "Should throw NullPointerException when incremented"
            );
        }
    }

    @Nested
    class EqualsMethod {

        @Test
        void shouldBeReflexive() {
            MetadataVersion v = new MetadataVersion(
                new String[] {"alpha", "123"}
            );
            assertTrue(v.equals(v));
        }

        @Test
        void shouldBeSymmetric() {
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
        void shouldBeTransitive() {
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
        void shouldBeConsistent() {
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
        void shouldReturnFalseIfOtherVersionIsOfDifferentType() {
            MetadataVersion v = new MetadataVersion(
                new String[] {"alpha", "123"}
            );
            assertFalse(v.equals(new String("alpha.123")));
        }

        @Test
        void shouldReturnFalseIfOtherVersionIsNull() {
            MetadataVersion v1 = new MetadataVersion(
                new String[] {"alpha", "123"}
            );
            MetadataVersion v2 = null;
            assertFalse(v1.equals(v2));
        }
    }

    @Nested
    class HashCodeMethod {

        @Test
        void shouldReturnSameHashCodeIfVersionsAreEqual() {
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

    @Nested
    class ToStringMethod {

        @Test
        void shouldReturnStringRepresentation() {
            String value = "beta.abc.def";
            MetadataVersion v = new MetadataVersion(value.split("\\."));
            assertEquals(value, v.toString());
        }
    }
}
