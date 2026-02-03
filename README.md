# kotlin-semver

[![License](https://img.shields.io/badge/license-MIT-green.svg?style=for-the-badge&label=License)](https://github.com/Osmerion/kotlin-semver/blob/master/LICENSE)
[![Maven Central](https://img.shields.io/maven-central/v/com.osmerion.kotlin-semver/kotlin-semver.svg?style=for-the-badge&label=Maven%20Central)](https://maven-badges.herokuapp.com/maven-central/com.osmerion.kotlin-semver/kotlin-semver)
![Kotlin](https://img.shields.io/badge/Kotlin-2%2E2-green.svg?style=for-the-badge&color=a97bff&logo=Kotlin)
![Java](https://img.shields.io/badge/Java-17-green.svg?style=for-the-badge&color=b07219&logo=Java)

A [Kotlin Multiplatform](https://kotlinlang.org/docs/mpp-intro.html) implementation of the [Semantic Versioning 2.0.0](https://semver.org/spec/v2.0.0.html)
specification with support for **parsing**, **comparing** and **incrementing** semantic versions accompanied by support
for **version constraints**.

The library is fully written in common Kotlin code. Prebuilt binaries are available for JVM (Java 17 or later), Wasm,
and all native targets.[^1][^2]

[^1]: Since this library does not rely on any platform-specific APIs, we aim to provide prebuilt libraries for all
      native targets supported by Kotlin. Despite that, some targets may be missing as target support may change over
      time. In case something is missing, please make sure to let us know.

[^2]: This library does not yet support Kotlin/JS as it would currently require ES2018 features whereas the Kotlin
      compiler only supports targeting ES5 and ES2015.

This project was forked from https://github.com/z4kn4fein/kotlin-semver. The fork gives up a few idiomatic Kotlin design
decisions to provide a significantly improved Java interoperability and a significantly more flexible API for
constraints.


## Versions

The `Version` class represents a semantic versions.

Versions can be constructed programmatically, part by part.

      Version(major = 3, minor = 5, patch = 2, preRelease = "alpha", buildMetadata = "build")

Alternatively, they can also be parsed from text:

      Version.parse("3.5.2-alpha+build")

### Comparing versions

`Version` implements `Comparable` following the specification which enables easy comparisons of two versions.

```kotlin
Version.parse("0.1.0") < Version.parse("0.1.1")                 // true
Version.parse("0.1.1") <= Version.parse("0.1.1")                // true
Version.parse("0.1.0-alpha.3") < Version.parse("0.1.0-alpha.4") // true

Version.parse("0.1.1").compareTo(Version.parse("0.1.0"))        //  1
Version.parse("0.1.0").compareTo(Version.parse("0.1.1"))        // -1
Version.parse("0.1.1").compareTo(Version.parse("0.1.1"))        //  0
```


## Constraints

In addition to exact versions, the library provides the `VersionConstraint` class to test if a version satisfies a set
of rules. Version constraints are commonly used to specify dependencies.

A constraint can be parsed from text by specifying the format the constraint is specified in.

```kotlin
VersionConstraint.parse(">=1.2.0", format = NpmConstraintFormat)
```

A version can then be tested against a constraint to determine if the constraint is satisfied by that version.

```kotlin
val constraint = VersionConstraint.parse(">=1.2.0", format = NpmConstraintFormat)
val version = Version.parse("1.2.1")

constraint isSatisfiedBy version    // true
version satisfies constraint        // true
```

### Constraints under the hood

Constraints can be thought of as formulas in [disjunctive normal form](https://en.wikipedia.org/wiki/Disjunctive_normal_form).
They are a disjunction of conjunctions of _comparators_. A comparator compares a version against a reference version to
test if the given version is less than, greater than or equal, equal, or not equal to the reference. This allows
construction of a set of disjoint version ranges in such a way that the existence of a version in one of the version
ranges is equivalent to the fulfillment of the constraint.


## Constraint Formats

As there is no standard for version constraints in SemVer, the library supports
multiple constraint formats and even provides (experimental) APIs to define
custom constraint formats.

### Maven

The `MavenConstraintFormat` implements the constraint format used by [Apache Maven](https://maven.apache.org/enforcer/enforcer-rules/versionRanges.html).

Constraints in this format consist of one or more ranges split by commas (`','`).
The syntax for ranges is loosely based on mathematical notations for intervals.

| Constraint          | Ranges                   |
|---------------------|--------------------------|
| `1.0`               | `>=1.0.0` (prefer 1.0.0) |
| `1.0.0`             | `>=1.0.0` (prefer 1.0.0) |
| `(,1.0.0]`          | `<=1.0.0`                |
| `(,1.0.0)`          | `<1.0.0`                 |
| `[1.0.0]`           | `>=1.0.0 && <1.0.1`      |
| `[1.0.0,)`          | `>=1.0.0`                |
| `(1.0.0,)`          | `>1.0.0`                 |
| `[1.0.0,2.0.0]`     | `>=1.0.0 && <=2.0.0`     |
| `(1.0.0,2.0.0)`     | `>1.0.0 && <2.0.0`       |
| `(,1.2.3),(1.2.3,)` | `<1.2.3 \|\| >1.2.3`     |


### NPM

The `NpmConstraintFormat` implements the constraint format used by [npm](https://github.com/npm/node-semver).

Constraints in this format consist of one or more ranges split b


#### Descriptors

A _descriptor_ is a potentially incomplete specification of a semantic version. The subset of descriptors that specify
a valid semantic version (e.g. `1.2.3`) is considered _complete_.

Descriptors that don't specify components by either omitting them (e.g. `1.2`, `1`) or by using one of `X`, `x`, `*` as
"stand in" (e.g. `1.2.x`, `1.x`) are considered _incomplete_.

How incomplete descriptors are resolved to versions differs based on where they are used. Generally, lower bounds "fill"
missing components with zeros whereas upper bounds allow flexibility in unspecified components.


#### Comparator Ranges

      <op>? <descriptor>

A comparator is composed of an operator and a descriptor. Supported operators are:
- `<` Less than
- `<=` Less than or equal to
- `>` Greater than
- `>=` Greater than or equal to
- `=` Equal to

If no operator is specified and the descriptor is a complete version, the `=` operator is assumed.

Comparators allow changes by comparing a proposed version against the reference version that derived from the descriptor
under consideration of the operator.

- `<=1.0.0` := `<1.0.1-0`
- `>1.0.0` := `>=1.0.1-0`
- `=1.0.0` := `>=1.0.0 <1.0.1-0`
- `>=1.x` := `>=2.0.0`
- `<1.x` := `<1.0.0`


#### X-Ranges

      <descriptor>

X-ranges are freestanding, incomplete descriptors. X-Ranges allow changes that do not modify the leftmost specified
component.

- `""` (empty) := `>=0.0.0`
- `1` := `>=1.0.0 <2.0.0-0`
- `1.2` := `>=1.2.0 <1.3.0-0`


#### Hyphen Ranges

      <descriptor> '-' <descriptor>

Hyphen range specify an inclusive interval of permitted versions.

- `1.2.3 - 2.3.4` := `>=1.2.3 <2.3.5-0`


#### Tilde Ranges

      '~' '>'? <descriptor>

Tilde ranges allow patch-level changes if the descriptor specifies a minor version, or minor-level changes otherwise.

- `~1.2.3` := `>=1.2.3 <1.(2+1).0` := `>=1.2.3 <1.3.0-0` 
- `~1.2` := `>=1.2.0 <0.(2+1).0` := `>=1.2.0 <1.3.0-0`
- `~1` := `>=1.0.0 <(1+1).0.0` := `>=1.0.0 <2.0.0-0`
- `~0.2.3` := `>=0.2.3 <0.(2+1).0` := `>=0.2.3 <0.3.0-0`
- `~0.2` := `>=0.2.0 <0.(2+1).0` := `>=0.2.0 <0.3.0-0`
- `~0` := `>=0.0.0 <(0+1).0.0` := `>=0.0.0 <1.0.0-0`

#### Caret Ranges

      '^' <descriptor>

Caret ranges allow changes that do not modify the left-most non-zero element in the version tuple. Put simple, caret
ranges lock versions larger than `1.0.0` to their major version, versions `0.X` where `X > 1` to their minor versions
and versions `0.0.X` to their patch version.

- `^1.2.3` := `>=1.2.3 <2.0.0-0`
- `^1.2` := `>=1.2.0 <2.0.0-0`
- `^1` := `>=1.0.0 <2.0.0-0`
- `^0.2.3` := `>=0.2.3 <0.3.0-0`
- `^0.0.3` := `>=0.0.3 <0.0.4-0`

Omitted `minor` and `patch` components in caret ranges are treated as `0` in the lower bound but are handled as non-zero
value when determining the upper bound. That is, the leftmost omitted value will always allow changes to its component
(including in `0.x` and `0.0.x` versions).

- `^0.x` := `>=0.0.0 <1.0.0-0`
- `^0.0.x` := `>=0.0.0 <0.1.0-0`


## Building from source

### Setup

This project uses [Gradle's toolchain support](https://docs.gradle.org/current/userguide/toolchains.html) to detect and
select the JDKs required to run the build. Please refer to the build scripts to find out which toolchains are requested.

An installed JDK 17 (or later) is required to use Gradle.

### Building

Once the setup is complete, invoke the respective Gradle tasks using the following command on Unix/macOS:

    ./gradlew <tasks>

or the following command on Windows:

    gradlew <tasks>

Important Gradle tasks to remember are:
- `clean`                   - clean build results
- `build`                   - assemble and test the project
- `publishToMavenLocal`     - build and install all public artifacts to the local maven repository

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
