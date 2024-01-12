Java SemVer Changelog
=====================

## v0.10.1 (Jan 12, 2024) ##
* Fixed backward compatibility of `Version`'s getters

## v0.10.0 (Jan 10, 2024) ##
* Made `Version` serializable ([#47](https://github.com/zafarkhaja/jsemver/issues/47))
* Enhanced `Version.Builder` class
* Widened numeric identifiers to `long`
* Replaced `Version`'s static factory methods with `Version.parse()` and `Version.of()`
* Created `Version.tryParse()` method which returns `Optional<Version>`
* Created `Version.isValid()` method ([#26](https://github.com/zafarkhaja/jsemver/issues/26))
* Updated version parser to accept partial versions, such as `1` or `1.2` ([#15](https://github.com/zafarkhaja/jsemver/issues/15))
* Renamed and enhanced `Version`'s getter methods
* Renamed and enhanced `Version`'s "incrementor" methods
* Changed `Version.satisfies(Expression)`'s parameter to `Predicate<Version>`
* Created `Version.isPreRelease()` method
* Created `Version.isStable()` method
* Created `Version.isPublicApiStable()` method
* Created `Version.isPublicApiCompatibleWith(Version)` method
* Created methods to check versions' compatibility ([#21](https://github.com/zafarkhaja/jsemver/issues/21))
* Refactored `Version`'s comparators and "comparator" methods
* Deprecated `Version.BUILD_AWARE_ORDER` comparator
* Updated Range Expressions parser to support double-symbol `&&` and `||` operators ([#23](https://github.com/zafarkhaja/jsemver/issues/23))
* Fixed Exceptions' `getMessage()` method ([#38](https://github.com/zafarkhaja/jsemver/issues/38))
* Performed major code and documentation improvements
* Fixed various bugs and warnings, improved stability
* Added `Automatic-Module-Name` to MANIFEST for JPMS support
* Upgraded Java support to 1.8
* Migrated to JUnit 5
* Added `CONTRIBUTING.md`
* Added `.editorconfig` file
* Included `LICENSE` in the JAR artifact ([#44](https://github.com/zafarkhaja/jsemver/issues/44))
* Improved deployment support
* Updated project dependencies
* Removed Travis CI integration

## v0.9.0 (Mar 19, 2015) ##
* Implemented internal DSL for SemVer Expressions ([#6](https://github.com/zafarkhaja/jsemver/issues/6))
* Added support for Caret (^) and X-Ranges to SemVer Expressions ([#18](https://github.com/zafarkhaja/jsemver/pull/18))
* Improved Tilde ranges to be compatible with `node-semver` ([#18](https://github.com/zafarkhaja/jsemver/pull/18))
* Refactored and polished SemVer Expressions parser
* Performed minor code improvements
* Updated project dependencies

## v0.8.0 (Aug 18, 2014) ##
* Implemented fluent interface for `Version.Builder`
* Rearranged and refactored exceptions
* Refactored version and SemVer Expressions parsers
* Improved error handling and reporting in parsers ([#7](https://github.com/zafarkhaja/jsemver/issues/7))
* Performed minor code improvements and bug fixes
* Enhanced Javadoc comments
* Added "Exception Handling" section to `README.md`

## v0.7.2 (Dec 30, 2013) ##
* Fixed `Version.hashCode()` bug ([#8](https://github.com/zafarkhaja/jsemver/issues/8))

## v0.7.1 (Dec 01, 2013) ##
* Fixed "unchecked generic array creation" warnings ([#5](https://github.com/zafarkhaja/jsemver/issues/5))
* Performed minor code and Javadoc improvements

## v0.7.0 (Nov 16, 2013) ##
* Upgraded the library to comply with SemVer 2.0.0
* Replaced regular expressions with a parser
* Implemented SemVer Expressions ([#1](https://github.com/zafarkhaja/jsemver/issues/1))
* Documented the source code with Javadoc ([#2](https://github.com/zafarkhaja/jsemver/issues/2))
* Published the library to Maven Central ([#4](https://github.com/zafarkhaja/jsemver/issues/4))
* Performed refactoring and minor code improvements
* Updated and reformatted `README.md`
* Added `CHANGELOG.md`

## Unpublished Versions (Jan 27 - Mar 31, 2013) ##
* Implemented basic functionality in compliance with SemVer 2.0.0-rc.2
* Created `Version.Builder` class
* Created `Version.BUILD_AWARE_ORDER` comparator
* Integrated Travis CI
