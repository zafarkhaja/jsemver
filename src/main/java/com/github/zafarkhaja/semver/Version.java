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
    
    private int majorVersion;
    private int minorVersion;
    private int patchVersion;
    
    private String preReleaseVersion;
    private String buildVersion;
    
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
        majorVersion = Integer.parseInt(matcher.group("major"));
        minorVersion = Integer.parseInt(matcher.group("minor"));
        patchVersion = Integer.parseInt(matcher.group("patch"));
        
        preReleaseVersion = matcher.group("preRelease");
        buildVersion      = matcher.group("build");
    }
    
    public int getMajorVersion() {
        return majorVersion;
    }
    
    public int getMinorVersion() {
        return minorVersion;
    }
    
    public int getPatchVersion() {
        return patchVersion;
    }
    
    public String getPreReleaseVersion() {
        return preReleaseVersion;
    }
    
    public String getBuildVersion() {
        return buildVersion;
    }
    
    public void bumpMajorVersion() {
        majorVersion = majorVersion + 1;
        minorVersion = 0;
        patchVersion = 0;
    }
    
    public void bumpMinorVersion() {
        minorVersion = minorVersion + 1;
        patchVersion = 0;
    }
    
    public void bumpPatchVersion() {
        patchVersion = patchVersion + 1;
    }
    
    @Override
    public int compareTo(Version other) {
        int result = compareNormalVersions(other);
        if (result == 0 && preReleaseVersion != null) {
            result = compareAlphaNumericVersions(
                preReleaseVersion, 
                other.getPreReleaseVersion()
            );
        }
        if (result == 0 && buildVersion != null) {
            result = compareAlphaNumericVersions(
                buildVersion, 
                other.getBuildVersion()
            );
        }
        return result;
    }
    
    private int compareNormalVersions(Version other) {
        int result = compareInts(majorVersion, other.getMajorVersion());
        if (result == 0) {
            result = compareInts(minorVersion, other.getMinorVersion());
            if (result == 0) {
                result = compareInts(patchVersion, other.getPatchVersion());
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
        for (int i = 0; i < getSmallestArrayLength(thisArr, otherArr); i++) {
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
            /**
             * Numeric identifiers always have lower precedence 
             * than non-numeric identifiers.
             */
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
