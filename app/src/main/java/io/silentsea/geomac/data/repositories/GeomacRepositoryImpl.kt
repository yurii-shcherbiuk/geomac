package io.silentsea.geomac.data.repositories

import androidx.paging.PagingSource
import androidx.room.RoomRawQuery
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.silentsea.geomac.core.Const
import io.silentsea.geomac.data.db.GeomacDao
import io.silentsea.geomac.data.db.entities.GeomacCoordinates
import io.silentsea.geomac.data.db.entities.GeomacItem
import io.silentsea.geomac.data.db.entities.GeomacItemWithCoordinates
import io.silentsea.geomac.domain.entites.AppleRequest
import io.silentsea.geomac.domain.entites.AppleResponse
import io.silentsea.geomac.domain.entites.GoogleRequest
import io.silentsea.geomac.domain.entites.GoogleResponse
import io.silentsea.geomac.domain.entites.MicrosoftRequest
import io.silentsea.geomac.domain.entites.MicrosoftResponse
import io.silentsea.geomac.domain.entites.MylnikovResponse
import io.silentsea.geomac.domain.entites.Services
import io.silentsea.geomac.domain.entites.YandexResponse
import io.silentsea.geomac.domain.repositories.GeomacRepository
import io.silentsea.geomac.utils.indexOf
import io.silentsea.geomac.utils.macString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.protobuf.ProtoBuf
import nl.adaptivity.xmlutil.serialization.XML
import nl.adaptivity.xmlutil.serialization.XmlSerializationPolicy
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import kotlin.math.pow
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class GeomacRepositoryImpl : GeomacRepository, KoinComponent {
    private val client: HttpClient by inject()
    private val geomacDao: GeomacDao by inject()

    @OptIn(ExperimentalSerializationApi::class)
    private val protobuf = ProtoBuf { encodeDefaults = true }

    private val xml = XML {
        defaultPolicy {
            autoPolymorphic = true
            encodeDefault = XmlSerializationPolicy.XmlEncodeDefault.ALWAYS
            ignoreUnknownChildren()
        }
    }

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    override fun search(vararg macs: Long): Flow<Long> = channelFlow {
        val services = listOf(
            ::apple,
            ::google,
            ::microsoft,
            ::mylnikov,
            ::yandex
        )

        macs
            .distinct()
            .partition { mac -> mac in 0..0xFFFFFFFFFFFF }
            .also { (valid, invalid) ->
                invalid.forEach { send(it) }

                valid.forEach { mac ->
                    launch(Dispatchers.IO) {
                        geomacDao.upsert(GeomacItem(mac))

                        services.map { invoke ->
                            async {
                                invoke(mac).onSuccess {
                                    geomacDao.upsert(it)
                                }
                            }
                        }.awaitAll()

                        send(mac)
                    }
                }
            }
    }

    override suspend fun delete(vararg macs: Long): Unit = withContext(Dispatchers.IO) {
        geomacDao.delete(*macs.distinct().toLongArray())
    }

    override suspend fun undo(vararg items: GeomacItemWithCoordinates): Unit =
        withContext(Dispatchers.IO) {
            geomacDao.undo(*items.distinctBy { item -> item.mac }.toTypedArray())
        }

    override fun getAllInRanges(vararg ranges: Pair<Long, Long>): PagingSource<Int, GeomacItemWithCoordinates> =
        geomacDao.getAll(
            RoomRawQuery(
                if (ranges.isNotEmpty()) {
                    "SELECT * FROM GeomacItem WHERE ${
                        ranges
                            .map { (start, end) -> if (start <= end) start to end else end to start }
                            .sortedBy { (start, _) -> start }
                            .fold(mutableListOf<Pair<Long, Long>>()) { acc, (currStart, currEnd) ->
                                if (acc.isEmpty()) {
                                    acc.add(currStart to currEnd)
                                } else {
                                    val (prevStart, prevEnd) = acc.last()
                                    if (currStart <= prevEnd + 1) {
                                        acc[acc.lastIndex] = prevStart to maxOf(prevEnd, currEnd)
                                    } else {
                                        acc.add(currStart to currEnd)
                                    }
                                }

                                acc
                            }
                            .joinToString(" OR ") { (start, end) -> "(mac BETWEEN $start AND $end)" }
                    } ORDER BY mac ASC"
                } else {
                    "SELECT * FROM GeomacItem ORDER BY mac ASC"
                }
            )
        )

    @OptIn(ExperimentalSerializationApi::class)
    private suspend fun apple(mac: Long): Result<GeomacCoordinates> = runCatching {
        val request = protobuf.encodeToByteArray(
            AppleRequest(
                noise = 0,
                signal = 100,
                wifis = listOf(
                    AppleRequest.Wifi(
                        mac = mac.macString()
                    )
                )
            )
        )

        val size =
            ByteBuffer.allocate(Short.SIZE_BYTES).apply { putShort(request.size.toShort()) }.array()

        val header = buildList {
            addAll(listOf(0x00, 0x01, 0x00, 0x05))
            addAll("en_US".toByteArray().toList())
            addAll(listOf(0x00, 0x13))
            addAll("com.apple.locationd".toByteArray().toList())
            addAll(listOf(0x00, 0x0C))
            addAll("8.4.1.12H321".toByteArray().toList())
            addAll(listOf(0x00, 0x00, 0x00, 0x01, 0x00, 0x00))
        }.toByteArray()

        val data = client
            .post(Const.APPLE) {
                setBody(header + size + request)
            }
            .body<ByteArray>()

        val marker = byteArrayOf(0x00, 0x00, 0x00, 0x01, 0x00, 0x00)

        val index = checkNotNull(
            data
                .indexOf(marker)
                .takeIf { index -> index != -1 }
        )

        val response = protobuf.decodeFromByteArray<AppleResponse>(
            data.copyOfRange(index + 8, data.size)
        )

        val (latitude, longitude) = checkNotNull(
            response.wifis
                .mapNotNull { it.location }
                .firstOrNull { location ->
                    location.latitude != null &&
                            location.longitude != null &&
                            location.latitude != -18000000000L &&
                            location.longitude != -18000000000L
                }
        )

        val scale = 10.0.pow(-8.0)

        GeomacCoordinates(
            mac = mac,
            service = Services.APPLE,
            latitude = latitude!!.toDouble() * scale,
            longitude = longitude!!.toDouble() * scale
        )
    }

    @OptIn(ExperimentalSerializationApi::class)
    private suspend fun google(mac: Long): Result<GeomacCoordinates> = runCatching {
        val tempMac = "112233445566".toLong(16)

        val request = protobuf.encodeToByteArray(
            GoogleRequest(
                header = GoogleRequest.Header(
                    version = "2021",
                    platform = "android/LEAGOO/full_wf562g_leagoo/wf562g_leagoo:6.0/MRA58K/1511161770:user/release-keys",
                    locale = "en_US"
                ),
                location = listOf(
                    GoogleRequest.Location(
                        data = GoogleRequest.Location.Data(
                            timestamp = 162723L,
                            wifis = listOf(
                                GoogleRequest.Location.Data.Wifi(
                                    text = "",
                                    mac = mac
                                ),
                                GoogleRequest.Location.Data.Wifi(
                                    text = "",
                                    mac = tempMac
                                )
                            ),
                            size = 2
                        )
                    )
                )
            )

        )

        val gzipped = ByteArrayOutputStream().use { byteArrayOutputStream ->
            GZIPOutputStream(byteArrayOutputStream).use { it.write(request) }
            byteArrayOutputStream.toByteArray()
        }

        val size = ByteBuffer.allocate(Int.SIZE_BYTES).apply { putInt(gzipped.size) }.array()

        val header = buildList {
            addAll(listOf(0x00, 0x02, 0x00, 0x00, 0x1f))
            addAll("location,2021,android,gms,en_US".toByteArray().toList())
            addAll(listOf(0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01))
            addAll("g".toByteArray().toList())
            addAll(ByteBuffer.allocate(Int.SIZE_BYTES).apply { putInt(187) }.array().toList())
            addAll(listOf(0x00, 0x01, 0x01, 0x00, 0x01, 0x00, 0x08))
            addAll("g:loc/ql".toByteArray().toList())
            addAll(listOf(0x00, 0x00, 0x00, 0x04))
            addAll("POST".toByteArray().toList())
            addAll(listOf(0x6d, 0x72, 0x00, 0x00, 0x00, 0x04))
            addAll("ROOT".toByteArray().toList())
            addAll(listOf(0x00))
            addAll(size.toList())
            addAll(listOf(0x00, 0x01))
            addAll("g".toByteArray().toList())
        }.toByteArray()

        val data = client
            .post(Const.GOOGLE) {
                setBody(header + gzipped + byteArrayOf(0x00, 0x00))
            }
            .body<ByteArray>()

        val marker = byteArrayOf(0x1f, 0x8b.toByte())

        val index = checkNotNull(
            data
                .indexOf(marker)
                .takeIf { index -> index != -1 }
        )

        val gunzipped =
            ByteArrayInputStream(data.copyOfRange(index, data.size)).use { byteArrayInputStream ->
                GZIPInputStream(byteArrayInputStream).use { it.readBytes() }
            }

        val response = protobuf.decodeFromByteArray<GoogleResponse>(gunzipped)

        val (latitude, longitude) = checkNotNull(
            response.data?.wifis
                ?.mapNotNull { it.wifiData?.location }
                ?.firstOrNull { location ->
                    location.latitude != null &&
                            location.longitude != null
                }
        )

        val scale = 10.0.pow(-7.0)

        GeomacCoordinates(
            mac = mac,
            service = Services.GOOGLE,
            latitude = latitude!!.toDouble() * scale,
            longitude = longitude!!.toDouble() * scale
        )
    }

    @OptIn(ExperimentalTime::class)
    private suspend fun microsoft(mac: Long): Result<GeomacCoordinates> = runCatching {
        val request = xml.encodeToString(
            MicrosoftRequest(
                requestHeader = MicrosoftRequest.RequestHeader(
                    timestamp = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
                    applicationId = "e1e71f6b-2149-45f3-a298-a20682ab5017",
                    trackingId = "21BF9AD6-CFD3-46B3-B041-EE90BD34FDBC",
                    deviceProfile = MicrosoftRequest.RequestHeader.DeviceProfile(
                        clientGuid = "0fc571be-4624-4ce0-b04e-911bdeb1a222",
                        platform = "Windows7",
                        deviceType = "PC",
                        osVersion = "7600.16695.amd64fre.win7_gdr.101026-1503",
                        lfVersion = "9.0.8080.16413",
                        extendedDeviceInfo = ""
                    )
                ),
                beaconFingerprint = MicrosoftRequest.BeaconFingerprint(
                    detections = MicrosoftRequest.BeaconFingerprint.Detections(
                        wifi7 = MicrosoftRequest.BeaconFingerprint.Detections.Wifi7(
                            bssId = mac.macString(),
                            rssi = "-1"
                        )
                    )
                )
            )
        )

        val data = client
            .post(Const.MICROSOFT) {
                contentType(ContentType.Application.Xml)
                setBody(request)
            }
            .body<String>()

        val response = xml.decodeFromString<MicrosoftResponse>(data)

        val (latitude, longitude) = checkNotNull(
            response.result?.locationResult?.resolvedPosition?.takeIf { position ->
                response.result.responseStatus == "Success" &&
                        response.result.locationResult.resolverStatus?.status == "Success" &&
                        response.result.locationResult.resolverStatus.source == "Internal" &&
                        response.result.locationResult.radialUncertainty != null &&
                        response.result.locationResult.radialUncertainty < 500 &&
                        position.latitude?.toDoubleOrNull() != null &&
                        position.longitude?.toDoubleOrNull() != null
            }
        )

        GeomacCoordinates(
            mac = mac,
            service = Services.MICROSOFT,
            latitude = latitude!!.toDouble(),
            longitude = longitude!!.toDouble()
        )
    }

    private suspend fun mylnikov(mac: Long): Result<GeomacCoordinates> = runCatching {
        val data = client
            .get(Const.MYLNIKOV) {
                parameter("v", "1.1")
                parameter("data", "open")
                parameter("bssid", mac.macString())
            }
            .body<String>()

        val response = json.decodeFromString<MylnikovResponse>(data)

        val (latitude, longitude) = checkNotNull(
            response.data?.takeIf { data ->
                data.lat != null &&
                        data.lon != null
            }
        )

        GeomacCoordinates(
            mac = mac,
            service = Services.MYLNIKOV,
            latitude = latitude!!,
            longitude = longitude!!
        )
    }

    private suspend fun yandex(mac: Long): Result<GeomacCoordinates> = runCatching {
        val data = client
            .get(Const.YANDEX) {
                parameter("wifinetworks", "${mac.macString("")}:-65")
            }
            .body<String>()

        val response = xml.decodeFromString<YandexResponse>(data)

        val (latitude, longitude) = checkNotNull(
            response.coordinates?.takeIf { coordinates ->
                response.source == "FoundByWifi" &&
                        coordinates.latitude?.toDoubleOrNull() != null &&
                        coordinates.longitude?.toDoubleOrNull() != null
            }
        )

        GeomacCoordinates(
            mac = mac,
            service = Services.YANDEX,
            latitude = latitude!!.toDouble(),
            longitude = longitude!!.toDouble()
        )
    }
}