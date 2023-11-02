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

internal class Condition(private val operator: Op, private val version: SemanticVersion) : VersionComparator {

    override fun isSatisfiedBy(version: SemanticVersion): Boolean {
        return when (operator) {
            Op.EQUAL -> version == this.version
            Op.NOT_EQUAL -> version != this.version
            Op.LESS_THAN -> version < this.version
            Op.LESS_THAN_OR_EQUAL -> version <= this.version
            Op.GREATER_THAN -> version > this.version
            Op.GREATER_THAN_OR_EQUAL -> version >= this.version
        }
    }

    override fun opposite(): String {
        return when (operator) {
            Op.EQUAL -> "${Op.NOT_EQUAL}$version"
            Op.NOT_EQUAL -> "${Op.EQUAL}$version"
            Op.LESS_THAN -> "${Op.GREATER_THAN_OR_EQUAL}$version"
            Op.LESS_THAN_OR_EQUAL -> "${Op.GREATER_THAN}$version"
            Op.GREATER_THAN -> "${Op.LESS_THAN_OR_EQUAL}$version"
            Op.GREATER_THAN_OR_EQUAL -> "${Op.LESS_THAN}$version"
        }
    }

    override fun toString(): String {
        return "$operator$version"
    }
}
