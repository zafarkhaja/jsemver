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
public class VersionParserCharStreamTest {

    @Test
    public void shouldBeBackedByCharArray() {
        String input = "abc";
        CharStream chars = new CharStream(input);
        assertArrayEquals(input.toCharArray(), chars.toArray());
    }

    @Test
    public void shouldNotReturnRealCharArray() {
        CharStream chars = new CharStream("abc");
        char[] charArray = chars.toArray();
        charArray[0] = 'z';
        assertEquals('z', charArray[0]);
        assertEquals('a', chars.lookahead());
    }

    @Test
    public void shouldConsumeCharactersOneByOne() {
        CharStream chars = new CharStream("abc");
        assertEquals('a', chars.consume());
        assertEquals('b', chars.consume());
        assertEquals('c', chars.consume());
    }

    @Test
    public void shouldReturnEolWhenNothingLeftToConsume() {
        CharStream chars = new CharStream("abc");
        assertEquals('a', chars.consume());
        assertEquals('b', chars.consume());
        assertEquals('c', chars.consume());
        assertEquals(CharStream.EOL, chars.consume());
    }

    @Test
    public void shouldRaiseErrorWhenUnexpectedCharTypeConsumed() {
        CharStream chars = new CharStream("abc");
        try {
            chars.consume(Char.DIGIT);
        } catch (UnexpectedCharacterException e) {
            return;
        }
        fail("Should raise error when unexpected character type is consumed");
    }

    @Test
    public void shouldLookaheadWithoutConsuming() {
        CharStream chars = new CharStream("abc");
        assertEquals('a', chars.lookahead());
        assertEquals('a', chars.lookahead());
    }

    @Test
    public void shouldLookaheadArbitraryNumberOfCharacters() {
        CharStream chars = new CharStream("abc");
        assertEquals('a', chars.lookahead(1));
        assertEquals('b', chars.lookahead(2));
        assertEquals('c', chars.lookahead(3));
    }

    @Test
    public void shouldReturnEolWhenNothingLeftToLookahead() {
        CharStream chars = new CharStream("abc");
        assertEquals('a', chars.consume());
        assertEquals('b', chars.consume());
        assertEquals('c', chars.consume());
        assertEquals(CharStream.EOL, chars.lookahead());
    }

    @Test
    public void shouldCheckIfLookaheadIsOfExpectedTypes() {
        CharStream chars = new CharStream("abc");
        assertTrue(chars.positiveLookahead(Char.LETTER));
        assertFalse(chars.positiveLookahead(Char.DIGIT, Char.PLUS));
    }

    @Test
    public void shouldCheckIfCharOfExpectedTypesExistBeforeGivenType() {
        CharStream chars = new CharStream("1.0.0");
        assertTrue(chars.positiveLookaheadBefore(Char.EOL, Char.DOT));
        assertFalse(chars.positiveLookaheadBefore(Char.EOL, Char.LETTER));
    }
}
