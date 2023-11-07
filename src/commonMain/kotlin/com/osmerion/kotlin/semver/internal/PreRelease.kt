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
package com.osmerion.kotlin.semver.internal

import com.osmerion.kotlin.semver.VersionFormatException
import kotlin.math.min

internal class PreRelease private constructor(private val parts: List<String>) : Comparable<PreRelease> {

    val identity: String get() = parts[0]

    fun increment(): PreRelease {
        val newParts = parts.toMutableList()

        val lastNumericItem = newParts.lastOrNull { it.toIntOrNull() != null }
        lastNumericItem?.let {
            val lastNumericIndex = newParts.indexOf(lastNumericItem)
            newParts[lastNumericIndex] = (lastNumericItem.toInt() + 1).toString()
        } ?: newParts.add(DEFAULT_INIT_PART)

        return PreRelease(newParts)
    }

    override fun compareTo(other: PreRelease): Int {
        val thisSize = parts.size
        val otherSize = other.parts.size

        val count = min(thisSize, otherSize)

        for (i in 0 until count) {
            val partResult = compareParts(parts[i], other.parts[i])
            if (partResult != 0) return partResult
        }

        return thisSize.compareTo(otherSize)
    }

    override fun equals(other: Any?): Boolean {
        val preRelease = other as? PreRelease
        return when {
            preRelease == null -> false
            compareTo(preRelease) == 0 -> true
            else -> false
        }
    }

    override fun hashCode(): Int {
        return toString().hashCode()
    }

    override fun toString(): String = parts.joinToString(".")

    private fun compareParts(part1: String, part2: String): Int {
        val firstPart = part1.toIntOrNull()
        val secondPart = part2.toIntOrNull()

        return when {
            firstPart != null && secondPart == null -> -1
            firstPart == null && secondPart != null -> 1
            firstPart != null && secondPart != null -> firstPart.compareTo(secondPart)
            else -> part1.compareTo(part2)
        }
    }

    companion object {
        private const val DEFAULT_INIT_PART = "0"
        private val onlyNumberRegex: Regex = Patterns.ONLY_NUMBER_REGEX.toRegex()
        private val onlyAlphaNumericAndHyphenRegex: Regex = Patterns.ONLY_ALPHANUMERIC_OR_HYPHEN_REGEX.toRegex()
        val default: PreRelease =
            PreRelease(listOf(DEFAULT_INIT_PART))

        operator fun invoke(preReleaseString: String): PreRelease =
            PreRelease(
                validate(preReleaseString)
            )

        private fun validate(preReleaseString: String): List<String> {
            if (preReleaseString.isBlank()) {
                return listOf(DEFAULT_INIT_PART)
            }

            val parts = preReleaseString.trim().split('.')
            for (part in parts) {
                val error = when {
                    part.isBlank() -> "Pre-release identity contains an empty part."
                    part.matches(onlyNumberRegex) && part.length > 1 && part[0] == '0' ->
                        "Pre-release part '$part' is numeric but contains a leading zero."
                    !part.matches(onlyAlphaNumericAndHyphenRegex) ->
                        "Pre-release part '$part' contains an invalid character."
                    else -> null
                }

                error?.let { throw VersionFormatException("$error ($preReleaseString)") } ?: continue
            }

            return parts
        }
    }
}
