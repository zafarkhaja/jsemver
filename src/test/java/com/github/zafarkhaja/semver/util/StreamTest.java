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

import java.util.Iterator;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Zafar Khaja {@literal <zafarkhaja@gmail.com>}
 */
class StreamTest {

    @Test
    void shouldBeBackedByArray() {
        Character[] input = {'a', 'b', 'c'};
        Stream<Character> stream = new Stream<>(input);
        assertArrayEquals(input, stream.toArray());
    }

    @Test
    void shouldImplementIterable() {
        Character[] input = {'a', 'b', 'c'};
        Stream<Character> stream = new Stream<>(input);
        Iterator<Character> it = stream.iterator();
        for (Character chr : input) {
            assertEquals(chr, it.next());
        }
        assertFalse(it.hasNext());
    }

    @Test
    void shouldNotReturnRealElementsArray() {
        Stream<Character> stream = new Stream<>(new Character[] {'a', 'b', 'c'});
        Character[] charArray = stream.toArray();
        charArray[0] = Character.valueOf('z');
        assertEquals(Character.valueOf('z'), charArray[0]);
        assertEquals(Character.valueOf('a'), stream.toArray()[0]);
    }

    @Test
    void shouldReturnArrayOfElementsThatAreLeftInStream() {
        Stream<Character> stream = new Stream<>(new Character[] {'a', 'b', 'c'});
        stream.consume();
        stream.consume();
        assertEquals(1, stream.toArray().length);
        assertEquals(Character.valueOf('c'), stream.toArray()[0]);
    }

    @Test
    void shouldConsumeElementsOneByOne() {
        Stream<Character> stream = new Stream<>(new Character[] {'a', 'b', 'c'});
        assertEquals(Character.valueOf('a'), stream.consume());
        assertEquals(Character.valueOf('b'), stream.consume());
        assertEquals(Character.valueOf('c'), stream.consume());
    }

    @Test
    void shouldRaiseErrorWhenUnexpectedElementConsumed() {
        Stream<Character> stream = new Stream<>(new Character[] {'a', 'b', 'c'});
        assertThrows(
            UnexpectedElementException.class,
            () -> stream.consume(element -> false),
            "Should raise error when unexpected element type is consumed"
        );
    }

    @Test
    void shouldLookaheadWithoutConsuming() {
        Stream<Character> stream = new Stream<>(new Character[] {'a', 'b', 'c'});
        assertEquals(Character.valueOf('a'), stream.lookahead());
        assertEquals(Character.valueOf('a'), stream.lookahead());
    }

    @Test
    void shouldLookaheadArbitraryNumberOfElements() {
        Stream<Character> stream = new Stream<>(new Character[] {'a', 'b', 'c'});
        assertEquals(Character.valueOf('a'), stream.lookahead(1));
        assertEquals(Character.valueOf('b'), stream.lookahead(2));
        assertEquals(Character.valueOf('c'), stream.lookahead(3));
    }

    @Test
    void shouldCheckIfLookaheadIsOfExpectedTypes() {
        Stream<Character> stream = new Stream<>(new Character[] {'a', 'b', 'c'});
        assertTrue(stream.positiveLookahead(element -> element == 'a'));
        assertFalse(stream.positiveLookahead(element -> element == 'c'));
    }

    @Test
    void shouldCheckIfElementOfExpectedTypesExistBeforeGivenType() {
        Stream<Character> stream = new Stream<>(new Character[] {'1', '.', '0', '.', '0'});
        assertTrue(stream.positiveLookaheadBefore(
            element -> element == '.',
            element -> element == '1'
        ));
        assertFalse(stream.positiveLookaheadBefore(
            element -> element == '1',
            element -> element == '.'
        ));
    }

    @Test
    void shouldCheckIfElementOfExpectedTypesExistUntilGivenPosition() {
        Stream<Character> stream = new Stream<>(new Character[] {'1', '.', '0', '.', '0'});
        assertTrue(stream.positiveLookaheadUntil(3, element -> element == '0'));
        assertFalse(stream.positiveLookaheadUntil(3, element -> element == 'a'));
    }

    @Test
    void shouldPushBackOneElementAtATime() {
        Stream<Character> stream = new Stream<>(new Character[] {'a', 'b', 'c'});
        assertEquals(Character.valueOf('a'), stream.consume());
        stream.pushBack();
        assertEquals(Character.valueOf('a'), stream.consume());
    }

    @Test
    void shouldStopPushingBackWhenThereAreNoElements() {
        Stream<Character> stream = new Stream<>(new Character[] {'a', 'b', 'c'});
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
    void shouldKeepTrackOfCurrentOffset() {
        Stream<Character> stream = new Stream<>(new Character[] {'a', 'b', 'c'});
        assertEquals(0, stream.currentOffset());
        stream.consume();
        assertEquals(1, stream.currentOffset());
        stream.consume();
        stream.consume();
        assertEquals(3, stream.currentOffset());
    }
}
