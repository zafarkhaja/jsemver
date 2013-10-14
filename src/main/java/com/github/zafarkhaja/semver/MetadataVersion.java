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
        for (String id : idents) {
            sb.append(id).append(".");
        }
        return sb.deleteCharAt(sb.lastIndexOf(".")).toString();
    }

    @Override
    public int compareTo(MetadataVersion other) {
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

    private int compareIdentifiers(String id1, String id2) {
        if (isInt(id1) && isInt(id2)) {
            return Integer.parseInt(id1) - Integer.parseInt(id2);
        } else {
            return id1.compareTo(id2);
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
