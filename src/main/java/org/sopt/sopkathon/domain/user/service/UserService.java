package org.sopt.sopkathon.domain.user.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.sopt.sopkathon.domain.user.code.UserErrorCode;
import org.sopt.sopkathon.domain.user.entity.User;
import org.sopt.sopkathon.domain.user.repository.UserRepository;
import org.sopt.sopkathon.global.exception.BaseException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;

  /**
   * 전화번호로 회원을 찾고, 없으면 새로 만든다. 저장과 조회 모두 정규화한 번호를 사용해 하이픈 유무와 관계없이 같은 회원으로
   * 취급한다.
   */
  @Transactional
  public User findOrCreateByPhoneNumber(String rawPhoneNumber) {
    String phoneNumber = User.normalizePhoneNumber(rawPhoneNumber);
    return userRepository.findByPhoneNumber(phoneNumber)
        .orElseGet(() -> userRepository.save(User.create(phoneNumber)));
  }

  @Transactional(readOnly = true)
  public User getUser(Long userId) {
    return userRepository
        .findById(userId)
        .orElseThrow(() -> new BaseException(UserErrorCode.USER_NOT_FOUND));
  }

  /** 위치·성별 등 온보딩 정보가 필요한 기능에서 사용한다. */
  @Transactional(readOnly = true)
  public User getOnboardedUser(Long userId) {
    User user = getUser(userId);
    if (!user.isOnboarded()) {
      throw new BaseException(UserErrorCode.USER_NOT_ONBOARDED);
    }
    return user;
  }

  @Transactional(readOnly = true)
  public List<User> findMatchCandidates(User me) {
    return userRepository.findMatchCandidates(me.getId(), me.getGender());
  }
}
