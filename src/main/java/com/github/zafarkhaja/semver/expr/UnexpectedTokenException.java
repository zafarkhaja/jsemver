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
 *
 * @author Zafar Khaja <zafarkhaja@gmail.com>
 */
public class UnexpectedTokenException extends ParserException {

    private final Token unexpected;
    private final Token.Type[] expected;

    UnexpectedTokenException(Token token, Token.Type... expected) {
        unexpected = token;
        this.expected = expected;
    }

    @Override
    public String toString() {
        String message = "Unexpected token '" + unexpected + "'";
        if (expected.length > 0) {
            message += ", expecting '" + Arrays.toString(expected) + "'";
        }
        return message;
    }
}
