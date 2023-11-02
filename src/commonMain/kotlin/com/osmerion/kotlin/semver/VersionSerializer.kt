package com.osmerion.kotlin.semver

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Built-in [kotlinx.serialization] serializer that encodes and decodes [SemanticVersion] as its string representation.
 *
 * @sample com.osmerion.kotlin.semver.samples.VersionSamples.serialization
 * @sample com.osmerion.kotlin.semver.samples.VersionSamples.deserialization
 */
public object VersionSerializer : KSerializer<SemanticVersion> {
    override fun deserialize(decoder: Decoder): SemanticVersion = decoder.decodeString().toVersion()
    override fun serialize(encoder: Encoder, value: SemanticVersion): Unit = encoder.encodeString(value.toString())
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Version", PrimitiveKind.STRING)
}

/**
 * Built-in [kotlinx.serialization] serializer that encodes and decodes
 * non-strict [SemanticVersion] as its string representation.
 *
 * @sample com.osmerion.kotlin.semver.samples.VersionSamples.looseSerialization
 * @sample com.osmerion.kotlin.semver.samples.VersionSamples.looseDeserialization
 */
public object LooseVersionSerializer : KSerializer<SemanticVersion> {
    override fun deserialize(decoder: Decoder): SemanticVersion = decoder.decodeString().toVersion(strict = false)
    override fun serialize(encoder: Encoder, value: SemanticVersion): Unit = encoder.encodeString(value.toString())
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("LooseVersion", PrimitiveKind.STRING)
}
