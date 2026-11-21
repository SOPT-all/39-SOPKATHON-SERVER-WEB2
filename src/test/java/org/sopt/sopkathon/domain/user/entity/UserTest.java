package org.sopt.sopkathon.domain.user.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.sopt.sopkathon.domain.user.code.UserErrorCode;
import org.sopt.sopkathon.global.exception.BaseException;

class UserTest {

  @ParameterizedTest
  @ValueSource(strings = {"01012345678", "010-1234-5678", "010 1234 5678", " 010.1234.5678 "})
  void 전화번호는_숫자만_남겨_같은_형식으로_저장한다(String input) {
    User user = User.create(input);

    assertThat(user.getPhoneNumber()).isEqualTo("01012345678");
  }

  @ParameterizedTest
  @ValueSource(strings = {"0111234567", "011-123-4567"})
  void 열자리_휴대폰_번호도_허용한다(String input) {
    assertThat(User.normalizePhoneNumber(input)).isEqualTo("0111234567");
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"02-123-4567", "0101234", "010123456789", "abc", "+82-10-1234-5678"})
  void 휴대폰_번호_형식이_아니면_예외가_발생한다(String input) {
    assertThatThrownBy(() -> User.create(input))
        .isInstanceOf(BaseException.class)
        .extracting("errorCode")
        .isEqualTo(UserErrorCode.INVALID_PHONE_NUMBER);
  }

  @Test
  void 온보딩을_완료하면_정보가_저장되고_완료_상태가_된다() {
    User user = User.create("01012345678");

    user.completeOnboarding(profile("정우").build());

    assertThat(user.isOnboarded()).isTrue();
    assertThat(user.hasLocation()).isTrue();
    assertThat(user.getLatitude()).isEqualByComparingTo("37.551234");
    assertThat(user.getLongitude()).isEqualByComparingTo("126.923456");
  }

  @Test
  void 이미_온보딩한_유저는_다시_온보딩할_수_없다() {
    User user = User.create("01012345678");
    user.completeOnboarding(profile("정우").build());

    assertThatThrownBy(() -> user.completeOnboarding(profile("다른이름").build()))
        .isInstanceOf(BaseException.class)
        .extracting("errorCode")
        .isEqualTo(UserErrorCode.ALREADY_ONBOARDED);
    assertThat(user.getNickname()).isEqualTo("정우");
  }

  @Test
  void 좌표가_없으면_온보딩_정보를_만들_수_없다() {
    assertThatThrownBy(() -> profile("정우").latitude(null).build())
        .isInstanceOf(NullPointerException.class)
        .hasMessage("latitude");
  }

  private OnboardingProfile.OnboardingProfileBuilder profile(String nickname) {
    return OnboardingProfile.builder()
        .nickname(nickname)
        .gender(Gender.MALE)
        .age(25)
        .profileImageUrl("https://example.com/a.jpg")
        .latitude(new BigDecimal("37.551234"))
        .longitude(new BigDecimal("126.923456"));
  }
}
