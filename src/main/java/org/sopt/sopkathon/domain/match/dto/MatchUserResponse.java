package org.sopt.sopkathon.domain.match.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.sopt.sopkathon.domain.user.entity.User;

@Schema(description = "매칭 상대 (전화번호·좌표는 포함하지 않음)")
public record MatchUserResponse(
    @Schema(description = "상대 회원 ID. 넘기기·요청 보내기 API에 사용", example = "7") Long userId,
    @Schema(description = "닉네임", example = "민지") String nickname,
    @Schema(description = "나이", example = "24") Integer age,
    @Schema(
            description = "프로필 사진 URL",
            example = "https://bucket.s3.ap-northeast-2.amazonaws.com/profile/xxx.jpg")
        String profileImageUrl,
    @Schema(description = "내 위치와의 직선 거리 (미터, 정수)", example = "800") int distance) {

  public static MatchUserResponse from(User user, int distance) {
    return new MatchUserResponse(
        user.getId(), user.getNickname(), user.getAge(), user.getProfileImageUrl(), distance);
  }
}
