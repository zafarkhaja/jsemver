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

import org.junit.jupiter.api.Test;
import static com.github.zafarkhaja.semver.expr.CompositeExpression.Helper.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Zafar Khaja {@literal <zafarkhaja@gmail.com>}
 */
class CompositeExpressionTest {

    @Test
    void shouldSupportEqualExpression() {
        assertTrue(eq("1.0.0").interpret("1.0.0"));
        assertFalse(eq("1.0.0").interpret("2.0.0"));
    }

    @Test
    void shouldSupportNotEqualExpression() {
        assertTrue(neq("1.0.0").interpret("2.0.0"));
    }

    @Test
    void shouldSupportGreaterExpression() {
        assertTrue(gt("1.0.0").interpret("2.0.0"));
        assertFalse(gt("2.0.0").interpret("1.0.0"));
    }

    @Test
    void shouldSupportGreaterOrEqualExpression() {
        assertTrue(gte("1.0.0").interpret("1.0.0"));
        assertTrue(gte("1.0.0").interpret("2.0.0"));
        assertFalse(gte("2.0.0").interpret("1.0.0"));
    }

    @Test
    void shouldSupportLessExpression() {
        assertTrue(lt("2.0.0").interpret("1.0.0"));
        assertFalse(lt("1.0.0").interpret("2.0.0"));
    }

    @Test
    void shouldSupportLessOrEqualExpression() {
        assertTrue(lte("1.0.0").interpret("1.0.0"));
        assertTrue(lte("2.0.0").interpret("1.0.0"));
        assertFalse(lte("1.0.0").interpret("2.0.0"));
    }

    @Test
    void shouldSupportNotExpression() {
        assertTrue(not(eq("1.0.0")).interpret("2.0.0"));
        assertFalse(not(eq("1.0.0")).interpret("1.0.0"));
    }

    @Test
    void shouldSupportAndExpression() {
        assertTrue(gt("1.0.0").and(lt("2.0.0")).interpret("1.5.0"));
        assertFalse(gt("1.0.0").and(lt("2.0.0")).interpret("2.5.0"));
    }

    @Test
    void shouldSupportOrExpression() {
        assertTrue(lt("1.0.0").or(gt("1.0.0")).interpret("1.5.0"));
        assertFalse(gt("1.0.0").or(gt("2.0.0")).interpret("0.5.0"));
    }

    @Test
    void shouldSupportComplexExpressions() {
        /* ((>=1.0.1 & <2) | (>=3.0 & <4)) & ((1-1.5) & (~1.5)) */
        CompositeExpression expr =
            gte("1.0.1").and(
                lt("2.0.0").or(
                    gte("3.0.0").and(
                        lt("4.0.0").and(
                            gte("1.0.0").and(
                                lte("1.5.0").and(
                                    gte("1.5.0").and(
                                        lt("2.0.0")
                                    )
                                )
                            )
                        )
                    )
                )
            );
        assertTrue(expr.interpret("1.5.0"));
        assertFalse(expr.interpret("2.5.0"));
    }
}
