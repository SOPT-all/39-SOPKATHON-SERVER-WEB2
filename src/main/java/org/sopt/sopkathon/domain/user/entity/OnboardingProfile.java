package org.sopt.sopkathon.domain.user.entity;

import java.math.BigDecimal;
import java.util.Objects;
import lombok.Builder;

/**
 * 온보딩으로 저장할 회원 정보. 모든 값이 필수다.
 *
 * <p>같은 타입이 이어지는 값(위도·경도, 이름·사진 URL)을 순서로 넘기다 바꿔 넣는 실수를 막기 위해 빌더로만 만든다.
 * 좌표가 비어 있는 회원이 생기면 매칭·근처 장소의 거리 계산이 실패하므로 null을 허용하지 않는다.
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
