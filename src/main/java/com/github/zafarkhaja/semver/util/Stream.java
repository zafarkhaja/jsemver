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
package com.github.zafarkhaja.semver.util;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 *
 * @author Zafar Khaja <zafarkhaja@gmail.com>
 */
public class Stream<E> implements Iterable<E> {

    public static interface ElementType<E> {
        boolean isMatchedBy(E element);
    }

    private final E[] elements;

    private int offset = 0;

    public Stream(E[] elements) {
        this.elements = elements.clone();
    }

    public E consume() {
        if (offset >= elements.length) {
            return null;
        }
        return elements[offset++];
    }

    public E consume(ElementType<E>... expected) {
        E lookahead = lookahead(1);
        for (ElementType<E> type : expected) {
            if (type.isMatchedBy(lookahead)) {
                return consume();
            }
        }
        throw new UnexpectedElementTypeException(lookahead, expected);
    }

    public E lookahead() {
        return lookahead(1);
    }

    public E lookahead(int position) {
        int idx = offset + position - 1;
        if (idx < elements.length) {
            return elements[idx];
        }
        return null;
    }

    public boolean positiveLookahead(ElementType<E>... expected) {
        for (ElementType<E> type : expected) {
            if (type.isMatchedBy(lookahead(1))) {
                return true;
            }
        }
        return false;
    }

    public boolean positiveLookaheadBefore(
        ElementType<E> before,
        ElementType<E>... expected
    ) {
        E lookahead;
        for (int i = 1; i <= elements.length; i++) {
            lookahead = lookahead(i);
            if (before.isMatchedBy(lookahead)) {
                break;
            }
            for (ElementType<E> type : expected) {
                if (type.isMatchedBy(lookahead)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean positiveLookaheadUntil(
        int until,
        ElementType<E>... expected
    ) {
        for (int i = 1; i <= until; i++) {
            for (ElementType<E> type : expected) {
                if (type.isMatchedBy(lookahead(i))) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public Iterator<E> iterator() {
        return new Iterator<E>() {

            private int index = offset;

            @Override
            public boolean hasNext() {
                return index < elements.length;
            }

            @Override
            public E next() {
                if (index >= elements.length) {
                    throw new NoSuchElementException();
                }
                return elements[index++];
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    public E[] toArray() {
        return Arrays.copyOfRange(elements, offset, elements.length);
    }
}
