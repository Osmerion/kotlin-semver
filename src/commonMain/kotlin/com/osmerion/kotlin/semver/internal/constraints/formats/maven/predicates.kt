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
import com.osmerion.kotlin.semver.internal.constraints.*

internal class ExactVersionMatch(private val descriptor: VersionDescriptor) : RangePredicate(
    startInclusive = descriptor.toVersion(),
    endExclusive = descriptor.toVersion(increment = true)
) {
    override fun toString(): String = "[$descriptor]"
}

internal class IntervalVersionRange private constructor(
    private val lowerBound: VersionDescriptor?,
    private val lowerBoundInclusive: Boolean,
    private val upperBound: VersionDescriptor?,
    private val upperBoundInclusive: Boolean
) : RangePredicate(
    startInclusive = lowerBound.toVersion(increment = !lowerBoundInclusive),
    endExclusive = upperBound.toVersion(increment = upperBoundInclusive)
) {

    companion object {

        operator fun invoke(
            lowerBound: VersionDescriptor?,
            lowerBoundInclusive: Boolean,
            upperBound: VersionDescriptor?,
            upperBoundInclusive: Boolean
        ): IntervalVersionRange {
            if (lowerBound == null && lowerBoundInclusive) throw ConstraintFormatException("Invalid interval with unspecified inclusive lower bound")
            if (upperBound == null && upperBoundInclusive) throw ConstraintFormatException("Invalid interval with unspecified inclusive upper bound")

            return IntervalVersionRange(lowerBound, lowerBoundInclusive, upperBound, upperBoundInclusive)
        }

    }

    override fun toString(): String = buildString {
        append(if (lowerBoundInclusive) "[" else "(")
        lowerBound?.also(::append)
        append(",")
        upperBound?.also(::append)
        append(if (upperBoundInclusive) "]" else ")")
    }

}

internal class MinimumVersion(private val descriptor: VersionDescriptor) : RangePredicate(
    startInclusive = descriptor.toVersion(),
    endExclusive = null
) {
    override fun toString(): String = "$descriptor"
}