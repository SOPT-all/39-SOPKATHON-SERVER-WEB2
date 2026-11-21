package org.sopt.sopkathon.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.sopt.sopkathon.domain.auth.dto.LoginRequest;
import org.sopt.sopkathon.domain.auth.dto.LoginResponse;
import org.sopt.sopkathon.domain.user.entity.User;
import org.sopt.sopkathon.domain.user.service.UserService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserService userService;

  /** 처음 입력한 번호면 회원을 만들고, 이미 있는 번호면 기존 회원으로 로그인한다. */
  public LoginResponse login(LoginRequest request) {
    User user = userService.findOrCreateByPhoneNumber(request.phoneNumber());
    return LoginResponse.from(user);
  }
}
