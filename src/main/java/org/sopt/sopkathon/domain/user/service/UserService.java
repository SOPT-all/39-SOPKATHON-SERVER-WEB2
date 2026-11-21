package org.sopt.sopkathon.domain.user.service;

import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.sopt.sopkathon.domain.user.code.UserErrorCode;
import org.sopt.sopkathon.domain.user.entity.Gender;
import org.sopt.sopkathon.domain.user.entity.OnboardingProfile;
import org.sopt.sopkathon.domain.user.entity.User;
import org.sopt.sopkathon.domain.user.repository.UserRepository;
import org.sopt.sopkathon.global.exception.BaseException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;

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

  /** 락으로 동시 요청을 순서대로 처리해, 두 번째 요청은 이미 온보딩한 상태를 보고 409로 실패한다. */
  @Transactional
  public User completeOnboarding(Long userId, OnboardingProfile profile) {
    User user = userRepository
        .findByIdForUpdate(userId)
        .orElseThrow(() -> new BaseException(UserErrorCode.USER_NOT_FOUND));
    user.completeOnboarding(profile);
    return user;
  }

  @Transactional(readOnly = true)
  public User getOnboardedUser(Long userId) {
    User user = getUser(userId);
    if (!user.isOnboarded() || !user.hasLocation()) {
      throw new BaseException(UserErrorCode.USER_NOT_ONBOARDED);
    }
    return user;
  }

  @Transactional(readOnly = true)
  public List<User> findOnboardedByGenderExcluding(Gender gender, Collection<Long> excludedIds) {
    if (excludedIds.isEmpty()) {
      throw new IllegalArgumentException("excludedIds must not be empty");
    }
    return userRepository.findOnboardedByGenderExcluding(gender, excludedIds);
  }
}
