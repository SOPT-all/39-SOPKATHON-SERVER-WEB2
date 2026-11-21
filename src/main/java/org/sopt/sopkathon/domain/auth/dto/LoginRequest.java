package org.sopt.sopkathon.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;

/** 형식 검증은 {@code User.normalizePhoneNumber()}에서 한다. */
public record LoginRequest(
    @NotBlank(message = "전화번호를 입력해주세요.")
    String phoneNumber
) {
}
