### 0.1.0

_Released 2026 Feb 03_

#### Overview

A [Kotlin Multiplatform](https://kotlinlang.org/docs/mpp-intro.html) implementation of the [Semantic Versioning 2.0.0](https://semver.org/spec/v2.0.0.html)
specification with support for **parsing**, **comparing** and **incrementing** semantic versions accompanied by support
for **version constraints**.

The library is fully written in common Kotlin code. Prebuilt binaries are available for JVM (Java 17 or later), Wasm,
and all native targets.

This project was forked from https://github.com/z4kn4fein/kotlin-semver. The fork gives up a few idiomatic Kotlin design
decisions to provide a significantly improved Java interoperability and a significantly more flexible API for
constraints.
