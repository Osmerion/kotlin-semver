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

    constructor(string: String): this(validate(string))

    companion object {

        val MIN = PreRelease(listOf("0"))

        private val onlyNumberRegex: Regex by lazy { Patterns.ONLY_NUMBER_REGEX.toRegex() }
        private val onlyAlphaNumericAndHyphenRegex: Regex by lazy { Patterns.ONLY_ALPHANUMERIC_OR_HYPHEN_REGEX.toRegex() }

        private fun validate(preReleaseString: String): List<String> {
            if (preReleaseString.isBlank()) throw VersionFormatException("Pre-release identifier may not be blank")

            val parts = preReleaseString.trim().split('.')
            for (part in parts) {
                val error = when {
                    part.isBlank() -> "Pre-release identity contains an empty part."
                    part.matches(onlyNumberRegex) && part.length > 1 && part[0] == '0' -> "Pre-release part '$part' is numeric but contains a leading zero."
                    !part.matches(onlyAlphaNumericAndHyphenRegex) -> "Pre-release part '$part' contains an invalid character."
                    else -> null
                }

                error?.let { throw VersionFormatException("$error ($preReleaseString)") } ?: continue
            }

            return parts
        }

    }

    override fun compareTo(other: PreRelease): Int {
        fun compareParts(part1: String, part2: String): Int {
            val firstPart = part1.toIntOrNull()
            val secondPart = part2.toIntOrNull()

            return when {
                firstPart != null && secondPart == null -> -1
                firstPart == null && secondPart != null -> 1
                firstPart != null && secondPart != null -> firstPart.compareTo(secondPart)
                else -> part1.compareTo(part2)
            }
        }

        val thisSize = parts.size
        val otherSize = other.parts.size

        val count = min(thisSize, otherSize)

        for (i in 0 until count) {
            val partResult = compareParts(parts[i], other.parts[i])
            if (partResult != 0) return partResult
        }

        return thisSize.compareTo(otherSize)
    }

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other is PreRelease -> parts == other.parts
        else -> false
    }

    override fun hashCode(): Int = toString().hashCode()
    override fun toString(): String = parts.joinToString(".")

    fun toSmallestLargerVersion(): PreRelease = PreRelease(parts + "0")

}