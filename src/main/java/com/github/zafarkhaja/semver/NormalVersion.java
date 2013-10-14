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

/**
 *
 * @author Zafar Khaja <zafarkhaja@gmail.com>
 */
class NormalVersion implements Comparable<NormalVersion> {

    private final int major;
    private final int minor;
    private final int patch;

    NormalVersion(int major, int minor, int patch) {
        if (major < 0 || minor < 0 || patch < 0) {
            throw new IllegalArgumentException(
                "Major, minor and patch versions MUST be non-negative integers."
            );
        }
        this.major = major;
        this.minor = minor;
        this.patch = patch;
    }

    int getMajor() {
        return major;
    }

    int getMinor() {
        return minor;
    }

    int getPatch() {
        return patch;
    }

    NormalVersion incrementMajor() {
        return new NormalVersion(major + 1, 0, 0);
    }

    NormalVersion incrementMinor() {
        return new NormalVersion(major, minor + 1, 0);
    }

    NormalVersion incrementPatch() {
        return new NormalVersion(major, minor, patch + 1);
    }

    @Override
    public int compareTo(NormalVersion other) {
        int result = major - other.major;
        if (result == 0) {
            result = minor - other.minor;
            if (result == 0) {
                result = patch - other.patch;
            }
        }
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof NormalVersion)) {
            return false;
        }
        return compareTo((NormalVersion) other) == 0;
    }

    @Override
    public int hashCode() {
        int hash = 17;
        hash = 31 * hash + major;
        hash = 31 * hash + minor;
        hash = 31 * hash + patch;
        return hash;
    }

    /**
     * Returns the string representation of this normal version.
     *
     * A normal version number MUST take the form X.Y.Z where X, Y, and Z are
     * non-negative integers. X is the major version, Y is the minor version,
     * and Z is the patch version. (SemVer p.2)
     */
    @Override
    public String toString() {
        return String.format("%d.%d.%d", major, minor, patch);
    }
}
