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
package com.osmerion.kotlin.semver.constraints

import com.osmerion.kotlin.semver.SemanticVersion

/**
 * A comparator defines a test for a version through an [operation][op] and a [reference].
 *
 * @since   0.1.0
 */
public class VersionComparator(public val op: Op, public val reference: SemanticVersion) {

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other is VersionComparator -> op == other.op && reference == other.reference
        else -> false
    }

    override fun hashCode(): Int {
        var hash = op.hashCode()
        hash *= 31 + reference.hashCode()
        return hash
    }

    /**
     * A comparison operator.
     *
     * @since   0.1.0
     */
    public enum class Op {
        LT,
        GTE,
        EQ,
        NEQ
    }

}
