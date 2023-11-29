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

internal abstract class RangePredicate(
    private val startInclusive: SemanticVersion?,
    private val endExclusive: SemanticVersion?
) : VersionPredicate {

    init {
        if (startInclusive != null && endExclusive != null && startInclusive >= endExclusive) {
            @Suppress("LeakingThis")
            throw ConstraintFormatException("Range predicate is invalid: $this")
        }
    }

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other is RangePredicate -> startInclusive == other.startInclusive
            && endExclusive == other.endExclusive
        else -> false
    }

    override fun hashCode(): Int {
        var hash = startInclusive.hashCode()
        hash *= 31 + endExclusive.hashCode()
        return hash
    }

    override val comparators: Set<VersionComparator>
        get() = buildSet {
            if (startInclusive != null) add(VersionComparator(VersionComparator.Op.GTE, startInclusive))
            if (endExclusive != null) add(VersionComparator(VersionComparator.Op.LT, endExclusive))
        }

}