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
package com.osmerion.kotlin.semver.constraints.npm

import com.osmerion.kotlin.semver.ConstraintFormat
import com.osmerion.kotlin.semver.SemanticVersion
import com.osmerion.kotlin.semver.constraints.ExperimentalConstraintApi
import com.osmerion.kotlin.semver.constraints.VersionPredicate
import com.osmerion.kotlin.semver.constraints.npm.internal.parseRange

/**
 * The version constraint format of NPM.
 *
 * Refer to [node-semver](https://github.com/npm/node-semver) for more information.
 *
 * @since   0.1.0
 */
@OptIn(ExperimentalConstraintApi::class)
public sealed class NpmConstraintFormat(
    public val isStrict: Boolean = true,
    public val includePreRelease: Boolean = false
) : ConstraintFormat {

    public companion object Default : NpmConstraintFormat()

    public class Custom(
        isStrict: Boolean = true,
        includePreRelease: Boolean = false
    ) : NpmConstraintFormat(
        isStrict = isStrict,
        includePreRelease = includePreRelease
    )

    @ExperimentalConstraintApi
    override fun parse(source: CharSequence): Pair<List<List<VersionPredicate>>, SemanticVersion?> {
        val source = source.replace("\\s+".toRegex(), " ")

        val predicates = source.split("||")
            .map { parseRange(it.trim()) }

        return predicates to null
    }

    @ExperimentalConstraintApi
    override fun toString(predicates: List<List<VersionPredicate>>): String =
        predicates.joinToString(separator = "||") { it.joinToString(separator = " ") }

}
