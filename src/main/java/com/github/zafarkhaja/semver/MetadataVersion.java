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

import java.util.Arrays;

/**
 *
 * @author Zafar Khaja <zafarkhaja@gmail.com>
 */
class MetadataVersion implements Comparable<MetadataVersion> {

    static final MetadataVersion NULL = new NullMetadataVersion();

    private static class NullMetadataVersion extends MetadataVersion {

        public NullMetadataVersion() {
            super(null);
        }

        @Override
        MetadataVersion increment() {
            throw new NullPointerException("Metadata version is NULL");
        }

        @Override
        public String toString() {
            return "";
        }

        @Override
        public int hashCode() {
            return 0;
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof NullMetadataVersion;
        }

        @Override
        public int compareTo(MetadataVersion other) {
            if (!equals(other)) {
                /**
                 * Pre-release versions have a lower precedence than
                 * the associated normal version. (SemVer p.9)
                 */
                return 1;
            }
            return 0;
        }
    }

    private final String[] idents;

    MetadataVersion(String[] identifiers) {
        idents = identifiers;
    }

    MetadataVersion increment() {
        String[] ids  = idents;
        String lastId = ids[ids.length - 1];
        if (isInt(lastId)) {
            int intId = Integer.parseInt(lastId);
            ids[ids.length - 1] = String.valueOf(++intId);
        } else {
            ids = Arrays.copyOf(ids, ids.length + 1);
            ids[ids.length - 1] = String.valueOf(1);
        }
        return new MetadataVersion(ids);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof MetadataVersion)) {
            return false;
        }
        return compareTo((MetadataVersion) other) == 0;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(idents);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (String ident : idents) {
            sb.append(ident).append(".");
        }
        return sb.deleteCharAt(sb.lastIndexOf(".")).toString();
    }

    @Override
    public int compareTo(MetadataVersion other) {
        if (other == MetadataVersion.NULL) {
            /**
             * Pre-release versions have a lower precedence than
             * the associated normal version. (SemVer p.9)
             */
            return -1;
        }
        int result = compareIdentifierArrays(other.idents);
        if (result == 0) {
            result = idents.length - other.idents.length;
        }
        return result;
    }

    private int compareIdentifierArrays(String[] otherIdents) {
        int result = 0;
        int length = getLeastCommonArrayLength(idents, otherIdents);
        for (int i = 0; i < length; i++) {
            result = compareIdentifiers(idents[i], otherIdents[i]);
            if (result != 0) {
                break;
            }
        }
        return result;
    }

    private int getLeastCommonArrayLength(String[] arr1, String[] arr2) {
        return arr1.length <= arr2.length ? arr1.length : arr2.length;
    }

    private int compareIdentifiers(String ident1, String ident2) {
        if (isInt(ident1) && isInt(ident2)) {
            return Integer.parseInt(ident1) - Integer.parseInt(ident2);
        } else {
            return ident1.compareTo(ident2);
        }
    }

    private boolean isInt(String str) {
        try {
            Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }
}
