package org.sopt.sopkathon.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.sopt.sopkathon.domain.auth.dto.LoginRequest;
import org.sopt.sopkathon.domain.auth.dto.LoginResponse;
import org.sopt.sopkathon.domain.user.entity.User;
import org.sopt.sopkathon.domain.user.service.UserService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

/**
 * {@code @Transactional}을 붙이지 않는다.
 * login()의 재시도는 UserService 트랜잭션이 호출마다 따로 롤백돼야 동작한다.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserService userService;

  public LoginResponse login(LoginRequest request) {
    User user;
    try {
      user = userService.findOrCreateByPhoneNumber(request.phoneNumber());
    } catch (DataIntegrityViolationException e) {
      // 동시 가입으로 유니크 제약에 걸린 경우: 먼저 만들어진 회원으로 로그인
      user = userService.findOrCreateByPhoneNumber(request.phoneNumber());
    }
    return LoginResponse.from(user);
  }
}
