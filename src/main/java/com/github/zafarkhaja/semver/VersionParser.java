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

import com.github.zafarkhaja.semver.util.Stream;
import com.github.zafarkhaja.semver.util.UnexpectedElementException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import static com.github.zafarkhaja.semver.VersionParser.CharType.*;

/**
 * A parser for the SemVer Version.
 *
 * @author Zafar Khaja <zafarkhaja@gmail.com>
 * @since 0.7.0
 */
class VersionParser implements Parser<Version> {

    /**
     * Valid character types.
     */
    static enum CharType implements Stream.ElementType<Character> {

        DIGIT {
            /**
             * {@inheritDoc}
             */
            @Override
            public boolean isMatchedBy(Character chr) {
                if (chr == null) {
                    return false;
                }
                return chr >= '0' && chr <= '9';
            }
        },
        LETTER {
            /**
             * {@inheritDoc}
             */
            @Override
            public boolean isMatchedBy(Character chr) {
                if (chr == null) {
                    return false;
                }
                return (chr >= 'a' && chr <= 'z')
                    || (chr >= 'A' && chr <= 'Z');
            }
        },
        DOT {
            /**
             * {@inheritDoc}
             */
            @Override
            public boolean isMatchedBy(Character chr) {
                if (chr == null) {
                    return false;
                }
                return chr == '.';
            }
        },
        HYPHEN {
            /**
             * {@inheritDoc}
             */
            @Override
            public boolean isMatchedBy(Character chr) {
                if (chr == null) {
                    return false;
                }
                return chr == '-';
            }
        },
        PLUS {
            /**
             * {@inheritDoc}
             */
            @Override
            public boolean isMatchedBy(Character chr) {
                if (chr == null) {
                    return false;
                }
                return chr == '+';
            }
        },
        EOL {
            /**
             * {@inheritDoc}
             */
            @Override
            public boolean isMatchedBy(Character chr) {
                return chr == null;
            }
        },
        ILLEGAL {
            /**
             * {@inheritDoc}
             */
            @Override
            public boolean isMatchedBy(Character chr) {
                EnumSet<CharType> itself = EnumSet.of(ILLEGAL);
                for (CharType type : EnumSet.complementOf(itself)) {
                    if (type.isMatchedBy(chr)) {
                        return false;
                    }
                }
                return true;
            }
        };
    }

    /**
     * The stream of characters.
     */
    private final Stream<Character> chars;

    /**
     * Constructs a {@code VersionParser} instance
     * with the input string to parse.
     *
     * @param input the input string to parse
     * @throws IllegalArgumentException if the input string is {@code NULL} or empty
     */
    VersionParser(String input) {
        if (input == null || input.isEmpty()) {
            throw new IllegalArgumentException("Input string is NULL or empty");
        }
        Character[] elements = new Character[input.length()];
        for (int i = 0; i < input.length(); i++) {
            elements[i] = Character.valueOf(input.charAt(i));
        }
        chars = new Stream<Character>(elements);
    }

    /**
     * Parses the input string.
     *
     * @param input the input string to parse
     * @return a valid version object
     * @throws ParseException when there is an error defined in
     *                        the SemVer or the formal grammar
     * @throws UnexpectedElementException when encounters an unexpected character type
     */
    @Override
    public Version parse(String input) {
        return parseValidSemVer();
    }

    /**
     * Parses the whole version including pre-release version and build metadata.
     *
     * @param version the version string to parse
     * @return a valid version object
     * @throws IllegalArgumentException if the input string is {@code NULL} or empty
     * @throws ParseException when there is an error defined in
     *                        the SemVer or the formal grammar
     * @throws UnexpectedElementException when encounters an unexpected character type
     */
    static Version parseValidSemVer(String version) {
        VersionParser parser = new VersionParser(version);
        return parser.parseValidSemVer();
    }

    /**
     * Parses the version core.
     *
     * @param versionCore the version core string to parse
     * @return a valid normal version object
     * @throws IllegalArgumentException if the input string is {@code NULL} or empty
     * @throws ParseException when there is an error defined in
     *                        the SemVer or the formal grammar
     * @throws UnexpectedElementException when encounters an unexpected character type
     */
    static NormalVersion parseVersionCore(String versionCore) {
        VersionParser parser = new VersionParser(versionCore);
        return parser.parseVersionCore();
    }

    /**
     * Parses the pre-release version.
     *
     * @param preRelease the pre-release version string to parse
     * @return a valid pre-release version object
     * @throws IllegalArgumentException if the input string is {@code NULL} or empty
     * @throws ParseException when there is an error defined in
     *                        the SemVer or the formal grammar
     */
    static MetadataVersion parsePreRelease(String preRelease) {
        VersionParser parser = new VersionParser(preRelease);
        return parser.parsePreRelease();
    }

    /**
     * Parses the build metadata.
     *
     * @param build the build metadata string to parse
     * @return a valid build metadata object
     * @throws IllegalArgumentException if the input string is {@code NULL} or empty
     * @throws ParseException when there is an error defined in
     *                        the SemVer or the formal grammar
     */
    static MetadataVersion parseBuild(String build) {
        VersionParser parser = new VersionParser(build);
        return parser.parseBuild();
    }

    /**
     * Parses the {@literal <valid semver>} non-terminal.
     *
     * <pre>
     * {@literal
     * <valid semver> ::= <version core>
     *                  | <version core> "-" <pre-release>
     *                  | <version core> "+" <build>
     *                  | <version core> "-" <pre-release> "+" <build>
     * }
     * </pre>
     *
     * @return a valid version object
     */
    private Version parseValidSemVer() {
        NormalVersion normal = parseVersionCore();
        MetadataVersion preRelease = MetadataVersion.NULL;
        if (chars.positiveLookahead(HYPHEN)) {
            chars.consume();
            preRelease = parsePreRelease();
        }
        MetadataVersion build = MetadataVersion.NULL;
        if (chars.positiveLookahead(PLUS)) {
            chars.consume();
            build = parseBuild();
        }
        return new Version(normal, preRelease, build);
    }

    /**
     * Parses the {@literal <version core>} non-terminal.
     *
     * <pre>
     * {@literal
     * <version core> ::= <major> "." <minor> "." <patch>
     * }
     * </pre>
     *
     * @return a valid normal version object
     */
    private NormalVersion parseVersionCore() {
        int major = Integer.parseInt(numericIdentifier());
        chars.consume(DOT);
        int minor = Integer.parseInt(numericIdentifier());
        chars.consume(DOT);
        int patch = Integer.parseInt(numericIdentifier());
        return new NormalVersion(major, minor, patch);
    }

    /**
     * Parses the {@literal <pre-release>} non-terminal.
     *
     * <pre>
     * {@literal
     * <pre-release> ::= <dot-separated pre-release identifiers>
     *
     * <dot-separated pre-release identifiers> ::= <pre-release identifier>
     *    | <pre-release identifier> "." <dot-separated pre-release identifiers>
     *
     * <pre-release identifier> ::= <alphanumeric identifier>
     *                            | <numeric identifier>
     * }
     * </pre>
     *
     * @return a valid pre-release version object
     * @throws ParseException if the pre-release version has empty identifier(s)
     */
    private MetadataVersion parsePreRelease() {
        List<String> idents = new ArrayList<String>();
        CharType end = closestEndpoint(PLUS, EOL);
        CharType before = closestEndpoint(DOT, end);
        do {
            checkForEmptyIdentifier();
            if (chars.positiveLookaheadBefore(before, LETTER, HYPHEN)) {
                idents.add(alphanumericIdentifier());
            } else {
                idents.add(numericIdentifier());
            }
            if (before == DOT) {
                chars.consume(DOT);
                before = closestEndpoint(DOT, end);
            }
        } while (!chars.positiveLookahead(end));
        return new MetadataVersion(idents.toArray(new String[idents.size()]));
    }

    /**
     * Parses the {@literal <build>} non-terminal.
     *
     * <pre>
     * {@literal
     * <build> ::= <dot-separated build identifiers>
     *
     * <dot-separated build identifiers> ::= <build identifier>
     *                | <build identifier> "." <dot-separated build identifiers>
     *
     * <build identifier> ::= <alphanumeric identifier>
     *                      | <digits>
     * }
     * </pre>
     *
     * @return a valid build metadata object
     * @throws ParseException if the build metadata has empty identifier(s)
     */
    private MetadataVersion parseBuild() {
        List<String> idents = new ArrayList<String>();
        CharType end = EOL;
        CharType before = closestEndpoint(DOT, end);
        do {
            checkForEmptyIdentifier();
            if (chars.positiveLookaheadBefore(before, LETTER, HYPHEN)) {
                idents.add(alphanumericIdentifier());
            } else {
                idents.add(digits());
            }
            if (before == DOT) {
                chars.consume(DOT);
                before = closestEndpoint(DOT, end);
            }
        } while (!chars.positiveLookahead(end));
        return new MetadataVersion(idents.toArray(new String[idents.size()]));
    }

    /**
     * Parses the {@literal <numeric identifier>} non-terminal.
     *
     * <pre>
     * {@literal
     * <numeric identifier> ::= "0"
     *                        | <positive digit>
     *                        | <positive digit> <digits>
     * }
     * </pre>
     *
     * @return a string representing the numeric identifier
     * @throws ParseException if the numeric identifier has leading zero(es)
     */
    private String numericIdentifier() {
        checkForLeadingZeroes();
        return digits();
    }

    /**
     * Parses the {@literal <alphanumeric identifier>} non-terminal.
     *
     * <pre>
     * {@literal
     * <alphanumeric identifier> ::= <non-digit>
     *             | <non-digit> <identifier characters>
     *             | <identifier characters> <non-digit>
     *             | <identifier characters> <non-digit> <identifier characters>
     * }
     * </pre>
     *
     * @return a string representing the alphanumeric identifier
     */
    private String alphanumericIdentifier() {
        StringBuilder sb = new StringBuilder();
        do {
            sb.append(chars.consume(DIGIT, LETTER, HYPHEN));
        } while (chars.positiveLookahead(DIGIT, LETTER, HYPHEN));
        return sb.toString();
    }

    /**
     * Parses the {@literal <digits>} non-terminal.
     *
     * <pre>
     * {@literal
     * <digits> ::= <digit>
     *            | <digit> <digits>
     * }
     * </pre>
     *
     * @return a string representing the digits
     */
    private String digits() {
        StringBuilder sb = new StringBuilder();
        do {
            sb.append(chars.consume(DIGIT));
        } while (chars.positiveLookahead(DIGIT));
        return sb.toString();
    }

    /**
     * Chooses the closest character.
     *
     * @param tryThis the character to try first
     * @param orThis the character to fallback to
     * @return the closest character
     */
    private CharType closestEndpoint(CharType tryThis, CharType orThis) {
        if (chars.positiveLookaheadBefore(orThis, tryThis)) {
            return tryThis;
        }
        return orThis;
    }

    /**
     * Checks for leading zeroes in the numeric identifiers.
     *
     * @throws ParseException if a numeric identifier has leading zero(es)
     */
    private void checkForLeadingZeroes() {
        Character la1 = chars.lookahead(1);
        Character la2 = chars.lookahead(2);
        if (la1 == '0' && DIGIT.isMatchedBy(la2)) {
            throw new ParseException(
                "Numeric identifier MUST NOT contain leading zeroes"
            );
        }
    }

    /**
     * Checks for empty identifiers in the pre-release version or build metadata.
     *
     * @throws ParseException if the pre-release version or build
     *                        metadata have empty identifier(s)
     */
    private void checkForEmptyIdentifier() {
        if (DOT.isMatchedBy(chars.lookahead(1))) {
            throw new ParseException("Identifiers MUST NOT be empty");
        }
    }
}
