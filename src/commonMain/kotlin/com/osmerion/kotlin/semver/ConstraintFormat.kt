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
package com.osmerion.kotlin.semver

/**
 * Formats for version constraint strings.
 *
 * @since   0.1.0
 */
public enum class ConstraintFormat {
    /**
     * The version constraint format of Maven.
     *
     * Refer to [Maven](https://maven.apache.org/enforcer/enforcer-rules/versionRanges.html)
     * for more information.
     *
     * @since   0.1.0
     */
    MAVEN,

    /**
     * The version constraint format of NPM.
     *
     * Refer to [node-semver](https://github.com/npm/node-semver) for more information.
     *
     * @since   0.1.0
     */
    NPM,

    /**
     * A custom version constraint format.
     *
     * TODO doc
     *
     * @since   0.1.0
     */
    @ExperimentalConstraintFormat
    OSMERION
}