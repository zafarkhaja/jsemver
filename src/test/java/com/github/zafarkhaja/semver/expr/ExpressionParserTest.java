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
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Zafar Khaja {@literal <zafarkhaja@gmail.com>}
 */
public class ExpressionParserTest {

    @Test
    public void shouldParseEqualComparisonRange() {
        ExpressionParser parser = new ExpressionParser(new Lexer());
        Expression eq = parser.parse("=1.0.0");
        assertTrue(eq.interpret(Version.of(1, 0, 0)));
    }

    @Test
    public void shouldParseEqualComparisonRangeIfOnlyFullVersionGiven() {
        ExpressionParser parser = new ExpressionParser(new Lexer());
        Expression eq = parser.parse("1.0.0");
        assertTrue(eq.interpret(Version.of(1, 0, 0)));
    }

    @Test
    public void shouldParseNotEqualComparisonRange() {
        ExpressionParser parser = new ExpressionParser(new Lexer());
        Expression ne = parser.parse("!=1.0.0");
        assertTrue(ne.interpret(Version.of(1, 2, 3)));
    }

    @Test
    public void shouldParseGreaterComparisonRange() {
        ExpressionParser parser = new ExpressionParser(new Lexer());
        Expression gt = parser.parse(">1.0.0");
        assertTrue(gt.interpret(Version.of(1, 2, 3)));
    }

    @Test
    public void shouldParseGreaterOrEqualComparisonRange() {
        ExpressionParser parser = new ExpressionParser(new Lexer());
        Expression ge = parser.parse(">=1.0.0");
        assertTrue(ge.interpret(Version.of(1, 0, 0)));
        assertTrue(ge.interpret(Version.of(1, 2, 3)));
    }

    @Test
    public void shouldParseLessComparisonRange() {
        ExpressionParser parser = new ExpressionParser(new Lexer());
        Expression lt = parser.parse("<1.2.3");
        assertTrue(lt.interpret(Version.of(1, 0, 0)));
    }

    @Test
    public void shouldParseLessOrEqualComparisonRange() {
        ExpressionParser parser = new ExpressionParser(new Lexer());
        Expression le = parser.parse("<=1.2.3");
        assertTrue(le.interpret(Version.of(1, 0, 0)));
        assertTrue(le.interpret(Version.of(1, 2, 3)));
    }

    @Test
    public void shouldSupportLongNumericIdentifiersInComparisonRanges() {
        long l = Integer.MAX_VALUE + 1L;
        String version = "=" + l + "." + l + "." + l;
        ExpressionParser parser = new ExpressionParser(new Lexer());
        Expression eq = parser.parse(version);
        assertTrue(eq.interpret(Version.of(l, l, l)));
    }

    @Test
    public void shouldParseTildeRange() {
        ExpressionParser parser = new ExpressionParser(new Lexer());
        Expression expr1 = parser.parse("~1");
        assertTrue(expr1.interpret(Version.of(1, 2, 3)));
        assertFalse(expr1.interpret(Version.of(3, 2, 1)));
        Expression expr2 = parser.parse("~1.2");
        assertTrue(expr2.interpret(Version.of(1, 2, 3)));
        assertFalse(expr2.interpret(Version.of(2, 0, 0)));
        Expression expr3 = parser.parse("~1.2.3");
        assertTrue(expr3.interpret(Version.of(1, 2, 3)));
        assertFalse(expr3.interpret(Version.of(1, 3, 0)));
    }

    @Test
    public void shouldSupportLongNumericIdentifiersInTildeRanges() {
        long l = Integer.MAX_VALUE + 1L;
        String tildeRange = "~" + l + "." + l + "." + l;
        ExpressionParser parser = new ExpressionParser(new Lexer());
        Expression expr = parser.parse(tildeRange);
        assertTrue(expr.interpret(Version.of(l, l, l)));
    }

    @Test
    public void shouldRaiseErrorIfIncrementCausesOverflowInTildeRanges() {
        long lmv = Long.MAX_VALUE;
        ExpressionParser parser = new ExpressionParser(new Lexer());
        for (String r : Arrays.asList(("~" + lmv), ("~1." + lmv), ("~1." + lmv + ".0"))) {
            assertThrows(ArithmeticException.class, () -> parser.parse(r));
        }
    }

    @Test
    public void shouldParseCaretRange() {
        ExpressionParser parser = new ExpressionParser(new Lexer());
        Expression expr1 = parser.parse("^1");
        assertTrue(expr1.interpret(Version.of(1, 2, 3)));
        assertFalse(expr1.interpret(Version.of(3, 2, 1)));
        Expression expr2 = parser.parse("^0.2");
        assertTrue(expr2.interpret(Version.of(0, 2, 3)));
        assertFalse(expr2.interpret(Version.of(0, 3, 0)));
        Expression expr3 = parser.parse("^0.0.3");
        assertTrue(expr3.interpret(Version.of(0, 0, 3)));
        assertFalse(expr3.interpret(Version.of(0, 0, 4)));
    }

    @Test
    public void shouldSupportLongNumericIdentifiersInCaretRanges() {
        long l = Integer.MAX_VALUE + 1L;
        String caretRange = "^" + l + "." + l + "." + l;
        ExpressionParser parser = new ExpressionParser(new Lexer());
        Expression expr = parser.parse(caretRange);
        assertTrue(expr.interpret(Version.of(l, l, l)));
    }

    @Test
    public void shouldRaiseErrorIfIncrementCausesOverflowInCaretRanges() {
        long lmv = Long.MAX_VALUE;
        ExpressionParser parser = new ExpressionParser(new Lexer());
        for (String r : Arrays.asList(("^" + lmv), ("^0." + lmv), ("^0.0." + lmv))) {
            assertThrows(ArithmeticException.class, () -> parser.parse(r));
        }
    }

    @Test
    public void shouldParsePartialVersionRange() {
        ExpressionParser parser = new ExpressionParser(new Lexer());
        Expression expr1 = parser.parse("1");
        assertTrue(expr1.interpret(Version.of(1, 2, 3)));
        Expression expr2 = parser.parse("2.0");
        assertTrue(expr2.interpret(Version.of(2, 0, 9)));
    }

    @Test
    public void shouldSupportLongNumericIdentifiersInPartialVersionRanges() {
        long l = Integer.MAX_VALUE + 1L;
        String partialVersion = l + "." + l;
        ExpressionParser parser = new ExpressionParser(new Lexer());
        Expression expr = parser.parse(partialVersion);
        assertTrue(expr.interpret(Version.of(l, l, l)));
    }

    @Test
    public void shouldRaiseErrorIfIncrementCausesOverflowInPartialVersionRanges() {
        String lmv = String.valueOf(Long.MAX_VALUE);
        ExpressionParser parser = new ExpressionParser(new Lexer());
        for (String r : Arrays.asList((lmv), ("1." + lmv))) {
            assertThrows(ArithmeticException.class, () -> parser.parse(r));
        }
    }

    @Test
    public void shouldParseWildcardRange() {
        ExpressionParser parser = new ExpressionParser(new Lexer());
        Expression expr1 = parser.parse("1.*");
        assertTrue(expr1.interpret(Version.of(1, 2, 3)));
        assertFalse(expr1.interpret(Version.of(3, 2, 1)));
        Expression expr2 = parser.parse("1.2.x");
        assertTrue(expr2.interpret(Version.of(1, 2, 3)));
        assertFalse(expr2.interpret(Version.of(1, 3, 2)));
        Expression expr3 = parser.parse("X");
        assertTrue(expr3.interpret(Version.of(1, 2, 3)));
    }

    @Test
    public void shouldSupportLongNumericIdentifiersInWildcardRanges() {
        long l = Integer.MAX_VALUE + 1L;
        String wildcardRange = l + "." + l + "." + "x";
        ExpressionParser parser = new ExpressionParser(new Lexer());
        Expression expr = parser.parse(wildcardRange);
        assertTrue(expr.interpret(Version.of(l, l, l)));
    }

    @Test
    public void shouldRaiseErrorIfIncrementCausesOverflowInWildcardRanges() {
        long lmv = Long.MAX_VALUE;
        ExpressionParser parser = new ExpressionParser(new Lexer());
        for (String r : Arrays.asList((lmv + ".x"), ("1." + lmv + ".x"))) {
            assertThrows(ArithmeticException.class, () -> parser.parse(r));
        }
    }

    @Test
    public void shouldParseHyphenRange() {
        ExpressionParser parser = new ExpressionParser(new Lexer());
        Expression range = parser.parse("1.0.0 - 2.0.0");
        assertTrue(range.interpret(Version.of(1, 2, 3)));
        assertFalse(range.interpret(Version.of(3, 2, 1)));
    }

    @Test
    public void shouldParseMultipleRangesJoinedWithAnd() {
        ExpressionParser parser = new ExpressionParser(new Lexer());
        Expression and = parser.parse(">=1.0.0 & <2.0.0");
        assertTrue(and.interpret(Version.of(1, 2, 3)));
        assertFalse(and.interpret(Version.of(3, 2, 1)));
    }

    @Test
    public void shouldParseMultipleRangesJoinedWithOr() {
        ExpressionParser parser = new ExpressionParser(new Lexer());
        Expression or = parser.parse("1.* | =2.0.0");
        assertTrue(or.interpret(Version.of(1, 2, 3)));
        assertFalse(or.interpret(Version.of(2, 1, 0)));
    }

    @Test
    public void shouldParseParenthesizedExpression() {
        ExpressionParser parser = new ExpressionParser(new Lexer());
        Expression expr = parser.parse("(1)");
        assertTrue(expr.interpret(Version.of(1, 2, 3)));
        assertFalse(expr.interpret(Version.of(2, 0, 0)));
    }

    @Test
    public void shouldParseExpressionWithMultipleParentheses() {
        ExpressionParser parser = new ExpressionParser(new Lexer());
        Expression expr = parser.parse("((1))");
        assertTrue(expr.interpret(Version.of(1, 2, 3)));
        assertFalse(expr.interpret(Version.of(2, 0, 0)));
    }

    @Test
    public void shouldParseNotExpression() {
        ExpressionParser parser = new ExpressionParser(new Lexer());
        Expression not1 = parser.parse("!(1)");
        assertTrue(not1.interpret(Version.of(2, 0, 0)));
        assertFalse(not1.interpret(Version.of(1, 2, 3)));
        Expression not2 = parser.parse("0.* & !(>=1 & <2)");
        assertTrue(not2.interpret(Version.of(0, 5, 0)));
        assertFalse(not2.interpret(Version.of(1, 0, 1)));
        Expression not3 = parser.parse("!(>=1 & <2) & >=2");
        assertTrue(not3.interpret(Version.of(2, 0, 0)));
        assertFalse(not3.interpret(Version.of(1, 2, 3)));
    }

    @Test
    public void shouldRespectPrecedenceWhenUsedWithParentheses() {
        ExpressionParser parser = new ExpressionParser(new Lexer());
        Expression expr1 = parser.parse("(~1.0 & <2.0) | >2.0");
        assertTrue(expr1.interpret(Version.of(2, 5, 0)));
        Expression expr2 = parser.parse("~1.0 & (<2.0 | >2.0)");
        assertFalse(expr2.interpret(Version.of(2, 5, 0)));
    }

    @Test
    public void shouldParseComplexExpressions() {
        ExpressionParser parser = new ExpressionParser(new Lexer());
        Expression expr = parser.parse("((>=1.0.1 & <2) | (>=3.0 & <4)) & ((1-1.5) & (~1.5))");
        assertTrue(expr.interpret(Version.of(1, 5, 0)));
        assertFalse(expr.interpret(Version.of(2, 5, 0)));
    }
}
