Java SemVer v0.7.0 [![Build Status](https://travis-ci.org/zafarkhaja/java-semver.png)](https://travis-ci.org/zafarkhaja/java-semver)
==================

Java SemVer is a Java implementation of the Semantic Versioning Specification
(http://semver.org/).

**NOTE**: The current version of the Java SemVer corresponds to the Semantic
Versioning 2.0.0-rc.2.


Versioning
----------
Java SemVer is versioned according to the SemVer Specification.

**NOTE**: The current release of the Java SemVer library has a major version of
zero which according to the SemVer p.4 means that the library is under initial
development and its public API should not be considered stable.


Usage
-----
Below are some common use cases for the Java SemVer library.

### Creating Versions ###
Java SemVer library is composed of one small package which contains a few
classes. All the classes but one are package-private and are not accessible
outside the package. The only public class is `Version` which acts as a
_facade_ for the client code. By design, the `Version` class is made immutable
by making its constructor package-private, so that it can not be subclassed or
directly instantiated. Instead of public constructor, the `Version` class
provides a _static factory method_, `Version.valueOf(String value)`.

```java
import com.github.zafarkhaja.semver.Version;

Version v = Version.valueOf("1.0.0-rc.1+build.1");

int major = v.getMajorVersion(); // 1
int minor = v.getMinorVersion(); // 0
int patch = v.getPatchVersion(); // 0

String normal     = v.getNormalVersion();     // "1.0.0"
String preRelease = v.getPreReleaseVersion(); // "rc.1"
String build      = v.getBuildMetadata();     // "build.1"

String str = v.toString(); // "1.0.0-rc.1+build.1"
```

Another way to create a `Version` is to use a _builder_ class `Version.Builder`.

```java
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
```

### Incrementing Versions ###
Because the `Version` class is immutable, the _incrementors_ return a new
instance of `Version` rather than modifying the given one. Each of the normal
version incrementors has an overloaded method that takes a pre-release version
as an argument.

```java
import com.github.zafarkhaja.semver.Version;

Version v1 = Version.valueOf("1.2.3");

// Incrementing major version
Version v2 = v1.incrementMajorVersion();        // "2.0.0"
Version v2 = v1.incrementMajorVersion("alpha"); // "2.0.0-alpha"

// Incrementing minor version
Version v3 = v1.incrementMinorVersion();        // "1.3.0"
Version v3 = v1.incrementMinorVersion("alpha"); // "1.3.0-alpha"

// Incrementing patch version
Version v4 = v1.incrementPatchVersion();        // "1.2.4"
Version v4 = v1.incrementPatchVersion("alpha"); // "1.2.4-alpha"

// Original Version is still the same
String str = v1.toString(); // "1.2.3"
```

There are also incrementor methods for pre-release version and build metadata.

```java
import com.github.zafarkhaja.semver.Version;

// Incrementing pre-release version
Version v1 = Version.valueOf("1.2.3-rc");        // considered as "rc.0"
Version v2 = v1.incrementPreReleaseVersion();    // "1.2.3-rc.1"
Version v3 = v2.incrementPreReleaseVersion();    // "1.2.3-rc.2"

// Incrementing build metadata
Version v1 = Version.valueOf("1.2.3-rc+build");  // considered as "build.0"
Version v2 = v1.incrementBuildMetadata();        // "1.2.3-rc+build.1"
Version v3 = v2.incrementBuildMetadata();        // "1.2.3-rc+build.2"
```

When incrementing normal or pre-release versions build metadata is always dropped.

```java
import com.github.zafarkhaja.semver.Version;

Version v1 = Version.valueOf("1.2.3-beta+build");

// Incrementing normal version
Version v2 = v1.incrementMajorVersion();        // "2.0.0"
Version v2 = v1.incrementMajorVersion("alpha"); // "2.0.0-alpha"

Version v3 = v1.incrementMinorVersion();        // "1.3.0"
Version v3 = v1.incrementMinorVersion("alpha"); // "1.3.0-alpha"

Version v4 = v1.incrementPatchVersion();        // "1.2.4"
Version v4 = v1.incrementPatchVersion("alpha"); // "1.2.4-alpha"

// Incrementing pre-release version
Version v2 = v1.incrementPreReleaseVersion();   // "1.2.3-beta.1"
```
**NOTE**: The discussion page https://github.com/mojombo/semver/issues/60 might
be of good use in better understanding some of the decisions made regarding the 
incrementor methods.

### Comparing Versions ###
Comparing versions with Java SemVer is easy. The `Version` class implements the
`Comparable` interface, it also overrides the `Object.equals(Object obj)` method
and provides some more methods for convenient comparing.

```java
import com.github.zafarkhaja.semver.Version;

Version v1 = Version.valueOf("1.0.0-rc.1+build.1");
Version v2 = Version.valueOf("1.3.7+build.2.b8f12d7");

int result = v1.compareTo(v2);  // < 0
boolean result = v1.equals(v2); // false

boolean result = v1.greaterThan(v2);           // false
boolean result = v1.greaterThanOrEqualTo(v2);  // false
boolean result = v1.lessThan(v2);              // true
boolean result = v1.lessThanOrEqualTo(v2);     // true
```

When determining version precedence build metadata is ignored (SemVer p.10).

```java
import com.github.zafarkhaja.semver.Version;

Version v1 = Version.valueOf("1.0.0+build.1");
Version v2 = Version.valueOf("1.0.0+build.2");

int result = v1.compareTo(v2);  // = 0
boolean result = v1.equals(v2); // true
```

Sometimes, however, you might want to compare versions with build metadata in
mind. For such cases Java SemVer provides a _comparator_ `Version.BUILD_AWARE_ORDER`
and a convenience method `Version.compareWithBuildsTo(Version other)`.

```java
import com.github.zafarkhaja.semver.Version;

Version v1 = Version.valueOf("1.0.0+build.1");
Version v2 = Version.valueOf("1.0.0+build.2");

int result = Version.BUILD_AWARE_ORDER.compare(v1, v2);  // < 0

int result = v1.compareWithBuildsTo(v2);  // < 0
boolean result = v1.equals(v2);           // false
```


TODO
----
* [Implement ranges](https://github.com/zafarkhaja/java-semver/issues/1)
* [Write doc comments for all API classes and methods](https://github.com/zafarkhaja/java-semver/issues/2)


Bugs and Features
-----------------
Bug reports and feature requests can be submitted at https://github.com/zafarkhaja/java-semver/issues.


License
-------
Java SemVer is licensed under the MIT License - see the `LICENSE` file for details.
