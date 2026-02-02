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
package com.osmerion.kotlin.semver.internal

import com.osmerion.kotlin.semver.SemanticVersion
import com.osmerion.kotlin.semver.constraints.ExperimentalConstraintApi
import com.osmerion.kotlin.semver.constraints.VersionComparator
import com.osmerion.kotlin.semver.constraints.VersionPredicate

/**
 * Condenses a formula of version predicates into a list of [version ranges][VersionRange].
 *
 * The given formula is expected to be in [disjunctive normal form](https://en.wikipedia.org/wiki/Disjunctive_normal_form).
 *
 * @return  a list of version ranges that uniquely describes the semantic meaning of the given formula
 */
@OptIn(ExperimentalConstraintApi::class)
internal fun List<List<VersionPredicate>>.toVersionRanges(): List<VersionRange> {
    /*
     * A list of ranges that specify which versions satisfy this constraint. To satisfy this constraint, a version
     * must be in at least one of the ranges.
     */
    val validRanges = mutableListOf<VersionRange>()

    for (conjunction in this) {
        val comparators = conjunction.flatMap(VersionPredicate::comparators)

        val eqComparators = comparators.filter { it.op == VersionComparator.Op.EQ }.toSet()
        val neqComparators = comparators.filter { it.op == VersionComparator.Op.NEQ }.toSet()

        if (eqComparators.any { eq -> neqComparators.any { neq -> eq.reference == neq.reference } }) {
            // The conjunction "A && !A" is unsatisfiable and does not contribute to the disjunction
            continue
        }

        val minInclusive = comparators.filter { it.op == VersionComparator.Op.GTE || it.op == VersionComparator.Op.EQ }
            .mapNotNull(VersionComparator::reference)
            .maxOrNull()

        val maxExclusive = (comparators.filter { it.op == VersionComparator.Op.LT }.mapNotNull(VersionComparator::reference) + comparators.filter { it.op == VersionComparator.Op.EQ }.map { it.reference.toSmallestLargerVersion() })
            .minOrNull()

        if (minInclusive != null && maxExclusive != null && minInclusive >= maxExclusive) {
            // The conjunction >=A && <A is unsatisfiable and does not contribute to the disjunction
            continue
        }

        // Ideally, this should be a SortedList for performance, but Kotlin's stdlib doesn't have one
        val rs = mutableSetOf(VersionRange(startInclusive = minInclusive, endExclusive = maxExclusive))

        for (neq in neqComparators) {
            val range = rs.find { (it.startInclusive == null || it.startInclusive <= neq.reference) && (it.endExclusive == null || neq.reference <= it.endExclusive) } ?: continue
            rs.remove(range)

            val lowerStartInclusive = range.startInclusive
            if (lowerStartInclusive != neq.reference) rs.add(VersionRange(lowerStartInclusive, neq.reference))

            val upperStartInclusive = neq.reference.toSmallestLargerVersion()
            val upperEndExclusive = range.endExclusive
            if (upperStartInclusive != upperEndExclusive) rs.add(VersionRange(upperStartInclusive, upperEndExclusive))
        }

        validRanges += rs
    }

    /*
     * At this point, "ranges" is effectively a disjunction of version ranges for which this constraint is valid.
     * Thus, we can now sort the ranges (since VersionRange is Comparable) and then iterate over the sorted list to
     * merge intersecting and adjacent ranges.
     * This way, we get the unique and minimal representation of this constraint in ranges.
     */
    validRanges.sort()

    return buildList {
        var currentRange = validRanges.firstOrNull() ?: let {
            add(VersionRange(null, SemanticVersion(0, 0, 0, "0")))
            return@buildList
        }

        for (i in 1 until validRanges.size) {
            val nextRange = validRanges[i]

            currentRange = if (currentRange intersects nextRange || currentRange.endExclusive == nextRange.startInclusive) {
                // Merge intersecting or adjacent ranges
                val nextEndExclusive = if (currentRange.endExclusive != null && nextRange.endExclusive != null) maxOf(currentRange.endExclusive, nextRange.endExclusive) else null
                VersionRange(currentRange.startInclusive, nextEndExclusive)
            } else {
                add(currentRange)
                nextRange
            }
        }

        add(currentRange)
    }
}

internal data class VersionRange(
    val startInclusive: SemanticVersion?,
    val endExclusive: SemanticVersion?
) : Comparable<VersionRange> {

    override fun compareTo(other: VersionRange): Int = when {
        this == other -> 0
        startInclusive == null && other.startInclusive != null -> -1
        startInclusive != null && other.startInclusive == null -> 1
        startInclusive != null && other.startInclusive != null -> startInclusive.compareTo(other.startInclusive)
        endExclusive == null && other.endExclusive != null -> -1
        endExclusive != null && other.endExclusive == null -> 1
        else -> error("Unexpected overlapping version ranges")
    }

    override fun toString(): String = buildString {
        if (startInclusive == null && endExclusive == null) return "*"

        if (startInclusive != null) {
            append(">=$startInclusive")
            if (endExclusive != null) append(" && <$endExclusive")
        } else if (endExclusive != null) {
            append("<$endExclusive")
        }
    }

    infix fun intersects(other: VersionRange): Boolean =
        // Check if this start is less than other's end
        (startInclusive == null || other.endExclusive == null || startInclusive < other.endExclusive) &&
            // Check if this end is more than other's start
            (endExclusive == null || other.startInclusive == null || other.startInclusive < endExclusive)

}
