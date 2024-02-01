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
package com.osmerion.kotlin.semver.constraints.npm.internal

internal enum class Op(private val stringValue: String) {
    EQUAL("="),
    LESS_THAN("<"),
    LESS_THAN_OR_EQUAL("<="),
    GREATER_THAN(">"),
    GREATER_THAN_OR_EQUAL(">=");

    override fun toString(): String = stringValue

}

internal fun String.startsWithOperator(): Op? {
    if (isEmpty()) return null

    if (length >= 2) {
        val op = substring(0, 2).toOperator()
        if (op != null) return op
    }

    return substring(0, 1).toOperator()
}

internal fun String.toOperator(): Op? {
    if (isEmpty()) return null

    return when (this) {
        "=" -> Op.EQUAL
        ">" -> Op.GREATER_THAN
        "<" -> Op.LESS_THAN
        ">=", "=>" -> Op.GREATER_THAN_OR_EQUAL
        "<=", "=<" -> Op.LESS_THAN_OR_EQUAL
        else -> null
    }
}
