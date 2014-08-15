/*
 * The MIT License
 *
 * Copyright 2012-2014 Zafar Khaja <zafarkhaja@gmail.com>.
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
import static com.github.zafarkhaja.semver.expr.Lexer.Token.Type.*;

/**
 * A parser for the SemVer Expressions.
 *
 * @author Zafar Khaja <zafarkhaja@gmail.com>
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
     *
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
        consumeNextToken(EOL);
        return expr;
    }

    /**
     * Parses the {@literal <semver-expr>} non-terminal.
     *
     * <pre>
     * {@literal
     * <semver-expr> ::= "(" <semver-expr> ")"
     *                 | "!" "(" <semver-expr> ")"
     *                 | <semver-expr> <boolean-expr>
     *                 | <expr>
     * }
     * </pre>
     *
     * @return the expression AST
     */
    private Expression parseSemVerExpression() {
        Expression expr;
        if (tokens.positiveLookahead(NOT)) {
            tokens.consume();
            consumeNextToken(LEFT_PAREN);
            expr = new Not(parseSemVerExpression());
            consumeNextToken(RIGHT_PAREN);
        } else if (tokens.positiveLookahead(LEFT_PAREN)) {
            consumeNextToken(LEFT_PAREN);
            expr = parseSemVerExpression();
            consumeNextToken(RIGHT_PAREN);
        } else {
            expr = parseExpression();
        }
        return parseBooleanExpression(expr);
    }

    /**
     * Parses the {@literal <boolean-expr>} non-terminal.
     *
     * <pre>
     * {@literal
     * <boolean-expr> ::= <boolean-op> <semver-expr>
     *                  | <epsilon>
     * }
     * </pre>
     *
     * @param expr the left-hand expression of the logical operators
     * @return the expression AST
     */
    private Expression parseBooleanExpression(Expression expr) {
        if (tokens.positiveLookahead(AND)) {
            tokens.consume();
            expr = new And(expr, parseSemVerExpression());
        } else if (tokens.positiveLookahead(OR)) {
            tokens.consume();
            expr = new Or(expr, parseSemVerExpression());
        }
        return expr;
    }

    /**
     * Parses the {@literal <expr>} non-terminal.
     *
     * <pre>
     * {@literal
     * <expr> ::= <comparison-expr>
     *          | <version-expr>
     *          | <tilde-expr>
     *          | <range-expr>
     * }
     * </pre>
     *
     * @return the expression AST
     */
    private Expression parseExpression() {
        if (tokens.positiveLookahead(TILDE)) {
            return parseTildeExpression();
        } else if (isVersionExpression()) {
            return parseVersionExpression();
        } else if (isRangeExpression()) {
            return parseRangeExpression();
        }
        return parseComparisonExpression();
    }

    /**
     * Parses the {@literal <comparison-expr>} non-terminal.
     *
     * <pre>
     * {@literal
     * <comparison-expr> ::= <comparison-op> <version>
     *                     | <version>
     * }
     * </pre>
     *
     * @return the expression AST
     */
    private Expression parseComparisonExpression() {
        Token token = tokens.lookahead();
        Expression expr;
        switch (token.type) {
            case EQUAL:
                tokens.consume();
                expr = new Equal(parseVersion());
                break;
            case NOT_EQUAL:
                tokens.consume();
                expr = new NotEqual(parseVersion());
                break;
            case GREATER:
                tokens.consume();
                expr = new Greater(parseVersion());
                break;
            case GREATER_EQUAL:
                tokens.consume();
                expr = new GreaterOrEqual(parseVersion());
                break;
            case LESS:
                tokens.consume();
                expr = new Less(parseVersion());
                break;
            case LESS_EQUAL:
                tokens.consume();
                expr = new LessOrEqual(parseVersion());
                break;
            default:
                expr = new Equal(parseVersion());
        }
        return expr;
    }

    /**
     * Parses the {@literal <tilde-expr>} non-terminal.
     *
     * <pre>
     * {@literal
     * <tilde-expr> ::= "~" <version>
     * }
     * </pre>
     *
     * @return the expression AST
     */
    private Expression parseTildeExpression() {
        consumeNextToken(TILDE);
        int major = intOf(consumeNextToken(NUMERIC).lexeme);
        if (!tokens.positiveLookahead(DOT)) {
            return new GreaterOrEqual(versionOf(major, 0, 0));
        }
        consumeNextToken(DOT);
        int minor = intOf(consumeNextToken(NUMERIC).lexeme);
        if (!tokens.positiveLookahead(DOT)) {
            return new And(
                new GreaterOrEqual(versionOf(major, minor, 0)),
                new Less(versionOf(major + 1, 0, 0))
            );
        }
        consumeNextToken(DOT);
        int patch = intOf(consumeNextToken(NUMERIC).lexeme);
        return new And(
            new GreaterOrEqual(versionOf(major, minor, patch)),
            new Less(versionOf(major, minor + 1, 0))
        );
    }

    /**
     * Determines if the following version terminals are part
     * of the {@literal <version-expr>} non-terminal.
     *
     * @return {@code true} if the following version terminals are
     *         part of the {@literal <version-expr>} non-terminal or
     *         {@code false} otherwise
     */
    private boolean isVersionExpression() {
        return isVersionFollowedBy(STAR);
    }

    /**
     * Parses the {@literal <version-expr>} non-terminal.
     *
     * <pre>
     * {@literal
     * <version-expr> ::= <major> "." "*"
     *                  | <major> "." <minor> "." "*"
     * }
     * </pre>
     *
     * @return the expression AST
     */
    private Expression parseVersionExpression() {
        int major = intOf(consumeNextToken(NUMERIC).lexeme);
        consumeNextToken(DOT);
        if (tokens.positiveLookahead(STAR)) {
            tokens.consume();
            return new And(
                new GreaterOrEqual(versionOf(major, 0, 0)),
                new Less(versionOf(major + 1, 0, 0))
            );
        }
        int minor = intOf(consumeNextToken(NUMERIC).lexeme);
        consumeNextToken(DOT);
        consumeNextToken(STAR);
        return new And(
            new GreaterOrEqual(versionOf(major, minor, 0)),
            new Less(versionOf(major, minor + 1, 0))
        );
    }

    /**
     * Determines if the following version terminals are
     * part of the {@literal <range-expr>} non-terminal.
     *
     * @return {@code true} if the following version terminals are
     *         part of the {@literal <range-expr>} non-terminal or
     *         {@code false} otherwise
     */
    private boolean isRangeExpression() {
        return isVersionFollowedBy(HYPHEN);
    }

    /**
     * Parses the {@literal <range-expr>} non-terminal.
     *
     * <pre>
     * {@literal
     * <range-expr> ::= <version> "-" <version>
     * }
     * </pre>
     *
     * @return the expression AST
     */
    private Expression parseRangeExpression() {
        Expression ge = new GreaterOrEqual(parseVersion());
        consumeNextToken(HYPHEN);
        Expression le = new LessOrEqual(parseVersion());
        return new And(ge, le);
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
        int major = intOf(consumeNextToken(NUMERIC).lexeme);
        int minor = 0;
        if (tokens.positiveLookahead(DOT)) {
            tokens.consume();
            minor = intOf(consumeNextToken(NUMERIC).lexeme);
        }
        int patch = 0;
        if (tokens.positiveLookahead(DOT)) {
            tokens.consume();
            patch = intOf(consumeNextToken(NUMERIC).lexeme);
        }
        return versionOf(major, minor, patch);
    }

    /**
     * Determines if the version terminals are
     * followed by the specified token type.
     *
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
     * Creates a {@code Version} instance for the specified integers.
     *
     * @param major the major version number
     * @param minor the minor version number
     * @param patch the patch version number
     * @return the version for the specified integers
     */
    private Version versionOf(int major, int minor, int patch) {
        return Version.forIntegers(major, minor, patch);
    }

    /**
     * Returns a {@code int} representation of the specified string.
     *
     * @param value the string to convert into an integer
     * @return the integer value of the specified string
     */
    private int intOf(String value) {
        return Integer.parseInt(value);
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
}
