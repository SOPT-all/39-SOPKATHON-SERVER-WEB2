package org.sopt.sopkathon.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.sopt.sopkathon.domain.user.entity.Gender;
import org.sopt.sopkathon.domain.user.entity.User;

/** 온보딩 결과. 좌표와 전화번호는 담지 않는다. */
@Schema(description = "온보딩 결과 (좌표·전화번호는 포함하지 않음)")
public record OnboardingResponse(
    @Schema(description = "회원 ID", example = "1") Long userId,
    @Schema(description = "저장된 이름 (앞뒤 공백 제거됨)", example = "정우") String nickname,
    @Schema(description = "성별", example = "MALE") Gender gender,
    @Schema(description = "나이", example = "25") Integer age,
    @Schema(
            description = "S3에 저장된 프로필 사진 URL",
            example = "https://bucket.s3.ap-northeast-2.amazonaws.com/profile/xxx.jpg")
        String profileImageUrl) {

  public static OnboardingResponse from(User user) {
    return new OnboardingResponse(
        user.getId(), user.getNickname(), user.getGender(), user.getAge(), user.getProfileImageUrl());
  }
}
