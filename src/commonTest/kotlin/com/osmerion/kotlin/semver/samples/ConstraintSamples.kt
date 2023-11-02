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

import com.osmerion.kotlin.semver.SemanticVersion
import com.osmerion.kotlin.semver.constraints.Constraint
import com.osmerion.kotlin.semver.constraints.ConstraintSerializer
import com.osmerion.kotlin.semver.constraints.satisfiedBy
import com.osmerion.kotlin.semver.constraints.satisfiedByAll
import com.osmerion.kotlin.semver.constraints.satisfiedByAny
import com.osmerion.kotlin.semver.constraints.toConstraint
import com.osmerion.kotlin.semver.constraints.toConstraintOrNull
import kotlinx.serialization.json.Json

class ConstraintSamples {
    fun constraint() {
        val constraints = listOf(
            "1.0.0",
            "!=1.0.0",
            "~1.0",
            "^1.x",
            "1.1.0 - 1.2.*",
            ">=1.1.0 <3 || =0.1 || 5 - 6",
            "v1",
            "v3 - v4",
            ">=v2.3"
        )

        constraints.forEach { println("[$it]: [${it.toConstraint()}]") }
    }

    fun parse() {
        print(Constraint.parse(">=1.0.0 || <5.x"))
    }

    fun toConstraint() {
        print(">=1.0".toConstraint())
    }

    fun toConstraintOrNull() {
        println(">=1.2".toConstraintOrNull())
        println(">=1.2a".toConstraintOrNull())
    }

    fun exception() {
        ">=1.2a|^3".toConstraint()
    }

    fun satisfiedBy() {
        val constraint = ">=1.1.0".toConstraint()
        val version = SemanticVersion.parse("1.1.0")
        print("$constraint satisfiedBy $version? ${constraint satisfiedBy version}")
    }

    fun satisfiedByAll() {
        val constraint = ">=1.1.0".toConstraint()
        val versions = listOf("1.1.0", "1.2.0").map(SemanticVersion::parse)
        print("$constraint satisfied by ${versions.joinToString(" and ")}? ${constraint satisfiedByAll versions}")
    }

    fun satisfiedByAny() {
        val constraint = ">=1.1.0".toConstraint()
        val versions = listOf("1.1.0", "1.0.0").map(SemanticVersion::parse)
        print("$constraint satisfied by ${versions.joinToString(" or ")}? ${constraint satisfiedByAny versions}")
    }

    fun serialization() {
        print(Json.encodeToString(ConstraintSerializer, ">1.2".toConstraint()))
    }

    fun deserialization() {
        val decoded = Json.decodeFromString(ConstraintSerializer, "\">1.2\"")
        print(decoded)
    }
}
