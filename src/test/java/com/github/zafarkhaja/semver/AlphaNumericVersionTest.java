/*
 * The MIT License
 *
 * Copyright 2013 Zafar Khaja <zafarkhaja@gmail.com>.
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
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

/**
 *
 * @author Zafar Khaja <zafarkhaja@gmail.com>
 */
@RunWith(Enclosed.class)
public class AlphaNumericVersionTest {

    public static class CoreFunctionalityTest {

        @Test
        public void mustConsistOfDotSeparatedIdentifiersOfAlphaNumericsAndHyphen() {
            String[] invalidVersions = {
                null,
                "",
                "123!",
                "1a:2b:3c",
                "123,abc,123",
            };
            for (String ver : invalidVersions) {
                try {
                    AlphaNumericVersion v = new AlphaNumericVersion(ver);
                } catch (Exception e) {
                    continue;
                }
                fail("Alpha-numeric version MUST consist of dot separated identifiers [0-9A-Za-z-]");
            }
        }

        @Test
        public void mustCompareEachIdentifierSeparately() {
            AlphaNumericVersion v1 = new AlphaNumericVersion("beta.2.abc");
            AlphaNumericVersion v2 = new AlphaNumericVersion("beta.1.edf");
            assertTrue(0 < v1.compareTo(v2));
        }

        @Test
        public void shouldCompareIdentifiersCountIfCommonIdentifiersAreEqual() {
            AlphaNumericVersion v1 = new AlphaNumericVersion("beta.abc");
            AlphaNumericVersion v2 = new AlphaNumericVersion("beta.abc.def");
            assertTrue(0 > v1.compareTo(v2));
        }

        @Test
        public void shouldComapareDigitsOnlyIdentifiersNumerically() {
            AlphaNumericVersion v1 = new AlphaNumericVersion("alpha.123");
            AlphaNumericVersion v2 = new AlphaNumericVersion("alpha.321");
            assertTrue(0 > v1.compareTo(v2));
        }

        @Test
        public void shouldCompareMixedIdentifiersLexicallyInAsciiSortOrder() {
            AlphaNumericVersion v1 = new AlphaNumericVersion("beta.abc");
            AlphaNumericVersion v2 = new AlphaNumericVersion("beta.111");
            assertTrue(0 < v1.compareTo(v2));
        }

        @Test
        public void shouldOverrideEqualsMethod() {
            AlphaNumericVersion v1 = new AlphaNumericVersion("alpha.123");
            AlphaNumericVersion v2 = new AlphaNumericVersion("alpha.123");
            AlphaNumericVersion v3 = new AlphaNumericVersion("alpha.321");
            assertTrue(v1.equals(v2));
            assertFalse(v1.equals(v3));
        }
    }

    public static class EqualsMethodTest {

        @Test
        public void shouldBeReflexive() {
            AlphaNumericVersion v = new AlphaNumericVersion("alpha.123");
            assertTrue(v.equals(v));
        }

        @Test
        public void shouldBeSymmetric() {
            AlphaNumericVersion v1 = new AlphaNumericVersion("alpha.123");
            AlphaNumericVersion v2 = new AlphaNumericVersion("alpha.123");
            assertTrue(v1.equals(v2));
            assertTrue(v2.equals(v1));
        }

        @Test
        public void shouldBeTransitive() {
            AlphaNumericVersion v1 = new AlphaNumericVersion("alpha.123");
            AlphaNumericVersion v2 = new AlphaNumericVersion("alpha.123");
            AlphaNumericVersion v3 = new AlphaNumericVersion("alpha.123");
            assertTrue(v1.equals(v2));
            assertTrue(v2.equals(v3));
            assertTrue(v1.equals(v3));
        }

        @Test
        public void shouldBeConsistent() {
            AlphaNumericVersion v1 = new AlphaNumericVersion("alpha.123");
            AlphaNumericVersion v2 = new AlphaNumericVersion("alpha.123");
            assertTrue(v1.equals(v2));
            assertTrue(v1.equals(v2));
            assertTrue(v1.equals(v2));
        }

        @Test
        public void shouldReturnFalseIfOtherVersionIsOfDifferentType() {
            AlphaNumericVersion v = new AlphaNumericVersion("alpha.123");
            assertFalse(v.equals(new String("alpha.123")));
        }

        @Test
        public void shouldReturnFalseIfOtherVersionIsNull() {
            AlphaNumericVersion v1 = new AlphaNumericVersion("alpha.123");
            AlphaNumericVersion v2 = null;
            assertFalse(v1.equals(v2));
        }
    }

    public static class HashCodeMethodTest {

        @Test
        public void shouldReturnSameHashCodeIfVersionsAreEqual() {
            AlphaNumericVersion v1 = new AlphaNumericVersion("alpha.123");
            AlphaNumericVersion v2 = new AlphaNumericVersion("alpha.123");
            assertTrue(v1.equals(v2));
            assertEquals(v1.hashCode(), v2.hashCode());
        }
    }

    public static class ToStringMethodTest {

        @Test
        public void shouldReturnStringRepresentation() {
            String value = "beta.abc.def";
            AlphaNumericVersion v = new AlphaNumericVersion(value);
            assertEquals(value, v.toString());
        }
    }
}
