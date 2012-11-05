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
    
    private int major;
    private int minor;
    private int patch;
    private String preRelease;
    private String build;
    
    private static final String NORMAL_VERSION = 
        "((?<major>\\d+)\\.(?<minor>\\d+)\\.(?<patch>\\d+))";
    
    private static final String PRE_RELEASE_VERSION = 
        "(?:-(?<preRelease>[0-9A-Za-z-]+(?:\\.[0-9A-Za-z-]+)*))?";
    
    private static final String BUILD_VERSION = 
        "(?:\\+(?<build>[0-9A-Za-z-]+(?:\\.[0-9A-Za-z-]+)*))?";
    
    private static final Pattern SEMVER_PATTERN = Pattern.compile(
        "^" + NORMAL_VERSION + PRE_RELEASE_VERSION + BUILD_VERSION + "$"
    );
    
    public Version(String version) {
        Matcher matcher = SEMVER_PATTERN.matcher(version);
        if (!matcher.matches()) {
            throw new IllegalArgumentException(
                "Illegal version format"
            );
        }
        major = Integer.parseInt(matcher.group("major"));
        minor = Integer.parseInt(matcher.group("minor"));
        patch = Integer.parseInt(matcher.group("patch"));
        
        preRelease = matcher.group("preRelease");
        build      = matcher.group("build");
    }
    
    public int getMajor() {
        return major;
    }
    
    public int getMinor() {
        return minor;
    }
    
    public int getPatch() {
        return patch;
    }
    
    public String getPreRelease() {
        return preRelease;
    }
    
    public String getBuild() {
        return build;
    }
    
    public void bumpMajor() {
        major = major + 1;
        minor = 0;
        patch = 0;
    }
    
    public void bumpMinor() {
        minor = minor + 1;
        patch = 0;
    }
    
    public void bumpPatch() {
        patch = patch + 1;
    }
    
    @Override
    public int compareTo(Version other) {
        int result = compareNormalVersions(other);
        if (result == 0 && preRelease != null) {
            result = compareAlphaNumericVersions(
                preRelease, 
                other.getPreRelease()
            );
        }
        if (result == 0 && build != null) {
            result = compareAlphaNumericVersions(
                build, 
                other.getBuild()
            );
        }
        return result;
    }
    
    private int compareNormalVersions(Version other) {
        int result = compareInts(major, other.getMajor());
        if (result == 0) {
            result = compareInts(minor, other.getMinor());
            if (result == 0) {
                result = compareInts(patch, other.getPatch());
            }
        }
        return result;
    }
    
    private int compareInts(int thisOp, int otherOp) {
        return (thisOp == otherOp) ? 0 : ((thisOp > otherOp) ? 1 : -1); 
    }
    
    private int compareAlphaNumericVersions(String thisOp, String otherOp) {
        String[] thisIdents  = thisOp.split("\\.");
        String[] otherIdents = otherOp.split("\\.");
        
        int result = compareIdentifierArrays(thisIdents, otherIdents);
        if (result == 0 && thisIdents.length != otherIdents.length) {
            result = (thisIdents.length > otherIdents.length) ? 1 : -1;
        }
        return result;
    }
    
    private int compareIdentifierArrays(String[] thisArr, String[] otherArr) {
        int result = 0;
        int loopCount = getSmallestArrayLength(thisArr, otherArr);
        for (int i = 0; i < loopCount; i++) {
            result = compareIdentifiers(thisArr[i], otherArr[i]);
            if (result != 0) {
                break;
            }
        }
        return result;
    }
    
    private int getSmallestArrayLength(String[] thisArr, String[] otherArr) {
        if (thisArr.length <= otherArr.length) {
            return thisArr.length;
        } else {
            return otherArr.length;
        }
    }
    
    private int compareIdentifiers(String thisIdent, String otherIdent) {
        if (isInt(thisIdent) && isInt(otherIdent)) {
            return compareInts(
                Integer.parseInt(thisIdent), 
                Integer.parseInt(otherIdent)
            );
        } else if (isInt(thisIdent) || isInt(otherIdent)) {
            return isInt(thisIdent) ? -1 : 1;
        } else {
            return thisIdent.compareTo(otherIdent);
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
