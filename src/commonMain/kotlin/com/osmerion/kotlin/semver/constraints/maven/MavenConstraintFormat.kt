/*
 * Copyright (c) 2022 Peter Csajtai
 * Copyright (c) 2023-2026 Leon Linhart
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
package com.osmerion.kotlin.semver.constraints.maven

import com.osmerion.kotlin.semver.ConstraintFormat
import com.osmerion.kotlin.semver.ConstraintFormatException
import com.osmerion.kotlin.semver.SemanticVersion
import com.osmerion.kotlin.semver.constraints.ExperimentalConstraintApi
import com.osmerion.kotlin.semver.constraints.VersionPredicate
import com.osmerion.kotlin.semver.constraints.maven.internal.ExactVersionMatch
import com.osmerion.kotlin.semver.constraints.maven.internal.IntervalVersionRange
import com.osmerion.kotlin.semver.constraints.maven.internal.MinimumVersion
import com.osmerion.kotlin.semver.constraints.maven.internal.parseMavenVersionDescriptor
import com.osmerion.kotlin.semver.constraints.maven.internal.toVersion

/**
 * The version constraint format of Maven.
 *
 * Refer to [Maven](https://maven.apache.org/enforcer/enforcer-rules/versionRanges.html)
 * for more information.
 *
 * @since   0.1.0
 */
@OptIn(ExperimentalConstraintApi::class)
public sealed class MavenConstraintFormat : ConstraintFormat {

    public companion object Default : MavenConstraintFormat()

    override fun parse(source: CharSequence): Pair<List<List<VersionPredicate>>, SemanticVersion?> {
        if (source.isBlank()) throw ConstraintFormatException("Constraint strings may not be blank")

        var preferredVersion: SemanticVersion? = null
        val predicateSets = buildList {
            var pos = 0
            var expectingMore = false

            while (pos < source.length) {
                when (val c = source[pos++]) {
                    '(', '[', ']' -> {
                        val endIndex = source.indexOfFirst(startIndex = pos) { it == ')' || it == ']' || it == '[' }
                        if (endIndex < 0) throw ConstraintFormatException("Unmatched opening character '$c' at index ${pos - 1}")

                        val lowerBoundInclusive = (c == '[')
                        val upperBoundInclusive = (source[endIndex] == ']')

                        val restrictionString = source.substring(startIndex = pos, endIndex = endIndex)
                        val indexOfComma = restrictionString.indexOf(',')

                        val predicate = if (indexOfComma >= 0) {
                            val lowerBoundString = if (indexOfComma > 0) restrictionString.substring(0, indexOfComma).trim() else ""
                            val upperBoundString = restrictionString.substring(indexOfComma + 1, restrictionString.length).trim()

                            val lowerBoundDescriptor = if (lowerBoundString.isNotEmpty()) parseMavenVersionDescriptor(lowerBoundString) else null
                            val upperBoundDescriptor = if (upperBoundString.isNotEmpty()) parseMavenVersionDescriptor(upperBoundString) else null

                            IntervalVersionRange(
                                lowerBound = lowerBoundDescriptor,
                                lowerBoundInclusive = lowerBoundInclusive,
                                upperBound = upperBoundDescriptor,
                                upperBoundInclusive = upperBoundInclusive
                            )
                        } else {
                            if (!lowerBoundInclusive || !upperBoundInclusive) throw ConstraintFormatException("Range with exclusive bound requires two version descriptors: $restrictionString")

                            val exactMatchDescriptor = parseMavenVersionDescriptor(restrictionString)
                            ExactVersionMatch(exactMatchDescriptor)
                        }

                        add(listOf(predicate))
                        pos = endIndex + 1

                        if (pos >= source.length || source[pos] != ',') {
                            expectingMore = false
                            break
                        }

                        pos++
                        expectingMore = true
                    }
                    else -> {
                        if (expectingMore) throw ConstraintFormatException("Expecting more version descriptors but got '$c' at index ${pos - 1}")

                        val descriptor = parseMavenVersionDescriptor(source)
                        add(listOf(MinimumVersion(descriptor)))

                        preferredVersion = descriptor.toVersion()
                        pos = source.length
                    }
                }
            }

            if (expectingMore) throw ConstraintFormatException("Expecting more version descriptors but reached end of input")
            if (pos < source.length) throw ConstraintFormatException("Parsing completed unexpectedly")
        }

        return predicateSets to preferredVersion
    }

    @ExperimentalConstraintApi
    override fun toString(predicates: List<List<VersionPredicate>>): String =
        predicates.joinToString(separator = ",") { "${it.single()}" }

    private inline fun CharSequence.indexOfFirst(
        startIndex: Int = 0,
        predicate: (Char) -> Boolean
    ): Int {
        val indices = startIndex.coerceAtLeast(0)..length

        for (index in indices) {
            if (predicate(this[index])) {
                return index
            }
        }

        return -1
    }

}
