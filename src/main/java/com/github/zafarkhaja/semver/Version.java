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
package com.github.zafarkhaja.semver;

import com.github.zafarkhaja.semver.expr.Expression;
import com.github.zafarkhaja.semver.expr.ExpressionParser;
import com.github.zafarkhaja.semver.expr.LexerException;
import com.github.zafarkhaja.semver.expr.UnexpectedTokenException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;
import java.util.Optional;
import static com.github.zafarkhaja.semver.Version.Validators.*;
import static com.github.zafarkhaja.semver.VersionParser.parseBuild;
import static com.github.zafarkhaja.semver.VersionParser.parsePreRelease;

/**
 * The {@code Version} class is the main class of the Java SemVer library.
 * <p>
 * This class implements the Facade design pattern.
 * It is also immutable, which makes the class thread-safe.
 *
 * @author Zafar Khaja {@literal <zafarkhaja@gmail.com>}
 * @since 0.1.0
 */
@SuppressWarnings("serial")
public class Version implements Comparable<Version>, Serializable {

    /**
     * A mutable builder for the immutable {@code Version} class
     */
    public static class Builder {

        private long major = 0;

        private long minor = 0;

        private long patch = 0;

        private String[] preReleaseIds = {};

        private String[] buildIds = {};

        /**
         * Default constructor, initializes fields with default values (0.0.0)
         */
        public Builder() {}

        /**
         * Sets the major version; the minor and patch versions are assigned 0.
         *
         * @param  major a major version number, non-negative
         * @return this {@code Builder} instance
         * @throws IllegalArgumentException if {@code major} is negative
         * @since  0.10.0
         */
        public Builder setVersionCore(long major) {
            return setVersionCore(major, 0, 0);
        }

        /**
         * Sets the major and minor versions; the patch version is assigned 0.
         *
         * @param  major a major version number, non-negative
         * @param  minor a minor version number, non-negative
         * @return this {@code Builder} instance
         * @throws IllegalArgumentException if any of the arguments is negative
         * @since  0.10.0
         */
        public Builder setVersionCore(long major, long minor) {
            return setVersionCore(major, minor, 0);
        }

        /**
         * Sets major, minor and patch versions.
         *
         * @param  major a major version number, non-negative
         * @param  minor a minor version number, non-negative
         * @param  patch a patch version number, non-negative
         * @return this {@code Builder} instance
         * @throws IllegalArgumentException if any of the arguments is negative
         * @since  0.10.0
         */
        public Builder setVersionCore(long major, long minor, long patch) {
            return
                setMajorVersion(major).
                setMinorVersion(minor).
                setPatchVersion(patch)
            ;
        }

        /**
         * Sets the major version.
         *
         * @param  major a major version number, non-negative
         * @return this {@code Builder} instance
         * @throws IllegalArgumentException if {@code major} is negative
         * @since  0.10.0
         */
        public Builder setMajorVersion(long major) {
            this.major = nonNegative(major, "major");
            return this;
        }

        /**
         * Sets the minor version.
         *
         * @param  minor a minor version number, non-negative
         * @return this {@code Builder} instance
         * @throws IllegalArgumentException if {@code minor} is negative
         * @since  0.10.0
         */
        public Builder setMinorVersion(long minor) {
            this.minor = nonNegative(minor, "minor");
            return this;
        }

        /**
         * Sets the patch version.
         *
         * @param  patch a patch version number, non-negative
         * @return this {@code Builder} instance
         * @throws IllegalArgumentException if {@code patch} is negative
         * @since  0.10.0
         */
        public Builder setPatchVersion(long patch) {
            this.patch = nonNegative(patch, "patch");
            return this;
        }

        /**
         * Sets the pre-release version.
         * <p>
         * Multiple identifiers can be specified in a single argument joined
         * with dots, or in separate arguments, or both.
         *
         * @param  ids one or more pre-release identifiers, non-null
         * @return this {@code Builder} instance
         * @throws IllegalArgumentException if {@code ids} is null/empty or contains null
         */
        public Builder setPreReleaseVersion(String... ids) {
            preReleaseIds = oneOrMoreNonNulls(ids, "ids").clone();
            return this;
        }

        /**
         * Appends (additional) pre-release identifier(s).
         * <p>
         * If no pre-release identifiers have been previously set, the method
         * works as {@link #setPreReleaseVersion(String...)}.
         * <p>
         * Multiple identifiers can be specified in a single argument joined
         * with dots, or in separate arguments, or both.
         *
         * @param  ids one or more pre-release identifiers, non-null
         * @return this {@code Builder} instance
         * @throws IllegalArgumentException if {@code ids} is null/empty or contains null
         * @see    #setPreReleaseVersion(String...)
         * @since  0.10.0
         */
        public Builder addPreReleaseIdentifiers(String... ids) {
            if (preReleaseIds.length == 0) {
                return setPreReleaseVersion(ids);
            }

            preReleaseIds = concatArrays(preReleaseIds, oneOrMoreNonNulls(ids, "ids"));
            return this;
        }

        /**
         * Unsets the pre-release version.
         *
         * @return this {@code Builder} instance
         * @since  0.10.0
         */
        public Builder unsetPreReleaseVersion() {
            preReleaseIds = new String[0];
            return this;
        }

        /**
         * Sets the build metadata.
         * <p>
         * Multiple identifiers can be specified in a single argument joined
         * with dots, or in separate arguments, or both.
         *
         * @param  ids one or more build identifiers, non-null
         * @return this {@code Builder} instance
         * @throws IllegalArgumentException if {@code ids} is null/empty or contains null
         */
        public Builder setBuildMetadata(String... ids) {
            buildIds = oneOrMoreNonNulls(ids, "ids").clone();
            return this;
        }

        /**
         * Appends (additional) build identifier(s).
         * <p>
         * If no build identifiers have been previously set, the method works as
         * {@link #setBuildMetadata(String...)}.
         * <p>
         * Multiple identifiers can be specified in a single argument joined
         * with dots, or in separate arguments, or both.
         *
         * @param  ids one or more build identifiers, non-null
         * @return this {@code Builder} instance
         * @throws IllegalArgumentException if {@code ids} is null/empty or contains null
         * @see    #setBuildMetadata(String...)
         * @since  0.10.0
         */
        public Builder addBuildIdentifiers(String... ids) {
            if (buildIds.length == 0) {
                return setBuildMetadata(ids);
            }

            buildIds = concatArrays(buildIds, oneOrMoreNonNulls(ids, "ids"));
            return this;
        }

        /**
         * Unsets the build metadata.
         *
         * @return this {@code Builder} instance
         * @since  0.10.0
         */
        public Builder unsetBuildMetadata() {
            buildIds = new String[0];
            return this;
        }

        /**
         * Obtains a {@code Version} instance with previously set values.
         *
         * @return a {@code Version} instance
         * @throws ParseException if any of the previously set identifiers can't be parsed
         * @see    Version#of(long, long, long, String, String)
         */
        public Version build() {
            return Version.of(
                major,
                minor,
                patch,
                joinIdentifiers(preReleaseIds),
                joinIdentifiers(buildIds)
            );
        }

        private static String[] concatArrays(String[] ids1, String[] ids2) {
            String[] ids = new String[ids1.length + ids2.length];
            System.arraycopy(ids1, 0, ids, 0, ids1.length);
            System.arraycopy(ids2, 0, ids, ids1.length, ids2.length);
            return ids;
        }

        /**
         * @deprecated forRemoval since 0.10.0
         */
        @Deprecated
        public Builder(String normal) {
            setNormalVersion(normal);
        }

        /**
         * @deprecated forRemoval since 0.10.0
         */
        @Deprecated
        @SuppressWarnings("DeprecatedIsStillUsed")
        public Builder setNormalVersion(String normal) {
            String[] parts = nonNull(normal, "normal").split("\\" + IDENTIFIER_SEPARATOR);
            return setVersionCore(
                Long.parseLong(parts[0]),
                parts.length > 1 ? Long.parseLong(parts[1]) : 0,
                parts.length > 2 ? Long.parseLong(parts[2]) : 0
            );
        }
    }

    /**
     * A comparator that sorts versions in increment order, from lowest to highest.
     * <p>
     * The comparator is intended for use in comparison-based data structures.
     *
     * @see   #compareToIgnoreBuildMetadata(Version)
     * @since 0.10.0
     */
    public static final Comparator<Version> INCREMENT_ORDER = Version::compareToIgnoreBuildMetadata;

    /**
     * A comparator that sorts versions in (highest) precedence order.
     * <p>
     * The ordering imposed by this comparator is reverse of the "natural"
     * increment ordering, that is, versions are arranged in descending order
     * from highest-precedence to lowest-precedence.
     * <p>
     * The comparator is intended for use in comparison-based data structures.
     *
     * @see   #INCREMENT_ORDER
     * @since 0.10.0
     */
    public static final Comparator<Version> PRECEDENCE_ORDER = INCREMENT_ORDER.reversed();

    private final long major;

    private final long minor;

    private final long patch;

    private final String[] preReleaseIds;

    private final String[] buildIds;

    private static final String IDENTIFIER_SEPARATOR = ".";

    private static final String PRE_RELEASE_PREFIX = "-";

    private static final String BUILD_PREFIX = "+";

    /**
     * @see #Version(long, long, long, String[], String[]) for documentation
     */
    Version(long major, long minor, long patch) {
        this(major, minor, patch, new String[0], new String[0]);
    }

    /**
     * @see #Version(long, long, long, String[], String[]) for documentation
     */
    Version(long major, long minor, long patch, String[] preReleaseIds) {
        this(major, minor, patch, preReleaseIds, new String[0]);
    }

    /**
     * Package-private constructor, for internal use only.
     *
     * @param  major a major version number, non-negative
     * @param  minor a minor version number, non-negative
     * @param  patch a patch version number, non-negative
     * @param  preReleaseIds the pre-release identifiers, non-null
     * @param  buildIds the build identifiers, non-null
     * @throws IllegalArgumentException if any of the numeric arguments is negative,
     *         or if any of the reference-type arguments is null
     */
    Version(long major, long minor, long patch, String[] preReleaseIds, String[] buildIds) {
        this.major = nonNegative(major, "major");
        this.minor = nonNegative(minor, "minor");
        this.patch = nonNegative(patch, "patch");
        this.preReleaseIds = nonNull(preReleaseIds, "preReleaseIds").clone();
        this.buildIds = nonNull(buildIds, "buildIds").clone();
    }

    /**
     * Obtains a {@code Version} instance by parsing the specified string.
     *
     * @param  version a string representing a SemVer version, non-null
     * @return a {@code Version} instance
     * @throws IllegalArgumentException if {@code version} is null
     * @throws ParseException if {@code version} can't be parsed
     * @since  0.10.0
     */
    public static Version parse(String version) {
        return VersionParser.parseValidSemVer(nonNull(version, "version"));
    }

    /**
     * Tries to obtain a {@code Version} instance by parsing the specified string.
     *
     * @param  version a string representing a SemVer version, nullable
     * @return an {@code Optional} with a {@code Version} instance, if the
     *         specified string can be parsed; empty {@code Optional} otherwise
     * @since  0.10.0
     */
    public static Optional<Version> tryParse(String version) {
        try {
            return Optional.of(Version.parse(version));
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

    /**
     * Checks validity of the specified SemVer version string.
     * <p>
     * Note that internally this method makes use of {@link #parse(String)} and
     * suppresses any exceptions, so using it to avoid dealing with exceptions
     * like so:
     *
     * <pre>{@code
     *   String version = "1.2.3";
     *   if (Version.isValid(version)) {
     *     Version v = Version.parse(version);
     *   }
     * }</pre>
     *
     * would mean parsing the same version string twice. In this case, as an
     * alternative, consider using {@link #tryParse(String)}.
     *
     * @param  version a string representing a SemVer version, nullable
     * @return {@code true}, if the specified string is a valid SemVer version;
     *         {@code false} otherwise
     * @since  0.10.0
     */
    public static boolean isValid(String version) {
        return tryParse(version).isPresent();
    }

    /**
     * Obtains a {@code Version} instance of the specified major version.
     *
     * @param  major a major version number, non-negative
     * @return a {@code Version} instance
     * @throws IllegalArgumentException if {@code major} is negative
     * @since  0.10.0
     */
    public static Version of(long major) {
        return Version.of(major, 0, 0, null, null);
    }

    /**
     * Obtains a {@code Version} instance of the specified major and pre-release
     * versions.
     *
     * @param  major a major version number, non-negative
     * @param  preRelease a pre-release version label, nullable
     * @return a {@code Version} instance
     * @throws IllegalArgumentException if {@code major} is negative
     * @throws ParseException if {@code preRelease} can't be parsed
     * @since  0.10.0
     */
    public static Version of(long major, String preRelease) {
        return Version.of(major, 0, 0, preRelease, null);
    }

    /**
     * Obtains a {@code Version} instance of the specified major and pre-release
     * versions, as well as build metadata.
     *
     * @param  major a major version number, non-negative
     * @param  preRelease a pre-release version label, nullable
     * @param  build a build metadata label, nullable
     * @return a {@code Version} instance
     * @throws IllegalArgumentException if {@code major} is negative
     * @throws ParseException if {@code preRelease} or {@code build} can't be parsed
     * @since  0.10.0
     */
    public static Version of(long major, String preRelease, String build) {
        return Version.of(major, 0, 0, preRelease, build);
    }

    /**
     * Obtains a {@code Version} instance of the specified major and minor versions.
     *
     * @param  major a major version number, non-negative
     * @param  minor a minor version number, non-negative
     * @return a {@code Version} instance
     * @throws IllegalArgumentException if {@code major} or {@code minor} is negative
     * @since  0.10.0
     */
    public static Version of(long major, long minor) {
        return Version.of(major, minor, 0, null, null);
    }

    /**
     * Obtains a {@code Version} instance of the specified major, minor and
     * pre-release versions.
     *
     * @param  major a major version number, non-negative
     * @param  minor a minor version number, non-negative
     * @param  preRelease a pre-release version label, nullable
     * @return a {@code Version} instance
     * @throws IllegalArgumentException if {@code major} or {@code minor} is negative
     * @throws ParseException if {@code preRelease} can't be parsed
     * @since  0.10.0
     */
    public static Version of(long major, long minor, String preRelease) {
        return Version.of(major, minor, 0, preRelease, null);
    }

    /**
     * Obtains a {@code Version} instance of the specified major, minor and
     * pre-release versions, as well as build metadata.
     *
     * @param  major a major version number, non-negative
     * @param  minor a minor version number, non-negative
     * @param  preRelease a pre-release version label, nullable
     * @param  build a build metadata label, nullable
     * @return a {@code Version} instance
     * @throws IllegalArgumentException if {@code major} or {@code minor} is negative
     * @throws ParseException if {@code preRelease} or {@code build} can't be parsed
     * @since  0.10.0
     */
    public static Version of(long major, long minor, String preRelease, String build) {
        return Version.of(major, minor, 0, preRelease, build);
    }

    /**
     * Obtains a {@code Version} instance of the specified major, minor and
     * patch versions.
     *
     * @param  major a major version number, non-negative
     * @param  minor a minor version number, non-negative
     * @param  patch a patch version number, non-negative
     * @return a {@code Version} instance
     * @throws IllegalArgumentException if any of the arguments is negative
     * @since  0.10.0
     */
    public static Version of(long major, long minor, long patch) {
        return Version.of(major, minor, patch, null, null);
    }

    /**
     * Obtains a {@code Version} instance of the specified major, minor, patch
     * and pre-release versions.
     *
     * @param  major a major version number, non-negative
     * @param  minor a minor version number, non-negative
     * @param  patch a patch version number, non-negative
     * @param  preRelease a pre-release version label, nullable
     * @return a {@code Version} instance
     * @throws IllegalArgumentException if any of the numeric arguments is negative
     * @throws ParseException if {@code preRelease} can't be parsed
     * @since  0.10.0
     */
    public static Version of(long major, long minor, long patch, String preRelease) {
        return Version.of(major, minor, patch, preRelease, null);
    }

    /**
     * Obtains a {@code Version} instance of the specified major, minor, patch
     * and pre-release versions, as well as build metadata.
     *
     * @param  major a major version number, non-negative
     * @param  minor a minor version number, non-negative
     * @param  patch a patch version number, non-negative
     * @param  preRelease a pre-release version label, nullable
     * @param  build a build metadata label, nullable
     * @return a {@code Version} instance
     * @throws IllegalArgumentException if any of the numeric arguments is negative
     * @throws ParseException if {@code preRelease} or {@code build} can't be parsed
     * @since  0.10.0
     */
    public static Version of(long major, long minor, long patch, String preRelease, String build) {
        return new Version(
            major,
            minor,
            patch,
            preRelease == null ? new String[0] : parsePreRelease(preRelease),
            build == null ? new String[0] : parseBuild(build)
        );
    }

    /**
     * Returns this {@code Version}'s major version.
     *
     * @return the major version number
     * @since  0.10.0
     */
    public long majorVersion() {
        return major;
    }

    /**
     * Returns this {@code Version}'s minor version.
     *
     * @return the minor version number
     * @since  0.10.0
     */
    public long minorVersion() {
        return minor;
    }

    /**
     * Returns this {@code Version}'s patch version.
     *
     * @return the patch version number
     * @since  0.10.0
     */
    public long patchVersion() {
        return patch;
    }

    /**
     * Returns this {@code Version}'s pre-release version in the form of
     * dot-separated identifiers.
     *
     * @return the pre-release version label, if present
     * @since  0.10.0
     */
    public Optional<String> preReleaseVersion() {
        return Optional.ofNullable(joinIdentifiers(preReleaseIds));
    }

    /**
     * Returns this {@code Version}'s build metadata in the form of
     * dot-separated identifiers.
     *
     * @return the build metadata label, if present
     * @since  0.10.0
     */
    public Optional<String> buildMetadata() {
        return Optional.ofNullable(joinIdentifiers(buildIds));
    }

    /**
     * Obtains the next {@code Version} by incrementing the major version number
     * by one, with an optional pre-release version label.
     * <p>
     * Multiple identifiers can be specified in a single argument joined with
     * dots, or in separate arguments, or both.
     * <p>
     * This method drops the build metadata, if present.
     *
     * @param  preReleaseIds zero or more pre-release identifiers, non-null
     * @return a {@code Version} instance
     * @throws ArithmeticException if the major version number overflows
     * @throws IllegalArgumentException if {@code preReleaseIds} is null or contains null
     * @throws ParseException if any of the specified identifiers can't be parsed
     * @since  0.10.0
     */
    public Version nextMajorVersion(String... preReleaseIds) {
        return nextMajorVersion(safeIncrement(major), preReleaseIds);
    }

    /**
     * Obtains the next {@code Version} of the specified major version number,
     * with an optional pre-release version label.
     * <p>
     * The specified major version number must be higher than this {@code Version}'s
     * major version.
     * <p>
     * Multiple identifiers can be specified in a single argument joined with
     * dots, or in separate arguments, or both.
     * <p>
     * This method drops the build metadata, if present.
     *
     * @param  major the next major version number, non-negative
     * @param  preReleaseIds zero or more pre-release identifiers, non-null
     * @return a {@code Version} instance
     * @throws IllegalArgumentException if {@code major} is negative, or if
     *         {@code preReleaseIds} is null or contains null
     * @throws IllegalStateException if {@code major} is lower than or equivalent
     *         to this {@code Version}'s major version
     * @throws ParseException if any of the specified identifiers can't be parsed
     * @since  0.10.0
     */
    public Version nextMajorVersion(long major, String... preReleaseIds) {
        if (this.major >= nonNegative(major, "major")) {
            throw new IllegalStateException("This major version is higher or equivalent");
        }

        String preRelease = joinIdentifiers(zeroOrMoreNonNulls(preReleaseIds, "preReleaseIds"));
        return Version.of(major, 0, 0, preRelease);
    }

    /**
     * Obtains the next {@code Version} by incrementing the minor version number
     * by one, with an optional pre-release version label.
     * <p>
     * Multiple identifiers can be specified in a single argument joined with
     * dots, or in separate arguments, or both.
     * <p>
     * This method drops the build metadata, if present.
     *
     * @param  preReleaseIds zero or more pre-release identifiers, non-null
     * @return a {@code Version} instance
     * @throws ArithmeticException if the minor version number overflows
     * @throws IllegalArgumentException if {@code preReleaseIds} is null or contains null
     * @throws ParseException if any of the specified identifiers can't be parsed
     * @since  0.10.0
     */
    public Version nextMinorVersion(String... preReleaseIds) {
        return nextMinorVersion(safeIncrement(minor), preReleaseIds);
    }

    /**
     * Obtains the next {@code Version} of the specified minor version number,
     * with an optional pre-release version label.
     * <p>
     * The specified minor version number must be higher than this {@code Version}'s
     * minor version.
     * <p>
     * Multiple identifiers can be specified in a single argument joined with
     * dots, or in separate arguments, or both.
     * <p>
     * This method drops the build metadata, if present.
     *
     * @param  minor the next minor version number, non-negative
     * @param  preReleaseIds zero or more pre-release identifiers, non-null
     * @return a {@code Version} instance
     * @throws IllegalArgumentException if {@code minor} is negative, or if
     *         {@code preReleaseIds} is null or contains null
     * @throws IllegalStateException if {@code minor} is lower than or equivalent
     *         to this {@code Version}'s minor version
     * @throws ParseException if any of the specified identifiers can't be parsed
     * @since  0.10.0
     */
    public Version nextMinorVersion(long minor, String... preReleaseIds) {
        if (this.minor >= nonNegative(minor, "minor")) {
            throw new IllegalStateException("This minor version is higher or equivalent");
        }

        String preRelease = joinIdentifiers(zeroOrMoreNonNulls(preReleaseIds, "preReleaseIds"));
        return Version.of(major, minor, 0, preRelease);
    }

    /**
     * Obtains the next {@code Version} by incrementing the patch version number
     * by one, with an optional pre-release version label.
     * <p>
     * Multiple identifiers can be specified in a single argument joined with
     * dots, or in separate arguments, or both.
     * <p>
     * This method drops the build metadata, if present.
     *
     * @param  preReleaseIds zero or more pre-release identifiers, non-null
     * @return a {@code Version} instance
     * @throws ArithmeticException if the patch version number overflows
     * @throws IllegalArgumentException if {@code preReleaseIds} is null or contains null
     * @throws ParseException if any of the specified identifiers can't be parsed
     * @since  0.10.0
     */
    public Version nextPatchVersion(String... preReleaseIds) {
        return nextPatchVersion(safeIncrement(patch), preReleaseIds);
    }

    /**
     * Obtains the next {@code Version} of the specified patch version number,
     * with an optional pre-release version label.
     * <p>
     * The specified patch version number must be higher than this {@code Version}'s
     * patch version.
     * <p>
     * Multiple identifiers can be specified in a single argument joined with
     * dots, or in separate arguments, or both.
     * <p>
     * This method drops the build metadata, if present.
     *
     * @param  patch the next patch version number, non-negative
     * @param  preReleaseIds zero or more pre-release identifiers, non-null
     * @return a {@code Version} instance
     * @throws IllegalArgumentException if {@code patch} is negative, or if
     *         {@code preReleaseIds} is null or contains null
     * @throws IllegalStateException if {@code patch} is lower than or equivalent
     *         to this {@code Version}'s patch version
     * @throws ParseException if any of the specified identifiers can't be parsed
     * @since  0.10.0
     */
    public Version nextPatchVersion(long patch, String... preReleaseIds) {
        if (this.patch >= nonNegative(patch, "patch")) {
            throw new IllegalStateException("This patch version is higher or equivalent");
        }

        String preRelease = joinIdentifiers(zeroOrMoreNonNulls(preReleaseIds, "preReleaseIds"));
        return Version.of(major, minor, patch, preRelease);
    }

    /**
     * Obtains the next {@code Version} by incrementing or replacing the
     * pre-release version.
     * <p>
     * If no pre-release identifiers are specified, the current pre-release
     * version's last numeric identifier is incremented. If the current
     * pre-release version's last identifier is not numeric, a new numeric
     * identifier of value "0" is appended for this operation. If specified,
     * however, the pre-release identifiers replace the current pre-release
     * version. The new pre-release version must be higher than this
     * {@code Version}'s pre-release version.
     * <p>
     * Multiple identifiers can be specified in a single argument joined with
     * dots, or in separate arguments, or both.
     * <p>
     * This method drops the build metadata, if present.
     *
     * @param  ids zero or more pre-release identifiers, non-null
     * @return a {@code Version} instance
     * @throws ArithmeticException if the incremented numeric identifier overflows
     * @throws IllegalArgumentException if {@code ids} is null or contains null
     * @throws IllegalStateException if invoked on a stable {@code Version}, or
     *         if the specified pre-release version is lower than or equivalent
     *         to this {@code Version}'s pre-release version
     * @throws ParseException if any of the specified identifiers can't be parsed
     * @since  0.10.0
     */
    public Version nextPreReleaseVersion(String... ids) {
        if (!isPreRelease()) {
            throw new IllegalStateException("Not a pre-release version");
        }

        zeroOrMoreNonNulls(ids, "ids");

        String[] newPreReleaseIds;
        if (ids.length > 0) {
            newPreReleaseIds = parsePreRelease(joinIdentifiers(ids));
            if (compareIdentifierArrays(preReleaseIds, newPreReleaseIds) >= 0) {
                throw new IllegalStateException("This pre-release version is higher or equivalent");
            }
        } else {
            newPreReleaseIds = incrementIdentifiers(preReleaseIds);
        }

        return new Version(major, minor, patch, newPreReleaseIds);
    }

    /**
     * Obtains the next {@code Version} by dropping the pre-release version.
     * <p>
     * This method drops the build metadata, if present.
     *
     * @return a {@code Version} instance
     * @since  0.10.0
     */
    public Version toStableVersion() {
        return isStable() ? this : new Version(major, minor, patch);
    }

    /**
     * Obtains a new {@code Version} with the specified build identifiers.
     * <p>
     * Multiple identifiers can be specified in a single argument joined with
     * dots, or in separate arguments, or both.
     *
     * @param  ids one or more build identifiers, non-null
     * @return a {@code Version} instance
     * @throws IllegalArgumentException if {@code ids} is null/empty or contains null
     * @throws ParseException if any of the specified identifiers can't be parsed
     * @since  0.10.0
     */
    public Version withBuildMetadata(String... ids) {
        String[] newBuildIds = parseBuild(joinIdentifiers(oneOrMoreNonNulls(ids, "ids")));
        return new Version(major, minor, patch, preReleaseIds, newBuildIds);
    }

    /**
     * Obtains a (new) {@code Version} without build metadata.
     *
     * @return a {@code Version} instance
     * @since  0.10.0
     */
    public Version withoutBuildMetadata() {
        return !buildMetadata().isPresent() ? this : new Version(major, minor, patch, preReleaseIds);
    }

    /**
     * Checks if this version satisfies the specified SemVer Expression string.
     * <p>
     * This method is a part of the SemVer Expressions API.
     *
     * @param expr the SemVer Expression string
     * @return {@code true} if this version satisfies the specified
     *         SemVer Expression or {@code false} otherwise
     * @throws ParseException in case of a general parse error
     * @throws LexerException when encounters an illegal character
     * @throws UnexpectedTokenException when comes across an unexpected token
     * @since 0.7.0
     */
    public boolean satisfies(String expr) {
        Parser<Expression> parser = ExpressionParser.newInstance();
        return satisfies(parser.parse(expr));
    }

    /**
     * Checks if this version satisfies the specified SemVer Expression.
     * <p>
     * This method is a part of the SemVer Expressions API.
     *
     * @param expr the SemVer Expression
     * @return {@code true} if this version satisfies the specified
     *         SemVer Expression or {@code false} otherwise
     * @since 0.9.0
     */
    public boolean satisfies(Expression expr) {
        return expr.interpret(this);
    }

    /**
     * Checks if this {@code Version} represents a pre-release version.
     * <p>
     * This method is opposite of {@link #isStable()}.
     *
     * @return {@code true}, if this {@code Version} represents a pre-release
     *         version; {@code false} otherwise
     * @see    #isStable()
     * @since  0.10.0
     */
    public boolean isPreRelease() {
        return preReleaseVersion().isPresent();
    }

    /**
     * Checks if this {@code Version} represents a stable version.
     * <p>
     * Pre-release versions are considered unstable. (SemVer p.9)
     *
     * @return {@code true}, if this {@code Version} represents a stable
     *         version; {@code false} otherwise
     * @see    #isPreRelease()
     * @since  0.10.0
     */
    public boolean isStable() {
        return !isPreRelease();
    }

    /**
     * Checks if this {@code Version} represents a stable public API.
     * <p>
     * Versions lower than 1.0.0 are for initial development, therefore the
     * public API should not be considered stable. (SemVer p.4)
     *
     * @return {@code true}, if this {@code Version} represents a stable public
     *         API; {@code false} otherwise
     * @since  0.10.0
     */
    public boolean isPublicApiStable() {
        return isHigherThanOrEquivalentTo(Version.of(1));
    }

    /**
     * Checks if two versions are compatible in terms of their public API.
     * <p>
     * Two versions are compatible in terms of public API iff they have the
     * same major version of 1 or higher. Being public API compatible doesn't
     * necessarily mean both versions have the same set of public API units.
     * It only means that the versions are interchangeable.
     *
     * @param  other the {@code Version} to compare with, non-null
     * @return {@code true}, if the versions are compatible in terms of public API;
     *         {@code false} otherwise
     * @throws IllegalArgumentException if {@code other} is null
     * @since  0.10.0
     */
    public boolean isPublicApiCompatibleWith(Version other) {
        return isPublicApiStable() && isSameMajorVersionAs(other);
    }

    /**
     * Checks if two {@code Version} instances are similar in terms of their
     * major versions.
     *
     * @param  other the {@code Version} to compare with, non-null
     * @return {@code true}, if the versions are similar in terms of major
     *         versions; {@code false} otherwise
     * @throws IllegalArgumentException if {@code other} is null
     * @since  0.10.0
     */
    public boolean isSameMajorVersionAs(Version other) {
        nonNull(other, "other");
        return major == other.major;
    }

    /**
     * Checks if two {@code Version} instances are similar in terms of their
     * major and minor versions.
     *
     * @param  other the {@code Version} to compare with, non-null
     * @return {@code true}, if the versions are similar in terms of major and
     *         minor versions; {@code false} otherwise
     * @throws IllegalArgumentException if {@code other} is null
     * @since  0.10.0
     */
    public boolean isSameMinorVersionAs(Version other) {
        nonNull(other, "other");
        return major == other.major && minor == other.minor;
    }

    /**
     * Checks if two {@code Version} instances are similar in terms of their
     * major, minor and patch versions.
     *
     * @param  other the {@code Version} to compare with, non-null
     * @return {@code true}, if the versions are similar in terms of major,
     *         minor and patch versions; {@code false} otherwise
     * @throws IllegalArgumentException if {@code other} is null
     * @since  0.10.0
     */
    public boolean isSamePatchVersionAs(Version other) {
        nonNull(other, "other");
        return major == other.major && minor == other.minor && patch == other.patch;
    }

    /**
     * Determines if this {@code Version} has a higher precedence compared with
     * the specified {@code Version}.
     *
     * @param  other the {@code Version} to compare with, non-null
     * @return {@code true}, if this {@code Version} is higher than the other
     *         {@code Version}; {@code false} otherwise
     * @throws IllegalArgumentException if {@code other} is null
     * @see    #compareToIgnoreBuildMetadata(Version)
     * @since  0.10.0
     */
    public boolean isHigherThan(Version other) {
        return compareToIgnoreBuildMetadata(other) > 0;
    }

    /**
     * Determines if this {@code Version} has a higher or equal precedence
     * compared with the specified {@code Version}.
     *
     * @param  other the {@code Version} to compare with, non-null
     * @return {@code true}, if this {@code Version} is higher than or equivalent
     *         to the other {@code Version}; {@code false} otherwise
     * @throws IllegalArgumentException if {@code other} is null
     * @see    #compareToIgnoreBuildMetadata(Version)
     * @since  0.10.0
     */
    public boolean isHigherThanOrEquivalentTo(Version other) {
        return compareToIgnoreBuildMetadata(other) >= 0;
    }

    /**
     * Determines if this {@code Version} has a lower precedence compared with
     * the specified {@code Version}.
     *
     * @param  other the {@code Version} to compare with, non-null
     * @return {@code true}, if this {@code Version} is lower than the other
     *         {@code Version}; {@code false} otherwise
     * @throws IllegalArgumentException if {@code other} is null
     * @see    #compareToIgnoreBuildMetadata(Version)
     * @since  0.10.0
     */
    public boolean isLowerThan(Version other) {
        return compareToIgnoreBuildMetadata(other) < 0;
    }

    /**
     * Determines if this {@code Version} has a lower or equal precedence
     * compared with the specified {@code Version}.
     *
     * @param  other the {@code Version} to compare with, non-null
     * @return {@code true}, if this {@code Version} is lower than or equivalent
     *         to the other {@code Version}; {@code false} otherwise
     * @throws IllegalArgumentException if {@code other} is null
     * @see    #compareToIgnoreBuildMetadata(Version)
     * @since  0.10.0
     */
    public boolean isLowerThanOrEquivalentTo(Version other) {
        return compareToIgnoreBuildMetadata(other) <= 0;
    }

    /**
     * Determines if this {@code Version} has the same precedence as the
     * specified {@code Version}.
     * <p>
     * As per SemVer p.10, build metadata is ignored when determining version
     * precedence. To test for exact equality, including build metadata, use
     * {@link #equals(Object)}.
     *
     * @param  other the {@code Version} to compare with, non-null
     * @return {@code true}, if this {@code Version} is equivalent to the other
     *         {@code Version}; {@code false} otherwise
     * @throws IllegalArgumentException if {@code other} is null
     * @see    #compareToIgnoreBuildMetadata(Version)
     * @since  0.10.0
     */
    public boolean isEquivalentTo(Version other) {
        return compareToIgnoreBuildMetadata(other) == 0;
    }

    /**
     * Compares versions, along with their build metadata.
     * <p>
     * Note that this method violates the SemVer p.10 ("build metadata must be
     * ignored") rule, hence can't be used for determining version precedence.
     * It was made so intentionally for it to be consistent with {@code equals}
     * as defined by {@link Comparable}, and to be used in comparison-based data
     * structures.
     * <p>
     * As the Specification defines no comparison rules for build metadata, this
     * behavior is strictly implementation-defined. Build metadata are compared
     * similarly to pre-release versions. A version with build metadata is
     * ordered after an equivalent one without it.
     * <p>
     * To compare Versions without their build metadata in order to determine
     * precedence use {@link #compareToIgnoreBuildMetadata(Version)}.
     *
     * @param  other the {@code Version} to compare with, non-null
     * @return a negative integer, zero or a positive integer if this
     *         {@code Version} is less than, equal to or greater than the
     *         specified {@code Version}
     * @throws IllegalArgumentException if {@code other} is null
     */
    @Override
    public int compareTo(Version other) {
        int result = compareToIgnoreBuildMetadata(other);
        if (result != 0) {
            return result;
        }

        result = compareIdentifierArrays(this.buildIds, other.buildIds);
        if (this.buildIds.length == 0 || other.buildIds.length == 0) {
            result = -1 * result;
        }
        return result;
    }

    /**
     * Compares versions, ignoring their build metadata.
     * <p>
     * This method adheres to the comparison rules defined by the Specification,
     * and as such can be used for determining version precedence, either as a
     * natural-order comparator ({@code Version::compareToIgnoreBuildMetadata}),
     * or as a regular method.
     *
     * @param  other the {@code Version} to compare with, non-null
     * @return a negative integer, zero or a positive integer if this
     *         {@code Version} is lower than, equivalent to or higher than the
     *         specified {@code Version}
     * @throws IllegalArgumentException if {@code other} is null
     * @since  0.10.0
     */
    public int compareToIgnoreBuildMetadata(Version other) {
        nonNull(other, "other");
        long result = major - other.major;
        if (result == 0) {
            result = minor - other.minor;
            if (result == 0) {
                result = patch - other.patch;
                if (result == 0) {
                    return compareIdentifierArrays(this.preReleaseIds, other.preReleaseIds);
                }
            }
        }
        return result < 0 ? -1 : 1;
    }

    /**
     * Checks if this {@code Version} exactly equals the specified {@code Version}.
     * <p>
     * Although primarily intended for use in hash-based data structures, it
     * can be used for testing for exact equality, including build metadata, if
     * needed. To test for equivalence use {@link #isEquivalentTo(Version)}.
     *
     * @param  other the {@code Version} to compare with, nullable
     * @return {@code true}, if this {@code Version} exactly equals the other
     *         {@code Version}; {@code false} otherwise
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Version)) {
            return false;
        }
        return compareTo((Version) other) == 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + Long.hashCode(major);
        hash = 97 * hash + Long.hashCode(minor);
        hash = 97 * hash + Long.hashCode(patch);
        hash = 97 * hash + Arrays.hashCode(preReleaseIds);
        hash = 97 * hash + Arrays.hashCode(buildIds);
        return hash;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(major);
        sb.append(IDENTIFIER_SEPARATOR);
        sb.append(minor);
        sb.append(IDENTIFIER_SEPARATOR);
        sb.append(patch);
        preReleaseVersion().ifPresent(r -> sb.append(PRE_RELEASE_PREFIX).append(r));
        buildMetadata().ifPresent(b -> sb.append(BUILD_PREFIX).append(b));
        return sb.toString();
    }

    /**
     * Converts this {@code Version} to {@code Builder}.
     * <p>
     * This method allows to use an instance of {@code Version} as a template
     * for new instances.
     *
     * @return a {@code Builder} instance populated with values from
     *         this {@code Version}
     * @since  0.10.0
     */
    public Builder toBuilder() {
        return new Builder()
            .setVersionCore(major, minor, patch)
            .setPreReleaseVersion(preReleaseIds)
            .setBuildMetadata(buildIds)
        ;
    }

    private static long safeIncrement(long l) {
        return Math.incrementExact(l);
    }

    private static String joinIdentifiers(String... ids) {
        return ids.length == 0 ? null : String.join(IDENTIFIER_SEPARATOR, ids);
    }

    private static String[] incrementIdentifiers(String[] ids) {
        String[] newIds;

        String lastId = ids[ids.length - 1];
        if (isNumeric(lastId)) {
            newIds = Arrays.copyOf(ids, ids.length);
            newIds[newIds.length - 1] = String.valueOf(safeIncrement(Long.parseLong(lastId)));
        } else {
            newIds = Arrays.copyOf(ids, ids.length + 1);
            newIds[newIds.length - 1] = String.valueOf(1);
        }

        return newIds;
    }

    private static int compareIdentifierArrays(String[] thisIds, String[] otherIds) {
        if (thisIds.length == 0 && otherIds.length == 0) {
            return 0;
        }

        if (thisIds.length == 0 || otherIds.length == 0) {
            // Pre-release versions have a lower precedence than
            // the associated normal version. (SemVer p.9)
            return thisIds.length == 0 ? 1 : -1;
        }

        int result = 0;
        int minLength = Math.min(thisIds.length, otherIds.length);
        for (int i = 0; i < minLength; i++) {
            result = compareIdentifiers(thisIds[i], otherIds[i]);
            if (result != 0) {
                break;
            }
        }

        if (result == 0) {
            // A larger set of pre-release fields has a higher
            // precedence than a smaller set, if all of the
            // preceding identifiers are equal. (SemVer p.11)
            result = thisIds.length - otherIds.length;
        }
        return result;
    }

    private static int compareIdentifiers(String thisId, String otherId) {
        if (isNumeric(thisId) && isNumeric(otherId)) {
            return Long.valueOf(thisId).compareTo(Long.valueOf(otherId));
        } else {
            return thisId.compareTo(otherId);
        }
    }

    private static boolean isNumeric(String id) {
        // filters out <digits>
        if (id.startsWith("0")) {
            return false;
        }
        return id.chars().allMatch(Character::isDigit);
    }

    static class Validators {

        static long nonNegative(long arg, String name) {
            if (arg < 0) {
                throw new IllegalArgumentException(name + " must not be negative");
            }
            return arg;
        }

        static <T> T nonNull(T arg, String name) {
            return nonNullOrThrow(arg, name + " must not be null");
        }

        static <T> T[] nonEmpty(T[] arg, String name) {
            if (nonNull(arg, name).length == 0) {
                throw new IllegalArgumentException(name + " must not be empty");
            }
            return arg;
        }

        static <T> T[] zeroOrMoreNonNulls(T[] arg, String name) {
            for (T t : nonNull(arg, name)) {
                nonNullOrThrow(t, name + " must not contain null");
            }
            return arg;
        }

        static <T> T[] oneOrMoreNonNulls(T[] arg, String name) {
            for (T t : nonEmpty(arg, name)) {
                nonNullOrThrow(t, name + " must not contain null");
            }
            return arg;
        }

        private static <T> T nonNullOrThrow(T arg, String msg) {
            if (arg == null) {
                throw new IllegalArgumentException(msg);
            }
            return arg;
        }
    }

    private static class SerializationProxy implements Serializable {

        private static final long serialVersionUID = 0L;

        /**
         * @serial string representation of valid SemVer version, the most
         * stable logical form of the {@code Version} class, which doesn't
         * depend on its internal implementation. Only Specification can
         * affect it by redefining its semantics and hence changing the way
         * it's parsed. The only downside of this form is that it requires
         * parsing on deserialization, which shouldn't be that big of a
         * problem considering the size of a typical version string.
         */
        private final String version;

        SerializationProxy(Version version) {
            this.version = version.toString();
        }

        private Object readResolve() {
            return Version.parse(version);
        }
    }

    private Object writeReplace() {
        return new SerializationProxy(this);
    }

    private void readObject(ObjectInputStream ois) throws InvalidObjectException {
        throw new InvalidObjectException("Proxy required");
    }

    /**
     * @deprecated forRemoval since 0.10.0, use {@link #compareTo(Version)}
     */
    @Deprecated
    public static final Comparator<Version> BUILD_AWARE_ORDER = Version::compareTo;

    /**
     * @deprecated forRemoval since 0.10.0, use {@link #parse(String)}
     */
    @Deprecated
    public static Version valueOf(String version) {
        return Version.parse(version);
    }

    /**
     * @deprecated forRemoval since 0.10.0, use {@link #of(long)}
     */
    @Deprecated
    public static Version forIntegers(int major) {
        return Version.of(major);
    }

    /**
     * @deprecated forRemoval since 0.10.0, use {@link #of(long, long)}
     */
    @Deprecated
    public static Version forIntegers(int major, int minor) {
        return Version.of(major, minor);
    }

    /**
     * @deprecated forRemoval since 0.10.0, use {@link #of(long, long, long)}
     */
    @Deprecated
    public static Version forIntegers(int major, int minor, int patch) {
        return Version.of(major, minor, patch);
    }

    /**
     * @deprecated forRemoval since 0.10.0
     */
    @Deprecated
    public String getNormalVersion() {
        return String.format(Locale.ROOT, "%d.%d.%d", major, minor, patch);
    }

    /**
     * @deprecated forRemoval since 0.10.0, use {@link #majorVersion()}
     */
    @Deprecated
    public long getMajorVersion() {
        return majorVersion();
    }

    /**
     * @deprecated forRemoval since 0.10.0, use {@link #minorVersion()}
     */
    @Deprecated
    public long getMinorVersion() {
        return minorVersion();
    }

    /**
     * @deprecated forRemoval since 0.10.0, use {@link #patchVersion()}
     */
    @Deprecated
    public long getPatchVersion() {
        return patchVersion();
    }

    /**
     * @deprecated forRemoval since 0.10.0, use {@link #preReleaseVersion()}
     */
    @Deprecated
    public String getPreReleaseVersion() {
        return preReleaseVersion().orElse("");
    }

    /**
     * @deprecated forRemoval since 0.10.0, use {@link #buildMetadata()}
     */
    @Deprecated
    public String getBuildMetadata() {
        return buildMetadata().orElse("");
    }

    /**
     * @deprecated forRemoval since 0.10.0, consider using {@link #nextPreReleaseVersion(String...)}
     */
    @Deprecated
    @SuppressWarnings("DeprecatedIsStillUsed")
    public Version setPreReleaseVersion(String preRelease) {
        return new Version(major, minor, patch, parsePreRelease(preRelease));
    }

    /**
     * @deprecated forRemoval since 0.10.0, use {@link #withBuildMetadata(String...)}
     */
    @Deprecated
    public Version setBuildMetadata(String build) {
        return withBuildMetadata(build);
    }

    /**
     * @deprecated forRemoval since 0.10.0, use {@link #nextMajorVersion(String...)}
     */
    @Deprecated
    public Version incrementMajorVersion() {
        return nextMajorVersion();
    }

    /**
     * @deprecated forRemoval since 0.10.0, use {@link #nextMajorVersion(String...)}
     */
    @Deprecated
    public Version incrementMajorVersion(String preRelease) {
        return nextMajorVersion(preRelease);
    }

    /**
     * @deprecated forRemoval since 0.10.0, use {@link #nextMinorVersion(String...)}
     */
    @Deprecated
    public Version incrementMinorVersion() {
        return nextMinorVersion();
    }

    /**
     * @deprecated forRemoval since 0.10.0, use {@link #nextMinorVersion(String...)}
     */
    @Deprecated
    public Version incrementMinorVersion(String preRelease) {
        return nextMinorVersion(preRelease);
    }

    /**
     * @deprecated forRemoval since 0.10.0, use {@link #nextPatchVersion(String...)}
     */
    @Deprecated
    public Version incrementPatchVersion() {
        return nextPatchVersion();
    }

    /**
     * @deprecated forRemoval since 0.10.0, use {@link #nextPatchVersion(String...)}
     */
    @Deprecated
    public Version incrementPatchVersion(String preRelease) {
        return nextPatchVersion(preRelease);
    }

    /**
     * @deprecated forRemoval since 0.10.0, use {@link #nextPreReleaseVersion(String...)}
     */
    @Deprecated
    public Version incrementPreReleaseVersion() {
        return nextPreReleaseVersion();
    }

    /**
     * @deprecated forRemoval since 0.10.0
     */
    @Deprecated
    @SuppressWarnings("DeprecatedIsStillUsed")
    public Version incrementBuildMetadata() {
        if (!buildMetadata().isPresent()) {
            throw new IllegalStateException("Build metadata empty");
        }
        return new Version(major, minor, patch, preReleaseIds, incrementIdentifiers(buildIds));
    }

    /**
     * @deprecated forRemoval since 0.10.0, use {@link #isHigherThan(Version)}
     */
    @Deprecated
    public boolean greaterThan(Version other) {
        return isHigherThan(other);
    }

    /**
     * @deprecated forRemoval since 0.10.0, use {@link #isHigherThanOrEquivalentTo(Version)}
     */
    @Deprecated
    public boolean greaterThanOrEqualTo(Version other) {
        return isHigherThanOrEquivalentTo(other);
    }

    /**
     * @deprecated forRemoval since 0.10.0, use {@link #isLowerThan(Version)}
     */
    @Deprecated
    public boolean lessThan(Version other) {
        return isLowerThan(other);
    }

    /**
     * @deprecated forRemoval since 0.10.0, use {@link #isLowerThanOrEquivalentTo(Version)}
     */
    @Deprecated
    public boolean lessThanOrEqualTo(Version other) {
        return isLowerThanOrEquivalentTo(other);
    }

    /**
     * @deprecated forRemoval since 0.10.0, use {@link #compareTo(Version)}
     */
    @Deprecated
    public int compareWithBuildsTo(Version other) {
        return compareTo(other);
    }
}
