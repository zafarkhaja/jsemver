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

import com.github.zafarkhaja.semver.Version;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author Zafar Khaja <zafarkhaja@gmail.com>
 */
public class ExpressionParserTest {

    @Test
    public void shouldParseEqualComparisonExpression() {
        ExpressionParser parser = new ExpressionParser(new Lexer());
        Expression eq = parser.parse("=1.0.0");
        assertTrue(eq.interpret(Version.valueOf("1.0.0")));
    }

    @Test
    public void shouldParseEqualComparisonExpressionIfOnlyVersionGiven() {
        ExpressionParser parser = new ExpressionParser(new Lexer());
        Expression eq = parser.parse("1.0.0");
        assertTrue(eq.interpret(Version.valueOf("1.0.0")));
    }

    @Test
    public void shouldParseNotEqualComparisonExpression() {
        ExpressionParser parser = new ExpressionParser(new Lexer());
        Expression ne = parser.parse("!=1.0.0");
        assertTrue(ne.interpret(Version.valueOf("1.2.3")));
    }

    @Test
    public void shouldParseGreaterComparisonExpression() {
        ExpressionParser parser = new ExpressionParser(new Lexer());
        Expression gt = parser.parse(">1.0.0");
        assertTrue(gt.interpret(Version.valueOf("1.2.3")));
    }

    @Test
    public void shouldParseGreaterOrEqualComparisonExpression() {
        ExpressionParser parser = new ExpressionParser(new Lexer());
        Expression ge = parser.parse(">=1.0.0");
        assertTrue(ge.interpret(Version.valueOf("1.0.0")));
        assertTrue(ge.interpret(Version.valueOf("1.2.3")));
    }

    @Test
    public void shouldParseLessComparisonExpression() {
        ExpressionParser parser = new ExpressionParser(new Lexer());
        Expression lt = parser.parse("<1.2.3");
        assertTrue(lt.interpret(Version.valueOf("1.0.0")));
    }

    @Test
    public void shouldParseLessOrEqualComparisonExpression() {
        ExpressionParser parser = new ExpressionParser(new Lexer());
        Expression le = parser.parse("<=1.2.3");
        assertTrue(le.interpret(Version.valueOf("1.0.0")));
        assertTrue(le.interpret(Version.valueOf("1.2.3")));
    }

    @Test
    public void shouldParseTildeExpression() {
        ExpressionParser parser = new ExpressionParser(new Lexer());
        Expression expr1 = parser.parse("~1");
        assertTrue(expr1.interpret(Version.valueOf("1.2.3")));
        assertFalse(expr1.interpret(Version.valueOf("3.2.1")));
        Expression expr2 = parser.parse("~1.2");
        assertTrue(expr2.interpret(Version.valueOf("1.2.3")));
        assertFalse(expr2.interpret(Version.valueOf("2.0.0")));
        Expression expr3 = parser.parse("~1.2.3");
        assertTrue(expr3.interpret(Version.valueOf("1.2.3")));
        assertFalse(expr3.interpret(Version.valueOf("1.3.0")));
    }

    @Test
    public void shouldParseShortFormOfVersion() {
        ExpressionParser parser = new ExpressionParser(new Lexer());
        Expression expr1 = parser.parse("1");
        assertTrue(expr1.interpret(Version.valueOf("1.0.0")));
        Expression expr2 = parser.parse("2.0");
        assertTrue(expr2.interpret(Version.valueOf("2.0.0")));
    }

    @Test
    public void shouldParseVersionExpression() {
        ExpressionParser parser = new ExpressionParser(new Lexer());
        Expression expr1 = parser.parse("1.*");
        assertTrue(expr1.interpret(Version.valueOf("1.2.3")));
        assertFalse(expr1.interpret(Version.valueOf("3.2.1")));
        Expression expr2 = parser.parse("1.2.*");
        assertTrue(expr2.interpret(Version.valueOf("1.2.3")));
        assertFalse(expr2.interpret(Version.valueOf("1.3.2")));
    }

    @Test
    public void shouldParseRangeExpression() {
        ExpressionParser parser = new ExpressionParser(new Lexer());
        Expression range = parser.parse("1.0.0 - 2.0.0");
        assertTrue(range.interpret(Version.valueOf("1.2.3")));
        assertFalse(range.interpret(Version.valueOf("3.2.1")));
    }

    @Test
    public void shouldParseAndBooleanExpression() {
        ExpressionParser parser = new ExpressionParser(new Lexer());
        Expression and = parser.parse(">=1.0.0 & <2.0.0");
        assertTrue(and.interpret(Version.valueOf("1.2.3")));
        assertFalse(and.interpret(Version.valueOf("3.2.1")));
    }

    @Test
    public void shouldParseOrBooleanExpression() {
        ExpressionParser parser = new ExpressionParser(new Lexer());
        Expression or = parser.parse("1.* | =2.0.0");
        assertTrue(or.interpret(Version.valueOf("1.2.3")));
        assertFalse(or.interpret(Version.valueOf("2.1.0")));
    }

    @Test
    public void shouldParseParenthesizedExpression() {
        ExpressionParser parser = new ExpressionParser(new Lexer());
        Expression expr = parser.parse("(1)");
        assertTrue(expr.interpret(Version.valueOf("1.0.0")));
        assertFalse(expr.interpret(Version.valueOf("2.0.0")));
    }

    @Test
    public void shouldParseExpressionWithMultipleParentheses() {
        ExpressionParser parser = new ExpressionParser(new Lexer());
        Expression expr = parser.parse("((1))");
        assertTrue(expr.interpret(Version.valueOf("1.0.0")));
        assertFalse(expr.interpret(Version.valueOf("2.0.0")));
    }

    @Test
    public void shouldParseNotExpression() {
        ExpressionParser parser = new ExpressionParser(new Lexer());
        Expression not1 = parser.parse("!(1)");
        assertTrue(not1.interpret(Version.valueOf("2.0.0")));
        assertFalse(not1.interpret(Version.valueOf("1.0.0")));
        Expression not2 = parser.parse("0.* & !(>=1 & <2)");
        assertTrue(not2.interpret(Version.valueOf("0.5.0")));
        assertFalse(not2.interpret(Version.valueOf("1.0.1")));
        Expression not3 = parser.parse("!(>=1 & <2) & >=2");
        assertTrue(not3.interpret(Version.valueOf("2.0.0")));
        assertFalse(not3.interpret(Version.valueOf("1.2.3")));
    }

    @Test
    public void shouldRespectPrecedenceWhenUsedWithParentheses() {
        ExpressionParser parser = new ExpressionParser(new Lexer());
        Expression expr1 = parser.parse("(~1.0 & <2.0) | >2.0");
        assertTrue(expr1.interpret(Version.valueOf("2.5.0")));
        Expression expr2 = parser.parse("~1.0 & (<2.0 | >2.0)");
        assertFalse(expr2.interpret(Version.valueOf("2.5.0")));
    }

    @Test
    public void shouldParseComplexExpressions() {
        ExpressionParser parser = new ExpressionParser(new Lexer());
        Expression expr = parser.parse(
            "((>=1.0.1 & <2) | (>=3.0 & <4)) & ((1-1.5) & (~1.5))"
        );
        assertTrue(expr.interpret(Version.valueOf("1.5.0")));
        assertFalse(expr.interpret(Version.valueOf("2.5.0")));
    }

    @Test
    public void shouldRaiseErrorIfClosingParenthesisIsMissing() {
        ExpressionParser parser = new ExpressionParser(new Lexer());
        try {
            parser.parse("((>=1.0.1 & < 2)");
        } catch (UnexpectedTokenException e) {
            return;
        }
        fail("Should raise error if closing parenthesis is missing");
    }
}
