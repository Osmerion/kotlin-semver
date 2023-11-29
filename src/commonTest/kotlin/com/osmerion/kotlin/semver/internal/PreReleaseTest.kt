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
package com.osmerion.kotlin.semver.internal

import com.osmerion.kotlin.semver.VersionFormatException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals

class PreReleaseTest {

    @Test
    fun testConstructorValidation() {
        assertFailsWith<VersionFormatException> { PreRelease(".alpha") }
        assertFailsWith<VersionFormatException> { PreRelease("alpha.") }
        assertFailsWith<VersionFormatException> { PreRelease(".alpha.") }
        assertFailsWith<VersionFormatException> { PreRelease("alpha. ") }
        assertFailsWith<VersionFormatException> { PreRelease("alpha.01") }
        assertFailsWith<VersionFormatException> { PreRelease("+alpha.01") }
        assertFailsWith<VersionFormatException> { PreRelease("%alpha") }
    }

    @Test
    fun testEquals() {
        assertEquals(PreRelease("alpha-3.Beta.0"), PreRelease("alpha-3.Beta.0"))
        assertNotEquals(PreRelease("alpha-3.Beta.0"), PreRelease("alpha-3.Beta.1"))
        assertFalse(PreRelease("alpha-3.Beta.1").equals(null))
    }

    @Test
    fun testToSmallestLargerVersion() {
        assertEquals("alpha.0", PreRelease("alpha").toSmallestLargerVersion().toString())
        assertEquals("alpha-0.0", PreRelease("alpha-0").toSmallestLargerVersion().toString())
    }

}