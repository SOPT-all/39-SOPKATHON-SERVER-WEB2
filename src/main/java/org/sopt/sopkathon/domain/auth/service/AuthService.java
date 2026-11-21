package org.sopt.sopkathon.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.sopt.sopkathon.domain.auth.dto.LoginRequest;
import org.sopt.sopkathon.domain.auth.dto.LoginResponse;
import org.sopt.sopkathon.domain.user.entity.User;
import org.sopt.sopkathon.domain.user.service.UserService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

/**
 * 주의: 이 클래스에는 {@code @Transactional}을 붙이지 않는다.
 *
 * <p>{@link #login}의 재시도는 {@code UserService}의 트랜잭션이 각각 따로 커밋·롤백된다는 전제로 동작한다. 여기에
 * 트랜잭션을 걸면 두 호출이 한 트랜잭션으로 묶여, 첫 시도가 실패한 순간 전체가 롤백 전용 상태가 되어 재시도도 실패한다.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserService userService;

  /** 처음 입력한 번호면 회원을 만들고, 이미 있는 번호면 기존 회원으로 로그인한다. */
  public LoginResponse login(LoginRequest request) {
    User user;
    try {
      user = userService.findOrCreateByPhoneNumber(request.phoneNumber());
    } catch (DataIntegrityViolationException e) {
      // 같은 새 번호로 동시에 요청이 들어와 다른 요청이 먼저 가입시킨 경우: 이미 만들어진 회원으로 로그인
      user = userService.findOrCreateByPhoneNumber(request.phoneNumber());
    }
    return LoginResponse.from(user);
  }
}
