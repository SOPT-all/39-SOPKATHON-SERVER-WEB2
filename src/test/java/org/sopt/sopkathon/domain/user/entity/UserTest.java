package org.sopt.sopkathon.domain.user.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
}
