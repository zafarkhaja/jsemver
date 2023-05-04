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
Version v = new Version.Builder()
  .setNormalVersion(1, 2, 3)
  .setPreReleaseVersion("pre-release")
  .setBuildMetadata("build.metadata")
  .build()
;
~~~

### Incrementing Versions ###
Because the `Version` class is immutable, the _incrementors_ return a new
instance of `Version` rather than modifying the given one. Each of the normal
version incrementors has an overloaded method that takes a pre-release version
as an argument.

~~~ java
Version v1 = Version.parse("1.2.3");

// Incrementing the major version
Version v2 = v1.incrementMajorVersion();        // "2.0.0"
Version v2 = v1.incrementMajorVersion("alpha"); // "2.0.0-alpha"

// Incrementing the minor version
Version v3 = v1.incrementMinorVersion();        // "1.3.0"
Version v3 = v1.incrementMinorVersion("alpha"); // "1.3.0-alpha"

// Incrementing the patch version
Version v4 = v1.incrementPatchVersion();        // "1.2.4"
Version v4 = v1.incrementPatchVersion("alpha"); // "1.2.4-alpha"

// Original Version is still the same
String str = v1.toString(); // "1.2.3"
~~~

There are also incrementor methods for the pre-release version and the build
metadata.

~~~ java
// Incrementing the pre-release version
Version v1 = Version.parse("1.2.3-rc");          // considered as "rc.0"
Version v2 = v1.incrementPreReleaseVersion();    // "1.2.3-rc.1"
Version v3 = v2.incrementPreReleaseVersion();    // "1.2.3-rc.2"

// Incrementing the build metadata
Version v1 = Version.parse("1.2.3-rc+build");    // considered as "build.0"
Version v2 = v1.incrementBuildMetadata();        // "1.2.3-rc+build.1"
Version v3 = v2.incrementBuildMetadata();        // "1.2.3-rc+build.2"
~~~

When incrementing the normal or pre-release versions the build metadata is
always dropped.

~~~ java
Version v1 = Version.parse("1.2.3-beta+build");

// Incrementing the normal version
Version v2 = v1.incrementMajorVersion();        // "2.0.0"
Version v2 = v1.incrementMajorVersion("alpha"); // "2.0.0-alpha"

Version v3 = v1.incrementMinorVersion();        // "1.3.0"
Version v3 = v1.incrementMinorVersion("alpha"); // "1.3.0-alpha"

Version v4 = v1.incrementPatchVersion();        // "1.2.4"
Version v4 = v1.incrementPatchVersion("alpha"); // "1.2.4-alpha"

// Incrementing the pre-release version
Version v2 = v1.incrementPreReleaseVersion();   // "1.2.3-beta.1"
~~~
**NOTE**: The discussion page https://github.com/mojombo/semver/issues/60 might
be of good use in better understanding some of the decisions made regarding the
incrementor methods.

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
Java SemVer supports the SemVer Expressions API which is implemented as both
internal DSL and external DSL. The entry point for the API are
the `Version.satisfies` methods.

### Internal DSL ###
The internal DSL is implemented by the `CompositeExpression` class using fluent
interface. For convenience, it also provides the `Helper` class with static
helper methods.

~~~ java
import static com.github.zafarkhaja.semver.expr.CompositeExpression.Helper.*;

Version v = Version.parse("1.0.0-beta");
boolean result = v.satisfies(gte("1.0.0").and(lt("2.0.0")));  // false
~~~

### External DSL ###
The BNF grammar for the external DSL can be found in the corresponding
[issue](https://github.com/zafarkhaja/jsemver/issues/1).

~~~ java
Version v = Version.parse("1.0.0-beta");
boolean result = v.satisfies(">=1.0.0 & <2.0.0");  // false
~~~

Below are examples of some common use cases, as well as syntactic sugar and some
other interesting capabilities of the SemVer Expressions external DSL.
* Wildcard Ranges (`*`|`X`|`x`) - `1.*` which is equivalent to `>=1.0.0 & <2.0.0`
* Tilde Ranges (`~`) - `~1.5` which is equivalent to `>=1.5.0 & <1.6.0`
* Hyphen Ranges (`-`) - `1.0-2.0` which is equivalent to `>=1.0.0 & <=2.0.0`
* Caret Ranges (`^`) - `^0.2.3` which is equivalent to `>=0.2.3 & <0.3.0`
* Partial Version Ranges - `1` which is equivalent to `1.X` or `>=1.0.0 & <2.0.0`
* Negation operator - `!(1.x)` which is equivalent to `<1.0.0 & >=2.0.0`
* Parenthesized expressions - `~1.3 | (1.4.* & !=1.4.5) | ~2`


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
