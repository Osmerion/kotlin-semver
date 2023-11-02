package com.osmerion.kotlin.semver

import com.osmerion.kotlin.semver.constraints.Constraint
import com.osmerion.kotlin.semver.constraints.toConstraint
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
    data class ToConstraintSerialize(val constraint: Constraint)

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
        val encoded = Json.encodeToString("> 1.2.3".toConstraint())
        assertEquals("\">1.2.3\"", encoded)
    }

    @Test
    fun testConstraintDeserialization() {
        val decoded = Json.decodeFromString<Constraint>("\"> 1.2.3\"")
        assertEquals("> 1.2.3".toConstraint(), decoded)
    }

    @Test
    fun testMemberConstraintSerialization() {
        val obj = ToConstraintSerialize(constraint = "> 1.2.3".toConstraint())
        val encoded = Json.encodeToString(obj)
        assertEquals("{\"constraint\":\">1.2.3\"}", encoded)
    }

    @Test
    fun testMemberConstraintDeserialization() {
        val decoded = Json.decodeFromString<ToConstraintSerialize>("{\"constraint\":\"> 1.2.3\"}")
        assertEquals("> 1.2.3".toConstraint(), decoded.constraint)
    }
}
