/*
 * The MIT License
 *
 * Copyright 2012 Zafar Khaja <zafarkhaja@gmail.com>.
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
import java.util.Comparator;

/**
 *
 * @author Zafar Khaja <zafarkhaja@gmail.com>
 */
public class Version implements Comparable<Version> {

    private final NormalVersion normal;
    private final MetadataVersion preRelease;
    private final MetadataVersion build;

    private static final String PRE_RELEASE_PREFIX = "-";
    private static final String BUILD_PREFIX = "+";

    public static class Builder {

        private String normal;
        private String preRelease;
        private String build;

        public Builder(String normal) {
            if (normal == null) {
                throw new NullPointerException(
                    "Normal version MUST NOT be NULL"
                );
            }
            this.normal = normal;
        }

        public void setPreReleaseVersion(String preRelease) {
            this.preRelease = preRelease;
        }

        public void setBuildMetadata(String build) {
            this.build = build;
        }

        public Version build() {
            return new Version(
                VersionParser.parseVersionCore(normal),
                VersionParser.parsePreRelease(preRelease),
                VersionParser.parseBuild(build)
            );
        }
    }

    public static final Comparator BUILD_AWARE_ORDER = new BuildAwareOrder();

    private static class BuildAwareOrder implements Comparator<Version> {

        @Override
        public int compare(Version v1, Version v2) {
            int result = v1.compareTo(v2);
            if (result == 0) {
                result = v1.build.compareTo(v2.build);
                if (v1.build == MetadataVersion.NULL ||
                    v2.build == MetadataVersion.NULL
                ) {
                    /**
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

    Version(NormalVersion normal) {
        this(normal, MetadataVersion.NULL, MetadataVersion.NULL);
    }

    Version(NormalVersion normal, MetadataVersion preRelease) {
        this(normal, preRelease, MetadataVersion.NULL);
    }

    Version(
        NormalVersion normal,
        MetadataVersion preRelease,
        MetadataVersion build
    ) {
        this.normal     = normal;
        this.preRelease = preRelease;
        this.build      = build;
    }

    public static Version valueOf(String version) {
        return VersionParser.parseValidSemVer(version);
    }

    public static Version forIntegers(int major) {
        return new Version(new NormalVersion(major, 0, 0));
    }

    public static Version forIntegers(int major, int minor) {
        return new Version(new NormalVersion(major, minor, 0));
    }

    public static Version forIntegers(int major, int minor, int patch) {
        return new Version(new NormalVersion(major, minor, patch));
    }

    public boolean satisfies(String expr) {
        Parser<Expression> parser = ExpressionParser.newInstance();
        return parser.parse(expr).interpret(this);
    }

    public Version incrementMajorVersion() {
        return new Version(normal.incrementMajor());
    }

    public Version incrementMajorVersion(String preRelease) {
        return new Version(
            normal.incrementMajor(),
            VersionParser.parsePreRelease(preRelease)
        );
    }

    public Version incrementMinorVersion() {
        return new Version(normal.incrementMinor());
    }

    public Version incrementMinorVersion(String preRelease) {
        return new Version(
            normal.incrementMinor(),
            VersionParser.parsePreRelease(preRelease)
        );
    }

    public Version incrementPatchVersion() {
        return new Version(normal.incrementPatch());
    }

    public Version incrementPatchVersion(String preRelease) {
        return new Version(
            normal.incrementPatch(),
            VersionParser.parsePreRelease(preRelease)
        );
    }

    public Version incrementPreReleaseVersion() {
        return new Version(normal, preRelease.increment());
    }

    public Version incrementBuildMetadata() {
        return new Version(normal, preRelease, build.increment());
    }

    public Version setPreReleaseVersion(String preRelease) {
        return new Version(
            normal,
            VersionParser.parsePreRelease(preRelease)
        );
    }

    public Version setBuildMetadata(String build) {
        return new Version(
            normal,
            preRelease,
            VersionParser.parseBuild(build)
        );
    }

    public int getMajorVersion() {
        return normal.getMajor();
    }

    public int getMinorVersion() {
        return normal.getMinor();
    }

    public int getPatchVersion() {
        return normal.getPatch();
    }

    public String getNormalVersion() {
        return normal.toString();
    }

    public String getPreReleaseVersion() {
        return preRelease.toString();
    }

    public String getBuildMetadata() {
        return build.toString();
    }

    public boolean greaterThan(Version other) {
        return compareTo(other) > 0;
    }

    public boolean greaterThanOrEqualTo(Version other) {
        return compareTo(other) >= 0;
    }

    public boolean lessThan(Version other) {
        return compareTo(other) < 0;
    }

    public boolean lessThanOrEqualTo(Version other) {
        return compareTo(other) <= 0;
    }

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

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + normal.hashCode();
        hash = 97 * hash + preRelease.hashCode();
        hash = 97 * hash + build.hashCode();
        return hash;
    }

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

    @Override
    public int compareTo(Version other) {
        int result = normal.compareTo(other.normal);
        if (result == 0) {
            result = preRelease.compareTo(other.preRelease);
        }
        return result;
    }

    public int compareWithBuildsTo(Version other) {
        return BUILD_AWARE_ORDER.compare(this, other);
    }
}
