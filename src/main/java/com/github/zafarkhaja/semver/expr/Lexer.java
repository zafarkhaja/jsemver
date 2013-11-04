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

import com.github.zafarkhaja.semver.util.Stream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Zafar Khaja <zafarkhaja@gmail.com>
 */
class Lexer {

    static class Token {

        enum Type implements Stream.ElementType<Token> {

            NUMERIC("0|[1-9][0-9]*"),
            DOT("\\."),
            HYPHEN("-"),
            EQUAL("="),
            NOT_EQUAL("!="),
            GREATER(">(?!=)"),
            GREATER_EQUAL(">="),
            LESS("<(?!=)"),
            LESS_EQUAL("<="),
            TILDE("~"),
            STAR("\\*"),
            AND("&"),
            OR("\\|"),
            NOT("!(?!=)"),
            LEFT_PAREN("\\("),
            RIGHT_PAREN("\\)"),
            WHITESPACE("\\s+"),
            EOL("?!") {
                @Override
                public boolean isMatchedBy(Token token) {
                    return token == null;
                }
            };

            final Pattern pattern;

            private Type(String regexp) {
                pattern = Pattern.compile("^(" + regexp + ")");
            }

            @Override
            public String toString() {
                return name() + "(" + pattern + ")";
            }

            @Override
            public boolean isMatchedBy(Token token) {
                if (token == null) {
                    return false;
                }
                return this == token.type;
            }
        }

        final Type type;
        final String lexeme;

        Token(Type type, String lexeme) {
            this.type = type;
            this.lexeme = (lexeme == null) ? "" : lexeme;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof Token)) {
                return false;
            }
            Token token = (Token) other;
            return type.equals(token.type) && lexeme.equals(token.lexeme);
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 71 * hash + type.hashCode();
            hash = 71 * hash + lexeme.hashCode();
            return hash;
        }

        @Override
        public String toString() {
            return type.name() + "(" + lexeme + ")";
        }
    }

    Lexer() {

    }

    Stream<Token> tokenize(String input) {
        List<Token> tokens = new ArrayList<Token>();
        while (!input.isEmpty()) {
            boolean matched = false;
            for (Token.Type tokenType : Token.Type.values()) {
                Matcher matcher = tokenType.pattern.matcher(input);
                if (matcher.find()) {
                    matched = true;
                    input = matcher.replaceFirst("");
                    if (tokenType != Token.Type.WHITESPACE) {
                        tokens.add(new Token(tokenType, matcher.group()));
                    }
                    break;
                }
            }
            if (!matched) {
                throw new LexerException(input);
            }
        }
        return new Stream<Token>(tokens.toArray(new Token[tokens.size()]));
    }
}
