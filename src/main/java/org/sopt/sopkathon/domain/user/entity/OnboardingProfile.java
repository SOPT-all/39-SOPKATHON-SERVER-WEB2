package org.sopt.sopkathon.domain.user.entity;

import java.math.BigDecimal;
import java.util.Objects;
import lombok.Builder;

/**
 * 같은 타입 값(위도·경도 등)을 바꿔 넣지 않도록 빌더로만 만든다.
 * 좌표가 비면 매칭·근처 장소의 거리 계산이 실패하므로 null을 허용하지 않는다.
 */
@Builder
public record OnboardingProfile(
    String nickname,
    Gender gender,
    Integer age,
    String profileImageUrl,
    BigDecimal latitude,
    BigDecimal longitude) {

  public OnboardingProfile {
    Objects.requireNonNull(nickname, "nickname");
    Objects.requireNonNull(gender, "gender");
    Objects.requireNonNull(age, "age");
    Objects.requireNonNull(profileImageUrl, "profileImageUrl");
    Objects.requireNonNull(latitude, "latitude");
    Objects.requireNonNull(longitude, "longitude");
  }
}
