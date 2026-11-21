package org.sopt.sopkathon.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.sopt.sopkathon.domain.image.ImageDirectory;
import org.sopt.sopkathon.domain.image.service.S3ImageService;
import org.sopt.sopkathon.domain.user.code.UserErrorCode;
import org.sopt.sopkathon.domain.user.dto.OnboardingRequest;
import org.sopt.sopkathon.domain.user.dto.OnboardingResponse;
import org.sopt.sopkathon.domain.user.entity.User;
import org.sopt.sopkathon.global.exception.BaseException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * {@code @Transactional}을 붙이지 않는다.
 * S3 업로드 동안 DB 커넥션을 잡지 않도록 저장만 UserService 트랜잭션에서 처리한다.
 */
@Service
@RequiredArgsConstructor
public class OnboardingService {

  private final UserService userService;
  private final S3ImageService s3ImageService;

  public OnboardingResponse completeOnboarding(
      Long userId, MultipartFile profileImage, OnboardingRequest request) {
    // 실패할 요청으로 S3에 사진이 쌓이지 않도록 업로드 전에 확인
    User user = userService.getUser(userId);
    if (user.isOnboarded()) {
      throw new BaseException(UserErrorCode.ALREADY_ONBOARDED);
    }

    String profileImageUrl = s3ImageService.upload(profileImage, ImageDirectory.PROFILE);

    // DB 저장이 실패하면 사진이 S3에 남는다 (해커톤 범위에서는 정리하지 않음)
    User onboarded = userService.completeOnboarding(userId, request.toProfile(profileImageUrl));
    return OnboardingResponse.from(onboarded);
  }
}
