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

import com.osmerion.kotlin.semver.SemanticVersion

internal interface ComparatorBuilder {
    val acceptedOperators: Array<String>
    fun buildComparator(operatorString: String, versionDescriptor: VersionDescriptor): VersionComparator
}

internal class RegularComparatorBuilder : ComparatorBuilder {
    override val acceptedOperators: Array<String> = arrayOf("=", "!=", ">", ">=", "=>", "<", "<=", "=<", "")

    override fun buildComparator(operatorString: String, versionDescriptor: VersionDescriptor): VersionComparator =
        versionDescriptor.toComparator(operatorString.toOperator())
}

internal class TildeComparatorBuilder : ComparatorBuilder {
    override val acceptedOperators: Array<String> = arrayOf("~>", "~")

    override fun buildComparator(operatorString: String, versionDescriptor: VersionDescriptor): VersionComparator =
        when {
            versionDescriptor.isWildcard -> versionDescriptor.toComparator()
            else -> {
                val version = SemanticVersion(
                    versionDescriptor.major,
                    versionDescriptor.minor,
                    versionDescriptor.patch,
                    versionDescriptor.preRelease,
                    versionDescriptor.buildMetadata
                )
                Range(
                    start = Condition(Op.GREATER_THAN_OR_EQUAL, version),
                    end = Condition(Op.LESS_THAN, version.toNextMinor(preRelease = "")),
                    Op.EQUAL
                )
            }
        }
}

internal class CaretComparatorBuilder : ComparatorBuilder {
    override val acceptedOperators: Array<String> = arrayOf("^")

    override fun buildComparator(operatorString: String, versionDescriptor: VersionDescriptor): VersionComparator =
        when {
            versionDescriptor.isMajorWildcard -> VersionComparator.greaterThanMin
            versionDescriptor.isMinorWildcard -> fromMinorWildcardCaret(versionDescriptor)
            versionDescriptor.isPatchWildcard -> fromPatchWildcardCaret(versionDescriptor)
            else -> {
                val version = SemanticVersion(
                    versionDescriptor.major,
                    versionDescriptor.minor,
                    versionDescriptor.patch,
                    versionDescriptor.preRelease,
                    versionDescriptor.buildMetadata
                )
                val endVersion = when {
                    versionDescriptor.majorString != "0" -> version.toNextMajor(preRelease = "")
                    versionDescriptor.minorString != "0" -> version.toNextMinor(preRelease = "")
                    versionDescriptor.patchString != "0" -> version.toNextPatch(preRelease = "")
                    else -> SemanticVersion(patch = 1, preRelease = "") // ^0.0.0 -> <0.0.1-0
                }
                Range(
                    start = Condition(Op.GREATER_THAN_OR_EQUAL, version),
                    end = Condition(Op.LESS_THAN, endVersion),
                    Op.EQUAL
                )
            }
        }

    private fun fromMinorWildcardCaret(versionDescriptor: VersionDescriptor): VersionComparator =
        when (versionDescriptor.majorString) {
            "0" ->
                Range(
                    VersionComparator.greaterThanMin,
                    Condition(Op.LESS_THAN, SemanticVersion(major = 1, preRelease = "")),
                    Op.EQUAL
                )
            else -> versionDescriptor.toComparator()
        }

    private fun fromPatchWildcardCaret(versionDescriptor: VersionDescriptor): VersionComparator =
        when {
            versionDescriptor.majorString == "0" && versionDescriptor.minorString == "0" ->
                Range(
                    VersionComparator.greaterThanMin,
                    Condition(Op.LESS_THAN, SemanticVersion(minor = 1, preRelease = "")),
                    Op.EQUAL
                )
            versionDescriptor.majorString != "0" -> {
                val version = SemanticVersion(major = versionDescriptor.major, minor = versionDescriptor.minor)
                Range(
                    Condition(Op.GREATER_THAN_OR_EQUAL, version),
                    Condition(Op.LESS_THAN, version.toNextMajor(preRelease = "")),
                    Op.EQUAL
                )
            }
            else -> versionDescriptor.toComparator()
        }
}
