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
package com.osmerion.kotlin.semver.internal.constraints

import com.osmerion.kotlin.semver.ConstraintFormatException
import com.osmerion.kotlin.semver.SemanticVersion
import com.osmerion.kotlin.semver.SemanticVersionConstraint

internal data class VersionDescriptor(
    val majorString: String,
    val minorString: String?,
    val patchString: String?,
    val preRelease: String? = null,
    val buildMetadata: String? = null
) {
    override fun toString(): String {
        return majorString +
            (minorString?.let { ".$minorString" } ?: "") +
            (patchString?.let { ".$patchString" } ?: "") +
            (preRelease?.let { "-$preRelease" } ?: "") +
            (buildMetadata?.let { "+$buildMetadata" } ?: "")
    }

    val isMajorWildcard: Boolean = wildcards.contains(majorString)
    val isMinorWildcard: Boolean = minorString?.let { wildcards.contains(it) } ?: true
    val isPatchWildcard: Boolean = patchString?.let { wildcards.contains(it) } ?: true

    val isWildcard: Boolean = isMajorWildcard || isMinorWildcard || isPatchWildcard

    val major: Int get() = majorString.toIntOrNull()
        ?: throw ConstraintFormatException("Invalid MAJOR number in: $this")

    val minor: Int get() = minorString?.toIntOrNull()
        ?: throw ConstraintFormatException("Invalid MINOR number in: $this")

    val patch: Int get() = patchString?.toIntOrNull()
        ?: throw ConstraintFormatException("Invalid PATCH number in: $this")

    fun toComparator(operator: Op = Op.EQUAL): VersionComparator {
        return when {
            isMajorWildcard ->
                when (operator) {
                    Op.GREATER_THAN, Op.LESS_THAN, Op.NOT_EQUAL ->
                        Condition(Op.LESS_THAN, VersionComparator.min.copy(preRelease = ""))
                    else -> VersionComparator.greaterThanMin
                }
            isMinorWildcard -> {
                val version = SemanticVersion(major = major, preRelease = preRelease, buildMetadata = buildMetadata)
                Range(
                    start = Condition(Op.GREATER_THAN_OR_EQUAL, version),
                    end = Condition(Op.LESS_THAN, version.toNextMajor(preRelease = "")),
                    operator
                )
            }
            isPatchWildcard -> {
                val version =
                    SemanticVersion(major = major, minor = minor, preRelease = preRelease, buildMetadata = buildMetadata)
                Range(
                    start = Condition(Op.GREATER_THAN_OR_EQUAL, version),
                    end = Condition(Op.LESS_THAN, version.toNextMinor(preRelease = "")),
                    operator
                )
            }
            else ->
                Condition(
                    operator,
                    SemanticVersion(major = major, minor = minor, patch = patch, preRelease, buildMetadata)
                )
        }
    }

    companion object {
        private val wildcards = arrayOf("*", "x", "X")
    }
}
