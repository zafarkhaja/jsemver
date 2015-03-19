Java SemVer v0.9.0 (SemVer 2) [![Build Status](https://travis-ci.org/zafarkhaja/jsemver.png)](https://travis-ci.org/zafarkhaja/jsemver)
============================

Java SemVer is a Java implementation of the Semantic Versioning Specification
(http://semver.org/).

### Versioning ###
Java SemVer is versioned according to the SemVer Specification.

**NOTE**: The current release of the Java SemVer library has a major version of
zero which according to the SemVer p.4 means that the library is under initial
development and its public API should not be considered stable.

### Table of Contents ###
* [Installation](#installation)
* [Usage](#usage)
  * [Creating Versions](#creating-versions)
  * [Incrementing Versions](#incrementing-versions)
  * [Comparing Versions](#comparing-versions)
* [SemVer Expressions API (Ranges)](#semver-expressions-api-ranges)
* [Exception Handling](#exception-handling)
* [Bugs and Features](#bugs-and-features)
* [License](#license)


Installation
------------
To install the Java SemVer libary add the following dependency to your Maven
project.

**Current stable version**
~~~ xml
<dependency>
  <groupId>com.github.zafarkhaja</groupId>
  <artifactId>java-semver</artifactId>
  <version>0.9.0</version>
</dependency>
~~~

**Development version**
~~~ xml
<dependency>
  <groupId>com.github.zafarkhaja</groupId>
  <artifactId>java-semver</artifactId>
  <version>0.10.0-SNAPSHOT</version>
</dependency>
~~~
**NOTE**: To use the development version you need to add the SNAPSHOT repository
to your `pom.xml` file: http://oss.sonatype.org/content/repositories/snapshots/.

Usage
-----
Below are some common use cases for the Java SemVer library.

### Creating Versions ###
The main class of the Java SemVer library is `Version` which implements the
Facade design pattern. By design, the `Version` class is made immutable by
making its constructors package-private, so that it can not be subclassed or
directly instantiated. Instead of public constructors, the `Version` class
provides few _static factory methods_.

One of the methods is the `Version.valueOf` method.

~~~ java
import com.github.zafarkhaja.semver.Version;

Version v = Version.valueOf("1.0.0-rc.1+build.1");

int major = v.getMajorVersion(); // 1
int minor = v.getMinorVersion(); // 0
int patch = v.getPatchVersion(); // 0

String normal     = v.getNormalVersion();     // "1.0.0"
String preRelease = v.getPreReleaseVersion(); // "rc.1"
String build      = v.getBuildMetadata();     // "build.1"

String str = v.toString(); // "1.0.0-rc.1+build.1"
~~~

The other static factory method is `Version.forIntegers` which is also
overloaded to allow fewer arguments.

~~~ java
import com.github.zafarkhaja.semver.Version;

Version v1 = Version.forIntegers(1);
Version v2 = Version.forIntegers(1, 2);
Version v3 = Version.forIntegers(1, 2, 3);
~~~

Another way to create a `Version` is to use a _builder_ class `Version.Builder`.

~~~ java
import com.github.zafarkhaja.semver.Version;

Version.Builder builder = new Version.Builder("1.0.0");
builder.setPreReleaseVersion("rc.1");
builder.setBuildMetadata("build.1");

Version v = builder.build();

int major = v.getMajorVersion(); // 1
int minor = v.getMinorVersion(); // 0
int patch = v.getPatchVersion(); // 0

String normal     = v.getNormalVersion();     // "1.0.0"
String preRelease = v.getPreReleaseVersion(); // "rc.1"
String build      = v.getBuildMetadata();     // "build.1"

String str = v.toString(); // "1.0.0-rc.1+build.1"
~~~

### Incrementing Versions ###
Because the `Version` class is immutable, the _incrementors_ return a new
instance of `Version` rather than modifying the given one. Each of the normal
version incrementors has an overloaded method that takes a pre-release version
as an argument.

~~~ java
import com.github.zafarkhaja.semver.Version;

Version v1 = Version.valueOf("1.2.3");

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
import com.github.zafarkhaja.semver.Version;

// Incrementing the pre-release version
Version v1 = Version.valueOf("1.2.3-rc");        // considered as "rc.0"
Version v2 = v1.incrementPreReleaseVersion();    // "1.2.3-rc.1"
Version v3 = v2.incrementPreReleaseVersion();    // "1.2.3-rc.2"

// Incrementing the build metadata
Version v1 = Version.valueOf("1.2.3-rc+build");  // considered as "build.0"
Version v2 = v1.incrementBuildMetadata();        // "1.2.3-rc+build.1"
Version v3 = v2.incrementBuildMetadata();        // "1.2.3-rc+build.2"
~~~

When incrementing the normal or pre-release versions the build metadata is
always dropped.

~~~ java
import com.github.zafarkhaja.semver.Version;

Version v1 = Version.valueOf("1.2.3-beta+build");

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
Comparing versions with Java SemVer is easy. The `Version` class implements the
`Comparable` interface, it also overrides the `Object.equals` method and provides
some more methods for convenient comparing.

~~~ java
import com.github.zafarkhaja.semver.Version;

Version v1 = Version.valueOf("1.0.0-rc.1+build.1");
Version v2 = Version.valueOf("1.3.7+build.2.b8f12d7");

int result = v1.compareTo(v2);  // < 0
boolean result = v1.equals(v2); // false

boolean result = v1.greaterThan(v2);           // false
boolean result = v1.greaterThanOrEqualTo(v2);  // false
boolean result = v1.lessThan(v2);              // true
boolean result = v1.lessThanOrEqualTo(v2);     // true
~~~

When determining version precedence the build metadata is ignored (SemVer p.10).

~~~ java
import com.github.zafarkhaja.semver.Version;

Version v1 = Version.valueOf("1.0.0+build.1");
Version v2 = Version.valueOf("1.0.0+build.2");

int result = v1.compareTo(v2);  // = 0
boolean result = v1.equals(v2); // true
~~~

Sometimes, however, you might want to compare versions with the build metadata
in mind. For such cases Java SemVer provides a _comparator_ `Version.BUILD_AWARE_ORDER`
and a convenience method `Version.compareWithBuildsTo`.

~~~ java
import com.github.zafarkhaja.semver.Version;

Version v1 = Version.valueOf("1.0.0+build.1");
Version v2 = Version.valueOf("1.0.0+build.2");

int result = Version.BUILD_AWARE_ORDER.compare(v1, v2);  // < 0

int result     = v1.compareTo(v2);            // = 0
boolean result = v1.equals(v2);               // true
int result     = v1.compareWithBuildsTo(v2);  // < 0
~~~


SemVer Expressions API (Ranges)
----------------------
Java SemVer supports the SemVer Expressions API which is implemented as both
internal DSL and external DSL. The entry point for the API are
the `Version.satisfies` methods.

### Internal DSL ###
The internal DSL is implemented by the `CompositeExpression` class using fluent
interface. For convenience, it also provides the `Helper` class with static
helper methods.

~~~ java
import com.github.zafarkhaja.semver.Version;
import static com.github.zafarkhaja.semver.expr.CompositeExpression.Helper.*;

Version v = Version.valueOf("1.0.0-beta");
boolean result = v.satisfies(gte("1.0.0").and(lt("2.0.0")));  // false
~~~

### External DSL ###
The BNF grammar for the external DSL can be found in the corresponding
[issue](https://github.com/zafarkhaja/jsemver/issues/1).

~~~ java
import com.github.zafarkhaja.semver.Version;

Version v = Version.valueOf("1.0.0-beta");
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


Exception Handling
------------------
There are two types of errors that may arrise while using Java SemVer
* `IllegalArgumentException` is thrown when the passed value is `NULL` or empty
  if a method accepts `string` argument or a negative integer if a method accepts
  `int` arguments.
* `ParseException` is thrown by methods that perform parsing of SemVer version
  strings or SemVer Expressions. There are few subtypes of the `ParseException`
  error
  - `UnexpectedCharacterException` is thrown when a SemVer version string contains
    an unexpected or illegal character
  - `LexerException` is thrown when a SemVer Expression contains an illegal character
  - `UnexpectedTokenException` is thrown when an unexpected token is encountered
    during the SemVer Expression parsing


Bugs and Features
-----------------
Bug reports and feature requests can be submitted at https://github.com/zafarkhaja/jsemver/issues.


License
-------
Java SemVer is licensed under the MIT License - see the `LICENSE` file for details.
