/*
 * Copyright (c) 2019-2024 Leon Linhart
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

@OptIn(ExperimentalConstraintApi::class)
internal object SimplePredicates {

    fun equal(version: SemanticVersion): VersionPredicate = object : VersionPredicate {
        override val comparators: Set<VersionComparator> get() = setOf(VersionComparator(VersionComparator.Op.EQ, version))
    }

    fun greaterThanOrEqual(version: SemanticVersion): VersionPredicate = object : VersionPredicate {
        override val comparators: Set<VersionComparator> get() = setOf(VersionComparator(VersionComparator.Op.GTE, version))
    }

    fun lessThan(version: SemanticVersion): VersionPredicate = object : VersionPredicate {
        override val comparators: Set<VersionComparator> get() = setOf(VersionComparator(VersionComparator.Op.LT, version))
    }

    fun notEqual(version: SemanticVersion): VersionPredicate = object : VersionPredicate {
        override val comparators: Set<VersionComparator> get() = setOf(VersionComparator(VersionComparator.Op.NEQ, version))
    }

}
