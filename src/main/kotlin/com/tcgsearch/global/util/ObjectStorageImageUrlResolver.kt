package com.tcgsearch.global.util

import com.tcgsearch.global.property.storage.ObjectStorageImageProperties
import java.net.URI
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import org.springframework.stereotype.Component

/**
 * 카드 이미지 URL을 클라이언트가 읽을 수 있는 URL로 변환합니다.
 *
 * 로컬 MinIO처럼 private bucket URL이 저장된 경우 S3 SigV4 presigned GET URL을 생성합니다.
 *
 * @author gurdl0525
 * @since 25-06-2026
 */
@Component
class ObjectStorageImageUrlResolver(
    private val properties: ObjectStorageImageProperties,
) {

    /**
     * 저장된 이미지 URL을 클라이언트가 직접 읽을 수 있는 URL로 변환합니다.
     *
     * 설정된 bucket URL이 아니거나 presign 설정이 꺼져 있으면 원본 URL을 그대로 반환합니다.
     *
     * @param rawUrl DB에 저장된 이미지 URL
     * @return 외부 클라이언트가 사용할 이미지 URL
     */
    fun resolve(rawUrl: String?): String? {
        if (rawUrl.isNullOrBlank() || !properties.enabled || !properties.isConfigured()) {
            return rawUrl
        }

        val objectKey = rawUrl.toObjectKey() ?: return rawUrl
        return presign(objectKey = objectKey, now = Instant.now())
    }

    private fun ObjectStorageImageProperties.isConfigured(): Boolean =
        bucket.isNotBlank() &&
            accessKey.isNotBlank() &&
            secretKey.isNotBlank() &&
            (publicEndpoint.ifBlank { endpoint }).isNotBlank()

    private fun String.toObjectKey(): String? {
        val uri = runCatching { URI(this) }.getOrNull() ?: return null
        val configuredEndpoint = properties.endpoint.ifBlank { properties.publicEndpoint }
        val configuredAuthority = runCatching { URI(configuredEndpoint).authority }.getOrNull()
        val publicAuthority = runCatching { URI(properties.publicEndpoint).authority }.getOrNull()

        if (uri.authority != configuredAuthority && uri.authority != publicAuthority) {
            return null
        }

        val path = uri.rawPath.trimStart('/')
        val bucketPrefix = "${properties.bucket}/"
        if (!path.startsWith(bucketPrefix)) {
            return null
        }

        return path.removePrefix(bucketPrefix).takeIf { it.isNotBlank() }
    }

    private fun presign(objectKey: String, now: Instant): String {
        val endpoint = properties.publicEndpoint.ifBlank { properties.endpoint }.trimEnd('/')
        val endpointUri = URI(endpoint)
        val host = endpointUri.authority
        val amzDate = AMZ_DATE_FORMATTER.format(now.atZone(ZoneOffset.UTC))
        val date = SCOPE_DATE_FORMATTER.format(now.atZone(ZoneOffset.UTC))
        val credentialScope = "$date/${properties.region}/$SERVICE/aws4_request"
        val canonicalUri = "/${properties.bucket}/${objectKey.canonicalPath()}"
        val queryWithoutSignature = sortedMapOf(
            "X-Amz-Algorithm" to ALGORITHM,
            "X-Amz-Credential" to "${properties.accessKey}/$credentialScope",
            "X-Amz-Date" to amzDate,
            "X-Amz-Expires" to properties.presignTtl.seconds.coerceIn(1, MAX_PRESIGN_SECONDS).toString(),
            "X-Amz-SignedHeaders" to SIGNED_HEADERS,
        )
        val canonicalQuery = queryWithoutSignature.toCanonicalQuery()
        val canonicalRequest = listOf(
            "GET",
            canonicalUri,
            canonicalQuery,
            "host:$host\n",
            SIGNED_HEADERS,
            UNSIGNED_PAYLOAD,
        ).joinToString("\n")
        val stringToSign = listOf(
            ALGORITHM,
            amzDate,
            credentialScope,
            canonicalRequest.sha256Hex(),
        ).joinToString("\n")
        val signature = signingKey(date).hmacSha256(stringToSign).toHex()

        return "$endpoint$canonicalUri?$canonicalQuery&X-Amz-Signature=$signature"
    }

    private fun String.canonicalPath(): String =
        split("/")
            .joinToString("/") { it.urlEncode() }

    private fun Map<String, String>.toCanonicalQuery(): String =
        entries.joinToString("&") { (key, value) -> "${key.urlEncode()}=${value.urlEncode()}" }

    private fun String.urlEncode(): String =
        URLEncoder
            .encode(this, StandardCharsets.UTF_8)
            .replace("+", "%20")
            .replace("%7E", "~")

    private fun String.sha256Hex(): String =
        MessageDigest
            .getInstance("SHA-256")
            .digest(toByteArray(StandardCharsets.UTF_8))
            .toHex()

    private fun signingKey(date: String): ByteArray =
        ("AWS4${properties.secretKey}").toByteArray(StandardCharsets.UTF_8)
            .hmacSha256(date)
            .hmacSha256(properties.region)
            .hmacSha256(SERVICE)
            .hmacSha256("aws4_request")

    private fun ByteArray.hmacSha256(data: String): ByteArray =
        Mac.getInstance(HMAC_SHA_256).run {
            init(SecretKeySpec(this@hmacSha256, HMAC_SHA_256))
            doFinal(data.toByteArray(StandardCharsets.UTF_8))
        }

    private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }

    private companion object {
        const val ALGORITHM = "AWS4-HMAC-SHA256"
        const val HMAC_SHA_256 = "HmacSHA256"
        const val SERVICE = "s3"
        const val SIGNED_HEADERS = "host"
        const val UNSIGNED_PAYLOAD = "UNSIGNED-PAYLOAD"
        const val MAX_PRESIGN_SECONDS = 604800L

        val AMZ_DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'")
        val SCOPE_DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
    }
}
