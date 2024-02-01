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
package com.osmerion.kotlin.semver.serializers

import com.osmerion.kotlin.semver.SemanticVersion
import com.osmerion.kotlin.semver.VersionConstraint
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class SerializationTests {

    @Serializable
    data class ToSerialize(val version: SemanticVersion)

    @Serializable
    data class ToLooseSerialize(
        @Serializable(with = LooseVersionSerializer::class)
        val version: SemanticVersion
    )

    @Serializable
    data class ToConstraintSerialize(val constraint: VersionConstraint)

    @Test
    fun testVersionSerialization() {
        val encoded = Json.encodeToString(SemanticVersion.parse("1.0.0-alpha.1+build.3"))
        assertEquals("\"1.0.0-alpha.1+build.3\"", encoded)
    }

    @Test
    fun testVersionDeserialization() {
        val decoded = Json.decodeFromString<SemanticVersion>("\"1.0.0-alpha.1+build.3\"")
        assertEquals(SemanticVersion.parse("1.0.0-alpha.1+build.3"), decoded)
    }

    @Test
    fun testMemberVersionSerialization() {
        val obj = ToSerialize(version = SemanticVersion.parse("1.0.0-alpha.1+build.3"))
        val encoded = Json.encodeToString(obj)
        assertEquals("{\"version\":\"1.0.0-alpha.1+build.3\"}", encoded)
    }

    @Test
    fun testMemberVersionDeserialization() {
        val decoded = Json.decodeFromString<ToSerialize>("{\"version\":\"1.0.0-alpha.1+build.3\"}")
        assertEquals(SemanticVersion.parse("1.0.0-alpha.1+build.3"), decoded.version)
    }

    @Test
    fun testMemberLooseVersionSerialization() {
        val obj = ToLooseSerialize(version = SemanticVersion.parse("1-alpha.1+build.3", strict = false))
        val encoded = Json.encodeToString(obj)
        assertEquals("{\"version\":\"1.0.0-alpha.1+build.3\"}", encoded)
    }

    @Test
    fun testMemberLooseVersionDeserialization() {
        val decoded = Json.decodeFromString<ToLooseSerialize>("{\"version\":\"1-alpha.1+build.3\"}")
        assertEquals(SemanticVersion.parse("1-alpha.1+build.3", strict = false), decoded.version)
    }

    @Test
    fun testConstraintSerialization() {
        val encoded = Json.encodeToString(VersionConstraint.parse("> 1.2.3"))
        assertEquals("\">1.2.3\"", encoded)
    }

    @Test
    fun testConstraintDeserialization() {
        val decoded = Json.decodeFromString<VersionConstraint>("\"> 1.2.3\"")
        assertEquals(VersionConstraint.parse("> 1.2.3"), decoded)
    }

    @Test
    fun testMemberConstraintSerialization() {
        val obj = ToConstraintSerialize(constraint = VersionConstraint.parse("> 1.2.3"))
        val encoded = Json.encodeToString(obj)
        assertEquals("{\"constraint\":\">1.2.3\"}", encoded)
    }

    @Test
    fun testMemberConstraintDeserialization() {
        val decoded = Json.decodeFromString<ToConstraintSerialize>("{\"constraint\":\"> 1.2.3\"}")
        assertEquals(VersionConstraint.parse("> 1.2.3"), decoded.constraint)
    }

}