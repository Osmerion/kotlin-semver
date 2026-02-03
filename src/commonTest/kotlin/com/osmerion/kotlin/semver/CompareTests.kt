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

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class CompareTests {

    @Test
    fun testLessThanByNumbers() {
        val version = Version.parse("5.2.3")
        assertTrue { version < Version.parse("6.0.0") }
        assertTrue { version < Version.parse("5.3.3") }
        assertTrue { version < Version.parse("5.2.4") }
    }

    @Test
    fun testLessThanByPreRelease() {
        val version = Version.parse("5.2.3-alpha.2")
        assertTrue { version < Version.parse("5.2.3-alpha.2.a") } // by pre-release part count
        assertTrue { version < Version.parse("5.2.3-alpha.3") } // by pre-release number comparison
        assertTrue { version < Version.parse("5.2.3-beta") } // by pre-release alphabetical comparison
        assertTrue { version <= Version.parse("5.2.3-alpha.2") }
    }

    @Test
    fun testPrecedenceFromSpec() {
        assertTrue { Version.parse("1.0.0") < Version.parse("2.0.0") }
        assertTrue { Version.parse("2.0.0") < Version.parse("2.1.0") }
        assertTrue { Version.parse("2.1.0") < Version.parse("2.1.1") }

        assertTrue { Version.parse("1.0.0-alpha") < Version.parse("1.0.0") }

        assertTrue { Version.parse("1.0.0-alpha") < Version.parse("1.0.0-alpha.1") }
        assertTrue { Version.parse("1.0.0-alpha.1") < Version.parse("1.0.0-alpha.beta") }
        assertTrue { Version.parse("1.0.0-alpha.beta") < Version.parse("1.0.0-beta") }
        assertTrue { Version.parse("1.0.0-beta") < Version.parse("1.0.0-beta.2") }
        assertTrue { Version.parse("1.0.0-beta.2") < Version.parse("1.0.0-beta.11") }
        assertTrue { Version.parse("1.0.0-beta.11") < Version.parse("1.0.0-rc.1") }
        assertTrue { Version.parse("1.0.0-rc.1") < Version.parse("1.0.0") }
    }

    @Test
    fun testCompareByPreReleaseNumberAlphabetical() {
        assertTrue { Version.parse("5.2.3-alpha.2") < Version.parse("5.2.3-alpha.a") }
        assertTrue { Version.parse("5.2.3-alpha.a") > Version.parse("5.2.3-alpha.2") }
    }

    @Test
    fun testCompareByPreReleaseAndStable() {
        assertTrue { Version.parse("5.2.3-alpha") < Version.parse("5.2.3") }
        assertTrue { Version.parse("5.2.3") > Version.parse("5.2.3-alpha") }
    }

    @Test
    fun testGreaterThanByNumbers() {
        val version = Version.parse("5.2.3")
        assertTrue { version > Version.parse("4.0.0") }
        assertTrue { version > Version.parse("5.1.3") }
        assertTrue { version > Version.parse("5.2.2") }
    }

    @Test
    fun testGreaterThanByPreRelease() {
        val version = Version.parse("5.2.3-alpha.2")
        assertTrue { version > Version.parse("5.2.3-alpha") } // by pre-release part count
        assertTrue { version > Version.parse("5.2.3-alpha.1") } // by pre-release number comparison
        assertTrue { version > Version.parse("5.2.3-a") } // by pre-release alphabetical comparison
        assertTrue { version >= Version.parse("5.2.3-alpha.2") }
    }

    @Test
    fun testEqual() {
        assertEquals(Version.parse("5.2.3-alpha.2"), Version.parse("5.2.3-alpha.2"))
        assertNotEquals(Version.parse("5.2.3-alpha.2"), Version.parse("5.2.3-alpha.5"))
        assertEquals(Version.parse("5.2.3"), Version.parse("5.2.3"))
        assertNotEquals(Version.parse("5.2.3"), Version.parse("5.2.4"))
        assertEquals(Version.parse("0.0.0"), Version.parse("0.0.0"))
        assertEquals(Version.parse("0.0.0-alpha.2").hashCode(), Version.parse("0.0.0-alpha.2").hashCode())
        assertEquals(Version.parse("0.0.0").hashCode(), Version.parse("0.0.0").hashCode())
        assertFalse(Version.parse("0.0.0").equals(null))
    }

    @Test
    fun testHashcodeIgnoreBuild() {
        assertEquals(Version.parse("5.2.3-alpha.2+build.34").hashCode(), Version.parse("5.2.3-alpha.2").hashCode())
        assertEquals(Version.parse("5.2.3-alpha.2+build.34").hashCode(), Version.parse("5.2.3-alpha.2+build.35").hashCode())
    }

    @Test
    fun testListOrder() {
        val list: List<Version> = listOf(
            Version.parse("1.0.1"),
            Version.parse("1.0.1-alpha"),
            Version.parse("1.0.1-alpha.beta"),
            Version.parse("1.0.1-alpha.3"),
            Version.parse("1.0.1-alpha.2"),
            Version.parse("1.1.0"),
            Version.parse("1.1.0+build"),
        )

        assertEquals(
            listOf(
                Version.parse("1.0.1-alpha"),
                Version.parse("1.0.1-alpha.2"),
                Version.parse("1.0.1-alpha.3"),
                Version.parse("1.0.1-alpha.beta"),
                Version.parse("1.0.1"),
                Version.parse("1.1.0"),
                Version.parse("1.1.0+build"),
            ),
            list.sorted()
        )
    }

}
