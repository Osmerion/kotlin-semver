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
package com.osmerion.kotlin.semver

import com.osmerion.kotlin.semver.constraints.ExperimentalConstraintApi
import com.osmerion.kotlin.semver.constraints.VersionPredicate

/**
 * Formats for version constraint strings.
 *
 * Implementing third-party constraint formats is experimentally supported.
 *
 * @since   0.1.0
 */
@SubclassOptInRequired(ExperimentalConstraintApi::class)
public interface ConstraintFormat {

    /**
     * Parses the given [source] into a pair of predicates and an optional preferred version.
     *
     * @param source    the source to parse
     *
     * @return  a list of predicates paired with the preferred version or `null`
     *
     * @since   0.1.0
     */
    @ExperimentalConstraintApi
    public fun parse(source: CharSequence): Pair<List<List<VersionPredicate>>, Version?>

    /**
     * Converts the list of predicates into the string representation.
     *
     * @param predicates    the predicates that make up the constraint
     *
     * @return  the string representation of the constraint
     *
     * @since   0.1.0
     */
    @ExperimentalConstraintApi
    public fun toString(predicates: List<List<VersionPredicate>>): String

}
