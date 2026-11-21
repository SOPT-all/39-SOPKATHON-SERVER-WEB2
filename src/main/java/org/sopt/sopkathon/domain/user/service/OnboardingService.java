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
 * 온보딩 흐름(검증 → 사진 업로드 → 저장)을 조합한다.
 *
 * <p>주의: 이 클래스에는 {@code @Transactional}을 붙이지 않는다. S3 업로드 동안 DB 커넥션을 잡지 않도록, 저장만
 * {@link UserService#completeOnboarding}의 트랜잭션에서 처리한다.
 */
@Service
@RequiredArgsConstructor
public class OnboardingService {

  private final UserService userService;
  private final S3ImageService s3ImageService;

  public OnboardingResponse completeOnboarding(
      Long userId, MultipartFile profileImage, OnboardingRequest request) {
    // 실패할 요청 때문에 S3에 사용되지 않는 사진이 쌓이지 않도록 업로드 전에 확인한다.
    User user = userService.getUser(userId);
    if (user.isOnboarded()) {
      throw new BaseException(UserErrorCode.ALREADY_ONBOARDED);
    }

    String profileImageUrl = s3ImageService.upload(profileImage, ImageDirectory.PROFILE);

    // DB 저장이 실패하면 업로드한 사진이 S3에 남는다. 해커톤 범위에서는 정리하지 않는다.
    User onboarded = userService.completeOnboarding(
        userId,
        request.nickname(),
        request.genderAsEnum(),
        request.age(),
        profileImageUrl,
        request.scaledLatitude(),
        request.scaledLongitude());
    return OnboardingResponse.from(onboarded);
  }
}
