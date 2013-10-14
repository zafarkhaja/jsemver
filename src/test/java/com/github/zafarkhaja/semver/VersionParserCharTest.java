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

import com.github.zafarkhaja.semver.VersionParser.Char;
import com.github.zafarkhaja.semver.VersionParser.CharStream;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Zafar Khaja <zafarkhaja@gmail.com>
 */
public class VersionParserCharTest {

    @Test
    public void shouldBeMatchedByDigit() {
        assertTrue(Char.DIGIT.isMatchedBy('0'));
        assertTrue(Char.DIGIT.isMatchedBy('9'));
        assertFalse(Char.DIGIT.isMatchedBy('a'));
        assertFalse(Char.DIGIT.isMatchedBy('A'));
    }

    @Test
    public void shouldBeMatchedByLetter() {
        assertTrue(Char.LETTER.isMatchedBy('a'));
        assertTrue(Char.LETTER.isMatchedBy('A'));
        assertFalse(Char.LETTER.isMatchedBy('0'));
        assertFalse(Char.LETTER.isMatchedBy('9'));
    }

    @Test
    public void shouldBeMatchedByDot() {
        assertTrue(Char.DOT.isMatchedBy('.'));
        assertFalse(Char.DOT.isMatchedBy('-'));
        assertFalse(Char.DOT.isMatchedBy('0'));
        assertFalse(Char.DOT.isMatchedBy('9'));
    }

    @Test
    public void shouldBeMatchedByHyphen() {
        assertTrue(Char.HYPHEN.isMatchedBy('-'));
        assertFalse(Char.HYPHEN.isMatchedBy('+'));
        assertFalse(Char.HYPHEN.isMatchedBy('a'));
        assertFalse(Char.HYPHEN.isMatchedBy('0'));
    }

    @Test
    public void shouldBeMatchedByPlus() {
        assertTrue(Char.PLUS.isMatchedBy('+'));
        assertFalse(Char.PLUS.isMatchedBy('-'));
        assertFalse(Char.PLUS.isMatchedBy('a'));
        assertFalse(Char.PLUS.isMatchedBy('0'));
    }

    @Test
    public void shouldBeMatchedByEol() {
        assertTrue(Char.EOL.isMatchedBy(CharStream.EOL));
        assertFalse(Char.EOL.isMatchedBy('-'));
        assertFalse(Char.EOL.isMatchedBy('a'));
        assertFalse(Char.EOL.isMatchedBy('0'));
    }
}
