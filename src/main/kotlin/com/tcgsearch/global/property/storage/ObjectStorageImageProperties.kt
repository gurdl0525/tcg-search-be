package com.tcgsearch.global.property.storage

import java.time.Duration
import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * 카드 이미지 object storage URL 변환 설정입니다.
 *
 * private bucket URL을 API 응답에서 presigned URL로 변환하기 위한 endpoint와 자격 증명을 관리합니다.
 *
 * @author gurdl0525
 * @since 25-06-2026
 */
@ConfigurationProperties(prefix = "app.storage.object.images")
data class ObjectStorageImageProperties(
    val enabled: Boolean = false,
    val endpoint: String = "",
    val publicEndpoint: String = "",
    val bucket: String = "",
    val accessKey: String = "",
    val secretKey: String = "",
    val region: String = "us-east-1",
    val presignTtl: Duration = Duration.ofMinutes(15),
)
