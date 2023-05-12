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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Locale;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static com.github.zafarkhaja.semver.expr.CompositeExpression.Helper.gte;
import static com.github.zafarkhaja.semver.expr.CompositeExpression.Helper.lt;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Zafar Khaja {@literal <zafarkhaja@gmail.com>}
 */
class VersionTest {

    @Nested
    class Builder {

        @Test
        void shouldSetVersionCore() {
            Version.Builder b = new Version.Builder();
            b.setVersionCore(1, 2, 3);
            assertEquals(Version.of(1, 2, 3), b.build());
        }

        @Test
        void shouldNotAcceptNegativeNumbersForVersionCore() {
            Version.Builder b = new Version.Builder();
            assertThrowsIllegalArgumentException(() -> b.setVersionCore(-1,  2,  3));
            assertThrowsIllegalArgumentException(() -> b.setVersionCore( 1, -2,  3));
            assertThrowsIllegalArgumentException(() -> b.setVersionCore( 1,  2, -3));
        }

        @Test
        void shouldSetVersionCoreWithDefaultMinorAndPatchValues() {
            Version.Builder b = new Version.Builder();
            b.setVersionCore(1);
            assertEquals(Version.of(1, 0, 0), b.build());
        }

        @Test
        void shouldNotAcceptNegativeNumbersForVersionCoreWithDefaultMinorAndPatchValues() {
            Version.Builder b = new Version.Builder();
            assertThrowsIllegalArgumentException(() -> b.setVersionCore(-1));
        }

        @Test
        void shouldSetVersionCoreWithDefaultPatchValue() {
            Version.Builder b = new Version.Builder();
            b.setVersionCore(1, 2);
            assertEquals(Version.of(1, 2, 0), b.build());
        }

        @Test
        void shouldNotAcceptNegativeNumbersForVersionCoreWithDefaultPatchValue() {
            Version.Builder b = new Version.Builder();
            assertThrowsIllegalArgumentException(() -> b.setVersionCore(-1, 2));
            assertThrowsIllegalArgumentException(() -> b.setVersionCore(1, -2));
        }

        @Test
        void shouldSetMajorVersion() {
            Version.Builder b = new Version.Builder();
            b.setMajorVersion(1);
            Version v = b.build();
            assertEquals(1, v.majorVersion());
        }

        @Test
        void shouldNotAcceptNegativeNumbersForMajorVersion() {
            Version.Builder b = new Version.Builder();
            assertThrowsIllegalArgumentException(() -> b.setMajorVersion(-1));
        }

        @Test
        void shouldSetMinorVersion() {
            Version.Builder b = new Version.Builder();
            b.setMinorVersion(2);
            Version v = b.build();
            assertEquals(2, v.minorVersion());
        }

        @Test
        void shouldNotAcceptNegativeNumbersForMinorVersion() {
            Version.Builder b = new Version.Builder();
            assertThrowsIllegalArgumentException(() -> b.setMinorVersion(-2));
        }

        @Test
        void shouldSetPatchVersion() {
            Version.Builder b = new Version.Builder();
            b.setPatchVersion(3);
            Version v = b.build();
            assertEquals(3, v.patchVersion());
        }

        @Test
        void shouldNotAcceptNegativeNumbersForPatchVersion() {
            Version.Builder b = new Version.Builder();
            assertThrowsIllegalArgumentException(() -> b.setPatchVersion(-3));
        }

        @Test
        void shouldSetPreReleaseVersion() {
            Version.Builder b = new Version.Builder();
            b.setPreReleaseVersion("pre", "release");
            Version v = b.build();
            assertEquals("pre.release", v.preReleaseVersion().get());
        }

        @Test
        void shouldMakeDefensiveCopyOfArgumentsWhenSettingPreReleaseVersion() {
            Version.Builder b = new Version.Builder();
            String[] ids = {"pre.release"};
            b.setPreReleaseVersion(ids);
            ids[0] = null;
            Version v = b.build();
            assertEquals("pre.release", v.preReleaseVersion().get());
        }

        @Test
        void shouldNotAcceptNullsForPreReleaseVersion() {
            Version.Builder b = new Version.Builder();
            assertThrowsIllegalArgumentException(() -> b.setPreReleaseVersion((String[]) null));
            assertThrowsIllegalArgumentException(() -> b.setPreReleaseVersion((String) null));
        }

        @Test
        void shouldNotAcceptEmptyArraysForPreReleaseVersion() {
            Version.Builder b = new Version.Builder();
            assertThrowsIllegalArgumentException(() -> b.setPreReleaseVersion(new String[0]));
            assertThrowsIllegalArgumentException(() -> b.setPreReleaseVersion());
        }

        @Test
        void shouldAddPreReleaseIdentifiers() {
            Version.Builder b = new Version.Builder();
            b.setPreReleaseVersion("pre");
            b.addPreReleaseIdentifiers("release", "1");
            Version v = b.build();
            assertEquals("pre.release.1", v.preReleaseVersion().get());
        }

        @Test
        void shouldMakeDefensiveCopyOfArgumentsWhenAddingPreReleaseIdentifiers() {
            Version.Builder b = new Version.Builder();
            b.setPreReleaseVersion("pre");
            String[] ids = {"release"};
            b.addPreReleaseIdentifiers(ids);
            ids[0] = null;
            Version v = b.build();
            assertEquals("pre.release", v.preReleaseVersion().get());
        }

        @Test
        void shouldNotAcceptNullsWhenAddingPreReleaseIdentifiers() {
            Version.Builder b = new Version.Builder();
            assertThrowsIllegalArgumentException(() -> b.addPreReleaseIdentifiers((String[]) null));
            assertThrowsIllegalArgumentException(() -> b.addPreReleaseIdentifiers((String) null));
        }

        @Test
        void shouldNotAcceptEmptyArraysWhenAddingPreReleaseIdentifiers() {
            Version.Builder b = new Version.Builder();
            assertThrowsIllegalArgumentException(() -> b.addPreReleaseIdentifiers(new String[0]));
            assertThrowsIllegalArgumentException(() -> b.addPreReleaseIdentifiers());
        }

        @Test
        void shouldUnsetPreReleaseVersion() {
            Version.Builder b = new Version.Builder();
            b.setPreReleaseVersion("pre-release");
            b.unsetPreReleaseVersion();
            Version v = b.build();
            assertFalse(v.preReleaseVersion().isPresent());
        }

        @Test
        void shouldSetBuildMetadata() {
            Version.Builder b = new Version.Builder();
            b.setBuildMetadata("build", "metadata");
            Version v = b.build();
            assertEquals("build.metadata", v.buildMetadata().get());
        }

        @Test
        void shouldMakeDefensiveCopyOfArgumentsWhenSettingBuildMetadata() {
            Version.Builder b = new Version.Builder();
            String[] ids = {"build.metadata"};
            b.setBuildMetadata(ids);
            ids[0] = null;
            Version v = b.build();
            assertEquals("build.metadata", v.buildMetadata().get());
        }

        @Test
        void shouldNotAcceptNullsForBuildMetadata() {
            Version.Builder b = new Version.Builder();
            assertThrowsIllegalArgumentException(() -> b.setBuildMetadata((String[]) null));
            assertThrowsIllegalArgumentException(() -> b.setBuildMetadata((String) null));
        }

        @Test
        void shouldNotAcceptEmptyArraysForBuildMetadata() {
            Version.Builder b = new Version.Builder();
            assertThrowsIllegalArgumentException(() -> b.setBuildMetadata(new String[0]));
            assertThrowsIllegalArgumentException(() -> b.setBuildMetadata());
        }

        @Test
        void shouldAddBuildIdentifiers() {
            Version.Builder b = new Version.Builder();
            b.setBuildMetadata("build");
            b.addBuildIdentifiers("metadata", "1");
            Version v = b.build();
            assertEquals("build.metadata.1", v.buildMetadata().get());
        }

        @Test
        void shouldMakeDefensiveCopyOfArgumentsWhenAddingBuildIdentifiers() {
            Version.Builder b = new Version.Builder();
            b.setBuildMetadata("build");
            String[] ids = {"metadata"};
            b.addBuildIdentifiers(ids);
            ids[0] = null;
            Version v = b.build();
            assertEquals("build.metadata", v.buildMetadata().get());
        }

        @Test
        void shouldNotAcceptNullsWhenAddingBuildIdentifiers() {
            Version.Builder b = new Version.Builder();
            assertThrowsIllegalArgumentException(() -> b.addBuildIdentifiers((String[]) null));
            assertThrowsIllegalArgumentException(() -> b.addBuildIdentifiers((String) null));
        }

        @Test
        void shouldNotAcceptEmptyArraysWhenAddingBuildIdentifiers() {
            Version.Builder b = new Version.Builder();
            assertThrowsIllegalArgumentException(() -> b.addBuildIdentifiers(new String[0]));
            assertThrowsIllegalArgumentException(() -> b.addBuildIdentifiers());
        }

        @Test
        void shouldUnsetBuildVersion() {
            Version.Builder b = new Version.Builder();
            b.setBuildMetadata("build.metadata");
            b.unsetBuildMetadata();
            Version v = b.build();
            assertFalse(v.buildMetadata().isPresent());
        }

        @Test
        @SuppressWarnings("deprecation")
        void shouldSetNormalVersion() {
            Version.Builder b = new Version.Builder();
            b.setNormalVersion("1.2.3");
            assertEquals(Version.of(1, 2, 3), b.build());
        }

        @Test
        @SuppressWarnings("deprecation")
        void shouldNotAcceptNullsWhenSettingNormalVersion() {
            Version.Builder b = new Version.Builder();
            assertThrowsIllegalArgumentException(() -> b.setNormalVersion(null));
        }
    }

    @Nested
    class CoreFunctionality {

        @Test
        void shouldNormallyTakeTheFormXDotYDotZWhereXYZAreNonNegativeIntegers() {
            Version v = Version.parse("1.2.3");
            assertEquals(1, v.majorVersion());
            assertEquals(2, v.minorVersion());
            assertEquals(3, v.patchVersion());
        }

        @Test
        void mayHavePreReleaseVersionFollowingPatchVersionPrependedWithHyphen() {
            Version v = Version.parse("1.2.3-pre-release");
            assertEquals("pre-release", v.preReleaseVersion().get());
        }

        @Test
        void mayHaveBuildMetadataFollowingPatchOrPreReleaseVersionPrependedWithPlus() {
            Version v = Version.parse("1.2.3+build.metadata");
            assertEquals("build.metadata", v.buildMetadata().get());
        }

        @Test
        void shouldParseFullSemVerCompliantVersionStrings() {
            Version v = Version.parse("1.2.3-pre-release+build.metadata");
            assertEquals(Version.of(1, 2, 3, "pre-release", "build.metadata"), v);
        }

        @Test
        void shouldCheckInputStringForNullBeforeParsing() {
            assertThrows(IllegalArgumentException.class, () -> Version.parse(null));
        }

        @Test
        void shouldTryToParseVersionStringsIfValid() {
            assertTrue(Version.tryParse("1.2.3-rc+abcdefg").isPresent());
            assertFalse(Version.tryParse("1.2.3+rc+abcdefg").isPresent());
        }

        @Test
        void shouldCheckValidityOfVersionStrings() {
            assertTrue(Version.isValid("1.2.3-pre-release+build.metadata"));
            assertFalse(Version.isValid("1.2.3-pre+release+build.metadata"));
        }

        @Test
        void shouldHaveStaticFactoryMethod() {
            Version v = Version.of(1, 2, 3, "pre-release", "build.metadata");
            assertEquals("1.2.3-pre-release+build.metadata", v.toString());
        }

        @Test
        void shouldNotAcceptNegativeNumbersInStaticFactoryMethod() {
            assertThrowsIllegalArgumentException(() -> Version.of(-1, 0, 0));
            assertThrowsIllegalArgumentException(() -> Version.of(0, -1, 0));
            assertThrowsIllegalArgumentException(() -> Version.of(0, 0, -1));
        }

        @ParameterizedTest
        @ValueSource(strings = {"rc.01", "rc+1", "rc..1", "rc.1+"})
        void shouldNotAcceptInvalidPreReleaseVersionInStaticFactoryMethod(String s) {
            assertThrows(ParseException.class, () -> Version.of(1, s));
        }

        @ParameterizedTest
        @ValueSource(strings = {"build+1", "build..1", "build.1+"})
        void shouldNotAcceptInvalidBuildMetadataInStaticFactoryMethod(String s) {
            assertThrows(ParseException.class, () -> Version.of(1, null, s));
        }

        @Test
        void shouldHaveGetters() {
            Version v = Version.of(1, 2, 3, "pre-release", "build.metadata");
            assertEquals(1, v.majorVersion());
            assertEquals(2, v.minorVersion());
            assertEquals(3, v.patchVersion());
            assertEquals("pre-release", v.preReleaseVersion().get());
            assertEquals("build.metadata", v.buildMetadata().get());
        }

        @Test
        void shouldReturnEmptyOptionalIfPreReleaseVersionIsNotSet() {
            Version v = Version.of(0, 0, 1, null);
            assertFalse(v.preReleaseVersion().isPresent());
        }

        @Test
        void shouldReturnEmptyOptionalIfBuildMetadataIsNotSet() {
            Version v = Version.of(0, 0, 1, null, null);
            assertFalse(v.buildMetadata().isPresent());
        }

        @Test
        void shouldObtainNextMajorVersion() {
            Version v1 = Version.of(1);
            Version v2 = v1.nextMajorVersion(3);
            assertEquals(3, v2.majorVersion());
        }

        @Test
        void shouldNotAcceptNegativeMajorVersion() {
            Version v = Version.of(1);
            assertThrowsIllegalArgumentException(() -> v.nextMajorVersion(-2));
        }

        @Test
        void shouldNotAllowToObtainLowerNextMajorVersion() {
            Version v = Version.of(3);
            assertThrows(IllegalStateException.class, () -> v.nextMajorVersion(2));
        }

        @Test
        void shouldNotAllowToObtainEquivalentNextMajorVersion() {
            Version v = Version.of(3);
            assertThrows(IllegalStateException.class, () -> v.nextMajorVersion(3));
        }

        @Test
        void shouldResetMinorVersionWhenIncreasingMajorVersion() {
            Version v1 = Version.of(3, 2, 1);
            Version v2 = v1.nextMajorVersion(4);
            assertEquals(0, v2.minorVersion());
        }

        @Test
        void shouldResetPatchVersionWhenIncreasingMajorVersion() {
            Version v1 = Version.of(3, 2, 1);
            Version v2 = v1.nextMajorVersion(4);
            assertEquals(0, v2.patchVersion());
        }

        @Test
        void shouldDropPreReleaseVersionWhenIncreasingMajorVersion() {
            Version v1 = Version.of(1, "pre-release");
            Version v2 = v1.nextMajorVersion(2);
            assertFalse(v2.preReleaseVersion().isPresent());
        }

        @Test
        void shouldDropBuildMetadataWhenIncreasingMajorVersion() {
            Version v1 = Version.of(1, null, "build.metadata");
            Version v2 = v1.nextMajorVersion(3);
            assertFalse(v2.buildMetadata().isPresent());
        }

        @Test
        void shouldSpecifyPreReleaseVersionWhenObtainingNextMajorVersion() {
            Version v1 = Version.of(1);
            Version v2 = v1.nextMajorVersion(2, "pre-release");
            assertEquals("pre-release", v2.preReleaseVersion().get());
        }

        @Test
        void shouldAcceptSeparateIdentifiersWhenIncreasingMajorVersion() {
            Version v1 = Version.of(1);
            Version v2 = v1.nextMajorVersion(3, "pre", "release");
            assertEquals("pre.release", v2.preReleaseVersion().get());
        }

        @Test
        void shouldNotAcceptNullPreReleaseVersionForNextMajorVersion() {
            Version v = Version.of(1);
            assertThrowsIllegalArgumentException(() -> v.nextMajorVersion(2, (String[]) null));
        }

        @Test
        void shouldNotAcceptNullIdentifiersForNextMajorVersion() {
            Version v = Version.of(1);
            assertThrowsIllegalArgumentException(() -> v.nextMajorVersion(2, (String) null));
        }

        @ParameterizedTest
        @ValueSource(strings = {"01", "rc.", ".rc", "rc!"})
        void shouldNotAcceptInvalidIdentifiersForNextMajorVersion(String id) {
            Version v = Version.of(1);
            assertThrows(ParseException.class, () -> v.nextMajorVersion(2, id));
        }

        @Test
        void shouldIncrementMajorVersionByOne() {
            Version v1 = Version.of(1);
            Version v2 = v1.nextMajorVersion();
            assertEquals(2, v2.majorVersion());
        }

        @Test
        void shouldNotAllowMajorVersionNumberToOverflow() {
            Version v = Version.of(Long.MAX_VALUE);
            assertThrows(ArithmeticException.class, v::nextMajorVersion);
        }

        @Test
        void shouldObtainNextMinorVersion() {
            Version v1 = Version.of(1, 2);
            Version v2 = v1.nextMinorVersion(4);
            assertEquals(4, v2.minorVersion());
        }

        @Test
        void shouldNotAcceptNegativeMinorVersion() {
            Version v = Version.of(1, 2);
            assertThrowsIllegalArgumentException(() -> v.nextMinorVersion(-3));
        }

        @Test
        void shouldNotAllowToObtainLowerNextMinorVersion() {
            Version v = Version.of(3, 2);
            assertThrows(IllegalStateException.class, () -> v.nextMinorVersion(1));
        }

        @Test
        void shouldNotAllowToObtainEquivalentNextMinorVersion() {
            Version v = Version.of(3, 2);
            assertThrows(IllegalStateException.class, () -> v.nextMinorVersion(2));
        }

        @Test
        void shouldNotChangeMajorVersionWhenIncreasingMinorVersion() {
            Version v1 = Version.of(3, 2, 1);
            Version v2 = v1.nextMinorVersion(3);
            assertEquals(v1.majorVersion(), v2.majorVersion());
        }

        @Test
        void shouldResetPatchVersionWhenIncreasingMinorVersion() {
            Version v1 = Version.of(3, 2, 1);
            Version v2 = v1.nextMinorVersion(3);
            assertEquals(0, v2.patchVersion());
        }

        @Test
        void shouldDropPreReleaseVersionWhenIncreasingMinorVersion() {
            Version v1 = Version.of(1, 2, "pre-release");
            Version v2 = v1.nextMinorVersion(3);
            assertFalse(v2.preReleaseVersion().isPresent());
        }

        @Test
        void shouldDropBuildMetadataWhenIncreasingMinorVersion() {
            Version v1 = Version.of(1, 2, null, "build.metadata");
            Version v2 = v1.nextMinorVersion(4);
            assertFalse(v2.buildMetadata().isPresent());
        }

        @Test
        void shouldSpecifyPreReleaseVersionWhenObtainingNextMinorVersion() {
            Version v1 = Version.of(1, 2);
            Version v2 = v1.nextMinorVersion(3, "pre-release");
            assertEquals("pre-release", v2.preReleaseVersion().get());
        }

        @Test
        void shouldAcceptSeparateIdentifiersWhenIncreasingMinorVersion() {
            Version v1 = Version.of(1, 2);
            Version v2 = v1.nextMinorVersion(3, "pre", "release");
            assertEquals("pre.release", v2.preReleaseVersion().get());
        }

        @Test
        void shouldNotAcceptNullPreReleaseVersionForNextMinorVersion() {
            Version v = Version.of(1, 2);
            assertThrowsIllegalArgumentException(() -> v.nextMinorVersion(3, (String[]) null));
        }

        @Test
        void shouldNotAcceptNullIdentifiersForNextMinorVersion() {
            Version v = Version.of(1, 2);
            assertThrowsIllegalArgumentException(() -> v.nextMinorVersion(3, (String) null));
        }

        @ParameterizedTest
        @ValueSource(strings = {"01", "rc.", ".rc", "rc!"})
        void shouldNotAcceptInvalidIdentifiersForNextMinorVersion(String id) {
            Version v = Version.of(1, 2);
            assertThrows(ParseException.class, () -> v.nextMinorVersion(3, id));
        }

        @Test
        void shouldIncrementMinorVersionByOne() {
            Version v1 = Version.of(1, 2);
            Version v2 = v1.nextMinorVersion();
            assertEquals(3, v2.minorVersion());
        }

        @Test
        void shouldNotAllowMinorVersionNumberToOverflow() {
            Version v = Version.of(0, Long.MAX_VALUE);
            assertThrows(ArithmeticException.class, v::nextMinorVersion);
        }

        @Test
        void shouldObtainNextPatchVersion() {
            Version v1 = Version.of(1, 2, 3);
            Version v2 = v1.nextPatchVersion(5);
            assertEquals(5, v2.patchVersion());
        }

        @Test
        void shouldNotAcceptNegativePatchVersion() {
            Version v = Version.of(1, 2, 3);
            assertThrowsIllegalArgumentException(() -> v.nextPatchVersion(-4));
        }

        @Test
        void shouldNotAllowToObtainLowerNextPatchVersion() {
            Version v = Version.of(3, 2, 1);
            assertThrows(IllegalStateException.class, () -> v.nextPatchVersion(0));
        }

        @Test
        void shouldNotAllowToObtainEquivalentNextPatchVersion() {
            Version v = Version.of(3, 2, 1);
            assertThrows(IllegalStateException.class, () -> v.nextPatchVersion(1));
        }

        @Test
        void shouldNotChangeMajorVersionWhenIncreasingPatchVersion() {
            Version v1 = Version.of(3, 2, 1);
            Version v2 = v1.nextPatchVersion(2);
            assertEquals(v1.majorVersion(), v2.majorVersion());
        }

        @Test
        void shouldNotChangeMinorVersionWhenIncreasingPatchVersion() {
            Version v1 = Version.of(3, 2, 1);
            Version v2 = v1.nextPatchVersion(2);
            assertEquals(v1.minorVersion(), v2.minorVersion());
        }

        @Test
        void shouldDropPreReleaseVersionWhenIncreasingPatchVersion() {
            Version v1 = Version.of(1, 2, 3, "pre-release");
            Version v2 = v1.nextPatchVersion(4);
            assertFalse(v2.preReleaseVersion().isPresent());
        }

        @Test
        void shouldDropBuildMetadataWhenIncreasingPatchVersion() {
            Version v1 = Version.of(1, 2, 3, null, "build.metadata");
            Version v2 = v1.nextPatchVersion(5);
            assertFalse(v2.buildMetadata().isPresent());
        }

        @Test
        void shouldSpecifyPreReleaseVersionWhenObtainingNextPatchVersion() {
            Version v1 = Version.of(1, 2, 3);
            Version v2 = v1.nextPatchVersion(4, "pre-release");
            assertEquals("pre-release", v2.preReleaseVersion().get());
        }

        @Test
        void shouldAcceptSeparateIdentifiersWhenIncreasingPatchVersion() {
            Version v1 = Version.of(1, 2, 3);
            Version v2 = v1.nextPatchVersion(4, "pre", "release");
            assertEquals("pre.release", v2.preReleaseVersion().get());
        }

        @Test
        void shouldNotAcceptNullPreReleaseVersionForNextPatchVersion() {
            Version v = Version.of(1, 2, 3);
            assertThrowsIllegalArgumentException(() -> v.nextPatchVersion(4, (String[]) null));
        }

        @Test
        void shouldNotAcceptNullIdentifiersForNextPatchVersion() {
            Version v = Version.of(1, 2, 3);
            assertThrowsIllegalArgumentException(() -> v.nextPatchVersion(4, (String) null));
        }

        @ParameterizedTest
        @ValueSource(strings = {"01", "rc.", ".rc", "rc!"})
        void shouldNotAcceptInvalidIdentifiersForNextPatchVersion(String id) {
            Version v = Version.of(1, 2, 3);
            assertThrows(ParseException.class, () -> v.nextPatchVersion(4, id));
        }

        @Test
        void shouldIncrementPatchVersionByOne() {
            Version v1 = Version.of(1, 2, 3);
            Version v2 = v1.nextPatchVersion();
            assertEquals(4, v2.patchVersion());
        }

        @Test
        void shouldNotAllowPatchVersionNumberToOverflow() {
            Version v = Version.of(0, 0, Long.MAX_VALUE);
            assertThrows(ArithmeticException.class, v::nextPatchVersion);
        }

        @Test
        void shouldObtainNextPreReleaseVersion() {
            Version v1 = Version.of(1, "alpha");
            Version v2 = v1.nextPreReleaseVersion("beta");
            assertEquals("beta", v2.preReleaseVersion().get());
        }

        @Test
        void shouldNotAllowToObtainNextPreReleaseVersionOfStableVersion() {
            Version v = Version.of(1);
            assertThrows(IllegalStateException.class, () -> v.nextPreReleaseVersion("alpha"));
        }

        @Test
        void shouldNotAcceptNullForNextPreReleaseVersion() {
            Version v = Version.of(1, "pre-release");
            assertThrowsIllegalArgumentException(() -> v.nextPreReleaseVersion((String[]) null));
        }

        @Test
        void shouldNotAcceptNullIdentifiersForNextPreReleaseVersion() {
            Version v = Version.of(1, "pre-release");
            assertThrowsIllegalArgumentException(() -> v.nextPreReleaseVersion((String) null));
        }

        @ParameterizedTest
        @ValueSource(strings = {"01", "rc.", ".rc", "rc!"})
        void shouldNotAcceptInvalidIdentifiersForNextPreReleaseVersion(String id) {
            Version v = Version.of(1, "pre-release");
            assertThrows(ParseException.class, () -> v.nextPreReleaseVersion(id));
        }

        @Test
        void shouldNotAllowToObtainLowerNextPreReleaseVersion() {
            Version v = Version.of(1, "beta");
            assertThrows(IllegalStateException.class, () -> v.nextPreReleaseVersion("alpha"));
        }

        @Test
        void shouldNotAllowToObtainEquivalentNextPreReleaseVersion() {
            Version v = Version.of(1, "beta");
            assertThrows(IllegalStateException.class, () -> v.nextPreReleaseVersion("beta"));
        }

        @Test
        void shouldNotChangeVersionCoreWhenPreReleaseVersionIsIncreased() {
            Version v1 = Version.of(1, 2, 3, "alpha");
            Version v2 = v1.nextPreReleaseVersion("beta");
            assertEquals(v1.majorVersion(), v2.majorVersion());
            assertEquals(v1.minorVersion(), v2.minorVersion());
            assertEquals(v1.patchVersion(), v2.patchVersion());
        }

        @Test
        void shouldDropBuildMetadataWhenPreReleaseVersionIsIncreased() {
            Version v1 = Version.of(1, "alpha", "build.metadata");
            Version v2 = v1.nextPreReleaseVersion("beta");
            assertFalse(v2.buildMetadata().isPresent());
        }

        @Test
        void shouldIncrementPreReleaseVersionByOne() {
            Version v1 = Version.of(1, "pre-release.1");
            Version v2 = v1.nextPreReleaseVersion();
            assertEquals("pre-release.2", v2.preReleaseVersion().get());
        }

        @Test
        void shouldAddNumericIdentifierToPreReleaseVersionIfNeededWhenIncrementing() {
            Version v1 = Version.of(1, "pre-release");
            Version v2 = v1.nextPreReleaseVersion();
            assertEquals("pre-release.1", v2.preReleaseVersion().get());
        }

        @Test
        void shouldNotAllowPreReleaseNumericIdentifierToOverflow() {
            Version v = Version.of(1, String.valueOf(Long.MAX_VALUE));
            assertThrows(ArithmeticException.class, v::nextPreReleaseVersion);
        }

        @Test
        void shouldPromotePreReleaseVersionToStableVersion() {
            Version v1 = Version.of(1, 2, 3, "pre-release");
            Version v2 = v1.toStableVersion();
            assertTrue(v2.isStable());
        }

        @Test
        void shouldNotChangeVersionCoreWhenPromotingToStableVersion() {
            Version v1 = Version.of(1, 2, 3, "pre-release");
            Version v2 = v1.toStableVersion();
            assertEquals(v1.majorVersion(), v2.majorVersion());
            assertEquals(v1.minorVersion(), v2.minorVersion());
            assertEquals(v1.patchVersion(), v2.patchVersion());
        }

        @Test
        void shouldDropBuildMetadataWhenPromotingToStableVersion() {
            Version v1 = Version.of(1, "pre-release", "build.metadata");
            Version v2 = v1.toStableVersion();
            assertFalse(v2.buildMetadata().isPresent());
        }

        @Test
        void shouldSetBuildMetadata() {
            Version v1 = Version.of(1);
            Version v2 = v1.withBuildMetadata("build", "metadata");
            assertEquals("build.metadata", v2.buildMetadata().get());
        }

        @Test
        void shouldNotChangeVersionCoreWhenSettingBuildMetadata() {
            Version v1 = Version.of(1, 2, 3, "pre-release");
            Version v2 = v1.withBuildMetadata("build.metadata");
            assertEquals(v1.majorVersion(), v2.majorVersion());
            assertEquals(v1.minorVersion(), v2.minorVersion());
            assertEquals(v1.patchVersion(), v2.patchVersion());
        }

        @Test
        void shouldNotChangePreReleaseVersionWhenSettingBuildMetadata() {
            Version v1 = Version.of(1, "pre-release");
            Version v2 = v1.withBuildMetadata("build.metadata");
            assertEquals(v1.preReleaseVersion().get(), v2.preReleaseVersion().get());
        }

        @Test
        void shouldNotAcceptNullForBuildMetadata() {
            Version v = Version.of(1);
            assertThrowsIllegalArgumentException(() -> v.withBuildMetadata((String[]) null));
        }

        @Test
        void shouldNotAcceptEmptyBuildMetadata() {
            Version v = Version.of(1);
            assertThrowsIllegalArgumentException(() -> v.withBuildMetadata((new String[0])));
        }

        @Test
        void shouldNotAcceptNullIdentifiersForBuildMetadata() {
            Version v = Version.of(1);
            assertThrowsIllegalArgumentException(() -> v.withBuildMetadata((String) null));
        }

        @ParameterizedTest
        @ValueSource(strings = {"build.", ".build", "build!"})
        void shouldNotAcceptInvalidIdentifiersForBuildMetadata(String id) {
            Version v = Version.of(1);
            assertThrows(ParseException.class, () -> v.withBuildMetadata(id));
        }

        @Test
        void shouldDropBuildMetadata() {
            Version v1 = Version.of(1, null, "build.metadata");
            Version v2 = v1.withoutBuildMetadata();
            assertFalse(v2.buildMetadata().isPresent());
        }

        @Test
        void shouldNotChangeVersionCoreWhenDroppingBuildMetadata() {
            Version v1 = Version.of(1, 2, 3, null, "build.metadata");
            Version v2 = v1.withoutBuildMetadata();
            assertEquals(v1.majorVersion(), v2.majorVersion());
            assertEquals(v1.minorVersion(), v2.minorVersion());
            assertEquals(v1.patchVersion(), v2.patchVersion());
        }

        @Test
        void shouldNotChangePreReleaseVersionWhenDroppingBuildMetadata() {
            Version v1 = Version.of(1, "pre-release", "build.metadata");
            Version v2 = v1.withoutBuildMetadata();
            assertEquals(v1.preReleaseVersion().get(), v2.preReleaseVersion().get());
        }

        @Test
        void shouldBeImmutable() {
            Version v = Version.of(1, 2, 3, "alpha", "test");
            assertNotEquals(v, v.nextMajorVersion());
            assertNotEquals(v, v.nextMajorVersion("beta"));
            assertNotEquals(v, v.nextMinorVersion());
            assertNotEquals(v, v.nextMinorVersion("gamma"));
            assertNotEquals(v, v.nextPatchVersion());
            assertNotEquals(v, v.nextPatchVersion("delta"));
            assertNotEquals(v, v.nextPreReleaseVersion());
            assertNotEquals(v, v.nextPreReleaseVersion("epsilon"));
            assertNotEquals(v, v.withBuildMetadata("build.metadata"));
            assertNotEquals(v, v.withoutBuildMetadata());
        }

        @Test
        void shouldCheckIfVersionSatisfiesExpression() {
            Version v = Version.of(2, 0, 0, "beta");
            assertTrue(v.satisfies(gte("1.0.0").and(lt("2.0.0"))));
            assertFalse(v.satisfies(gte("2.0.0").and(lt("3.0.0"))));
        }

        @Test
        void shouldConsiderPreReleaseVersionsAsUnstable() {
            Version v = Version.of(1, 2, 3, "rc");
            assertTrue(v.isPreRelease());
            assertFalse(v.isStable());
        }

        @Test
        void shouldConsiderNonPreReleaseVersionsAsStable() {
            Version v = Version.of(1, 2, 3);
            assertFalse(v.isPreRelease());
            assertTrue(v.isStable());
        }

        @Test
        void shouldConsiderPublicApiAsUnstableIfMajorVersionIsZero() {
            Version v = Version.of(0, 10, 0);
            assertFalse(v.isPublicApiStable());
        }

        @Test
        void shouldConsiderPublicApiAsStableIfMajorVersionIsOneOrHigher() {
            Version v = Version.of(1);
            assertTrue(v.isPublicApiStable());
        }

        @Test
        void shouldCheckIfVersionsAreCompatibleInTermsOfMajorVersions() {
            Version v1 = Version.of(1, 1, 1);
            Version v2 = Version.of(1, 2, 3);
            Version v3 = Version.of(2, 2, 2);
            assertTrue(v1.isSameMajorVersionAs(v2));
            assertFalse(v1.isSameMajorVersionAs(v3));
        }

        @Test
        void shouldCheckForNullsWhenComparingMajorVersions() {
            Version v = Version.of(1);
            assertThrowsIllegalArgumentException(() -> v.isSameMajorVersionAs(null));
        }

        @Test
        void shouldCheckIfVersionsAreCompatibleInTermsOfMajorAndMinorVersions() {
            Version v1 = Version.of(1, 1, 1);
            Version v2 = Version.of(1, 1, 2);
            Version v3 = Version.of(1, 2, 3);
            assertTrue(v1.isSameMinorVersionAs(v2));
            assertFalse(v1.isSameMinorVersionAs(v3));
        }

        @Test
        void shouldCheckForNullsWhenComparingMajorAndMinorVersions() {
            Version v = Version.of(1);
            assertThrowsIllegalArgumentException(() -> v.isSameMinorVersionAs(null));
        }

        @Test
        void shouldCheckIfVersionsAreCompatibleInTermsOfMajorMinorAndPatchVersions() {
            Version v1 = Version.of(1, 1, 1);
            Version v2 = Version.of(1, 1, 1);
            Version v3 = Version.of(1, 1, 2);
            assertTrue(v1.isSamePatchVersionAs(v2));
            assertFalse(v1.isSamePatchVersionAs(v3));
        }

        @Test
        void shouldCheckForNullsWhenComparingMajorMinorAndPatchVersions() {
            Version v = Version.of(1);
            assertThrowsIllegalArgumentException(() -> v.isSamePatchVersionAs(null));
        }

        @Test
        void shouldDetermineIfItsPrecedenceIsHigherThanThatOfOthers() {
            Version v1 = Version.of(3, 2, 1);
            Version v2 = Version.of(1, 2, 3);
            assertTrue(v1.isHigherThan(v2));
            assertFalse(v2.isHigherThan(v1));
            assertFalse(v1.isHigherThan(v1));
        }

        @Test
        void shouldDetermineIfItsPrecedenceIsHigherThanOrEqualToThatOfOthers() {
            Version v1 = Version.of(3, 2, 1);
            Version v2 = Version.of(1, 2, 3);
            assertTrue(v1.isHigherThanOrEquivalentTo(v2));
            assertFalse(v2.isHigherThanOrEquivalentTo(v1));
            assertTrue(v1.isHigherThanOrEquivalentTo(v1));
        }

        @Test
        void shouldDetermineIfItsPrecedenceIsLowerThanThatOfOthers() {
            Version v1 = Version.of(3, 2, 1);
            Version v2 = Version.of(1, 2, 3);
            assertFalse(v1.isLowerThan(v2));
            assertTrue(v2.isLowerThan(v1));
            assertFalse(v1.isLowerThan(v1));
        }

        @Test
        void shouldDetermineIfItsPrecedenceIsLowerThanOrEqualToThatOfOthers() {
            Version v1 = Version.of(3, 2, 1);
            Version v2 = Version.of(1, 2, 3);
            assertFalse(v1.isLowerThanOrEquivalentTo(v2));
            assertTrue(v2.isLowerThanOrEquivalentTo(v1));
            assertTrue(v1.isLowerThanOrEquivalentTo(v1));
        }

        @Test
        void shouldDetermineIfItsPrecedenceIsEqualToThatOfOthers() {
            Version v1 = Version.of(1, 2, 3, "pre-release");
            Version v2 = Version.of(1, 2, 3, "pre-release");
            Version v3 = Version.of(1, 2, 3);
            assertTrue(v1.isEquivalentTo(v2));
            assertFalse(v1.isEquivalentTo(v3));
        }

        @Test
        void shouldIgnoreBuildMetadataWhenCheckingForEquivalence() {
            Version v1 = Version.of(1, 2, 3, "pre-release", "build.metadata");
            Version v2 = Version.of(1, 2, 3, "pre-release");
            assertTrue(v1.isEquivalentTo(v2));
        }

        @Test
        void shouldBeAbleToCompareWithoutBuildMetadata() {
            Version v1 = Version.of(1, 2, 3, "pre-release", "build.metadata.1");
            Version v2 = Version.of(1, 2, 3, "pre-release", "build.metadata.2");
            assertTrue(0 > v1.compareTo(v2));
            assertTrue(0 == v1.compareToIgnoreBuildMetadata(v2));
        }

        @Test
        void shouldRaiseErrorIfComparedWithNull() {
            Version v = Version.of(1);
            assertThrowsIllegalArgumentException(() -> v.compareToIgnoreBuildMetadata(null));
        }

        @Test
        void shouldOverrideEqualsMethod() {
            Version v1 = Version.of(1, 2, 3, "pre-release", "build.metadata");
            Version v2 = Version.of(1, 2, 3, "pre-release", "build.metadata");
            Version v3 = Version.of(1, 2, 3, "pre-release");
            assertEquals(v1, v2);
            assertNotEquals(v1, v3);
        }

        @Test
        void shouldConvertToBuilderWithPrepopulatedValues() {
            Version v = Version.of(1, 2, 3, "pre-release", "build.metadata");
            Version.Builder b = v.toBuilder();
            assertEquals(b.build(), v);
        }

        @Test
        @SuppressWarnings("deprecation")
        void shouldSetPreReleaseVersion() {
            Version v1 = Version.of(1, 0, 0);
            Version v2 = v1.setPreReleaseVersion("pre-release");
            assertEquals("1.0.0-pre-release", v2.toString());
        }

        @Test
        @SuppressWarnings("deprecation")
        void shouldDropBuildMetadataWhenSettingPreReleaseVersion() {
            Version v1 = Version.of(1, 0, 0, "alpha", "build.metadata");
            Version v2 = v1.setPreReleaseVersion("beta");
            assertEquals("1.0.0-beta", v2.toString());
        }

        @Test
        @SuppressWarnings("deprecation")
        void shouldIncrementBuildMetadata() {
            Version v1 = Version.of(1, 0, 0, null, "build.metadata.1");
            Version v2 = v1.incrementBuildMetadata();
            assertEquals("1.0.0+build.metadata.2", v2.toString());
        }

        @Test
        @SuppressWarnings("deprecation")
        void shouldNotAllowToIncrementEmptyBuildMetadata() {
            Version v = Version.of(1, 0, 0);
            assertThrows(IllegalStateException.class, v::incrementBuildMetadata);
        }
    }

    @Nested
    class CompareToMethod {

        @Test
        void shouldCompareMajorVersionNumerically() {
            Version v = Version.of(22);
            assertTrue(0 < v.compareTo(Version.of(3)));
            assertTrue(0 == v.compareTo(Version.of(22)));
            assertTrue(0 > v.compareTo(Version.of(111)));
        }

        @Test
        void shouldCompareMinorVersionNumerically() {
            Version v = Version.of(0, 22);
            assertTrue(0 < v.compareTo(Version.of(0, 3)));
            assertTrue(0 == v.compareTo(Version.of(0, 22)));
            assertTrue(0 > v.compareTo(Version.of(0, 111)));
        }

        @Test
        void shouldComparePatchVersionNumerically() {
            Version v = Version.of(0, 0, 22);
            assertTrue(0 < v.compareTo(Version.of(0, 0, 3)));
            assertTrue(0 == v.compareTo(Version.of(0, 0, 22)));
            assertTrue(0 > v.compareTo(Version.of(0, 0, 111)));
        }

        @Test
        void shouldCompareAlphanumericIdentifiersLexicallyInAsciiOrder() {
            Version v = Version.of(1, "beta");
            assertTrue(0 < v.compareTo(Version.of(1, "alpha")));
            assertTrue(0 == v.compareTo(Version.of(1, "beta")));
            assertTrue(0 > v.compareTo(Version.of(1, "gamma")));
        }

        @Test
        void shouldGiveHigherPrecedenceToNonNumericIdentifierThanNumeric() {
            Version v1 = Version.of(1, "abc");
            Version v2 = Version.of(1, "111");
            assertTrue(0 < v1.compareTo(v2));
        }

        @Test
        void shouldCompareNumericIdentifiersNumerically() {
            Version v = Version.of(1, "22");
            assertTrue(0 > v.compareTo(Version.of(1, "111")));
            assertTrue(0 == v.compareTo(Version.of(1, "22")));
            assertTrue(0 < v.compareTo(Version.of(1, "3")));
        }

        @Test
        void shouldGiveHigherPrecedenceToVersionWithLargerSetOfIdentifiers() {
            Version v1 = Version.of(1, "a.b.c");
            Version v2 = Version.of(1, "a.b.c.d");
            assertTrue(0 > v1.compareTo(v2));
        }

        @Test
        void shouldGiveHigherPrecedenceToStableVersionThanPreReleaseVersion() {
            Version v1 = Version.of(1, "pre-release");
            Version v2 = Version.of(1);
            assertTrue(0 > v1.compareTo(v2));
        }

        @Test
        void shouldGiveHigherPrecedenceToVersionWithBuildMetadata() {
            Version v1 = Version.of(1, "pre-release", "build.metadata");
            Version v2 = Version.of(1, "pre-release");
            assertTrue(0 < v1.compareTo(v2));
        }

        @Test
        void shouldBeConsistentWithEquals() {
            Version v1 = Version.of(1, 2, 3, "pre-release", "build.metadata");
            Version v2 = Version.of(1, 2, 3, "pre-release", "build.metadata");
            assertEquals(v1, v2);
            assertEquals(0, v1.compareTo(v2));
        }

        @Test
        void shouldCorrectlyCompareVersionsWithBuildMetadata() {
            String[] versions = {
                "1.0.0-alpha",
                "1.0.0-alpha.1",
                "1.0.0-alpha.beta",
                "1.0.0-beta",
                "1.0.0-beta.2",
                "1.0.0-beta.11",
                "1.0.0-rc.1",
                "1.0.0-rc.1+build.1",
                "1.0.0",
                "1.0.0+0.3.7",
                "1.3.7+build",
                "1.3.7+build.2.b8f12d7",
                "1.3.7+build.11.e0f985a"
            };
            for (int i = 1; i < versions.length; i++) {
                Version v1 = Version.parse(versions[i-1]);
                Version v2 = Version.parse(versions[i]);
                assertTrue(0 > v1.compareTo(v2));
            }
        }
    }

    @Nested
    class IncrementOrderComparator {

        @Test
        void shouldSortInIncrementOrder() {
            Version v1 = Version.of(1, 2, 3);
            Version v2 = Version.of(3, 2, 1);
            assertTrue(0 > v1.compareTo(v2));
            assertTrue(0 > Version.INCREMENT_ORDER.compare(v1, v2));
        }

        @Test
        void shouldIgnoreBuildMetadata() {
            Version v1 = Version.of(1, 2, 3, "pre-release", "build.metadata.1");
            Version v2 = Version.of(1, 2, 3, "pre-release", "build.metadata.2");
            assertTrue(0 == Version.INCREMENT_ORDER.compare(v1, v2));
        }
    }

    @Nested
    class PrecedenceOrderComparator {

        @Test
        void shouldSortInPrecedenceOrder() {
            Version v1 = Version.of(1, 2, 3);
            Version v2 = Version.of(3, 2, 1);
            assertTrue(0 > Version.INCREMENT_ORDER.compare(v1, v2));
            assertTrue(0 < Version.PRECEDENCE_ORDER.compare(v1, v2));
        }

        @Test
        void shouldIgnoreBuildMetadata() {
            Version v1 = Version.of(1, 2, 3, "pre-release", "build.metadata.1");
            Version v2 = Version.of(1, 2, 3, "pre-release", "build.metadata.2");
            assertTrue(0 == Version.PRECEDENCE_ORDER.compare(v1, v2));
        }
    }

    @Nested
    class EqualsMethod {

        @Test
        void shouldBeReflexive() {
            Version v = Version.of(1, 2, 3);
            assertEquals(v, v);
        }

        @Test
        void shouldBeSymmetric() {
            Version v1 = Version.of(1, 2, 3);
            Version v2 = Version.of(1, 2, 3);
            assertEquals(v1, v2);
            assertEquals(v2, v1);
        }

        @Test
        void shouldBeTransitive() {
            Version v1 = Version.of(1, 2, 3);
            Version v2 = Version.of(1, 2, 3);
            Version v3 = Version.of(1, 2, 3);
            assertEquals(v1, v2);
            assertEquals(v2, v3);
            assertEquals(v1, v3);
        }

        @Test
        void shouldBeConsistent() {
            Version v1 = Version.of(1, 2, 3);
            Version v2 = Version.of(1, 2, 3);
            assertEquals(v1, v2);
            assertEquals(v1, v2);
        }

        @Test
        void shouldReturnFalseIfOtherVersionIsOfDifferentType() {
            Version v1 = Version.of(1, 2, 3);
            assertNotEquals(v1, "1.2.3");
        }

        @Test
        void shouldReturnFalseIfOtherVersionIsNull() {
            Version v1 = Version.of(1, 2, 3);
            Version v2 = null;
            assertNotEquals(v1, v2);
        }

        @Test
        void shouldNotIgnoreBuildMetadataWhenCheckingForExactEquality() {
            Version v1 = Version.of(1, 2, 3, "pre-release", "build.metadata");
            Version v2 = Version.of(1, 2, 3, "pre-release");
            assertNotEquals(v1, v2);
        }
    }

    @Nested
    class HashCodeMethod {

        @Test
        void shouldReturnSameHashCodeIfVersionsAreEqual() {
            Version v1 = Version.of(1, 2, 3, "pre-release", "build.metadata");
            Version v2 = Version.of(1, 2, 3, "pre-release", "build.metadata");
            assertEquals(v1, v2);
            assertEquals(v1.hashCode(), v2.hashCode());
        }

        @Test
        void shouldReturnDifferentHashCodesIfVersionsAreNotEqual() {
            Version v1 = Version.of(1, 2, 3, "pre-release", "build.metadata");
            Version v2 = Version.of(1, 2, 3, "pre-release");
            assertNotEquals(v1, v2);
            assertNotEquals(v1.hashCode(), v2.hashCode());
        }
    }

    @Nested
    class ToStringMethod {

        @Test
        void shouldReturnStringRepresentation() {
            String value = "1.2.3-beta+build";
            Version v = Version.parse(value);
            assertEquals(value, v.toString());
        }

        @Test
        void shouldUseRootLocale() {
            Locale.setDefault(new Locale("hi", "IN"));
            Version v = Version.of(1, 2, 3);
            assertEquals("1.2.3", v.toString());
            Locale.setDefault(Locale.ROOT);
        }
    }

    @Nested
    class Serialization {

        @Test
        void shouldBeSerializable() throws Exception {
            Version v1 = Version.of(1, 2, 3, "alpha.1", "build.1");
            Version v2 = deserialize(serialize(v1));
            assertEquals(v1, v2);
        }

        private byte[] serialize(Version v) throws Exception {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(v);
            return baos.toByteArray();
        }

        private Version deserialize(byte[] bytes) throws Exception {
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            ObjectInputStream ois = new ObjectInputStream(bais);
            return (Version) ois.readObject();
        }
    }

    private static void assertThrowsIllegalArgumentException(Executable exec) {
        assertThrows(IllegalArgumentException.class, exec);
    }
}
