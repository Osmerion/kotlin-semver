package com.osmerion.kotlin.semver

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Built-in [kotlinx.serialization] serializer that encodes and decodes [Version] as its string representation.
 *
 * @sample com.osmerion.kotlin.semver.samples.VersionSamples.serialization
 * @sample com.osmerion.kotlin.semver.samples.VersionSamples.deserialization
 */
public object VersionSerializer : KSerializer<Version> {
    override fun deserialize(decoder: Decoder): Version = decoder.decodeString().toVersion()
    override fun serialize(encoder: Encoder, value: Version): Unit = encoder.encodeString(value.toString())
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Version", PrimitiveKind.STRING)
}

/**
 * Built-in [kotlinx.serialization] serializer that encodes and decodes
 * non-strict [Version] as its string representation.
 *
 * @sample com.osmerion.kotlin.semver.samples.VersionSamples.looseSerialization
 * @sample com.osmerion.kotlin.semver.samples.VersionSamples.looseDeserialization
 */
public object LooseVersionSerializer : KSerializer<Version> {
    override fun deserialize(decoder: Decoder): Version = decoder.decodeString().toVersion(strict = false)
    override fun serialize(encoder: Encoder, value: Version): Unit = encoder.encodeString(value.toString())
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("LooseVersion", PrimitiveKind.STRING)
}
