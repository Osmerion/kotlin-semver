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
package com.osmerion.kotlin.semver.constraints

import com.osmerion.kotlin.semver.SemanticVersion

internal class Range(
    private val start: VersionComparator,
    private val end: VersionComparator,
    private val operator: Op
) : VersionComparator {

    override fun isSatisfiedBy(version: SemanticVersion): Boolean =
        when (operator) {
            Op.EQUAL -> start.isSatisfiedBy(version) && end.isSatisfiedBy(version)
            Op.NOT_EQUAL -> !start.isSatisfiedBy(version) || !end.isSatisfiedBy(version)
            Op.LESS_THAN -> !start.isSatisfiedBy(version) && end.isSatisfiedBy(version)
            Op.LESS_THAN_OR_EQUAL -> end.isSatisfiedBy(version)
            Op.GREATER_THAN -> start.isSatisfiedBy(version) && !end.isSatisfiedBy(version)
            Op.GREATER_THAN_OR_EQUAL -> start.isSatisfiedBy(version)
        }

    override fun opposite(): String =
        when (operator) {
            Op.EQUAL -> toStringByOperator(Op.NOT_EQUAL)
            Op.NOT_EQUAL -> toStringByOperator(Op.EQUAL)
            Op.LESS_THAN -> toStringByOperator(Op.GREATER_THAN_OR_EQUAL)
            Op.LESS_THAN_OR_EQUAL -> toStringByOperator(Op.GREATER_THAN)
            Op.GREATER_THAN -> toStringByOperator(Op.LESS_THAN_OR_EQUAL)
            Op.GREATER_THAN_OR_EQUAL -> toStringByOperator(Op.LESS_THAN)
        }

    override fun toString(): String = toStringByOperator(operator)

    private fun toStringByOperator(operator: Op): String =
        when (operator) {
            Op.EQUAL -> "$start $end"
            Op.NOT_EQUAL -> "${start.opposite()} || ${end.opposite()}"
            Op.LESS_THAN -> start.opposite()
            Op.LESS_THAN_OR_EQUAL -> "$end"
            Op.GREATER_THAN -> end.opposite()
            Op.GREATER_THAN_OR_EQUAL -> "$start"
        }
}
