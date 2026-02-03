# kotlin-semver

[![License](https://img.shields.io/badge/license-MIT-green.svg?style=for-the-badge&label=License)](https://github.com/Osmerion/kotlin-semver/blob/master/LICENSE)
[![Maven Central](https://img.shields.io/maven-central/v/com.osmerion.kotlin-semver/kotlin-semver.svg?style=for-the-badge&label=Maven%20Central)](https://maven-badges.herokuapp.com/maven-central/com.osmerion.kotlin-semver/kotlin-semver)
![Kotlin](https://img.shields.io/badge/Kotlin-2%2E2-green.svg?style=for-the-badge&color=a97bff&logo=Kotlin)
![Java](https://img.shields.io/badge/Java-17-green.svg?style=for-the-badge&color=b07219&logo=Java)

A [Kotlin Multiplatform](https://kotlinlang.org/docs/mpp-intro.html)
implementation of the [Semantic Versioning 2.0.0](https://semver.org/spec/v2.0.0.html)
specification with support for **parsing**, **comparing** and **incrementing**
semantic versions accompanied by support for **version constraints**.

The library is fully written in common Kotlin code. Prebuilt binaries are
available for JVM (Java 17 or later), Wasm, and all native targets.[^1][^2]

[^1]: Since this library does not rely on any platform-specific APIs, we aim to
      provide prebuilt libraries for all native targets supported by Kotlin.
      Despite that, some targets may be missing as target support may change
      over time. In case something is missing, please make sure to let us know.

[^2]: This library does not yet support Kotlin/JS as it would currently require
      ES2018 features whereas the Kotlin compiler only supports targeting ES5
      and ES2015.

This project was forked from https://github.com/z4kn4fein/kotlin-semver. The
fork gives up a few idiomatic Kotlin design decisions to provide a significantly
improved Java interoperability.


## Versions

The `SemanticVersion` class represents a full semantic version. A
`SemanticVersion` can be constructed using either manually, part by part:

```kotlin
val version = SemanticVersion(major = 3, minor = 5, patch = 2, preRelease = "alpha", buildMetadata = "build")
println(version) // 3.5.2-alpha+build
```

Or parsed from text (i.e. a `String`):

```kotlin
val version = SemanticVersion.parse("3.5.2-alpha+build")
println(version) // 3.5.2-alpha+build
```


## Constraints

In addition to exact versions, the library provides the `VersionConstraint`
class to test if a version satisfies a set of rules. Version constraints are
commonly used to specify dependencies.

As there is no standard for version constraints in SemVer, the library supports
multiple constraint formats and even provides (experimental) APIs to define
custom constraint formats.

### Maven

The `MavenConstraintFormat` implements the constraint format used by [Apache Maven](https://maven.apache.org/enforcer/enforcer-rules/versionRanges.html).

```kotlin
val constraint = VersionConstraint.parse("[1.0,)", format = NpmConstraintFormat)
val version = SemanticVersion(1, 2, 3)

assertTrue(version satisfies constraint)
```


### NPM

The `NpmConstraintFormat` implements the constraint format used by [npm](https://github.com/npm/node-semver).

```kotlin
val constraint = VersionConstraint.parse("^1.2", format = NpmConstraintFormat)
val version = SemanticVersion(1, 2, 3)

assertTrue(version satisfies constraint)
```


## Building from source

### Setup

This project uses [Gradle's toolchain support](https://docs.gradle.org/current/userguide/toolchains.html)
to detect and select the JDKs required to run the build. Please refer to the
build scripts to find out which toolchains are requested.

An installed JDK 17 (or later) is required to use Gradle.

### Building

Once the setup is complete, invoke the respective Gradle tasks using the
following command on Unix/macOS:

    ./gradlew <tasks>

or the following command on Windows:

    gradlew <tasks>

Important Gradle tasks to remember are:
- `clean`                   - clean build results
- `build`                   - assemble and test the project
- `publishToMavenLocal`     - build and install all public artifacts to the
                              local maven repository

Additionally `tasks` may be used to print a list of all available tasks.


## License

```
Copyright (c) 2022 Peter Csajtai
Copyright (c) 2023-2026 Leon Linhart

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
