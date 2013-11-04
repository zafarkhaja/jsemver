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

import org.junit.Test;
import static com.github.zafarkhaja.semver.VersionParser.Char.*;
import static org.junit.Assert.*;

/**
 *
 * @author Zafar Khaja <zafarkhaja@gmail.com>
 */
public class VersionParserCharTest {

    @Test
    public void shouldBeMatchedByDigit() {
        assertTrue(DIGIT.isMatchedBy('0'));
        assertTrue(DIGIT.isMatchedBy('9'));
        assertFalse(DIGIT.isMatchedBy('a'));
        assertFalse(DIGIT.isMatchedBy('A'));
    }

    @Test
    public void shouldBeMatchedByLetter() {
        assertTrue(LETTER.isMatchedBy('a'));
        assertTrue(LETTER.isMatchedBy('A'));
        assertFalse(LETTER.isMatchedBy('0'));
        assertFalse(LETTER.isMatchedBy('9'));
    }

    @Test
    public void shouldBeMatchedByDot() {
        assertTrue(DOT.isMatchedBy('.'));
        assertFalse(DOT.isMatchedBy('-'));
        assertFalse(DOT.isMatchedBy('0'));
        assertFalse(DOT.isMatchedBy('9'));
    }

    @Test
    public void shouldBeMatchedByHyphen() {
        assertTrue(HYPHEN.isMatchedBy('-'));
        assertFalse(HYPHEN.isMatchedBy('+'));
        assertFalse(HYPHEN.isMatchedBy('a'));
        assertFalse(HYPHEN.isMatchedBy('0'));
    }

    @Test
    public void shouldBeMatchedByPlus() {
        assertTrue(PLUS.isMatchedBy('+'));
        assertFalse(PLUS.isMatchedBy('-'));
        assertFalse(PLUS.isMatchedBy('a'));
        assertFalse(PLUS.isMatchedBy('0'));
    }

    @Test
    public void shouldBeMatchedByEol() {
        assertTrue(EOL.isMatchedBy(null));
        assertFalse(EOL.isMatchedBy('-'));
        assertFalse(EOL.isMatchedBy('a'));
        assertFalse(EOL.isMatchedBy('0'));
    }
}
