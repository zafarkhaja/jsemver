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

import com.github.zafarkhaja.semver.Parser;
import com.github.zafarkhaja.semver.Version;
import com.github.zafarkhaja.semver.expr.Lexer.Token;
import com.github.zafarkhaja.semver.util.Stream;
import com.github.zafarkhaja.semver.util.Stream.ElementType;
import java.util.EnumSet;
import java.util.Iterator;
import static com.github.zafarkhaja.semver.expr.Lexer.Token.Type.*;

/**
 *
 * @author Zafar Khaja <zafarkhaja@gmail.com>
 */
public class ExpressionParser implements Parser<Expression> {

    private final Lexer lexer;
    private Stream<Token> tokens;

    ExpressionParser(Lexer lexer) {
        this.lexer = lexer;
    }

    public static Parser<Expression> newInstance() {
        return new ExpressionParser(new Lexer());
    }

    @Override
    public Expression parse(String input) {
        tokens = lexer.tokenize(input);
        return parseSemVerExpression();
    }

    private Expression parseSemVerExpression() {
        Expression expr;
        if (tokens.positiveLookahead(NOT)) {
            tokens.consume();
            tokens.consume(LEFT_PAREN);
            expr = new Not(parseSemVerExpression());
            tokens.consume(RIGHT_PAREN);
        } else if (tokens.positiveLookahead(LEFT_PAREN)) {
            tokens.consume(LEFT_PAREN);
            expr = parseSemVerExpression();
            tokens.consume(RIGHT_PAREN);
        } else {
            expr = parseExpression();
        }
        return parseBooleanExpression(expr);
    }

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

    private Expression parseTildeExpression() {
        tokens.consume(TILDE);
        int major = intOf(tokens.consume(NUMERIC).lexeme);
        if (!tokens.positiveLookahead(DOT)) {
            return new GreaterOrEqual(versionOf(major, 0, 0));
        }
        tokens.consume(DOT);
        int minor = intOf(tokens.consume(NUMERIC).lexeme);
        if (!tokens.positiveLookahead(DOT)) {
            return new And(
                new GreaterOrEqual(versionOf(major, minor, 0)),
                new Less(versionOf(major + 1, 0, 0))
            );
        }
        tokens.consume(DOT);
        int patch = intOf(tokens.consume(NUMERIC).lexeme);
        return new And(
            new GreaterOrEqual(versionOf(major, minor, patch)),
            new Less(versionOf(major, minor + 1, 0))
        );
    }

    private boolean isVersionExpression() {
        return isVersionFollowedBy(STAR);
    }

    private Expression parseVersionExpression() {
        int major = intOf(tokens.consume(NUMERIC).lexeme);
        tokens.consume(DOT);
        if (tokens.positiveLookahead(STAR)) {
            tokens.consume();
            return new And(
                new GreaterOrEqual(versionOf(major, 0, 0)),
                new Less(versionOf(major + 1, 0, 0))
            );
        }
        int minor = intOf(tokens.consume(NUMERIC).lexeme);
        tokens.consume(DOT);
        tokens.consume(STAR);
        return new And(
            new GreaterOrEqual(versionOf(major, minor, 0)),
            new Less(versionOf(major, minor + 1, 0))
        );
    }

    private boolean isRangeExpression() {
        return isVersionFollowedBy(HYPHEN);
    }

    private Expression parseRangeExpression() {
        Expression ge = new GreaterOrEqual(parseVersion());
        tokens.consume(HYPHEN);
        Expression le = new LessOrEqual(parseVersion());
        return new And(ge, le);
    }

    private Version parseVersion() {
        int major = intOf(tokens.consume(NUMERIC).lexeme);
        int minor = 0;
        if (tokens.positiveLookahead(DOT)) {
            tokens.consume();
            minor = intOf(tokens.consume(NUMERIC).lexeme);
        }
        int patch = 0;
        if (tokens.positiveLookahead(DOT)) {
            tokens.consume();
            patch = intOf(tokens.consume(NUMERIC).lexeme);
        }
        return versionOf(major, minor, patch);
    }

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

    private Version versionOf(int major, int minor, int patch) {
        return Version.forIntegers(major, minor, patch);
    }

    private int intOf(String value) {
        return Integer.parseInt(value);
    }
}
