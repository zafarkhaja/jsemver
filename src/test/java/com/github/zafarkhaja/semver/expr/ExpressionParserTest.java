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

import com.github.zafarkhaja.semver.Version;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Zafar Khaja {@literal <zafarkhaja@gmail.com>}
 */
class ExpressionParserTest {

    @Test
    void shouldParseEqualComparisonRange() {
        ExpressionParser parser = new ExpressionParser(new Lexer());
        Expression eq = parser.parse("=1.0.0");
        assertTrue(eq.interpret(Version.parse("1.0.0")));
    }

    @Test
    void shouldParseEqualComparisonRangeIfOnlyFullVersionGiven() {
        ExpressionParser parser = new ExpressionParser(new Lexer());
        Expression eq = parser.parse("1.0.0");
        assertTrue(eq.interpret(Version.parse("1.0.0")));
    }

    @Test
    void shouldParseNotEqualComparisonRange() {
        ExpressionParser parser = new ExpressionParser(new Lexer());
        Expression ne = parser.parse("!=1.0.0");
        assertTrue(ne.interpret(Version.parse("1.2.3")));
    }

    @Test
    void shouldParseGreaterComparisonRange() {
        ExpressionParser parser = new ExpressionParser(new Lexer());
        Expression gt = parser.parse(">1.0.0");
        assertTrue(gt.interpret(Version.parse("1.2.3")));
    }

    @Test
    void shouldParseGreaterOrEqualComparisonRange() {
        ExpressionParser parser = new ExpressionParser(new Lexer());
        Expression ge = parser.parse(">=1.0.0");
        assertTrue(ge.interpret(Version.parse("1.0.0")));
        assertTrue(ge.interpret(Version.parse("1.2.3")));
    }

    @Test
    void shouldParseLessComparisonRange() {
        ExpressionParser parser = new ExpressionParser(new Lexer());
        Expression lt = parser.parse("<1.2.3");
        assertTrue(lt.interpret(Version.parse("1.0.0")));
    }

    @Test
    void shouldParseLessOrEqualComparisonRange() {
        ExpressionParser parser = new ExpressionParser(new Lexer());
        Expression le = parser.parse("<=1.2.3");
        assertTrue(le.interpret(Version.parse("1.0.0")));
        assertTrue(le.interpret(Version.parse("1.2.3")));
    }

    @Test
    void shouldParseTildeRange() {
        ExpressionParser parser = new ExpressionParser(new Lexer());
        Expression expr1 = parser.parse("~1");
        assertTrue(expr1.interpret(Version.parse("1.2.3")));
        assertFalse(expr1.interpret(Version.parse("3.2.1")));
        Expression expr2 = parser.parse("~1.2");
        assertTrue(expr2.interpret(Version.parse("1.2.3")));
        assertFalse(expr2.interpret(Version.parse("2.0.0")));
        Expression expr3 = parser.parse("~1.2.3");
        assertTrue(expr3.interpret(Version.parse("1.2.3")));
        assertFalse(expr3.interpret(Version.parse("1.3.0")));
    }

    @Test
    void shouldParseCaretRange() {
        ExpressionParser parser = new ExpressionParser(new Lexer());
        Expression expr1 = parser.parse("^1");
        assertTrue(expr1.interpret(Version.parse("1.2.3")));
        assertFalse(expr1.interpret(Version.parse("3.2.1")));
        Expression expr2 = parser.parse("^0.2");
        assertTrue(expr2.interpret(Version.parse("0.2.3")));
        assertFalse(expr2.interpret(Version.parse("0.3.0")));
        Expression expr3 = parser.parse("^0.0.3");
        assertTrue(expr3.interpret(Version.parse("0.0.3")));
        assertFalse(expr3.interpret(Version.parse("0.0.4")));
    }

    @Test
    void shouldParsePartialVersionRange() {
        ExpressionParser parser = new ExpressionParser(new Lexer());
        Expression expr1 = parser.parse("1");
        assertTrue(expr1.interpret(Version.parse("1.2.3")));
        Expression expr2 = parser.parse("2.0");
        assertTrue(expr2.interpret(Version.parse("2.0.9")));
    }

    @Test
    void shouldParseWildcardRange() {
        ExpressionParser parser = new ExpressionParser(new Lexer());
        Expression expr1 = parser.parse("1.*");
        assertTrue(expr1.interpret(Version.parse("1.2.3")));
        assertFalse(expr1.interpret(Version.parse("3.2.1")));
        Expression expr2 = parser.parse("1.2.x");
        assertTrue(expr2.interpret(Version.parse("1.2.3")));
        assertFalse(expr2.interpret(Version.parse("1.3.2")));
        Expression expr3 = parser.parse("X");
        assertTrue(expr3.interpret(Version.parse("1.2.3")));
    }

    @Test
    void shouldParseHyphenRange() {
        ExpressionParser parser = new ExpressionParser(new Lexer());
        Expression range = parser.parse("1.0.0 - 2.0.0");
        assertTrue(range.interpret(Version.parse("1.2.3")));
        assertFalse(range.interpret(Version.parse("3.2.1")));
    }

    @Test
    void shouldParseMultipleRangesJoinedWithAnd() {
        ExpressionParser parser = new ExpressionParser(new Lexer());
        Expression and = parser.parse(">=1.0.0 & <2.0.0");
        assertTrue(and.interpret(Version.parse("1.2.3")));
        assertFalse(and.interpret(Version.parse("3.2.1")));
    }

    @Test
    void shouldParseMultipleRangesJoinedWithOr() {
        ExpressionParser parser = new ExpressionParser(new Lexer());
        Expression or = parser.parse("1.* | =2.0.0");
        assertTrue(or.interpret(Version.parse("1.2.3")));
        assertFalse(or.interpret(Version.parse("2.1.0")));
    }

    @Test
    void shouldParseParenthesizedExpression() {
        ExpressionParser parser = new ExpressionParser(new Lexer());
        Expression expr = parser.parse("(1)");
        assertTrue(expr.interpret(Version.parse("1.2.3")));
        assertFalse(expr.interpret(Version.parse("2.0.0")));
    }

    @Test
    void shouldParseExpressionWithMultipleParentheses() {
        ExpressionParser parser = new ExpressionParser(new Lexer());
        Expression expr = parser.parse("((1))");
        assertTrue(expr.interpret(Version.parse("1.2.3")));
        assertFalse(expr.interpret(Version.parse("2.0.0")));
    }

    @Test
    void shouldParseNotExpression() {
        ExpressionParser parser = new ExpressionParser(new Lexer());
        Expression not1 = parser.parse("!(1)");
        assertTrue(not1.interpret(Version.parse("2.0.0")));
        assertFalse(not1.interpret(Version.parse("1.2.3")));
        Expression not2 = parser.parse("0.* & !(>=1 & <2)");
        assertTrue(not2.interpret(Version.parse("0.5.0")));
        assertFalse(not2.interpret(Version.parse("1.0.1")));
        Expression not3 = parser.parse("!(>=1 & <2) & >=2");
        assertTrue(not3.interpret(Version.parse("2.0.0")));
        assertFalse(not3.interpret(Version.parse("1.2.3")));
    }

    @Test
    void shouldRespectPrecedenceWhenUsedWithParentheses() {
        ExpressionParser parser = new ExpressionParser(new Lexer());
        Expression expr1 = parser.parse("(~1.0 & <2.0) | >2.0");
        assertTrue(expr1.interpret(Version.parse("2.5.0")));
        Expression expr2 = parser.parse("~1.0 & (<2.0 | >2.0)");
        assertFalse(expr2.interpret(Version.parse("2.5.0")));
    }

    @Test
    void shouldParseComplexExpressions() {
        ExpressionParser parser = new ExpressionParser(new Lexer());
        Expression expr = parser.parse("((>=1.0.1 & <2) | (>=3.0 & <4)) & ((1-1.5) & (~1.5))");
        assertTrue(expr.interpret(Version.parse("1.5.0")));
        assertFalse(expr.interpret(Version.parse("2.5.0")));
    }
}
