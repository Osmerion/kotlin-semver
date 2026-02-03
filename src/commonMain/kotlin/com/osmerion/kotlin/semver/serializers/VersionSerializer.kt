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
package com.osmerion.kotlin.semver.serializers

import com.osmerion.kotlin.semver.Version
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
 *
 * @since   0.1.0
 */
public object VersionSerializer : KSerializer<Version> {

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("SemanticVersion", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Version = Version.parse(decoder.decodeString())
    override fun serialize(encoder: Encoder, value: Version): Unit = encoder.encodeString(value.toString())

}

/**
 * Built-in [kotlinx.serialization] serializer that encodes and decodes non-strict [Version] as its string
 * representation.
 *
 * @sample com.osmerion.kotlin.semver.samples.VersionSamples.looseSerialization
 * @sample com.osmerion.kotlin.semver.samples.VersionSamples.looseDeserialization
 *
 * @since   0.1.0
 */
public object LooseVersionSerializer : KSerializer<Version> {

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("LooseSemanticVersion", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Version = Version.parse(decoder.decodeString(), strict = false)
    override fun serialize(encoder: Encoder, value: Version): Unit = encoder.encodeString(value.toString())

}
