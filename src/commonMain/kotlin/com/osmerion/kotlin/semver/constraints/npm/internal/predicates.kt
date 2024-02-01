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
@file:OptIn(ExperimentalConstraintApi::class)
package com.osmerion.kotlin.semver.constraints.npm.internal

import com.osmerion.kotlin.semver.SemanticVersion
import com.osmerion.kotlin.semver.constraints.ExperimentalConstraintApi
import com.osmerion.kotlin.semver.constraints.RangePredicate
import com.osmerion.kotlin.semver.constraints.VersionComparator
import com.osmerion.kotlin.semver.constraints.VersionPredicate

internal data class AnyVersion(val str: String) : RangePredicate(
    startInclusive = null,
    endExclusive = null
) {
    override fun toString(): String = str
}

internal data class CaretVersionRange(private val descriptor: NpmVersionDescriptor?) : RangePredicate(
    startInclusive = descriptor.toVersion(),
    endExclusive = when (descriptor) {
        null, is StarVersionDescriptor -> null
        is RegularNpmVersionDescriptor -> {
            fun String.isX() = equals("X", ignoreCase = true)
            when {
                descriptor.majorString.isX() -> null
                descriptor.major != 0 || descriptor.minor == null || descriptor.minorString!!.isX() -> SemanticVersion(descriptor.major!! + 1, 0, 0, "0")
                descriptor.minor != 0 || descriptor.patch == null || descriptor.patchString!!.isX() -> SemanticVersion(descriptor.major!!, descriptor.minor!! + 1, 0, "0")
                else -> SemanticVersion(descriptor.major!!, descriptor.minor!!, descriptor.patch!!, "0")
            }
        }
    }
) {
    override fun toString(): String = buildString {
        append('^')
        descriptor?.let(::append)
    }
}

internal data class ComparatorPredicate(
    private val descriptor: NpmVersionDescriptor,
    private val op: Op?
) : VersionPredicate {

    override val comparators: Set<VersionComparator> get() {
        val reference = when (op) {
            null, Op.EQUAL, Op.GREATER_THAN_OR_EQUAL -> descriptor.toVersion()
            Op.LESS_THAN_OR_EQUAL -> descriptor.toVersion()?.toSmallestLargerVersion()
            Op.LESS_THAN -> when (descriptor) {
                is StarVersionDescriptor -> return setOf(VersionComparator(op = VersionComparator.Op.LT, SemanticVersion(0, 0, 0, "0")))
                is RegularNpmVersionDescriptor -> when {
                    isX(descriptor.majorString) -> return setOf(VersionComparator(op = VersionComparator.Op.LT, SemanticVersion(0, 0, 0, "0")))
                    else -> descriptor.toVersion()?.let {
                        if (descriptor.minor == null || descriptor.patch == null) {
                            it.copy(preRelease = "0")
                        } else {
                            it
                        }
                    }
                }
            }
            Op.GREATER_THAN -> when (descriptor) {
                is StarVersionDescriptor -> return setOf(VersionComparator(op = VersionComparator.Op.LT, SemanticVersion(0, 0, 0, "0")))
                is RegularNpmVersionDescriptor -> when {
                    isX(descriptor.majorString) -> return setOf(VersionComparator(op = VersionComparator.Op.LT, SemanticVersion(0, 0, 0, "0")))
                    descriptor.minor == null -> descriptor.toVersion()?.toNextMajor(preRelease = "0")
                    descriptor.patch == null -> descriptor.toVersion()?.toNextMinor(preRelease = "0")
                    descriptor.preRelease == null -> descriptor.toVersion()?.toNextPatch(preRelease = "0")
                    else -> descriptor.toVersion()
                }
            }
        }

        if (reference != null) {
            return setOf(VersionComparator(
                op = when (op) {
                    null, Op.EQUAL -> VersionComparator.Op.EQ
                    Op.LESS_THAN, Op.LESS_THAN_OR_EQUAL -> VersionComparator.Op.LT
                    Op.GREATER_THAN, Op.GREATER_THAN_OR_EQUAL -> VersionComparator.Op.GTE
                },
                reference = reference
            ))
        } else {
            return emptySet()
        }
    }

    override fun equals(other: Any?): Boolean = when (other) {
        null -> false
        is ComparatorPredicate -> this.normalizeEq() == other.normalizeEq()
        else -> false
    }

    override fun hashCode(): Int = normalizeEq().hashCode()

    private fun ComparatorPredicate.normalizeEq(): ComparatorPredicate = copy(op = op ?: Op.EQUAL)

    override fun toString(): String = buildString {
        op?.let(::append)
        append(descriptor)
    }

}

internal data class HyphenVersionRange(
    private val lowerBound: NpmVersionDescriptor,
    private val upperBound: NpmVersionDescriptor
) : RangePredicate(
    startInclusive = lowerBound.toVersion(),
    endExclusive = when (upperBound) {
        is StarVersionDescriptor -> null
        is RegularNpmVersionDescriptor -> when {
            isX(upperBound.majorString) -> null
            upperBound.minor == null -> upperBound.toVersion()!!.toNextMajor(preRelease = "0")
            upperBound.patch == null -> upperBound.toVersion()!!.toNextMinor(preRelease = "0")
            else -> upperBound.toVersion()?.toSmallestLargerVersion()
        }
    }
) {
    override fun toString(): String = "$lowerBound-$upperBound"
}

internal class TildeVersionRange(private val descriptor: NpmVersionDescriptor?) : RangePredicate(
    startInclusive = descriptor.toVersion(),
    endExclusive = when (descriptor) {
        null, is StarVersionDescriptor -> null
        is RegularNpmVersionDescriptor -> when {
            descriptor.patch != null -> descriptor.toVersion()?.toNextMinor(preRelease = "0")
            else -> descriptor.toVersion()?.toNextMajor(preRelease = "0")
        }
    }
) {
    override fun toString(): String = buildString {
        append('~')
        descriptor?.let(::append)
    }
}

internal class XVersionRange(private val descriptor: NpmVersionDescriptor?) : RangePredicate(
    startInclusive = when (descriptor) {
        null, is StarVersionDescriptor -> null
        is RegularNpmVersionDescriptor -> when {
            isX(descriptor.majorString) -> null
            else -> descriptor.toVersion()
        }
    },
    endExclusive = when (descriptor) {
        null, is StarVersionDescriptor -> null
        is RegularNpmVersionDescriptor -> when {
            isX(descriptor.majorString) -> null
            descriptor.minor == null || isX(descriptor.minorString) -> SemanticVersion(descriptor.major!! + 1, 0, 0, "0")
            descriptor.patch == null || isX(descriptor.patchString) -> SemanticVersion(descriptor.major!!, descriptor.minor!! + 1, 0, "0")
            else -> SemanticVersion(descriptor.major!!, descriptor.minor!!, descriptor.patch!! + 1, "0")
        }
    }
) {
    override fun toString(): String = "$descriptor"
}
