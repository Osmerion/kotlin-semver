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
package com.osmerion.kotlin.semver.constraints

import com.osmerion.kotlin.semver.Patterns

internal interface ConditionProcessor {
    val regex: Regex
    fun processCondition(match: MatchResult): VersionComparator
}

internal class OperatorConditionProcessor : ConditionProcessor {
    override val regex: Regex = Patterns.OPERATOR_CONDITION_REGEX.toRegex()
    private val comparatorBuilders = arrayOf(
        RegularComparatorBuilder(),
        TildeComparatorBuilder(),
        CaretComparatorBuilder()
    )

    @Suppress("MagicNumber")
    override fun processCondition(match: MatchResult): VersionComparator {
        val operator = match.groups[1]?.value ?: ""
        val major = match.groups[2]?.value ?: ""
        val minor = match.groups[3]?.value
        val patch = match.groups[4]?.value
        val preRelease = match.groups[5]?.value
        val buildMetadata = match.groups[6]?.value
        val descriptor = VersionDescriptor(major, minor, patch, preRelease, buildMetadata)
        comparatorBuilders.forEach { builder ->
            if (operator in builder.acceptedOperators) {
                return builder.buildComparator(operator, descriptor)
            }
        }
        throw ConstraintFormatException(
            "Invalid constraint operator: " +
                "$operator in $descriptor"
        )
    }
}

internal class HyphenConditionProcessor : ConditionProcessor {
    override val regex: Regex = Patterns.HYPHEN_CONDITION_REGEX.toRegex()

    @Suppress("MagicNumber")
    override fun processCondition(match: MatchResult): VersionComparator {
        val start = VersionDescriptor(
            majorString = match.groups[1]?.value ?: "",
            minorString = match.groups[2]?.value,
            patchString = match.groups[3]?.value,
            preRelease = match.groups[4]?.value,
            buildMetadata = match.groups[5]?.value
        )
        val end = VersionDescriptor(
            majorString = match.groups[6]?.value ?: "",
            minorString = match.groups[7]?.value,
            patchString = match.groups[8]?.value,
            preRelease = match.groups[9]?.value,
            buildMetadata = match.groups[10]?.value
        )
        return Range(
            start.toComparator(Op.GREATER_THAN_OR_EQUAL),
            end.toComparator(Op.LESS_THAN_OR_EQUAL),
            Op.EQUAL
        )
    }
}
