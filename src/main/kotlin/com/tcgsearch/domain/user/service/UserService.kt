package com.tcgsearch.domain.user.service

import com.tcgsearch.domain.user.dto.response.CurrentUserResponse
import java.util.UUID

/**
 * 사용자 조회 유스케이스를 정의합니다.
 *
 * 인증된 사용자 식별자를 기준으로 API 응답 모델을 구성합니다.
 *
 * @author gurdl0525
 * @since 16-06-2026
 */
interface UserService {
    /**
     * 현재 인증된 사용자 정보를 조회합니다.
     *
     * @param userId JWT subject에서 얻은 사용자 식별자
     * @return 현재 사용자 응답
     */
    fun getCurrentUser(userId: UUID): CurrentUserResponse
}
