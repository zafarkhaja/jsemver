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
package com.github.zafarkhaja.semver.expr;

import com.github.zafarkhaja.semver.ParserException;
import com.github.zafarkhaja.semver.expr.Lexer.*;
import java.util.Arrays;

/**
 * Thrown when a token of unexpected types is encountered during the parsing.
 *
 * @author Zafar Khaja <zafarkhaja@gmail.com>
 * @since 0.7.0
 */
public class UnexpectedTokenException extends ParserException {

    /**
     * The unexpected token.
     */
    private final Token unexpected;

    /**
     * The array of the expected token types.
     */
    private final Token.Type[] expected;

    /**
     * Constructs a {@code UnexpectedTokenException} instance
     * with the unexpected token and the expected types.
     *
     * @param token the unexpected token
     * @param expected an array of the expected token types
     */
    UnexpectedTokenException(Token token, Token.Type... expected) {
        unexpected = token;
        this.expected = expected;
    }

    /**
     * Returns the string representation of this exception
     * containing the information about the unexpected
     * token and, if available, about the expected types.
     *
     * @return the string representation of this exception
     */
    @Override
    public String toString() {
        String message = "Unexpected token '" + unexpected + "'";
        if (expected.length > 0) {
            message += ", expecting '" + Arrays.toString(expected) + "'";
        }
        return message;
    }
}