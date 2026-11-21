package org.sopt.sopkathon.domain.callrequest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.sopt.sopkathon.domain.user.entity.Gender;
import org.sopt.sopkathon.domain.user.entity.User;

@Schema(description = "요청을 보낸 사람 (받은 사람에게만 전화번호 공개)")
public record RequesterResponse(
    @Schema(description = "보낸 사람 ID", example = "4") Long userId,
    @Schema(description = "닉네임", example = "김영희") String nickname,
    @Schema(description = "성별", example = "FEMALE") Gender gender,
    @Schema(description = "나이", example = "67") Integer age,
    @Schema(
            description = "프로필 사진 URL",
            example = "https://bucket.s3.ap-northeast-2.amazonaws.com/profile/xxx.jpg")
        String profileImageUrl,
    @Schema(description = "전화번호. 하이픈 없이 숫자만", example = "01012345678") String phoneNumber,
    @Schema(description = "나와의 직선 거리 (미터, 100m 단위로 올림)", example = "500") int distance) {

  public static RequesterResponse from(User requester, int distance) {
    return new RequesterResponse(
        requester.getId(),
        requester.getNickname(),
        requester.getGender(),
        requester.getAge(),
        requester.getProfileImageUrl(),
        requester.getPhoneNumber(),
        distance);
  }
}
