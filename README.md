Java SemVer v0.5.0 [![Build Status](https://travis-ci.org/zafarkhaja/java-semver.png)](https://travis-ci.org/zafarkhaja/java-semver)
==================
Java SemVer is a Java implementation of the Semantic Versioning Specification 
(http://semver.org/).

**NOTE**: The current version of the Java SemVer corresponds to the Semantic 
Versioning 2.0.0-rc.1.

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
String build      = v.getBuildVersion();      // "build.1"

String str = v.toString(); // "1.0.0-rc.1+build.1"
```

### Incrementing Versions ###
Because the `Version` class is immutable, the _incrementors_ return a new 
instance of `Version` rather than modifying the given one.

```java
import com.github.zafarkhaja.semver.Version;

Version v1 = Version.valueOf("1.2.3");

Version v2 = v1.incrementMajorVersion();
String str = v2.toString(); // "2.0.0"

Version v3 = v1.incrementMinorVersion();
String str = v3.toString(); // "1.3.0"

Version v4 = v1.incrementPatchVersion();
String str = v4.toString(); // "1.2.4"

String str = v1.toString(); // "1.2.3"
```

### Comparing Versions ###
Comparing versions with Java SemVer is easy. The `Version` class implements the 
`Comparable` interface, it also overrides the `Object.equals(Object obj)` method 
and provides some more methods for convenient comparing.

```java
import com.github.zafarkhaja.semver.Version;

Version v1 = Version.valueOf("1.0.0-rc.1+build.1");
Version v2 = Version.valueOf("1.3.7+build.2.b8f12d7");

int result = v1.compareTo(v2); // (result < 0)

boolean result = v1.equals(v2); // (result == false)

boolean result = v1.greaterThan(v2);           // (result == false)
boolean result = v1.greaterThanOrEqualsTo(v2); // (result == false)
boolean result = v1.lessThan(v2);              // (result == true)
boolean result = v1.lessThanOrEqualsTo(v2);    // (result == true)
```

Issues
------
As of the moment, there is an ambiguity, at least for me, on what should be the 
behavior of pre-release and build versions when the normal version numbers are 
incremented. See the discussion page https://github.com/mojombo/semver/issues/60.

TODO
----
* implement ranges

License
-------
Java SemVer is licensed under the MIT License - see the `LICENSE` file for details.
