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
import com.osmerion.kotlin.semver.constraints.ExperimentalConstraintApi
import com.osmerion.kotlin.semver.constraints.npm.internal.CaretVersionRange
import com.osmerion.kotlin.semver.constraints.npm.internal.ComparatorPredicate
import com.osmerion.kotlin.semver.constraints.npm.internal.HyphenVersionRange
import com.osmerion.kotlin.semver.constraints.npm.internal.Op
import com.osmerion.kotlin.semver.constraints.npm.internal.TildeVersionRange
import com.osmerion.kotlin.semver.constraints.npm.internal.parseNpmVersionDescriptor
import com.osmerion.kotlin.semver.internal.VersionRange
import com.osmerion.kotlin.semver.internal.toVersionRanges
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalConstraintApi::class)
class PredicatesTest {

    @Test
    fun testCaretVersionRange() {
        val format = NpmConstraintFormat

        assertEquals(
            listOf(VersionRange(Version(1), Version(2, 0, 0, preRelease = "0"))),
            listOf(listOf(CaretVersionRange(format.parseNpmVersionDescriptor("1")))).toVersionRanges()
        )

        assertEquals(
            listOf(VersionRange(Version(1, 2), Version(2, 0, 0, preRelease = "0"))),
            listOf(listOf(CaretVersionRange(format.parseNpmVersionDescriptor("1.2")))).toVersionRanges()
        )

        assertEquals(
            listOf(VersionRange(Version(1, 2, 3), Version(2, 0, 0, preRelease = "0"))),
            listOf(listOf(CaretVersionRange(format.parseNpmVersionDescriptor("1.2.3")))).toVersionRanges()
        )

        assertEquals(
            listOf(VersionRange(Version(1, 2, 3, preRelease = "1"), Version(2, 0, 0, preRelease = "0"))),
            listOf(listOf(CaretVersionRange(format.parseNpmVersionDescriptor("1.2.3-1")))).toVersionRanges()
        )

        assertEquals(
            listOf(VersionRange(Version(0, 0, 1), Version(0, 0, 2, preRelease = "0"))),
            listOf(listOf(CaretVersionRange(format.parseNpmVersionDescriptor("0.0.1")))).toVersionRanges()
        )

        assertEquals(
            listOf(VersionRange(Version(0, 1, 0), Version(0, 2, 0, preRelease = "0"))),
            listOf(listOf(CaretVersionRange(format.parseNpmVersionDescriptor("0.1.0")))).toVersionRanges()
        )
    }

    @Test
    fun testComparator() {
        val format = NpmConstraintFormat

        // EQUAL
        assertEquals(
            listOf(VersionRange(Version(1), Version(1, 0, 1, preRelease = "0"))),
            listOf(listOf(ComparatorPredicate(format, format.parseNpmVersionDescriptor("1"), Op.EQUAL))).toVersionRanges()
        )

        assertEquals(
            listOf(VersionRange(Version(1, 2), Version(1, 2, 1, preRelease = "0"))),
            listOf(listOf(ComparatorPredicate(format, format.parseNpmVersionDescriptor("1.2"), Op.EQUAL))).toVersionRanges()
        )

        assertEquals(
            listOf(VersionRange(Version(1, 2, 3), Version(1, 2, 4, preRelease = "0"))),
            listOf(listOf(ComparatorPredicate(format, format.parseNpmVersionDescriptor("1.2.3"), Op.EQUAL))).toVersionRanges()
        )

        assertEquals(
            listOf(VersionRange(Version(1, 2, 3, preRelease = "1"), Version(1, 2, 3, preRelease = "1.0"))),
            listOf(listOf(ComparatorPredicate(format, format.parseNpmVersionDescriptor("1.2.3-1"), Op.EQUAL))).toVersionRanges()
        )

        // LESS_THAN
        assertEquals(
            listOf(VersionRange(null, Version(1, 0, 0, preRelease = "0"))),
            listOf(listOf(ComparatorPredicate(format, format.parseNpmVersionDescriptor("1"), Op.LESS_THAN))).toVersionRanges()
        )

        assertEquals(
            listOf(VersionRange(null, Version(1, 2, 0, preRelease = "0"))),
            listOf(listOf(ComparatorPredicate(format, format.parseNpmVersionDescriptor("1.2"), Op.LESS_THAN))).toVersionRanges()
        )

        assertEquals(
            listOf(VersionRange(null, Version(1, 2, 3))),
            listOf(listOf(ComparatorPredicate(format, format.parseNpmVersionDescriptor("1.2.3"), Op.LESS_THAN))).toVersionRanges()
        )

        assertEquals(
            listOf(VersionRange(null, Version(1, 2, 3, preRelease = "1"))),
            listOf(listOf(ComparatorPredicate(format, format.parseNpmVersionDescriptor("1.2.3-1"), Op.LESS_THAN))).toVersionRanges()
        )

        // LESS_THAN_OR_EQUAL
        assertEquals(
            listOf(VersionRange(null, Version(2, 0, 0, "0"))),
            listOf(listOf(ComparatorPredicate(format, format.parseNpmVersionDescriptor("1"), Op.LESS_THAN_OR_EQUAL))).toVersionRanges()
        )

        assertEquals(
            listOf(VersionRange(null, Version(1, 3, 0, "0"))),
            listOf(listOf(ComparatorPredicate(format, format.parseNpmVersionDescriptor("1.2"), Op.LESS_THAN_OR_EQUAL))).toVersionRanges()
        )

        assertEquals(
            listOf(VersionRange(null, Version(1, 2, 4, "0"))),
            listOf(listOf(ComparatorPredicate(format, format.parseNpmVersionDescriptor("1.2.3"), Op.LESS_THAN_OR_EQUAL))).toVersionRanges()
        )

        assertEquals(
            listOf(VersionRange(null, Version(1, 2, 3, preRelease = "1.0"))),
            listOf(listOf(ComparatorPredicate(format, format.parseNpmVersionDescriptor("1.2.3-1"), Op.LESS_THAN_OR_EQUAL))).toVersionRanges()
        )

        // GREATER_THAN
        assertEquals(
            listOf(VersionRange(Version(2, 0, 0), null)),
            listOf(listOf(ComparatorPredicate(format, format.parseNpmVersionDescriptor("1"), Op.GREATER_THAN))).toVersionRanges()
        )

        assertEquals(
            listOf(VersionRange(Version(1, 3, 0), null)),
            listOf(listOf(ComparatorPredicate(format, format.parseNpmVersionDescriptor("1.2"), Op.GREATER_THAN))).toVersionRanges()
        )

        assertEquals(
            listOf(VersionRange(Version(1, 2, 4), null)),
            listOf(listOf(ComparatorPredicate(format, format.parseNpmVersionDescriptor("1.2.3"), Op.GREATER_THAN))).toVersionRanges()
        )

        assertEquals(
            listOf(VersionRange(Version(1, 2, 3, preRelease = "1.0"), null)),
            listOf(listOf(ComparatorPredicate(format, format.parseNpmVersionDescriptor("1.2.3-1"), Op.GREATER_THAN))).toVersionRanges()
        )

        // GREATER_THAN_OR_EQUAL
        assertEquals(
            listOf(VersionRange(Version(1, 0, 0), null)),
            listOf(listOf(ComparatorPredicate(format, format.parseNpmVersionDescriptor("1"), Op.GREATER_THAN_OR_EQUAL))).toVersionRanges()
        )

        assertEquals(
            listOf(VersionRange(Version(1, 2, 0), null)),
            listOf(listOf(ComparatorPredicate(format, format.parseNpmVersionDescriptor("1.2"), Op.GREATER_THAN_OR_EQUAL))).toVersionRanges()
        )

        assertEquals(
            listOf(VersionRange(Version(1, 2, 3), null)),
            listOf(listOf(ComparatorPredicate(format, format.parseNpmVersionDescriptor("1.2.3"), Op.GREATER_THAN_OR_EQUAL))).toVersionRanges()
        )

        assertEquals(
            listOf(VersionRange(Version(1, 2, 3, preRelease = "1"), null)),
            listOf(listOf(ComparatorPredicate(format, format.parseNpmVersionDescriptor("1.2.3-1"), Op.GREATER_THAN_OR_EQUAL))).toVersionRanges()
        )
    }

    @Test
    fun testHyphenVersionRange() {
        val format = NpmConstraintFormat

        assertEquals(
            listOf(VersionRange(Version(1), Version(3, 0, 0, preRelease = "0"))),
            listOf(listOf(HyphenVersionRange(format, format.parseNpmVersionDescriptor("1"), format.parseNpmVersionDescriptor("2")))).toVersionRanges()
        )

        assertEquals(
            listOf(VersionRange(Version(1), Version(2, 4, 0, preRelease = "0"))),
            listOf(listOf(HyphenVersionRange(format, format.parseNpmVersionDescriptor("1"), format.parseNpmVersionDescriptor("2.3")))).toVersionRanges()
        )

        assertEquals(
            listOf(VersionRange(Version(1), Version(2, 3, 5, preRelease = "0"))),
            listOf(listOf(HyphenVersionRange(format, format.parseNpmVersionDescriptor("1"), format.parseNpmVersionDescriptor("2.3.4")))).toVersionRanges()
        )

        assertEquals(
            listOf(VersionRange(Version(1), Version(2, 3, 4, preRelease = "5.0"))),
            listOf(listOf(HyphenVersionRange(format, format.parseNpmVersionDescriptor("1"), format.parseNpmVersionDescriptor("2.3.4-5")))).toVersionRanges()
        )
    }

    @Test
    fun testTildeVersionRange() {
        val format = NpmConstraintFormat

        assertEquals(
            listOf(VersionRange(Version(1), Version(2, 0, 0, preRelease = "0"))),
            listOf(listOf(TildeVersionRange(format, format.parseNpmVersionDescriptor("1")))).toVersionRanges()
        )

        assertEquals(
            listOf(VersionRange(Version(1, 2), Version(1, 3, 0, preRelease = "0"))),
            listOf(listOf(TildeVersionRange(format, format.parseNpmVersionDescriptor("1.2")))).toVersionRanges()
        )

        assertEquals(
            listOf(VersionRange(Version(1, 2, 3), Version(1, 3, 0, preRelease = "0"))),
            listOf(listOf(TildeVersionRange(format, format.parseNpmVersionDescriptor("1.2.3")))).toVersionRanges()
        )

        assertEquals(
            listOf(VersionRange(Version(1, 2, 3, preRelease = "1"), Version(1, 3, 0, preRelease = "0"))),
            listOf(listOf(TildeVersionRange(format, format.parseNpmVersionDescriptor("1.2.3-1")))).toVersionRanges()
        )
    }

}
