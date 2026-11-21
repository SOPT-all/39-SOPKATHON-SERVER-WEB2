package org.sopt.sopkathon.domain.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.sopt.sopkathon.domain.auth.dto.LoginRequest;
import org.sopt.sopkathon.domain.auth.dto.LoginResponse;
import org.sopt.sopkathon.domain.user.entity.User;
import org.sopt.sopkathon.domain.user.service.UserService;
import org.springframework.dao.DataIntegrityViolationException;

class AuthServiceTest {

  private static final String PHONE_NUMBER = "01012345678";

  private final UserService userService = mock(UserService.class);
  private final AuthService authService = new AuthService(userService);

  @Test
  void 동시_가입으로_유니크_제약에_걸리면_한_번_더_조회해서_로그인한다() {
    User user = User.create(PHONE_NUMBER);
    given(userService.findOrCreateByPhoneNumber(PHONE_NUMBER))
        .willThrow(new DataIntegrityViolationException("duplicate"))
        .willReturn(user);

    LoginResponse response = authService.login(new LoginRequest(PHONE_NUMBER));

    assertThat(response.onboarded()).isFalse();
    verify(userService, times(2)).findOrCreateByPhoneNumber(PHONE_NUMBER);
  }

  @Test
  void 재시도까지_실패하면_예외를_그대로_던진다() {
    given(userService.findOrCreateByPhoneNumber(PHONE_NUMBER))
        .willThrow(new DataIntegrityViolationException("duplicate"));

    assertThatThrownBy(() -> authService.login(new LoginRequest(PHONE_NUMBER)))
        .isInstanceOf(DataIntegrityViolationException.class);
    verify(userService, times(2)).findOrCreateByPhoneNumber(PHONE_NUMBER);
  }
}
