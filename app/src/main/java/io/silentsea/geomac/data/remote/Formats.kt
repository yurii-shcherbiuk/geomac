package io.silentsea.geomac.data.remote

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.protobuf.ProtoBuf
import nl.adaptivity.xmlutil.XmlDeclMode
import nl.adaptivity.xmlutil.serialization.XML
import nl.adaptivity.xmlutil.serialization.XmlSerializationPolicy

@OptIn(ExperimentalSerializationApi::class)
internal val ProtoFormat = ProtoBuf { encodeDefaults = true }

internal val XmlFormat = XML.v1 {
    xmlDeclMode = XmlDeclMode.None

    policy {
        autoPolymorphic = true
        encodeDefault = XmlSerializationPolicy.XmlEncodeDefault.ALWAYS
        ignoreUnknownChildren()
    }
}

internal val JsonFormat = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
}
