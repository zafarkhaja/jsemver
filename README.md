Java SemVer v0.10.0-SNAPSHOT (SemVer 2)
=======================================

Java SemVer is a Java implementation of the Semantic Versioning Specification
(http://semver.org/).

### Versioning ###
Java SemVer is versioned according to the SemVer Specification.

**NOTE**: The current version of the Java SemVer library has a major version of
zero, which according to the SemVer p.4 means that the library is under initial
development and its public API should not be considered stable.

### Contributing ###
For the guidelines on how to report bugs, request features, submit code changes,
and contribute in various other ways please refer to the `CONTRIBUTING.md` file.

### License ###
Java SemVer is licensed under the MIT License - see the `LICENSE` file for details.


Library Usage
-------------
* [Installation](#installation)
* [Common Use Cases](#common-use-cases)
  * [Creating Versions](#creating-versions)
  * [Incrementing Versions](#incrementing-versions)
  * [Comparing Versions](#comparing-versions)
* [Range Expressions](#range-expressions)
* [Exception Handling](#exception-handling)


## Installation ##
To install the Java SemVer library add the following dependency to your project

**Latest stable version**
~~~ xml
<dependency>
  <groupId>com.github.zafarkhaja</groupId>
  <artifactId>java-semver</artifactId>
  <version>0.9.0</version>
</dependency>
~~~

**Current development version**
~~~ xml
<dependency>
  <groupId>com.github.zafarkhaja</groupId>
  <artifactId>java-semver</artifactId>
  <version>0.10.0-SNAPSHOT</version>
</dependency>
~~~
**NOTE**: To use the development version you need to add the Snapshot repository
(https://oss.sonatype.org/content/repositories/snapshots/) to your build
configuration file.


## Common Use Cases ##
The Java SemVer library is built around the `Version` class which represents
version as defined by the SemVer Specification. The `Version` class contains
methods for parsing version strings, incrementing obtained versions, checking
their individual characteristics, comparing with each other and determining
their relative precedence.

Below are some common use cases of the `Version` class

~~~ java
import com.github.zafarkhaja.semver.Version;
~~~

### Creating Versions ###
There are 3 ways to obtain a `Version` instance:

1. by using the `Version.parse()` or `Version.tryParse()` methods
~~~ java
Version v = Version.parse("1.2.3-pre-release+build.metadata");

Optional<Version> o = Version.tryParse("1.2.3-pre-release+build.metadata");
~~~

2. by using the `Version.of()` method
~~~ java
Version v = Version.of(1, 2, 3, "pre-release", "build.metadata");
~~~

3. by using the `Version.Builder` class
~~~ java
Version v1 = new Version.Builder()  // 0.0.0
  .setVersionCore(0, 1)             // 0.1.0
  .setMajorVersion(1)               // 1.1.0
  .setPreReleaseVersion("beta")     // 1.1.0-beta
  .addPreReleaseIdentifiers("1")    // 1.1.0-beta.1
  .setBuildMetadata("build", "1")   // 1.1.0-beta.1+build.1
  .unsetBuildMetadata()             // 1.1.0-beta.1
  .build()
;

Version v2 = v1.toBuilder()  // 1.1.0-beta.1
  .setMinorVersion(2)        // 1.2.0-beta.1
  .setPatchVersion(3)        // 1.2.3-beta.1
  .unsetPreReleaseVersion()  // 1.2.3
  .build()
;
~~~

### Incrementing Versions ###
The `Version` class provides "incrementor" methods for incrementing normal and
pre-release versions. Due to its nature as metadata, incrementing build metadata
is not supported.

~~~ java
Version v = Version.of(0, 1, 0)      // 0.1.0
  .nextPatchVersion()                // 0.1.1
  .nextMinorVersion()                // 0.2.0
  .withBuildMetadata("abcdefg")      // 0.2.0+abcdefg
  .nextMajorVersion("beta")          // 1.0.0-beta
  .nextPreReleaseVersion()           // 1.0.0-beta.1
  .nextPreReleaseVersion("rc", "1")  // 1.0.0-rc.1
  .nextPreReleaseVersion()           // 1.0.0-rc.2
  .toStableVersion()                 // 1.0.0
;
~~~

### Comparing Versions ###
The recommended way to determine version precedence is to use "comparator" methods

~~~ java
Version v1 = Version.of(1, 2, 3, "rc.1", "build.1");
Version v2 = Version.of(1, 2, 3, "rc.1", "build.2");

v1.isHigherThan(v2);                // false
v1.isHigherThanOrEquivalentTo(v2);  // true
v1.isLowerThan(v2);                 // false
v1.isLowerThanOrEquivalentTo(v2);   // true
v1.isEquivalentTo(v2);              // true
~~~

The other options, mainly intended for use in comparison-based data structures,
are the `Version.INCREMENT_ORDER` comparator for "natural" ordering, and the
`Version.PRECEDENCE_ORDER` comparator for highest precedence ordering, which is
reverse of the former one.

~~~ java
Version v1 = Version.of(1, 2, 3, "rc.1");
Version v2 = Version.of(1, 2, 3, "rc.2");

Version.INCREMENT_ORDER.compare(v1, v2);   // < 0 -> [v1, v2]
Version.PRECEDENCE_ORDER.compare(v1, v2);  // > 0 -> [v2, v1]
~~~

**NOTE**: The `equals()` and `compareTo()` methods don't adhere to the
Specification regarding build metadata, and therefore shouldn't be used for
determining version precedence. Also, they are not really intended to be used
directly, but rather by hash- and comparison-based data structures, respectively.
That said, there are still cases where they can prove useful, like testing
versions for exact equality or ordering them based on their build metadata.

~~~ java
Version v1 = Version.of(1, 2, 3, null, "build.1");
Version v2 = Version.of(1, 2, 3, null, "build.2");

v1.isEquivalentTo(v2);  // true
v1.equals(v2);          // false

v1.isLowerThan(v2);     // false
v1.compareTo(v2);       // < 0
~~~


## Range Expressions ##
Java SemVer supports Range Expressions with an opinionated
[BNF grammar](https://github.com/zafarkhaja/jsemver/issues/1).

**NOTE**: The Java SemVer Range Expressions are not fully compatible with the
`node-semver` ranges.

~~~ java
Version v = Version.of(1, 2, 3, "pre-release");
v.satisfies(">=1.0.0 & <2.0.0");  // false
~~~

The following is the list of supported notations and their interpretations:
* Wildcard Ranges (`*`|`X`|`x`): `1.*` interpreted as `>=1.0.0 & <2.0.0`
* Tilde Ranges (`~`): `~1.5` interpreted as `>=1.5.0 & <1.6.0`
* Hyphen Ranges (`-`): `1.0-2.0` interpreted as `>=1.0.0 & <=2.0.0`
* Caret Ranges (`^`): `^0.2.3` interpreted as `>=0.2.3 & <0.3.0`
* Partial Version Ranges: `1` interpreted as `1.x` or `>=1.0.0 & <2.0.0`
* Negation operator: `!(1.x)` interpreted as `<1.0.0 & >=2.0.0`
* Parenthesized expressions: `~1.3 | (1.4.* & !=1.4.5) | ~2`

There is also an internal DSL available just in case...

~~~ java
import static com.github.zafarkhaja.semver.expr.CompositeExpression.Helper.*;

Version v = Version.of(1, "beta");
v.satisfies(gte(Version.of(1)).and(lt(Version.of(2))));  // false
~~~


## Exception Handling ##
These are the exceptions you can expect when working with the `Version` class:
* `ArithmeticException` is thrown if increment operation causes numeric identifier
  overflow
* `IllegalArgumentException`, depending on the method and the parameter type, is
  thrown if you pass a `null` reference, an empty `String`, or a negative number
* `IllegalStateException` is thrown in situations when a certain method call is
  unexpected, like incrementing a pre-release version of a stable version
* `ParseException` and its subtypes are thrown if the specified string argument
  can't be parsed
