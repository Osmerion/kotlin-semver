/*
 * Copyright (c) 2022 Peter Csajtai
 * Copyright (c) 2023 Leon Linhart
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
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.osmerion.kotlin.semver.samples

import com.osmerion.kotlin.semver.LooseVersionSerializer
import com.osmerion.kotlin.semver.SemanticVersion
import com.osmerion.kotlin.semver.VersionSerializer
import com.osmerion.kotlin.semver.constraints.toConstraint
import com.osmerion.kotlin.semver.inc
import com.osmerion.kotlin.semver.nextMajor
import com.osmerion.kotlin.semver.nextMinor
import com.osmerion.kotlin.semver.nextPatch
import com.osmerion.kotlin.semver.nextPreRelease
import com.osmerion.kotlin.semver.satisfies
import com.osmerion.kotlin.semver.satisfiesAll
import com.osmerion.kotlin.semver.satisfiesAny
import com.osmerion.kotlin.semver.withoutSuffixes
import kotlinx.serialization.json.Json

class VersionSamples {
    fun explode() {
        val version = SemanticVersion.parse("1.2.3-alpha.1+build.1")
        println("Version: $version")
        println("Major: ${version.major}, Minor: ${version.minor}, Patch: ${version.patch}")
        println("Pre-release: ${version.preRelease}")
        println("Build metadata: ${version.buildMetadata}")
        println("Is it pre-release? ${version.isPreRelease}")
        println("Is it stable? ${version.isStable}")

        // equality
        println("Is 1.0.0 == 1.0.0? ${SemanticVersion.parse("1.0.0") == SemanticVersion.parse("1.0.0")}")
        println("Is 1.0.0 == 1.0.1? ${SemanticVersion.parse("1.0.0") == SemanticVersion.parse("1.0.1")}")

        // comparison
        println("Is 1.0.1 > 1.0.0? ${SemanticVersion.parse("1.0.1") > SemanticVersion.parse("1.0.0")}")
        println("Is 1.0.0-alpha.1 > 1.0.0-alpha.0? ${SemanticVersion.parse("1.0.0-alpha.1") > SemanticVersion.parse("1.0.0-alpha.0")}")

        // range
        println("Is 1.0.1 in 1.0.0 .. 1.0.2? ${SemanticVersion.parse("1.0.1") in SemanticVersion.parse("1.0.0")..SemanticVersion.parse("1.0.2")}")

        // destructuring
        print("Destructuring: ")
        val (major, minor, patch, preRelease, build) = SemanticVersion.parse("1.0.0-alpha+build")
        print("$major $minor $patch $preRelease $build")
    }

    fun parseStrict() {
        println(SemanticVersion.parse("1.0.0-alpha.1+build.1"))
    }

    fun parseLoose() {
        println(SemanticVersion.parse("v1.0-alpha.1+build.1", strict = false))
        println(SemanticVersion.parse("1-alpha", strict = false))
        println(SemanticVersion.parse("2", strict = false))
    }

    fun exception() {
        SemanticVersion.parse("1.0.a")
    }

    fun preReleaseException() {
        SemanticVersion.parse("1.0.1").nextPatch(preRelease = "alpha.01")
    }

    fun construct() {
        println(SemanticVersion(major = 1, preRelease = "alpha"))
        println(SemanticVersion(major = 1, minor = 1, buildMetadata = "build"))
    }

    fun toVersionStrict() {
        print(SemanticVersion.parse("1.0.0-alpha.1+build.1"))
    }

    fun toVersionLoose() {
        println(SemanticVersion.parse("v1.0.0-alpha.1+build.1", strict = false))
        println(SemanticVersion.parse("v1-alpha", strict = false))
        println(SemanticVersion.parse("2", strict = false))
    }

    fun toVersionOrNullStrict() {
        println(SemanticVersion.tryParse("1.0.0-alpha.1+build.1"))
        println(SemanticVersion.tryParse("1.1.a"))
        println(SemanticVersion.tryParse("v1.1.0"))
        println(SemanticVersion.tryParse("1.1"))
    }

    fun toVersionOrNullLoose() {
        println(SemanticVersion.tryParse("v1.1.0", strict = false))
        println(SemanticVersion.tryParse("1.1-alpha.1+build.1", strict = false))
        println(SemanticVersion.tryParse("1", strict = false))
        println(SemanticVersion.tryParse("v1", strict = false))
    }

    fun copy() {
        val version = SemanticVersion.parse("1.0.0-alpha.1")
        print(version.copy(minor = 1, preRelease = "beta.0"))
    }

    fun inc() {
        val version = SemanticVersion.parse("1.0.0-alpha.1")
        println(version.inc(by = com.osmerion.kotlin.semver.Inc.MAJOR))
        println(version.inc(by = com.osmerion.kotlin.semver.Inc.MINOR))
        println(version.inc(by = com.osmerion.kotlin.semver.Inc.PATCH))
        println(version.inc(by = com.osmerion.kotlin.semver.Inc.PRE_RELEASE))

        println(version.inc(by = com.osmerion.kotlin.semver.Inc.MAJOR, preRelease = ""))
        println(version.inc(by = com.osmerion.kotlin.semver.Inc.MINOR, preRelease = "beta"))
    }

    fun nextMajor() {
        val version = SemanticVersion.parse("1.0.0-alpha.1")
        println(version.nextMajor())
        println(version.nextMajor(preRelease = ""))
        println(version.nextMajor(preRelease = "alpha"))
        println(version.nextMajor(preRelease = "SNAPSHOT"))
    }

    fun nextMinor() {
        val version = SemanticVersion.parse("1.0.0-alpha.1")
        println(version.nextMinor())
        println(version.nextMinor(preRelease = ""))
        println(version.nextMinor(preRelease = "alpha"))
        println(version.nextMinor(preRelease = "SNAPSHOT"))
    }

    fun nextPatch() {
        val version = SemanticVersion.parse("1.0.0-alpha.1")
        println(version.nextPatch())
        println(version.nextPatch(preRelease = ""))
        println(version.nextPatch(preRelease = "alpha"))
        println(version.nextPatch(preRelease = "SNAPSHOT"))
    }

    fun nextPreRelease() {
        val version = SemanticVersion.parse("1.0.0-alpha.1")
        println(version.nextPreRelease())
        println(version.nextPreRelease(preRelease = ""))
        println(version.nextPreRelease(preRelease = "alpha"))
        println(version.nextPreRelease(preRelease = "SNAPSHOT"))
    }

    fun min() {
        print(SemanticVersion.min)
    }

    fun satisfies() {
        val constraint = ">=1.1.0".toConstraint()
        val version = SemanticVersion.parse("1.1.1")
        print("$version satisfies $constraint? ${version satisfies constraint}")
    }

    fun satisfiesAll() {
        val constraints = listOf(">=1.1.0", "~1").map { it.toConstraint() }
        val version = SemanticVersion.parse("1.1.1")
        print("$version satisfies ${constraints.joinToString(" and ")}? ${version satisfiesAll constraints}")
    }

    fun satisfiesAny() {
        val constraints = listOf(">=1.1.0", "~1").map { it.toConstraint() }
        val version = SemanticVersion.parse("1.1.1")
        print("$version satisfies ${constraints.joinToString(" or ")}? ${version satisfiesAny constraints}")
    }

    fun withoutSuffixes() {
        val version = SemanticVersion.parse("1.0.0-alpha.1+build")
        print(version.withoutSuffixes())
    }

    fun serialization() {
        print(Json.encodeToString(VersionSerializer, SemanticVersion.parse("1.0.0-alpha.1+build")))
    }

    fun deserialization() {
        val decoded = Json.decodeFromString(VersionSerializer, "\"1.0.0-alpha.1+build\"")

        println(decoded.major)
        println(decoded.minor)
        println(decoded.patch)
        println(decoded.preRelease)
        println(decoded.buildMetadata)
    }

    fun looseSerialization() {
        print(Json.encodeToString(LooseVersionSerializer, SemanticVersion.parse("1-alpha.1+build", strict = false)))
    }

    fun looseDeserialization() {
        val decoded = Json.decodeFromString(LooseVersionSerializer, "\"1+build.3\"")

        println(decoded.major)
        println(decoded.minor)
        println(decoded.patch)
        println(decoded.preRelease)
        println(decoded.buildMetadata)
    }
}
