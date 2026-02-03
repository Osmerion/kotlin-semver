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

import com.osmerion.kotlin.semver.Version
import com.osmerion.kotlin.semver.VersionConstraint
import com.osmerion.kotlin.semver.constraints.ExperimentalConstraintApi
import com.osmerion.kotlin.semver.internal.VersionRange
import com.osmerion.kotlin.semver.internal.toVersionRanges
import kotlin.test.*

/** Unit tests for [NPM][NpmConstraintFormat] version constraints. */
@OptIn(ExperimentalConstraintApi::class)
class NpmConstraintTest {

    @Test
    fun testToComparators() {
        // https://github.com/npm/node-semver/blob/ac9b35769ab0ddfefd5a3af4a3ecaf3da2012352/test/ranges/to-comparators.js
        assertRangeEquals(
            VersionRange(Version(1), Version(2, 0, 1, preRelease = "0")),
            "1.0.0 - 2.0.0"
        )
        assertRangeEquals(
            VersionRange(Version(1), Version(1, 0, 1, preRelease = "0")),
            "1.0.0"
        )
        assertRangeEquals(
            VersionRange(null, null),
            ">=*"
        )
        assertRangeEquals(
            VersionRange(null, null),
            ""
        )
        assertRangeEquals(
            VersionRange(null, null),
            "*"
        )
        assertRangeEquals(
            VersionRange(Version(1), null),
            ">=1.0.0"
        )
        assertRangeEquals(
            VersionRange(Version(1, 0, 1), null),
            ">1.0.0"
        )
        assertRangeEquals(
            VersionRange(null, Version(2, 0, 1, preRelease = "0")),
            "<=2.0.0"
        )
        assertRangeEquals(
            VersionRange(Version(1), Version(2, 0, 0, preRelease = "0")),
            "1"
        )
        assertRangeEquals(
            VersionRange(null, Version(2, 0, 1, preRelease = "0")),
            "<=2.0.0"
        )
        assertRangeEquals(
            VersionRange(null, Version(2, 0, 0)),
            "<2.0.0"
        )
        assertRangeEquals(
            VersionRange(Version(1), null),
            ">= 1.0.0"
        )
        assertRangeEquals(
            VersionRange(Version(1), null),
            ">=  1.0.0"
        )
        assertRangeEquals(
            VersionRange(Version(1), null),
            ">=   1.0.0"
        )
        assertRangeEquals(
            VersionRange(Version(1, 0, 1), null),
            "> 1.0.0"
        )
        assertRangeEquals(
            VersionRange(null, Version(2, 0, 0)),
            "<\t2.0.0"
        )
        assertRangeEquals(
            VersionRange(Version(0, 1, 97), null),
            ">=0.1.97"
        )
        assertRangeEquals(
            listOf(
                VersionRange(Version(0, 1, 20), Version(0, 1, 21, "0")),
                VersionRange(Version(1, 2, 4), Version(1, 2, 5, "0"))
            ),
            "0.1.20 || 1.2.4"
        )
        assertRangeEquals(
            listOf(
                VersionRange(null, Version(0, 0, 1)),
                VersionRange(Version(0, 2, 3), null)
            ),
            ">=0.2.3 || <0.0.1"
        )
        assertRangeEquals(
            VersionRange(null, null),
            "||"
        )
        assertRangeEquals(
            VersionRange(Version(2, 0, 0), Version(3, 0, 0, "0")),
            "2.x.x"
        )
        assertRangeEquals(
            VersionRange(Version(1, 2, 0), Version(1, 3, 0, "0")),
            "1.2.x"
        )
        assertRangeEquals(
            listOf(
                VersionRange(Version(1, 2, 0), Version(1, 3, 0, "0")),
                VersionRange(Version(2, 0, 0), Version(3, 0, 0, "0"))
            ),
            "1.2.x || 2.x"
        )
        assertRangeEquals(
            VersionRange(null, null),
            "x"
        )
        assertRangeEquals(
            VersionRange(Version(2), Version(3, 0, 0, "0")),
            "2.*.*"
        )
        assertRangeEquals(
            VersionRange(Version(1, 2), Version(1, 3, 0, "0")),
            "1.2.*"
        )
        assertRangeEquals(
            listOf(
                VersionRange(Version(1, 2), Version(1, 3, 0, "0")),
                VersionRange(Version(2), Version(3, 0, 0, "0"))
            ),
            "1.2.* || 2.*"
        )
        assertRangeEquals(
            VersionRange(Version(2), Version(3, 0, 0, "0")),
            "2"
        )
        assertRangeEquals(
            VersionRange(Version(2, 3), Version(2, 4, 0, "0")),
            "2.3"
        )
        assertRangeEquals(
            VersionRange(Version(2, 4, 0), Version(2, 5, 0, "0")),
            "~2.4"
        )
        assertRangeEquals(
            VersionRange(Version(2, 4, 0), Version(2, 5, 0, "0")),
            "~>2.4"
        )
        assertRangeEquals(
            VersionRange(Version(3, 2, 1), Version(3, 3, 0, "0")),
            "~>3.2.1"
        )
        assertRangeEquals(
            VersionRange(Version(1), Version(2, 0, 0, "0")),
            "~1"
        )
        assertRangeEquals(
            VersionRange(Version(1), Version(2, 0, 0, "0")),
            "~>1"
        )
        assertRangeEquals(
            VersionRange(Version(1), Version(2, 0, 0, "0")),
            "~> 1"
        )
        assertRangeEquals(
            VersionRange(Version(1), Version(1, 1, 0, "0")),
            "~1.0"
        )
        assertRangeEquals(
            VersionRange(Version(1), Version(1, 1, 0, "0")),
            "~ 1.0"
        )
        assertRangeEquals(
            VersionRange(Version(1, 0, 3), Version(1, 1, 0, "0")),
            "~ 1.0.3"
        )
        assertRangeEquals(
            VersionRange(Version(1, 0, 3), Version(1, 1, 0, "0")),
            "~> 1.0.3"
        )
        assertRangeEquals(
            VersionRange(null, Version(1, 0, 0, "0")),
            "<1"
        )
        assertRangeEquals(
            VersionRange(null, Version(1, 0, 0, "0")),
            "< 1"
        )
        assertRangeEquals(
            VersionRange(Version(1), null),
            ">=1"
        )
        assertRangeEquals(
            VersionRange(Version(1), null),
            ">= 1"
        )
        assertRangeEquals(
            VersionRange(null, Version(1, 2, 0, "0")),
            "<1.2"
        )
        assertRangeEquals(
            VersionRange(null, Version(1, 2, 0, "0")),
            "< 1.2"
        )
        assertRangeEquals(
            VersionRange(null, Version(0, 0, 0, "0")),
            "1 2"
        )
        assertRangeEquals(
            VersionRange(Version(1, 2), Version(3, 4, 6, "0")),
            "1.2 - 3.4.5"
        )
        assertRangeEquals(
            VersionRange(Version(1, 2, 3), Version(3, 5, 0, "0")),
            "1.2.3 - 3.4"
        )
        assertRangeEquals(
            VersionRange(Version(1, 2, 3), Version(4, 0, 0, "0")),
            "1.2.3 - 3"
        )
        assertRangeEquals(
            VersionRange(null, Version(0, 0, 0, "0")),
            ">*"
        )
        assertRangeEquals(
            VersionRange(null, Version(0, 0, 0, "0")),
            "<*"
        )
        assertRangeEquals(
            VersionRange(null, Version(0, 0, 0, "0")),
            ">X"
        )
        assertRangeEquals(
            VersionRange(null, Version(0, 0, 0, "0")),
            "<X"
        )
        assertRangeEquals(
            VersionRange(null, Version(0, 0, 0, "0")),
            "<x <* || >* 2-x"
        )
        assertRangeEquals(
            VersionRange(null, null),
            ">x 2.x || * || <x"
        )
    }

    @Test
    fun testEquality() {
        // https://github.com/npm/node-semver/blob/main/test/fixtures/equality.js
        fun assertRangesAreEqual(a: String, b: String) {
            val constraintA = VersionConstraint.parse(a)
            val constraintB = VersionConstraint.parse(b)

            if (VersionConstraint.parse(a) != VersionConstraint.parse(b)) {
                fail("Constraints are not equal: $constraintA $constraintB")
            }
        }

        assertRangesAreEqual("1.2.3", "v1.2.3")
        assertRangesAreEqual("1.2.3", "=1.2.3")
        assertRangesAreEqual("1.2.3", "v 1.2.3")
        assertRangesAreEqual("1.2.3", "= 1.2.3")
        assertRangesAreEqual("1.2.3-0", "v1.2.3-0")
        assertRangesAreEqual("1.2.3-0", "=1.2.3-0")
        assertRangesAreEqual("1.2.3-0", "v 1.2.3-0")
        assertRangesAreEqual("1.2.3-0", "= 1.2.3-0")
        assertRangesAreEqual("1.2.3-0", " v1.2.3-0")
        assertRangesAreEqual("1.2.3-0", " =1.2.3-0")
        assertRangesAreEqual("1.2.3-0", " v 1.2.3-0")
        assertRangesAreEqual("1.2.3-0", " = 1.2.3-0")
        assertRangesAreEqual("1.2.3-1", "v1.2.3-1")
        assertRangesAreEqual("1.2.3-1", "=1.2.3-1")
        assertRangesAreEqual("1.2.3-1", "v 1.2.3-1")
        assertRangesAreEqual("1.2.3-1", "= 1.2.3-1")
        assertRangesAreEqual("1.2.3-1", " v1.2.3-1")
        assertRangesAreEqual("1.2.3-1", " =1.2.3-1")
        assertRangesAreEqual("1.2.3-1", " v 1.2.3-1")
        assertRangesAreEqual("1.2.3-1", " = 1.2.3-1")
        assertRangesAreEqual("1.2.3-beta", "v1.2.3-beta")
        assertRangesAreEqual("1.2.3-beta", "=1.2.3-beta")
        assertRangesAreEqual("1.2.3-beta", "v 1.2.3-beta")
        assertRangesAreEqual("1.2.3-beta", "= 1.2.3-beta")
        assertRangesAreEqual("1.2.3-beta", " v1.2.3-beta")
        assertRangesAreEqual("1.2.3-beta", " =1.2.3-beta")
        assertRangesAreEqual("1.2.3-beta", " v 1.2.3-beta")
        assertRangesAreEqual("1.2.3-beta", " = 1.2.3-beta")
        assertRangesAreEqual("1.2.3-beta+build", " = 1.2.3-beta+otherbuild")
        assertRangesAreEqual("1.2.3+build", " = 1.2.3+otherbuild")
        assertRangesAreEqual("1.2.3-beta+build", "1.2.3-beta+otherbuild")
        assertRangesAreEqual("1.2.3+build", "1.2.3+otherbuild")
        assertRangesAreEqual("  v1.2.3+build", "1.2.3+otherbuild")
    }

    private fun assertRangeEquals(
        expected: VersionRange,
        source: CharSequence
    ): Unit = assertRangeEquals(listOf(expected), source)

    private fun assertRangeEquals(
        expected: List<VersionRange>,
        source: CharSequence
    ) {
        assertEquals(
            expected = expected,
            actual = NpmConstraintFormat.parse(source).first.toVersionRanges()
        )
    }

    @Test
    fun testIncludes() {
        // https://github.com/npm/node-semver/blob/120968b76760cb0db85a72bde2adedd0e9628793/test/fixtures/range-include.js
        fun assertSatisfies(constraint: String, version: String, loose: Boolean = false, includePreRelease: Boolean = false) {
            val format = NpmConstraintFormat.Custom(
                isStrict = !loose,
                includePreRelease = includePreRelease
            )

            val constraint = VersionConstraint.parse(constraint, format)
            val version = Version.parse(version, strict = !loose)

            if (!constraint.isSatisfiedBy(version, includePreRelease = includePreRelease)) {
                fail("Version '${version}' does not satisfy constraint '${constraint}'")
            }
        }

        assertSatisfies("1.0.0 - 2.0.0", "1.2.3")
        assertSatisfies("^1.2.3+build", "1.2.3")
        assertSatisfies("^1.2.3+build", "1.3.0")
        assertSatisfies("1.2.3-pre+asdf - 2.4.3-pre+asdf", "1.2.3")
        assertSatisfies("1.2.3pre+asdf - 2.4.3-pre+asdf", "1.2.3", true)
        assertSatisfies("1.2.3-pre+asdf - 2.4.3pre+asdf", "1.2.3", true)
        assertSatisfies("1.2.3pre+asdf - 2.4.3pre+asdf", "1.2.3", true)
        assertSatisfies("1.2.3-pre+asdf - 2.4.3-pre+asdf", "1.2.3-pre.2")
        assertSatisfies("1.2.3-pre+asdf - 2.4.3-pre+asdf", "2.4.3-alpha")
        assertSatisfies("1.2.3+asdf - 2.4.3+asdf", "1.2.3")
        assertSatisfies("1.0.0", "1.0.0")
        assertSatisfies(">=*", "0.2.4")
        assertSatisfies("", "1.0.0")
        assertSatisfies("*", "1.2.3", loose = true)
        assertSatisfies("*", "v1.2.3", loose = true)
        assertSatisfies(">=1.0.0", "1.0.0", loose = true)
        assertSatisfies(">=1.0.0", "1.0.1", loose = true)
        assertSatisfies(">=1.0.0", "1.1.0", loose = true)
        assertSatisfies(">1.0.0", "1.0.1", loose = true)
        assertSatisfies(">1.0.0", "1.1.0")
        assertSatisfies("<=2.0.0", "2.0.0")
        assertSatisfies("<=2.0.0", "1.9999.9999")
        assertSatisfies("<=2.0.0", "0.2.9")
        assertSatisfies("<2.0.0", "1.9999.9999")
        assertSatisfies("<2.0.0", "0.2.9")
        assertSatisfies(">= 1.0.0", "1.0.0")
        assertSatisfies(">=  1.0.0", "1.0.1")
        assertSatisfies(">=   1.0.0", "1.1.0")
        assertSatisfies("> 1.0.0", "1.0.1")
        assertSatisfies(">  1.0.0", "1.1.0")
        assertSatisfies("<=   2.0.0", "2.0.0")
        assertSatisfies("<= 2.0.0", "1.9999.9999")
        assertSatisfies("<=  2.0.0", "0.2.9")
        assertSatisfies("<    2.0.0", "1.9999.9999")
        assertSatisfies("<\t2.0.0", "0.2.9")
        assertSatisfies(">=0.1.97", "v0.1.97", loose = true)
        assertSatisfies(">=0.1.97", "0.1.97")
        assertSatisfies("0.1.20 || 1.2.4", "1.2.4")
        assertSatisfies(">=0.2.3 || <0.0.1", "0.0.0")
        assertSatisfies(">=0.2.3 || <0.0.1", "0.2.3")
        assertSatisfies(">=0.2.3 || <0.0.1", "0.2.4")
        assertSatisfies("||", "1.3.4")
        assertSatisfies("2.x.x", "2.1.3")
        assertSatisfies("1.2.x", "1.2.3")
        assertSatisfies("1.2.x || 2.x", "2.1.3")
        assertSatisfies("1.2.x || 2.x", "1.2.3")
        assertSatisfies("x", "1.2.3")
        assertSatisfies("2.*.*", "2.1.3")
        assertSatisfies("1.2.*", "1.2.3")
        assertSatisfies("1.2.* || 2.*", "2.1.3")
        assertSatisfies("1.2.* || 2.*", "1.2.3")
        assertSatisfies("*", "1.2.3")
        assertSatisfies("2", "2.1.2")
        assertSatisfies("2.3", "2.3.1")
        assertSatisfies("~0.0.1", "0.0.1")
        assertSatisfies("~0.0.1", "0.0.2")
        assertSatisfies("~x", "0.0.9") // >=2.4.0 <2.5.0
        assertSatisfies("~2", "2.0.9") // >=2.4.0 <2.5.0
        assertSatisfies("~2.4", "2.4.0") // >=2.4.0 <2.5.0
        assertSatisfies("~2.4", "2.4.5")
        assertSatisfies("~>3.2.1", "3.2.2") // >=3.2.1 <3.3.0,
        assertSatisfies("~1", "1.2.3") // >=1.0.0 <2.0.0
        assertSatisfies("~>1", "1.2.3")
        assertSatisfies("~> 1", "1.2.3")
        assertSatisfies("~1.0", "1.0.2") // >=1.0.0 <1.1.0,
        assertSatisfies("~ 1.0", "1.0.2")
        assertSatisfies("~ 1.0.3", "1.0.12")
        assertSatisfies("~ 1.0.3alpha", "1.0.12", loose = true)
        assertSatisfies(">=1", "1.0.0")
        assertSatisfies(">= 1", "1.0.0")
        assertSatisfies("<1.2", "1.1.1")
        assertSatisfies("< 1.2", "1.1.1")
        assertSatisfies("~v0.5.4-pre", "0.5.5")
        assertSatisfies("~v0.5.4-pre", "0.5.4")
        assertSatisfies("=0.7.x", "0.7.2")
        assertSatisfies("<=0.7.x", "0.7.2")
        assertSatisfies(">=0.7.x", "0.7.2")
        assertSatisfies("<=0.7.x", "0.6.2")
        assertSatisfies("~1.2.1 >=1.2.3", "1.2.3")
        assertSatisfies("~1.2.1 =1.2.3", "1.2.3")
        assertSatisfies("~1.2.1 1.2.3", "1.2.3")
        assertSatisfies("~1.2.1 >=1.2.3 1.2.3", "1.2.3")
        assertSatisfies("~1.2.1 1.2.3 >=1.2.3", "1.2.3")
        assertSatisfies(">=1.2.1 1.2.3", "1.2.3")
        assertSatisfies("1.2.3 >=1.2.1", "1.2.3")
        assertSatisfies(">=1.2.3 >=1.2.1", "1.2.3")
        assertSatisfies(">=1.2.1 >=1.2.3", "1.2.3")
        assertSatisfies(">=1.2", "1.2.8")
        assertSatisfies("^1.2.3", "1.8.1")
        assertSatisfies("^0.1.2", "0.1.2")
        assertSatisfies("^0.1", "0.1.2")
        assertSatisfies("^0.0.1", "0.0.1")
        assertSatisfies("^1.2", "1.4.2")
        assertSatisfies("^1.2 ^1", "1.4.2")
        assertSatisfies("^1.2.3-alpha", "1.2.3-pre")
        assertSatisfies("^1.2.0-alpha", "1.2.0-pre")
        assertSatisfies("^0.0.1-alpha", "0.0.1-beta")
        assertSatisfies("^0.0.1-alpha", "0.0.1")
        assertSatisfies("^0.1.1-alpha", "0.1.1-beta")
        assertSatisfies("^x", "1.2.3")
        assertSatisfies("x - 1.0.0", "0.9.7")
        assertSatisfies("x - 1.x", "0.9.7")
        assertSatisfies("1.0.0 - x", "1.9.7")
        assertSatisfies("1.x - x", "1.9.7")
        assertSatisfies("<=7.x", "7.9.9")
        assertSatisfies("2.x", "2.0.0-pre.0", includePreRelease = true)
        assertSatisfies("2.x", "2.1.0-pre.0", includePreRelease = true)
        assertSatisfies("1.1.x", "1.1.0-a", includePreRelease = true)
        assertSatisfies("1.1.x", "1.1.1-a", includePreRelease = true)
        assertSatisfies("*", "1.0.0-rc1", includePreRelease = true)
        assertSatisfies("^1.0.0-0", "1.0.1-rc1", includePreRelease = true)
        assertSatisfies("^1.0.0-rc2", "1.0.1-rc1", includePreRelease = true)
        assertSatisfies("^1.0.0", "1.0.1-rc1", includePreRelease = true)
        assertSatisfies("^1.0.0", "1.1.0-rc1", includePreRelease = true)
        assertSatisfies("1 - 2", "2.0.0-pre", includePreRelease = true)
        assertSatisfies("1 - 2", "1.0.0-pre", includePreRelease = true)
        assertSatisfies("1.0 - 2", "1.0.0-pre", includePreRelease = true)

        assertSatisfies("=0.7.x", "0.7.0-asdf", includePreRelease = true)
        assertSatisfies(">=0.7.x", "0.7.0-asdf", includePreRelease = true)
        assertSatisfies("<=0.7.x", "0.7.0-asdf", includePreRelease = true)

        assertSatisfies(">=1.0.0 <=1.1.0", "1.1.0-pre", includePreRelease = true)
    }

    @Test
    fun testExclude() {
        // https://github.com/npm/node-semver/blob/120968b76760cb0db85a72bde2adedd0e9628793/test/fixtures/range-exclude.js
        fun assertDoesNotSatisfy(constraint: String, version: String, loose: Boolean = false, includePreRelease: Boolean = false) {
            val format = NpmConstraintFormat.Custom(
                isStrict = !loose,
                includePreRelease = includePreRelease
            )

            val constraint = VersionConstraint.parse(constraint, format)
            val version = Version.parse(version, strict = !loose)

            if (constraint.isSatisfiedBy(version, includePreRelease = includePreRelease)) {
                fail("Version '${version}' satisfies constraint '${constraint}'")
            }
        }

        assertDoesNotSatisfy("1.0.0 - 2.0.0", "2.2.3")
        assertDoesNotSatisfy("1.2.3+asdf - 2.4.3+asdf", "1.2.3-pre.2")
        assertDoesNotSatisfy("1.2.3+asdf - 2.4.3+asdf", "2.4.3-alpha")
        assertDoesNotSatisfy("^1.2.3+build", "2.0.0")
        assertDoesNotSatisfy("^1.2.3+build", "1.2.0")
        assertDoesNotSatisfy("^1.2.3", "1.2.3-pre")
        assertDoesNotSatisfy("^1.2", "1.2.0-pre")
        assertDoesNotSatisfy(">1.2", "1.3.0-beta")
        assertDoesNotSatisfy("<=1.2.3", "1.2.3-beta")
        assertDoesNotSatisfy("^1.2.3", "1.2.3-beta")
        assertDoesNotSatisfy("=0.7.x", "0.7.0-asdf")
        assertDoesNotSatisfy(">=0.7.x", "0.7.0-asdf")
        assertDoesNotSatisfy("<=0.7.x", "0.7.0-asdf")
        assertDoesNotSatisfy("1", "1.0.0beta", loose = true)
        assertDoesNotSatisfy("<1", "1.0.0beta", loose = true)
        assertDoesNotSatisfy("< 1", "1.0.0beta", loose = true)
        assertDoesNotSatisfy("1.0.0", "1.0.1")
        assertDoesNotSatisfy(">=1.0.0", "0.0.0")
        assertDoesNotSatisfy(">=1.0.0", "0.0.1")
        assertDoesNotSatisfy(">=1.0.0", "0.1.0")
        assertDoesNotSatisfy(">1.0.0", "0.0.1")
        assertDoesNotSatisfy(">1.0.0", "0.1.0")
        assertDoesNotSatisfy("<=2.0.0", "3.0.0")
        assertDoesNotSatisfy("<=2.0.0", "2.9999.9999")
        assertDoesNotSatisfy("<=2.0.0", "2.2.9")
        assertDoesNotSatisfy("<2.0.0", "2.9999.9999")
        assertDoesNotSatisfy("<2.0.0", "2.2.9")
        assertDoesNotSatisfy(">=0.1.97", "v0.1.93", loose = true)
        assertDoesNotSatisfy(">=0.1.97", "0.1.93")
        assertDoesNotSatisfy("0.1.20 || 1.2.4", "1.2.3")
        assertDoesNotSatisfy(">=0.2.3 || <0.0.1", "0.0.3")
        assertDoesNotSatisfy(">=0.2.3 || <0.0.1", "0.2.2")
        assertDoesNotSatisfy("2.x.x", "1.1.3")
        assertDoesNotSatisfy("2.x.x", "3.1.3")
        assertDoesNotSatisfy("1.2.x", "1.3.3")
        assertDoesNotSatisfy("1.2.x || 2.x", "3.1.3")
        assertDoesNotSatisfy("1.2.x || 2.x", "1.1.3")
        assertDoesNotSatisfy("2.*.*", "1.1.3")
        assertDoesNotSatisfy("2.*.*", "3.1.3")
        assertDoesNotSatisfy("1.2.*", "1.3.3")
        assertDoesNotSatisfy("1.2.* || 2.*", "3.1.3")
        assertDoesNotSatisfy("1.2.* || 2.*", "1.1.3")
        assertDoesNotSatisfy("2", "1.1.2")
        assertDoesNotSatisfy("2.3", "2.4.1")
        assertDoesNotSatisfy("~0.0.1", "0.1.0-alpha")
        assertDoesNotSatisfy("~0.0.1", "0.1.0")
        assertDoesNotSatisfy("~2.4", "2.5.0") // >=2.4.0 <2.5.0
        assertDoesNotSatisfy("~2.4", "2.3.9")
        assertDoesNotSatisfy("~>3.2.1", "3.3.2") // >=3.2.1 <3.3.0
        assertDoesNotSatisfy("~>3.2.1", "3.2.0") // >=3.2.1 <3.3.0
        assertDoesNotSatisfy("~1", "0.2.3") // >=1.0.0 <2.0.0
        assertDoesNotSatisfy("~>1", "2.2.3")
        assertDoesNotSatisfy("~1.0", "1.1.0") // >=1.0.0 <1.1.0
        assertDoesNotSatisfy("<1", "1.0.0")
        assertDoesNotSatisfy(">=1.2", "1.1.1")
        assertDoesNotSatisfy("1", "2.0.0beta", loose = true)
        assertDoesNotSatisfy("~v0.5.4-beta", "0.5.4-alpha")
        assertDoesNotSatisfy("=0.7.x", "0.8.2")
        assertDoesNotSatisfy(">=0.7.x", "0.6.2")
        assertDoesNotSatisfy("<0.7.x", "0.7.2")
        assertDoesNotSatisfy("<1.2.3", "1.2.3-beta")
        assertDoesNotSatisfy("=1.2.3", "1.2.3-beta")
        assertDoesNotSatisfy(">1.2", "1.2.8")
        assertDoesNotSatisfy("^0.0.1", "0.0.2-alpha")
        assertDoesNotSatisfy("^0.0.1", "0.0.2")
        assertDoesNotSatisfy("^1.2.3", "2.0.0-alpha")
        assertDoesNotSatisfy("^1.2.3", "1.2.2")
        assertDoesNotSatisfy("^1.2", "1.1.9")
        assertDoesNotSatisfy("*", "v1.2.3-foo", loose = true)

        // invalid versions never satisfy, but shouldn't throw
//        ['*', 'not a version')
//        ['>=2', 'glorp')
//        ['>=2', false)
//
//        ['2.x', '3.0.0-pre.0', { includePrerelease: true })
//        ['^1.0.0', '1.0.0-rc1', { includePrerelease: true })
//        ['^1.0.0', '2.0.0-rc1', { includePrerelease: true })
//        ['^1.2.3-rc2', '2.0.0', { includePrerelease: true })
//        ['^1.0.0', '2.0.0-rc1')
//
//        ['1 - 2', '3.0.0-pre', { includePrerelease: true })
//        ['1 - 2', '2.0.0-pre')
//        ['1 - 2', '1.0.0-pre')
//        ['1.0 - 2', '1.0.0-pre')
//
//        assertDoesNotSatisfy("1.1.x", '1.0.0-a')
//        assertDoesNotSatisfy("1.1.x", '1.1.0-a')
//        assertDoesNotSatisfy("1.1.x", '1.2.0-a')
//        assertDoesNotSatisfy("1.1.x", '1.2.0-a', { includePrerelease: true })
//        assertDoesNotSatisfy("1.1.x", '1.0.0-a', { includePrerelease: true })
//        assertDoesNotSatisfy("1.x", '1.0.0-a')
//        assertDoesNotSatisfy("1.x", '1.1.0-a')
//        assertDoesNotSatisfy("1.x", '1.2.0-a')
//        assertDoesNotSatisfy("1.x", '0.0.0-a', { includePrerelease: true })
//        assertDoesNotSatisfy("1.x", '2.0.0-a', { includePrerelease: true })
//
//        ['>=1.0.0 <1.1.0', '1.1.0')
//        ['>=1.0.0 <1.1.0', '1.1.0', { includePrerelease: true })
//        ['>=1.0.0 <1.1.0', '1.1.0-pre')
//        ['>=1.0.0 <1.1.0-pre', '1.1.0-pre')
//
//        ['== 1.0.0 || foo', '2.0.0', { loose: true })
    }

}
