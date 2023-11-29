/*
 * Copyright (c) 2019-2023 Leon Linhart
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
package com.osmerion.kotlin.semver.internal.constraints.formats.maven

import com.osmerion.kotlin.semver.ConstraintFormatException
import com.osmerion.kotlin.semver.SemanticVersion
import com.osmerion.kotlin.semver.internal.constraints.*

internal fun parseMavenConstraint(string: String): Pair<List<List<VersionPredicate>>, SemanticVersion?> {
    var preferredVersion: SemanticVersion? = null
    val predicateSets = buildList {
        var pos = 0
        var expectingMore = false

        while (pos < string.length) {
            when (val c = string[pos++]) {
                '(', '[', ']' -> {
                    val endIndex = string.indexOfFirst(startIndex = pos) { it == ')' || it == ']' || it == '[' }
                    if (endIndex < 0) throw ConstraintFormatException("") // TODO err

                    val lowerBoundInclusive = (c == '[')
                    val upperBoundInclusive = (string[endIndex] == ']')

                    val restrictionString = string.substring(startIndex = pos, endIndex = endIndex)
                    val indexOfComma = restrictionString.indexOf(',')

                    val predicate = if (indexOfComma >= 0) {
                        val lowerBoundString = if (indexOfComma > 0) restrictionString.substring(0, indexOfComma).trim() else ""
                        val upperBoundString = restrictionString.substring(indexOfComma + 1, restrictionString.length).trim()

                        val lowerBoundDescriptor = if (lowerBoundString.isNotEmpty()) VersionDescriptor.parse(lowerBoundString) else null
                        val upperBoundDescriptor = if (upperBoundString.isNotEmpty()) VersionDescriptor.parse(upperBoundString) else null

                        IntervalVersionRange(
                            lowerBound = lowerBoundDescriptor,
                            lowerBoundInclusive = lowerBoundInclusive,
                            upperBound = upperBoundDescriptor,
                            upperBoundInclusive = upperBoundInclusive
                        )
                    } else {
                        if (!lowerBoundInclusive || !upperBoundInclusive) throw ConstraintFormatException("") // TODO err

                        val exactMatchDescriptor = VersionDescriptor.parse(restrictionString)
                        ExactVersionMatch(exactMatchDescriptor)
                    }

                    add(listOf(predicate))
                    pos = endIndex + 1

                    if (pos >= string.length || string[pos] != ',') {
                        expectingMore = false
                        break
                    }

                    pos++
                    expectingMore = true
                }
                else -> {
                    if (expectingMore) throw ConstraintFormatException("") // TODO err

                    val descriptor = VersionDescriptor.parse(string)

                    add(listOf(MinimumVersion(descriptor)))
                    preferredVersion = descriptor.toVersion()
                    pos = string.length
                }
            }
        }

        if (expectingMore) throw ConstraintFormatException("") // TODO err
        if (pos < string.length) throw ConstraintFormatException("") // TODO err
    }

    return predicateSets to preferredVersion
}

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