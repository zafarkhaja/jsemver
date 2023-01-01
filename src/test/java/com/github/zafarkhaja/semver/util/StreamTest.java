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
package com.github.zafarkhaja.semver.util;

import com.github.zafarkhaja.semver.util.Stream.ElementType;
import java.util.Iterator;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Zafar Khaja <zafarkhaja@gmail.com>
 */
public class StreamTest {

    @Test
    public void shouldBeBackedByArray() {
        Character[] input = {'a', 'b', 'c'};
        Stream<Character> stream = new Stream<Character>(input);
        assertArrayEquals(input, stream.toArray());
    }

    @Test
    public void shouldImplementIterable() {
        Character[] input = {'a', 'b', 'c'};
        Stream<Character> stream = new Stream<Character>(input);
        Iterator<Character> it = stream.iterator();
        for (Character chr : input) {
            assertEquals(chr, it.next());
        }
        assertFalse(it.hasNext());
    }

    @Test
    public void shouldNotReturnRealElementsArray() {
        Stream<Character> stream = new Stream<Character>(
            new Character[] {'a', 'b', 'c'}
        );
        Character[] charArray = stream.toArray();
        charArray[0] = Character.valueOf('z');
        assertEquals(Character.valueOf('z'), charArray[0]);
        assertEquals(Character.valueOf('a'), stream.toArray()[0]);
    }

    @Test
    public void shouldReturnArrayOfElementsThatAreLeftInStream() {
        Stream<Character> stream = new Stream<Character>(
            new Character[] {'a', 'b', 'c'}
        );
        stream.consume();
        stream.consume();
        assertEquals(1, stream.toArray().length);
        assertEquals(Character.valueOf('c'), stream.toArray()[0]);
    }

    @Test
    public void shouldConsumeElementsOneByOne() {
        Stream<Character> stream = new Stream<Character>(
            new Character[] {'a', 'b', 'c'}
        );
        assertEquals(Character.valueOf('a'), stream.consume());
        assertEquals(Character.valueOf('b'), stream.consume());
        assertEquals(Character.valueOf('c'), stream.consume());
    }

    @Test
    public void shouldRaiseErrorWhenUnexpectedElementConsumed() {
        Stream<Character> stream = new Stream<Character>(
            new Character[] {'a', 'b', 'c'}
        );
        try {
            stream.consume(new ElementType<Character>() {
                @Override
                public boolean isMatchedBy(Character element) {
                    return false;
                }
            });
        } catch (UnexpectedElementException e) {
            return;
        }
        fail("Should raise error when unexpected element type is consumed");
    }

    @Test
    public void shouldLookaheadWithoutConsuming() {
        Stream<Character> stream = new Stream<Character>(
            new Character[] {'a', 'b', 'c'}
        );
        assertEquals(Character.valueOf('a'), stream.lookahead());
        assertEquals(Character.valueOf('a'), stream.lookahead());
    }

    @Test
    public void shouldLookaheadArbitraryNumberOfElements() {
        Stream<Character> stream = new Stream<Character>(
            new Character[] {'a', 'b', 'c'}
        );
        assertEquals(Character.valueOf('a'), stream.lookahead(1));
        assertEquals(Character.valueOf('b'), stream.lookahead(2));
        assertEquals(Character.valueOf('c'), stream.lookahead(3));
    }

    @Test
    public void shouldCheckIfLookaheadIsOfExpectedTypes() {
        Stream<Character> stream = new Stream<Character>(
            new Character[] {'a', 'b', 'c'}
        );
        assertTrue(stream.positiveLookahead(
            new ElementType<Character>() {
                @Override
                public boolean isMatchedBy(Character element) {
                    return element == 'a';
                }
            }
        ));
        assertFalse(stream.positiveLookahead(
            new ElementType<Character>() {
                @Override
                public boolean isMatchedBy(Character element) {
                    return element == 'c';
                }
            }
        ));
    }

    @Test
    public void shouldCheckIfElementOfExpectedTypesExistBeforeGivenType() {
        Stream<Character> stream = new Stream<Character>(
            new Character[] {'1', '.', '0', '.', '0'}
        );
        assertTrue(stream.positiveLookaheadBefore(
            new ElementType<Character>() {
                @Override
                public boolean isMatchedBy(Character element) {
                    return element == '.';
                }
            },
            new ElementType<Character>() {
                @Override
                public boolean isMatchedBy(Character element) {
                    return element == '1';
                }
            }
        ));
        assertFalse(stream.positiveLookaheadBefore(
            new ElementType<Character>() {
                @Override
                public boolean isMatchedBy(Character element) {
                    return element == '1';
                }
            },
            new ElementType<Character>() {
                @Override
                public boolean isMatchedBy(Character element) {
                    return element == '.';
                }
            }
        ));
    }

    @Test
    public void shouldCheckIfElementOfExpectedTypesExistUntilGivenPosition() {
        Stream<Character> stream = new Stream<Character>(
            new Character[] {'1', '.', '0', '.', '0'}
        );
        assertTrue(stream.positiveLookaheadUntil(
            3,
            new ElementType<Character>() {
                @Override
                public boolean isMatchedBy(Character element) {
                    return element == '0';
                }
            }
        ));
        assertFalse(stream.positiveLookaheadUntil(
            3,
            new ElementType<Character>() {
                @Override
                public boolean isMatchedBy(Character element) {
                    return element == 'a';
                }
            }
        ));
    }

    @Test
    public void shouldPushBackOneElementAtATime() {
        Stream<Character> stream = new Stream<Character>(
            new Character[] {'a', 'b', 'c'}
        );
        assertEquals(Character.valueOf('a'), stream.consume());
        stream.pushBack();
        assertEquals(Character.valueOf('a'), stream.consume());
    }

    @Test
    public void shouldStopPushingBackWhenThereAreNoElements() {
        Stream<Character> stream = new Stream<Character>(
            new Character[] {'a', 'b', 'c'}
        );
        assertEquals(Character.valueOf('a'), stream.consume());
        assertEquals(Character.valueOf('b'), stream.consume());
        assertEquals(Character.valueOf('c'), stream.consume());
        stream.pushBack();
        stream.pushBack();
        stream.pushBack();
        stream.pushBack();
        assertEquals(Character.valueOf('a'), stream.consume());
    }

    @Test
    public void shouldKeepTrackOfCurrentOffset() {
        Stream<Character> stream = new Stream<Character>(
            new Character[] {'a', 'b', 'c'}
        );
        assertEquals(0, stream.currentOffset());
        stream.consume();
        assertEquals(1, stream.currentOffset());
        stream.consume();
        stream.consume();
        assertEquals(3, stream.currentOffset());
    }
}
