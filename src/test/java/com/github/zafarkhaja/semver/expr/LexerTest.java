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
package com.github.zafarkhaja.semver.expr;

import com.github.zafarkhaja.semver.expr.Lexer.*;
import com.github.zafarkhaja.semver.util.Stream;
import org.junit.jupiter.api.Test;
import static com.github.zafarkhaja.semver.expr.Lexer.Token.Type.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Zafar Khaja {@literal <zafarkhaja@gmail.com>}
 */
class LexerTest {

    @Test
    void shouldTokenizeVersionString() {
        Token[] expected = {
            new Token(GREATER, ">",  0),
            new Token(NUMERIC, "1",  1),
            new Token(DOT,     ".",  2),
            new Token(NUMERIC, "0",  3),
            new Token(DOT,     ".",  4),
            new Token(NUMERIC, "0",  5),
            new Token(EOI,     null, 6),
        };
        Lexer lexer = new Lexer();
        Stream<Token> stream = lexer.tokenize(">1.0.0");
        assertArrayEquals(expected, stream.toArray());
    }

    @Test
    void shouldSkipWhitespaces() {
        Token[] expected = {
            new Token(GREATER, ">",  0),
            new Token(NUMERIC, "1",  2),
            new Token(EOI,     null, 3),
        };
        Lexer lexer = new Lexer();
        Stream<Token> stream = lexer.tokenize("> 1");
        assertArrayEquals(expected, stream.toArray());
    }

    @Test
    void shouldEndWithEol() {
        Token[] expected = {
            new Token(NUMERIC, "1",  0),
            new Token(DOT,     ".",  1),
            new Token(NUMERIC, "2",  2),
            new Token(DOT,     ".",  3),
            new Token(NUMERIC, "3",  4),
            new Token(EOI,     null, 5),
        };
        Lexer lexer = new Lexer();
        Stream<Token> stream = lexer.tokenize("1.2.3");
        assertArrayEquals(expected, stream.toArray());
    }

    @Test
    void shouldRaiseErrorOnIllegalCharacter() {
        Lexer lexer = new Lexer();
        assertThrows(
            LexerException.class,
            () -> lexer.tokenize("@1.0.0"),
            "Should raise error on illegal character"
        );
    }
}
