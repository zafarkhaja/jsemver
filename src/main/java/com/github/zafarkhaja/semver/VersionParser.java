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
package com.github.zafarkhaja.semver;

import com.github.zafarkhaja.semver.VersionParser.CharStream;
import java.util.ArrayList;
import java.util.List;
import static com.github.zafarkhaja.semver.VersionParser.Char.*;

/**
 *
 * @author Zafar Khaja <zafarkhaja@gmail.com>
 */
class VersionParser implements Parser<Version> {

    static class CharStream {

        static interface CharType {
            boolean isMatchedBy(char chr);
        }

        private final char[] data;

        private int offset = 0;

        static final char EOL = (char) -1;

        CharStream(String input) {
            data = input.toCharArray();
        }

        char consume() {
            if (offset + 1 <= data.length) {
                return data[offset++];
            }
            return EOL;
        }

        char consume(CharType... expected) {
            char la = lookahead(1);
            for (CharType charType : expected) {
                if (charType.isMatchedBy(la)) {
                    return consume();
                }
            }
            throw new UnexpectedCharacterException(la, expected);
        }

        char lookahead() {
            return lookahead(1);
        }

        char lookahead(int pos) {
            int idx = offset + pos - 1;
            if (idx < data.length) {
                return data[idx];
            }
            return EOL;
        }

        boolean positiveLookahead(CharType... expected) {
            char la = lookahead(1);
            for (CharType charType : expected) {
                if (charType.isMatchedBy(la)) {
                    return true;
                }
            }
            return false;
        }

        boolean positiveLookaheadBefore(CharType before, CharType... expected) {
            char la;
            for (int i = 1; i <= data.length; i++) {
                la = lookahead(i);
                if (before.isMatchedBy(la)) {
                    break;
                }
                for (CharType charType : expected) {
                    if (charType.isMatchedBy(la)) {
                        return true;
                    }
                }
            }
            return false;
        }

        char[] toArray() {
            return data.clone();
        }
    }

    static enum Char implements CharStream.CharType {

        DIGIT {
            @Override
            public boolean isMatchedBy(char chr) {
                return chr >= '0' && chr <= '9';
            }
        },
        LETTER {
            @Override
            public boolean isMatchedBy(char chr) {
                return (chr >= 'a' && chr <= 'z')
                    || (chr >= 'A' && chr <= 'Z');
            }
        },
        DOT {
            @Override
            public boolean isMatchedBy(char chr) {
                return chr == '.';
            }
        },
        HYPHEN {
            @Override
            public boolean isMatchedBy(char chr) {
                return chr == '-';
            }
        },
        PLUS {
            @Override
            public boolean isMatchedBy(char chr) {
                return chr == '+';
            }
        },
        EOL {
            @Override
            public boolean isMatchedBy(char chr) {
                return chr == CharStream.EOL;
            }
        };
    }

    private final CharStream chars;

    VersionParser(String input) {
        chars = new CharStream(input);
    }

    @Override
    public Version parse(String input) {
        return parseValidSemVer();
    }

    static Version parseValidSemVer(String version) {
        VersionParser parser = new VersionParser(version);
        return parser.parseValidSemVer();
    }

    static NormalVersion parseVersionCore(String versionCore) {
        VersionParser parser = new VersionParser(versionCore);
        return parser.parseVersionCore();
    }

    static MetadataVersion parsePreRelease(String preRelease) {
        VersionParser parser = new VersionParser(preRelease);
        return parser.parsePreRelease();
    }

    static MetadataVersion parseBuild(String build) {
        VersionParser parser = new VersionParser(build);
        return parser.parseBuild();
    }

    private Version parseValidSemVer() {
        NormalVersion normalVersion = parseVersionCore();
        MetadataVersion preReleaseVersion = null;
        MetadataVersion buildMetadata = null;
        if (chars.positiveLookahead(HYPHEN)) {
            chars.consume();
            preReleaseVersion = parsePreRelease();
        }
        if (chars.positiveLookahead(PLUS)) {
            chars.consume();
            buildMetadata = parseBuild();
        }
        return new Version(
            normalVersion,
            preReleaseVersion,
            buildMetadata
        );
    }

    private NormalVersion parseVersionCore() {
        int major = Integer.parseInt(numericIdentifier());
        chars.consume(DOT);
        int minor = Integer.parseInt(numericIdentifier());
        chars.consume(DOT);
        int patch = Integer.parseInt(numericIdentifier());
        return new NormalVersion(major, minor, patch);
    }

    private MetadataVersion parsePreRelease() {
        Char end = closestEndpoint(PLUS, EOL);
        Char before = closestEndpoint(DOT, end);
        List<String> idents = new ArrayList<String>();
        while (!chars.positiveLookahead(end)) {
            if (before == DOT) {
                checkForEmptyIdentifier();
            }
            if (chars.positiveLookaheadBefore(before, LETTER, HYPHEN)) {
                idents.add(alphanumericIdentifier());
            } else {
                idents.add(numericIdentifier());
            }
            if (before == DOT) {
                chars.consume();
            }
            before = closestEndpoint(DOT, end);
        }
        return new MetadataVersion(
            idents.toArray(new String[idents.size()])
        );
    }

    private MetadataVersion parseBuild() {
        Char end = EOL;
        Char before = closestEndpoint(DOT, end);
        List<String> idents = new ArrayList<String>();
        while (!chars.positiveLookahead(end)) {
            if (before == DOT) {
                checkForEmptyIdentifier();
            }
            if (chars.positiveLookaheadBefore(before, LETTER, HYPHEN)) {
                idents.add(alphanumericIdentifier());
            } else {
                idents.add(digits());
            }
            if (before == DOT) {
                chars.consume();
            }
            before = closestEndpoint(DOT, end);
        }
        return new MetadataVersion(
            idents.toArray(new String[idents.size()])
        );
    }

    private String numericIdentifier() {
        checkForLeadingZeroes();
        StringBuilder sb = new StringBuilder();
        sb.append(chars.consume(DIGIT));
        while (chars.positiveLookahead(DIGIT)) {
            sb.append(chars.consume());
        }
        return sb.toString();
    }

    private String alphanumericIdentifier() {
        StringBuilder sb = new StringBuilder();
        sb.append(chars.consume(DIGIT, LETTER, HYPHEN));
        while (chars.positiveLookahead(DIGIT, LETTER, HYPHEN)) {
            sb.append(chars.consume());
        }
        return sb.toString();
    }

    private String digits() {
        StringBuilder sb = new StringBuilder();
        sb.append(chars.consume(DIGIT));
        while (chars.positiveLookahead(DIGIT)) {
            sb.append(chars.consume());
        }
        return sb.toString();
    }

    private Char closestEndpoint(Char tryThis, Char orThis) {
        if (chars.positiveLookaheadBefore(orThis, tryThis)) {
            return tryThis;
        }
        return orThis;
    }

    private void checkForLeadingZeroes() {
        char la1 = chars.lookahead(1);
        char la2 = chars.lookahead(2);
        if (la1 == '0' && DIGIT.isMatchedBy(la2)) {
            throw new GrammarException(
                "Numeric identifier MUST NOT contain leading zeroes"
            );
        }
    }

    private void checkForEmptyIdentifier() {
        if (DOT.isMatchedBy(chars.lookahead(1))) {
            throw new GrammarException("Identifiers MUST NOT be empty");
        }
    }
}
