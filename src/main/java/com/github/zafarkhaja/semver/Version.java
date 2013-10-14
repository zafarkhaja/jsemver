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

        public Builder(String normalVersion) {
            if (normalVersion == null) {
                throw new NullPointerException(
                    "Normal version MUST NOT be NULL"
                );
            }
            normal = normalVersion;
        }

        public void setPreReleaseVersion(String preReleaseVersion) {
            preRelease = preReleaseVersion;
        }

        public void setBuildMetadata(String buildMetadata) {
            build = buildMetadata;
        }

        public Version build() {
            MetadataVersion preReleaseVersion = null;
            if (preRelease != null) {
                preReleaseVersion = VersionParser.parsePreRelease(preRelease);
            }
            MetadataVersion buildMetadata = null;
            if (build != null) {
                buildMetadata = VersionParser.parseBuild(build);
            }
            return new Version(
                VersionParser.parseVersionCore(normal),
                preReleaseVersion,
                buildMetadata
            );
        }
    }

    public static final Comparator BUILD_AWARE_ORDER = new BuildAwareOrder();

    private static class BuildAwareOrder implements Comparator<Version> {

        @Override
        public int compare(Version v1, Version v2) {
            int result = v1.compareTo(v2);
            if (result == 0) {
                result = compareBuilds(v1, v2);
            }
            return result;
        }

        private int compareBuilds(Version v1, Version v2) {
            int result = 0;
            if (v1.build != null && v2.build != null) {
                result = v1.build.compareTo(v2.build);
            } else if (v1.build == null ^ v2.build == null) {
                /**
                 * Build versions should have a higher precedence
                 * than the associated normal version.
                 */
                result = (v1.build == null) ? -1 : 1;
            }
            return result;
        }
    }

    Version(NormalVersion normal) {
        this(normal, null, null);
    }

    Version(NormalVersion normal, MetadataVersion preRelease) {
        this(normal, preRelease, null);
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
        if (preRelease == null) {
            throw new NullPointerException("Pre-release version is NULL");
        }
        return new Version(normal, preRelease.increment());
    }

    public Version incrementBuildMetadata() {
        if (build == null) {
            throw new NullPointerException("Build metadata is NULL");
        }
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
        return (preRelease != null) ? preRelease.toString() : "";
    }

    public String getBuildMetadata() {
        return (build != null) ? build.toString() : "";
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
            sb.append(BUILD_PREFIX).append(getBuildMetadata());
        }
        return sb.toString();
    }

    @Override
    public int compareTo(Version other) {
        int result = normal.compareTo(other.normal);
        if (result == 0) {
            result = comparePreReleases(other);
        }
        return result;
    }

    public int compareWithBuildsTo(Version other) {
        return BUILD_AWARE_ORDER.compare(this, other);
    }

    private int comparePreReleases(Version other) {
        int result = 0;
        if (preRelease != null && other.preRelease != null) {
            result = preRelease.compareTo(other.preRelease);
        } else if (preRelease == null ^ other.preRelease == null) {
            /**
             * Pre-release versions have a lower precedence than
             * the associated normal version. (SemVer p.9)
             */
            result = (preRelease == null) ? 1 : -1;
        }
        return result;
    }
}
