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

import com.github.zafarkhaja.semver.Parser;
import com.github.zafarkhaja.semver.Version;
import com.github.zafarkhaja.semver.expr.Lexer.Token;
import com.github.zafarkhaja.semver.util.Stream;
import com.github.zafarkhaja.semver.util.Stream.ElementType;
import com.github.zafarkhaja.semver.util.UnexpectedElementException;
import java.util.EnumSet;
import java.util.Iterator;
import static com.github.zafarkhaja.semver.expr.CompositeExpression.Helper.*;
import static com.github.zafarkhaja.semver.expr.Lexer.Token.Type.*;

/**
 * A parser for the SemVer Expressions.
 *
 * @author Zafar Khaja {@literal <zafarkhaja@gmail.com>}
 * @since 0.7.0
 */
public class ExpressionParser implements Parser<Expression> {

    /**
     * The lexer instance used for tokenization of the input string.
     */
    private final Lexer lexer;

    /**
     * The stream of tokens produced by the lexer.
     */
    private Stream<Token> tokens;

    /**
     * Constructs a {@code ExpressionParser} instance
     * with the corresponding lexer.
     *
     * @param lexer the lexer to use for tokenization of the input string
     */
    ExpressionParser(Lexer lexer) {
        this.lexer = lexer;
    }

    /**
     * Creates and returns new instance of the {@code ExpressionParser} class.
     * <p>
     * This method implements the Static Factory Method pattern.
     *
     * @return a new instance of the {@code ExpressionParser} class
     */
    public static Parser<Expression> newInstance() {
        return new ExpressionParser(new Lexer());
    }

    /**
     * Parses the SemVer Expressions.
     *
     * @param input a string representing the SemVer Expression
     * @return the AST for the SemVer Expressions
     * @throws LexerException when encounters an illegal character
     * @throws UnexpectedTokenException when consumes a token of an unexpected type
     */
    @Override
    public Expression parse(String input) {
        tokens = lexer.tokenize(input);
        Expression expr = parseSemVerExpression();
        consumeNextToken(EOI);
        return expr;
    }

    /**
     * Parses the {@literal <semver-expr>} non-terminal.
     *
     * <pre>
     * {@literal
     * <semver-expr> ::= "(" <semver-expr> ")"
     *                 | "!" "(" <semver-expr> ")"
     *                 | <semver-expr> <more-expr>
     *                 | <range>
     * }
     * </pre>
     *
     * @return the expression AST
     */
    private CompositeExpression parseSemVerExpression() {
        CompositeExpression expr;
        if (tokens.positiveLookahead(NOT)) {
            tokens.consume();
            consumeNextToken(LEFT_PAREN);
            expr = not(parseSemVerExpression());
            consumeNextToken(RIGHT_PAREN);
        } else if (tokens.positiveLookahead(LEFT_PAREN)) {
            consumeNextToken(LEFT_PAREN);
            expr = parseSemVerExpression();
            consumeNextToken(RIGHT_PAREN);
        } else {
            expr = parseRange();
        }
        return parseMoreExpressions(expr);
    }

    /**
     * Parses the {@literal <more-expr>} non-terminal.
     *
     * <pre>
     * {@literal
     * <more-expr> ::= <boolean-op> <semver-expr> | epsilon
     * }
     * </pre>
     *
     * @param expr the left-hand expression of the logical operators
     * @return the expression AST
     */
    private CompositeExpression parseMoreExpressions(CompositeExpression expr) {
        if (tokens.positiveLookahead(AND)) {
            tokens.consume();
            expr = expr.and(parseSemVerExpression());
        } else if (tokens.positiveLookahead(OR)) {
            tokens.consume();
            expr = expr.or(parseSemVerExpression());
        }
        return expr;
    }

    /**
     * Parses the {@literal <range>} non-terminal.
     *
     * <pre>
     * {@literal
     * <expr> ::= <comparison-range>
     *          | <wildcard-expr>
     *          | <tilde-range>
     *          | <caret-range>
     *          | <hyphen-range>
     *          | <partial-version-range>
     * }
     * </pre>
     *
     * @return the expression AST
     */
    private CompositeExpression parseRange() {
        if (tokens.positiveLookahead(TILDE)) {
            return parseTildeRange();
        } else if (tokens.positiveLookahead(CARET)) {
            return parseCaretRange();
        } else if (isWildcardRange()) {
            return parseWildcardRange();
        } else if (isHyphenRange()) {
            return parseHyphenRange();
        } else if (isPartialVersionRange()) {
            return parsePartialVersionRange();
        }
        return parseComparisonRange();
    }

    /**
     * Parses the {@literal <comparison-range>} non-terminal.
     *
     * <pre>
     * {@literal
     * <comparison-range> ::= <comparison-op> <version> | <version>
     * }
     * </pre>
     *
     * @return the expression AST
     */
    private CompositeExpression parseComparisonRange() {
        Token token = tokens.lookahead();
        CompositeExpression expr;
        switch (token.type) {
            case EQUAL:
                tokens.consume();
                expr = eq(parseVersion());
                break;
            case NOT_EQUAL:
                tokens.consume();
                expr = neq(parseVersion());
                break;
            case GREATER:
                tokens.consume();
                expr = gt(parseVersion());
                break;
            case GREATER_EQUAL:
                tokens.consume();
                expr = gte(parseVersion());
                break;
            case LESS:
                tokens.consume();
                expr = lt(parseVersion());
                break;
            case LESS_EQUAL:
                tokens.consume();
                expr = lte(parseVersion());
                break;
            default:
                expr = eq(parseVersion());
        }
        return expr;
    }

    /**
     * Parses the {@literal <tilde-range>} non-terminal.
     *
     * <pre>
     * {@literal
     * <tilde-range> ::= "~" <version>
     * }
     * </pre>
     *
     * @return the expression AST
     */
    private CompositeExpression parseTildeRange() {
        consumeNextToken(TILDE);
        long major = consumeNextNumeric();
        if (!tokens.positiveLookahead(DOT)) {
            Version lo = Version.of(major);
            Version hi = lo.incrementMajorVersion();
            return gte(lo).and(lt(hi));
        }
        consumeNextToken(DOT);
        long minor = consumeNextNumeric();
        if (!tokens.positiveLookahead(DOT)) {
            Version lo = Version.of(major, minor);
            Version hi = lo.incrementMinorVersion();
            return gte(lo).and(lt(hi));
        }
        consumeNextToken(DOT);
        long patch = consumeNextNumeric();
        Version lo = Version.of(major, minor, patch);
        Version hi = lo.incrementMinorVersion();
        return gte(lo).and(lt(hi));
    }

    /**
     * Parses the {@literal <caret-range>} non-terminal.
     *
     * <pre>
     * {@literal
     * <caret-range> ::= "^" <version>
     * }
     * </pre>
     *
     * @return the expression AST
     */
    private CompositeExpression parseCaretRange() {
        consumeNextToken(CARET);
        long major = consumeNextNumeric();
        if (!tokens.positiveLookahead(DOT)) {
            Version lo = Version.of(major);
            Version hi = lo.incrementMajorVersion();
            return gte(lo).and(lt(hi));
        }
        consumeNextToken(DOT);
        long minor = consumeNextNumeric();
        if (!tokens.positiveLookahead(DOT)) {
            Version lo = Version.of(major, minor);
            Version hi = major > 0 ? lo.incrementMajorVersion() : lo.incrementMinorVersion();
            return gte(lo).and(lt(hi));
        }
        consumeNextToken(DOT);
        long patch = consumeNextNumeric();
        Version lo = Version.of(major, minor, patch);
        CompositeExpression gte = gte(lo);
        if (major > 0) {
            return gte.and(lt(lo.incrementMajorVersion()));
        } else if (minor > 0) {
            return gte.and(lt(lo.incrementMinorVersion()));
        } else if (patch > 0) {
            return gte.and(lt(lo.incrementPatchVersion()));
        }
        return eq(lo);
    }

    /**
     * Determines if the following version terminals are part
     * of the {@literal <wildcard-range>} non-terminal.
     *
     * @return {@code true} if the following version terminals are
     *         part of the {@literal <wildcard-range>} non-terminal or
     *         {@code false} otherwise
     */
    private boolean isWildcardRange() {
        return isVersionFollowedBy(WILDCARD);
    }

    /**
     * Parses the {@literal <wildcard-range>} non-terminal.
     *
     * <pre>
     * {@literal
     * <wildcard-range> ::= <wildcard>
     *                    | <major> "." <wildcard>
     *                    | <major> "." <minor> "." <wildcard>
     *
     * <wildcard> ::= "*" | "x" | "X"
     * }
     * </pre>
     *
     * @return the expression AST
     */
    private CompositeExpression parseWildcardRange() {
        if (tokens.positiveLookahead(WILDCARD)) {
            tokens.consume();
            return gte(Version.of(0, 0, 0));
        }

        long major = consumeNextNumeric();
        consumeNextToken(DOT);
        if (tokens.positiveLookahead(WILDCARD)) {
            tokens.consume();
            Version lo = Version.of(major);
            Version hi = lo.incrementMajorVersion();
            return gte(lo).and(lt(hi));
        }

        long minor = consumeNextNumeric();
        consumeNextToken(DOT);
        consumeNextToken(WILDCARD);
        Version lo = Version.of(major, minor);
        Version hi = lo.incrementMinorVersion();
        return gte(lo).and(lt(hi));
    }

    /**
     * Determines if the following version terminals are
     * part of the {@literal <hyphen-range>} non-terminal.
     *
     * @return {@code true} if the following version terminals are
     *         part of the {@literal <hyphen-range>} non-terminal or
     *         {@code false} otherwise
     */
    private boolean isHyphenRange() {
        return isVersionFollowedBy(HYPHEN);
    }

    /**
     * Parses the {@literal <hyphen-range>} non-terminal.
     *
     * <pre>
     * {@literal
     * <hyphen-range> ::= <version> "-" <version>
     * }
     * </pre>
     *
     * @return the expression AST
     */
    private CompositeExpression parseHyphenRange() {
        CompositeExpression gte = gte(parseVersion());
        consumeNextToken(HYPHEN);
        return gte.and(lte(parseVersion()));
    }

    /**
     * Determines if the following version terminals are part
     * of the {@literal <partial-version-range>} non-terminal.
     *
     * @return {@code true} if the following version terminals are part
     *         of the {@literal <partial-version-range>} non-terminal or
     *         {@code false} otherwise
     */
    private boolean isPartialVersionRange() {
        if (!tokens.positiveLookahead(NUMERIC)) {
            return false;
        }
        EnumSet<Token.Type> expected = EnumSet.complementOf(EnumSet.of(NUMERIC, DOT));
        return tokens.positiveLookaheadUntil(5, expected.toArray(new Token.Type[0]));
    }

    /**
     * Parses the {@literal <partial-version-range>} non-terminal.
     *
     * <pre>
     * {@literal
     * <partial-version-range> ::= <major> | <major> "." <minor>
     * }
     * </pre>
     *
     * @return the expression AST
     */
    private CompositeExpression parsePartialVersionRange() {
        long major = consumeNextNumeric();
        if (!tokens.positiveLookahead(DOT)) {
            Version lo = Version.of(major);
            Version hi = lo.incrementMajorVersion();
            return gte(lo).and(lt(hi));
        }
        consumeNextToken(DOT);
        long minor = consumeNextNumeric();
        Version lo = Version.of(major, minor);
        Version hi = lo.incrementMinorVersion();
        return gte(lo).and(lt(hi));
    }

    /**
     * Parses the {@literal <version>} non-terminal.
     *
     * <pre>
     * {@literal
     * <version> ::= <major>
     *             | <major> "." <minor>
     *             | <major> "." <minor> "." <patch>
     * }
     * </pre>
     *
     * @return the parsed version
     */
    private Version parseVersion() {
        long major = consumeNextNumeric();
        long minor = 0;
        if (tokens.positiveLookahead(DOT)) {
            tokens.consume();
            minor = consumeNextNumeric();
        }
        long patch = 0;
        if (tokens.positiveLookahead(DOT)) {
            tokens.consume();
            patch = consumeNextNumeric();
        }
        return Version.of(major, minor, patch);
    }

    /**
     * Determines if the version terminals are
     * followed by the specified token type.
     * <p>
     * This method is essentially a {@code lookahead(k)} method
     * which allows to solve the grammar's ambiguities.
     *
     * @param type the token type to check
     * @return {@code true} if the version terminals are followed by
     *         the specified token type or {@code false} otherwise
     */
    private boolean isVersionFollowedBy(ElementType<Token> type) {
        EnumSet<Token.Type> expected = EnumSet.of(NUMERIC, DOT);
        Iterator<Token> it = tokens.iterator();
        Token lookahead = null;
        while (it.hasNext()) {
            lookahead = it.next();
            if (!expected.contains(lookahead.type)) {
                break;
            }
        }
        return type.isMatchedBy(lookahead);
    }

    /**
     * Tries to consume the next token in the stream.
     *
     * @param expected the expected types of the next token
     * @return the next token in the stream
     * @throws UnexpectedTokenException when encounters an unexpected token type
     */
    private Token consumeNextToken(Token.Type... expected) {
        try {
            return tokens.consume(expected);
        } catch (UnexpectedElementException e) {
            throw new UnexpectedTokenException(e);
        }
    }

    private long consumeNextNumeric() {
        return Long.parseLong(consumeNextToken(NUMERIC).lexeme);
    }
}
