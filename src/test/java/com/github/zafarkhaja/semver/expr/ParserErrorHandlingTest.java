/*
 * The MIT License
 *
 * Copyright 2012-2024 Zafar Khaja <zafarkhaja@gmail.com>.
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

import com.github.zafarkhaja.semver.expr.Lexer.Token;
import java.util.Arrays;
import java.util.Collection;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import static com.github.zafarkhaja.semver.expr.Lexer.Token.Type.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Zafar Khaja {@literal <zafarkhaja@gmail.com>}
 */
class ParserErrorHandlingTest {

    @ParameterizedTest
    @MethodSource("parameters")
    void shouldCorrectlyHandleParseErrors(
        String invalidExpr,
        Token unexpected,
        Token.Type[] expected
    ) {
        UnexpectedTokenException e = assertThrows(
            UnexpectedTokenException.class,
            () -> ExpressionParser.newInstance().parse(invalidExpr)
        );
        assertNotNull(e.getMessage());
        assertEquals(unexpected, e.getUnexpectedToken());
        assertArrayEquals(expected, e.getExpectedTokenTypes());
    }

    static Collection<Object[]> parameters() {
        return Arrays.asList(new Object[][] {
            { "1)",           new Token(RIGHT_PAREN, ")", 1),  new Token.Type[] { EOI } },
            { "(>1.0.1",      new Token(EOI, null, 7),         new Token.Type[] { RIGHT_PAREN } },
            { "((>=1 & <2)",  new Token(EOI, null, 11),        new Token.Type[] { RIGHT_PAREN } },
            { ">=1.0.0 &",    new Token(EOI, null, 9),         new Token.Type[] { NUMERIC } },
            { "(>2.0 |)",     new Token(RIGHT_PAREN, ")", 7),  new Token.Type[] { NUMERIC } },
            { "& 1.2",        new Token(AND, "&", 0),          new Token.Type[] { NUMERIC } },
        });
    }
}
