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
            assertEquals(1, v.getMajorVersion());
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
            assertEquals(2, v.getMinorVersion());
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
            assertEquals(3, v.getPatchVersion());
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
            assertEquals("pre.release", v.getPreReleaseVersion());
        }

        @Test
        void shouldMakeDefensiveCopyOfArgumentsWhenSettingPreReleaseVersion() {
            Version.Builder b = new Version.Builder();
            String[] ids = {"pre.release"};
            b.setPreReleaseVersion(ids);
            ids[0] = null;
            Version v = b.build();
            assertEquals("pre.release", v.getPreReleaseVersion());
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
            assertEquals("pre.release.1", v.getPreReleaseVersion());
        }

        @Test
        void shouldMakeDefensiveCopyOfArgumentsWhenAddingPreReleaseIdentifiers() {
            Version.Builder b = new Version.Builder();
            b.setPreReleaseVersion("pre");
            String[] ids = {"release"};
            b.addPreReleaseIdentifiers(ids);
            ids[0] = null;
            Version v = b.build();
            assertEquals("pre.release", v.getPreReleaseVersion());
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
            assertEquals("", v.getPreReleaseVersion());
        }

        @Test
        void shouldSetBuildMetadata() {
            Version.Builder b = new Version.Builder();
            b.setBuildMetadata("build", "metadata");
            Version v = b.build();
            assertEquals("build.metadata", v.getBuildMetadata());
        }

        @Test
        void shouldMakeDefensiveCopyOfArgumentsWhenSettingBuildMetadata() {
            Version.Builder b = new Version.Builder();
            String[] ids = {"build.metadata"};
            b.setBuildMetadata(ids);
            ids[0] = null;
            Version v = b.build();
            assertEquals("build.metadata", v.getBuildMetadata());
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
            assertEquals("build.metadata.1", v.getBuildMetadata());
        }

        @Test
        void shouldMakeDefensiveCopyOfArgumentsWhenAddingBuildIdentifiers() {
            Version.Builder b = new Version.Builder();
            b.setBuildMetadata("build");
            String[] ids = {"metadata"};
            b.addBuildIdentifiers(ids);
            ids[0] = null;
            Version v = b.build();
            assertEquals("build.metadata", v.getBuildMetadata());
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
            assertEquals("", v.getBuildMetadata());
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
            assertEquals(1, v.getMajorVersion());
            assertEquals(2, v.getMinorVersion());
            assertEquals(3, v.getPatchVersion());
        }

        @Test
        void mayHavePreReleaseVersionFollowingPatchVersionPrependedWithHyphen() {
            Version v = Version.parse("1.2.3-pre-release");
            assertEquals("pre-release", v.getPreReleaseVersion());
        }

        @Test
        void mayHaveBuildMetadataFollowingPatchOrPreReleaseVersionPrependedWithPlus() {
            Version v = Version.parse("1.2.3+build.metadata");
            assertEquals("build.metadata", v.getBuildMetadata());
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
        void shouldProvideIncrementMajorVersionMethod() {
            Version v = Version.of(1, 2, 3);
            Version incrementedMajor = v.incrementMajorVersion();
            assertEquals("2.0.0", incrementedMajor.toString());
        }

        @Test
        void shouldResetMinorAndPatchWhenMajorIsIncremented() {
            Version v = Version.of(1, 2, 3);
            Version incremented = v.incrementMajorVersion();
            assertEquals(2, incremented.getMajorVersion());
            assertEquals(0, incremented.getMinorVersion());
            assertEquals(0, incremented.getPatchVersion());
        }

        @Test
        void shouldIncrementMajorVersionWithPreReleaseIfProvided() {
            Version v = Version.of(1, 2, 3);
            Version incrementedMajor = v.incrementMajorVersion("beta");
            assertEquals("2.0.0-beta", incrementedMajor.toString());
        }

        @Test
        void shouldProvideIncrementMinorVersionMethod() {
            Version v = Version.of(1, 2, 3);
            Version incrementedMinor = v.incrementMinorVersion();
            assertEquals("1.3.0", incrementedMinor.toString());
        }

        @Test
        void shouldResetPatchWhenMinorIsIncremented() {
            Version v = Version.of(1, 2, 3);
            Version incremented = v.incrementMinorVersion();
            assertEquals(1, incremented.getMajorVersion());
            assertEquals(3, incremented.getMinorVersion());
            assertEquals(0, incremented.getPatchVersion());
        }

        @Test
        void shouldIncrementMinorVersionWithPreReleaseIfProvided() {
            Version v = Version.of(1, 2, 3);
            Version incrementedMinor = v.incrementMinorVersion("alpha");
            assertEquals("1.3.0-alpha", incrementedMinor.toString());
        }

        @Test
        void shouldProvideIncrementPatchVersionMethod() {
            Version v = Version.of(1, 2, 3);
            Version incrementedPatch = v.incrementPatchVersion();
            assertEquals("1.2.4", incrementedPatch.toString());
        }

        @Test
        void shouldIncrementPatchVersionWithPreReleaseIfProvided() {
            Version v = Version.of(1, 2, 3);
            Version incrementedPatch = v.incrementPatchVersion("rc");
            assertEquals("1.2.4-rc", incrementedPatch.toString());
        }

        @Test
        void shouldRaiseErrorIfIncrementCausesOverflow() {
            Version v = Version.of(
                Long.MAX_VALUE,
                Long.MAX_VALUE,
                Long.MAX_VALUE,
                String.valueOf(Long.MAX_VALUE),
                String.valueOf(Long.MAX_VALUE)
            );
            assertThrows(ArithmeticException.class, v::incrementMajorVersion);
            assertThrows(ArithmeticException.class, v::incrementMinorVersion);
            assertThrows(ArithmeticException.class, v::incrementPatchVersion);
            assertThrows(ArithmeticException.class, v::incrementPreReleaseVersion);
            assertThrows(ArithmeticException.class, v::incrementBuildMetadata);
        }

        @Test
        void shouldDropBuildMetadataWhenIncrementing() {
            Version v = Version.of(1, 2, 3, "alpha", "build");

            Version major1 = v.incrementMajorVersion();
            assertEquals("2.0.0", major1.toString());
            Version major2 = v.incrementMajorVersion("beta");
            assertEquals("2.0.0-beta", major2.toString());

            Version minor1 = v.incrementMinorVersion();
            assertEquals("1.3.0", minor1.toString());
            Version minor2 = v.incrementMinorVersion("beta");
            assertEquals("1.3.0-beta", minor2.toString());

            Version patch1 = v.incrementPatchVersion();
            assertEquals("1.2.4", patch1.toString());
            Version patch2 = v.incrementPatchVersion("beta");
            assertEquals("1.2.4-beta", patch2.toString());
        }

        @Test
        void shouldThrowExceptionWhenIncrementingEmptyPreReleaseVersion() {
            Version v1 = Version.of(1, 0, 0);
            assertThrows(IllegalStateException.class, v1::incrementPreReleaseVersion);
        }

        @Test
        void shouldDropBuildMetadataWhenIncrementingPreReleaseVersion() {
            Version v1 = Version.of(1, 0, 0, "beta.1", "build");
            Version v2 = v1.incrementPreReleaseVersion();
            assertEquals("1.0.0-beta.2", v2.toString());
        }

        @Test
        void shouldProvideIncrementBuildMetadataMethod() {
            Version v1 = Version.of(1, 0, 0, null, "build.1");
            Version v2 = v1.incrementBuildMetadata();
            assertEquals("1.0.0+build.2", v2.toString());
        }

        @Test
        void shouldThrowExceptionWhenIncrementingEmptyBuildMetadata() {
            Version v1 = Version.of(1, 0, 0);
            assertThrows(IllegalStateException.class, v1::incrementBuildMetadata);
        }

        @Test
        void shouldAppendNumericIdentifierToBuildMetadataToBeIncrementedIfLastOneIsDigits() {
            Version v1 = Version.of(0, 0, 1, null, "build.01");
            Version v2 = v1.incrementBuildMetadata();
            assertEquals("build.01.1", v2.getBuildMetadata());
        }

        @Test
        void shouldBeImmutable() {
            Version v = Version.of(1, 2, 3, "alpha.1", "build.1");

            assertNotEquals(v, v.incrementMajorVersion());
            assertNotEquals(v, v.incrementMinorVersion());
            assertNotEquals(v, v.incrementPatchVersion());
            assertNotEquals(v, v.incrementPreReleaseVersion());
            assertNotEquals(v, v.setPreReleaseVersion("alpha.2"));
            assertNotEquals(v.toString(), v.incrementBuildMetadata().toString());
            assertNotEquals(v.toString(), v.setBuildMetadata("build.2").toString());
        }

        @Test
        void shouldProvideSetPreReleaseVersionMethod() {
            Version v1 = Version.of(1, 0, 0);
            Version v2 = v1.setPreReleaseVersion("alpha");
            assertEquals("1.0.0-alpha", v2.toString());
        }

        @Test
        void shouldDropBuildMetadataWhenSettingPreReleaseVersion() {
            Version v1 = Version.of(1, 0, 0, "alpha", "build");
            Version v2 = v1.setPreReleaseVersion("beta");
            assertEquals("1.0.0-beta", v2.toString());
        }

        @Test
        void shouldProvideSetBuildMetadataMethod() {
            Version v1 = Version.of(1, 0, 0);
            Version v2 = v1.setBuildMetadata("build");
            assertEquals("1.0.0+build", v2.toString());
        }

        @Test
        void shouldReturnEmptyStringOnGetPreReleaseVersionIfEmpty() {
            Version v = Version.of(0, 0, 1, null);
            assertTrue(v.getPreReleaseVersion().isEmpty());
        }

        @Test
        void shouldReturnEmptyStringOnGetBuildMetadataIfEmpty() {
            Version v = Version.of(0, 0, 1, null, null);
            assertTrue(v.getBuildMetadata().isEmpty());
        }

        @Test
        void shouldProvideIncrementPreReleaseVersionMethod() {
            Version v1 = Version.of(1, 0, 0, "beta.1");
            Version v2 = v1.incrementPreReleaseVersion();
            assertEquals("1.0.0-beta.2", v2.toString());
        }

        @Test
        void shouldAppendNumericIdentifierToPreReleaseVersionToBeIncrementedIfAbsent() {
            Version v1 = Version.of(0, 0, 1, "alpha");
            Version v2 = v1.incrementPreReleaseVersion();
            assertEquals("alpha.1", v2.getPreReleaseVersion());
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
            assertFalse(v.isStable());
        }

        @Test
        void shouldConsiderNonPreReleaseVersionsAsStable() {
            Version v = Version.of(1, 2, 3);
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
