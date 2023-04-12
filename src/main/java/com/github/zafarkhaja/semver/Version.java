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
import java.util.Comparator;
import java.util.Optional;

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
     * The normal version.
     */
    private final NormalVersion normal;

    /**
     * The pre-release version.
     */
    private final MetadataVersion preRelease;

    /**
     * The build metadata.
     */
    private final MetadataVersion build;

    /**
     * A separator that separates the pre-release
     * version from the normal version.
     */
    private static final String PRE_RELEASE_PREFIX = "-";

    /**
     * A separator that separates the build metadata from
     * the normal version or the pre-release version.
     */
    private static final String BUILD_PREFIX = "+";

    /**
     * A mutable builder for the immutable {@code Version} class.
     */
    public static class Builder {

        /**
         * The normal version string.
         */
        private String normal;

        /**
         * The pre-release version string.
         */
        private String preRelease;

        /**
         * The build metadata string.
         */
        private String build;

        /**
         * Constructs a {@code Builder} instance.
         */
        public Builder() {

        }

        /**
         * Constructs a {@code Builder} instance with the
         * string representation of the normal version.
         *
         * @param normal the string representation of the normal version
         */
        public Builder(String normal) {
            this.normal = normal;
        }

        /**
         * Sets the normal version.
         *
         * @param normal the string representation of the normal version
         * @return this builder instance
         */
        public Builder setNormalVersion(String normal) {
            this.normal = normal;
            return this;
        }

        /**
         * Sets the pre-release version.
         *
         * @param preRelease the string representation of the pre-release version
         * @return this builder instance
         */
        public Builder setPreReleaseVersion(String preRelease) {
            this.preRelease = preRelease;
            return this;
        }

        /**
         * Sets the build metadata.
         *
         * @param build the string representation of the build metadata
         * @return this builder instance
         */
        public Builder setBuildMetadata(String build) {
            this.build = build;
            return this;
        }

        /**
         * Builds a {@code Version} object.
         *
         * @return a newly built {@code Version} instance
         * @throws ParseException when invalid version string is provided
         * @throws UnexpectedCharacterException is a special case of {@code ParseException}
         */
        public Version build() {
            StringBuilder sb = new StringBuilder();
            if (isFilled(normal)) {
                sb.append(normal);
            }
            if (isFilled(preRelease)) {
                sb.append(PRE_RELEASE_PREFIX).append(preRelease);
            }
            if (isFilled(build)) {
                sb.append(BUILD_PREFIX).append(build);
            }
            return VersionParser.parseValidSemVer(sb.toString());
        }

        /**
         * Checks if a string has a usable value.
         *
         * @param str the string to check
         * @return {@code true} if the string is filled or {@code false} otherwise
         */
        private boolean isFilled(String str) {
            return str != null && !str.isEmpty();
        }
    }

    /**
     * A comparator that respects the build metadata when comparing versions.
     */
    public static final Comparator<Version> BUILD_AWARE_ORDER = new BuildAwareOrder();

    /**
     * A build-aware comparator.
     */
    private static class BuildAwareOrder implements Comparator<Version> {

        /**
         * Compares two {@code Version} instances taking
         * into account their build metadata.
         * <p>
         * When compared build metadata is divided into identifiers. The
         * numeric identifiers are compared numerically, and the alphanumeric
         * identifiers are compared in the ASCII sort order.
         * <p>
         * If one of the compared versions has no defined build
         * metadata, this version is considered to have a lower
         * precedence than that of the other.
         *
         * @return {@inheritDoc}
         */
        @Override
        public int compare(Version v1, Version v2) {
            int result = v1.compareTo(v2);
            if (result == 0) {
                result = v1.build.compareTo(v2.build);
                if (v1.build == MetadataVersion.NULL ||
                    v2.build == MetadataVersion.NULL
                ) {
                    /*
                     * Build metadata should have a higher precedence
                     * than the associated normal version which is the
                     * opposite compared to pre-release versions.
                     */
                    result = -1 * result;
                }
            }
            return result;
        }
    }

    /**
     * @see #Version(NormalVersion, MetadataVersion, MetadataVersion) for documentation
     */
    Version(NormalVersion normal) {
        this(normal, MetadataVersion.NULL, MetadataVersion.NULL);
    }

    /**
     * @see #Version(NormalVersion, MetadataVersion, MetadataVersion) for documentation
     */
    Version(NormalVersion normal, MetadataVersion preRelease) {
        this(normal, preRelease, MetadataVersion.NULL);
    }

    /**
     * Package-private constructor, for internal use only.
     *
     * @param normal the normal version
     * @param preRelease the pre-release version
     * @param build the build metadata
     */
    Version(NormalVersion normal, MetadataVersion preRelease, MetadataVersion build) {
        this.normal     = normal;
        this.preRelease = preRelease;
        this.build      = build;
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
        return VersionParser.parseValidSemVer(requireNonNull(version, "version"));
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
            new NormalVersion(major, minor, patch),
            preRelease == null ? MetadataVersion.NULL : VersionParser.parsePreRelease(preRelease),
            build == null ? MetadataVersion.NULL : VersionParser.parseBuild(build)
        );
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
     * Increments the major version.
     *
     * @return a new instance of the {@code Version} class
     * @throws ArithmeticException if the major version number overflows
     */
    public Version incrementMajorVersion() {
        return new Version(normal.incrementMajor());
    }

    /**
     * Increments the major version and appends the pre-release version.
     *
     * @param preRelease the pre-release version to append
     * @return a new instance of the {@code Version} class
     * @throws ArithmeticException if the major version number overflows
     * @throws IllegalArgumentException if the input string is {@code NULL} or empty
     * @throws ParseException when invalid version string is provided
     * @throws UnexpectedCharacterException is a special case of {@code ParseException}
     */
    public Version incrementMajorVersion(String preRelease) {
        return new Version(
            normal.incrementMajor(),
            VersionParser.parsePreRelease(preRelease)
        );
    }

    /**
     * Increments the minor version.
     *
     * @return a new instance of the {@code Version} class
     * @throws ArithmeticException if the minor version number overflows
     */
    public Version incrementMinorVersion() {
        return new Version(normal.incrementMinor());
    }

    /**
     * Increments the minor version and appends the pre-release version.
     *
     * @param preRelease the pre-release version to append
     * @return a new instance of the {@code Version} class
     * @throws ArithmeticException if the minor version number overflows
     * @throws IllegalArgumentException if the input string is {@code NULL} or empty
     * @throws ParseException when invalid version string is provided
     * @throws UnexpectedCharacterException is a special case of {@code ParseException}
     */
    public Version incrementMinorVersion(String preRelease) {
        return new Version(
            normal.incrementMinor(),
            VersionParser.parsePreRelease(preRelease)
        );
    }

    /**
     * Increments the patch version.
     *
     * @return a new instance of the {@code Version} class
     * @throws ArithmeticException if the patch version number overflows
     */
    public Version incrementPatchVersion() {
        return new Version(normal.incrementPatch());
    }

    /**
     * Increments the patch version and appends the pre-release version.
     *
     * @param preRelease the pre-release version to append
     * @return a new instance of the {@code Version} class
     * @throws ArithmeticException if the patch version number overflows
     * @throws IllegalArgumentException if the input string is {@code NULL} or empty
     * @throws ParseException when invalid version string is provided
     * @throws UnexpectedCharacterException is a special case of {@code ParseException}
     */
    public Version incrementPatchVersion(String preRelease) {
        return new Version(
            normal.incrementPatch(),
            VersionParser.parsePreRelease(preRelease)
        );
    }

    /**
     * Increments the pre-release version.
     *
     * @return a new instance of the {@code Version} class
     * @throws ArithmeticException if the numeric identifier overflows
     */
    public Version incrementPreReleaseVersion() {
        return new Version(normal, preRelease.increment());
    }

    /**
     * Increments the build metadata.
     *
     * @return a new instance of the {@code Version} class
     * @throws ArithmeticException if the numeric identifier overflows
     */
    public Version incrementBuildMetadata() {
        return new Version(normal, preRelease, build.increment());
    }

    /**
     * Sets the pre-release version.
     *
     * @param preRelease the pre-release version to set
     * @return a new instance of the {@code Version} class
     * @throws IllegalArgumentException if the input string is {@code NULL} or empty
     * @throws ParseException when invalid version string is provided
     * @throws UnexpectedCharacterException is a special case of {@code ParseException}
     */
    public Version setPreReleaseVersion(String preRelease) {
        return new Version(normal, VersionParser.parsePreRelease(preRelease));
    }

    /**
     * Sets the build metadata.
     *
     * @param build the build metadata to set
     * @return a new instance of the {@code Version} class
     * @throws IllegalArgumentException if the input string is {@code NULL} or empty
     * @throws ParseException when invalid version string is provided
     * @throws UnexpectedCharacterException is a special case of {@code ParseException}
     */
    public Version setBuildMetadata(String build) {
        return new Version(normal, preRelease, VersionParser.parseBuild(build));
    }

    /**
     * Returns the major version number.
     *
     * @return the major version number
     */
    public long getMajorVersion() {
        return normal.getMajor();
    }

    /**
     * Returns the minor version number.
     *
     * @return the minor version number
     */
    public long getMinorVersion() {
        return normal.getMinor();
    }

    /**
     * Returns the patch version number.
     *
     * @return the patch version number
     */
    public long getPatchVersion() {
        return normal.getPatch();
    }

    /**
     * Returns the string representation of the normal version.
     *
     * @return the string representation of the normal version
     */
    public String getNormalVersion() {
        return normal.toString();
    }

    /**
     * Returns the string representation of the pre-release version.
     *
     * @return the string representation of the pre-release version
     */
    public String getPreReleaseVersion() {
        return preRelease.toString();
    }

    /**
     * Returns the string representation of the build metadata.
     *
     * @return the string representation of the build metadata
     */
    public String getBuildMetadata() {
        return build.toString();
    }

    /**
     * Checks if this version is greater than the other version.
     *
     * @param other the other version to compare to
     * @return {@code true} if this version is greater than the other version
     *         or {@code false} otherwise
     * @see #compareTo(Version other)
     */
    public boolean greaterThan(Version other) {
        return compareTo(other) > 0;
    }

    /**
     * Checks if this version is greater than or equal to the other version.
     *
     * @param other the other version to compare to
     * @return {@code true} if this version is greater than or equal
     *         to the other version or {@code false} otherwise
     * @see #compareTo(Version other)
     */
    public boolean greaterThanOrEqualTo(Version other) {
        return compareTo(other) >= 0;
    }

    /**
     * Checks if this version is less than the other version.
     *
     * @param other the other version to compare to
     * @return {@code true} if this version is less than the other version
     *         or {@code false} otherwise
     * @see #compareTo(Version other)
     */
    public boolean lessThan(Version other) {
        return compareTo(other) < 0;
    }

    /**
     * Checks if this version is less than or equal to the other version.
     *
     * @param other the other version to compare to
     * @return {@code true} if this version is less than or equal
     *         to the other version or {@code false} otherwise
     * @see #compareTo(Version other)
     */
    public boolean lessThanOrEqualTo(Version other) {
        return compareTo(other) <= 0;
    }

    /**
     * Checks if this version equals the other version.
     * <p>
     * The comparison is done by the {@code Version.compareTo} method.
     *
     * @param other the other version to compare to
     * @return {@code true} if this version equals the other version
     *         or {@code false} otherwise
     * @see #compareTo(Version other)
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
        hash = 97 * hash + normal.hashCode();
        hash = 97 * hash + preRelease.hashCode();
        return hash;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getNormalVersion());
        if (!getPreReleaseVersion().isEmpty()) {
            sb.append(PRE_RELEASE_PREFIX).append(getPreReleaseVersion());
        }
        if (!getBuildMetadata().isEmpty()) {
            sb.append(BUILD_PREFIX).append(getBuildMetadata());
        }
        return sb.toString();
    }

    /**
     * Compares this version to the other version.
     * <p>
     * This method does not take into account the versions' build
     * metadata. If you want to compare the versions' build metadata
     * use the {@code Version.compareWithBuildsTo} method or the
     * {@code Version.BUILD_AWARE_ORDER} comparator.
     *
     * @param other the other version to compare to
     * @return a negative integer, zero or a positive integer if this version
     *         is less than, equal to or greater than the specified version
     * @see #BUILD_AWARE_ORDER
     * @see #compareWithBuildsTo(Version other)
     */
    @Override
    public int compareTo(Version other) {
        int result = normal.compareTo(other.normal);
        if (result == 0) {
            result = preRelease.compareTo(other.preRelease);
        }
        return result;
    }

    /**
     * Compare this version to the other version
     * taking into account the build metadata.
     * <p>
     * The method makes use of the {@code Version.BUILD_AWARE_ORDER} comparator.
     *
     * @param other the other version to compare to
     * @return integer result of comparison compatible with
     *         that of the {@code Comparable.compareTo} method
     * @see #BUILD_AWARE_ORDER
     */
    public int compareWithBuildsTo(Version other) {
        return BUILD_AWARE_ORDER.compare(this, other);
    }

    private static <T> T requireNonNull(T arg, String name) {
        if (arg == null) {
            throw new IllegalArgumentException(name + " must not be null");
        }
        return arg;
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
     * @deprecated forRemoval since 0.10.0, use {@link #parse(String)}
     *
     * @param  version a string representing a SemVer version, non-null
     * @return a {@code Version} instance
     * @throws IllegalArgumentException if {@code version} is null
     * @throws ParseException if {@code version} can't be parsed
     */
    @Deprecated
    public static Version valueOf(String version) {
        return Version.parse(version);
    }

    /**
     * @deprecated forRemoval since 0.10.0, use {@link #of(long)}
     *
     * @param  major a major version number, non-negative
     * @return a {@code Version} instance
     * @throws IllegalArgumentException if {@code major} is negative
     */
    @Deprecated
    public static Version forIntegers(int major) {
        return Version.of(major);
    }

    /**
     * @deprecated forRemoval since 0.10.0, use {@link #of(long, long)}
     *
     * @param  major a major version number, non-negative
     * @param  minor a minor version number, non-negative
     * @return a {@code Version} instance
     * @throws IllegalArgumentException if {@code major} or {@code minor} is negative
     */
    @Deprecated
    public static Version forIntegers(int major, int minor) {
        return Version.of(major, minor);
    }

    /**
     * @deprecated forRemoval since 0.10.0, use {@link #of(long, long, long)}
     *
     * @param  major a major version number, non-negative
     * @param  minor a minor version number, non-negative
     * @param  patch a patch version number, non-negative
     * @return a {@code Version} instance
     * @throws IllegalArgumentException if any of the arguments is negative
     */
    @Deprecated
    public static Version forIntegers(int major, int minor, int patch) {
        return Version.of(major, minor, patch);
    }
}
