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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Zafar Khaja <zafarkhaja@gmail.com>
 */
public class Version implements Comparable<Version> {

    private final NormalVersion normal;
    private final AlphaNumericVersion preRelease;
    private final AlphaNumericVersion build;

    private static final String PRE_RELEASE_PREFIX = "-";
    private static final String BUILD_PREFIX = "+";

    private static final Pattern SEMVER_PATTERN;

    static {
        StringBuilder sb = new StringBuilder();
        sb.append("^")
            .append(NormalVersion.FORMAT)
            .append("(?:")
                .append(PRE_RELEASE_PREFIX)
                .append(AlphaNumericVersion.FORMAT)
            .append(")?").append("(?:")
                .append("\\").append(BUILD_PREFIX)
                .append(AlphaNumericVersion.FORMAT)
            .append(")?")
        .append("$");
        
        SEMVER_PATTERN = Pattern.compile(sb.toString());
    }

    Version(
        NormalVersion normal,
        AlphaNumericVersion preRelease,
        AlphaNumericVersion build
    ) {
        this.normal     = normal;
        this.preRelease = preRelease;
        this.build      = build;
    }

    public static Version valueOf(String value) {
        Matcher matcher = SEMVER_PATTERN.matcher(value);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Illegal version format");
        }

        NormalVersion normal = new NormalVersion(
            Integer.parseInt(matcher.group(1)),
            Integer.parseInt(matcher.group(2)),
            Integer.parseInt(matcher.group(3))
        );

        AlphaNumericVersion preRelease =
            (matcher.group(4) != null) ?
                new AlphaNumericVersion(matcher.group(4)) :
                    null;

        AlphaNumericVersion build =
            (matcher.group(5) != null) ?
                new AlphaNumericVersion(matcher.group(5)) :
                    null;

        return new Version(normal, preRelease, build);
    }

    public Version incrementMajorVersion() {
        return new Version(normal.incrementMajor(), preRelease, build);
    }

    public Version incrementMinorVersion() {
        return new Version(normal.incrementMinor(), preRelease, build);
    }

    public Version incrementPatchVersion() {
        return new Version(normal.incrementPatch(), preRelease, build);
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
        return (preRelease != null) ? preRelease.toString() : "";
    }

    public String getBuildVersion() {
        return (build != null) ? build.toString() : "";
    }

    public boolean greaterThan(Version other) {
        return compareTo(other) > 0 ? true : false;
    }

    public boolean greaterThanOrEqualsTo(Version other) {
        return compareTo(other) >= 0 ? true : false;
    }

    public boolean lessThan(Version other) {
        return compareTo(other) < 0 ? true : false;
    }

    public boolean lessThanOrEqualsTo(Version other) {
        return compareTo(other) <= 0 ? true : false;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Version)) {
            return false;
        }
        return compareTo((Version) other) == 0 ? true : false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + (normal != null ? normal.hashCode() : 0);
        hash = 97 * hash + (preRelease != null ? preRelease.hashCode() : 0);
        hash = 97 * hash + (build != null ? build.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getNormalVersion());
        if (preRelease != null) {
            sb.append(PRE_RELEASE_PREFIX).append(getPreReleaseVersion());
        }
        if (build != null) {
            sb.append(BUILD_PREFIX).append(getBuildVersion());
        }
        return sb.toString();
    }

    @Override
    public int compareTo(Version other) {
        int result = normal.compareTo(other.normal);
        if (result == 0) {
            result = comparePreReleases(other);
            if (result == 0) {
                result = compareBuilds(other);
            }
        }
        return result;
    }

    private int comparePreReleases(Version other) {
        int result = 0;
        if (preRelease != null && other.preRelease != null) {
            result = preRelease.compareTo(other.preRelease);
        } else if (preRelease == null ^ other.preRelease == null) {
            /**
             * Pre-release versions satisfy but have a lower precedence
             * than the associated normal version. (SemVer p.9)
             */
            result = (preRelease == null) ? 1 : -1;
        }
        return result;
    }

    private int compareBuilds(Version other) {
        int result = 0;
        if (build != null && other.build != null) {
            result = build.compareTo(other.build);
        } else if (build == null ^ other.build == null) {
            /**
             * Build versions satisfy and have a higher precedence
             * than the associated normal version. (SemVer p.10)
             */
            result = (build == null) ? -1 : 1;
        }
        return result;
    }
}
